package com.svenj.tools.pdf.pdfium;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfPasswordException;
import com.svenj.tools.pdf.R;
import com.svenj.tools.pdf.SystemBarUtils;
import com.svenj.tools.pdf.permissions.RxPermissions;
import com.svenj.tools.pdf.repositories.AppDatabase;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.repositories.PdfDAO;

import java.io.File;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

// TODO 使用BottomSheetBehavior实现目录预览
public class ReaderActivity extends AppCompatActivity implements OnPageChangeListener,
        OnLoadCompleteListener, OnPageErrorListener, OnPageScrollListener, OnErrorListener {
    private static final String TAG = "PdfReaderActivity";

    private ActionBar mActionBar;
    private PDFView mPdfView;
    private int pageNumber = 0;

    private Pdf mPdf;
    private Uri mFileUri;

    private boolean mToolbarVisible = true;
    private AlertDialog mPasswordDialog = null;
    private boolean mDocumentOpened = false;

    /**
     * 只保存本地文件的阅读记录，即uri以file开头
     * 因为像第三方通过ACTION_VIEW传过来的uri可能是本地FileProvider
     */
    private boolean mPdfCanSave = false;

    RxPermissions permissions;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        permissions = new RxPermissions(this);

        final Toolbar toolbar = findViewById(R.id.toolBar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.topMargin = SystemBarUtils.getStatusBarHeight(this);
        toolbar.setLayoutParams(lp);

        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPdfView = findViewById(R.id.pdfView);
        mPdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Intent intent = getIntent();
        mFileUri = intent.getData();

        if (mFileUri != null) {
            Log.e(TAG, "uri : " + mFileUri.toString());
            mPdfCanSave = TextUtils.equals(mFileUri.getScheme(), "file");
            Disposable disposable = permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                getPdfInformation(mFileUri);
                            } else {
                                showErrorDialog();
                            }
                        }
                    });
            disposables.add(disposable);
        } else {
            showErrorDialog();
        }
    }

    @Override
    protected void onStop() {
        // save read record
        if (mDocumentOpened && mPdfCanSave) {
            if (mPdf.getId() == 0) {
                setPdfModifiedTimeAndSize();
            }
            mPdf.setReadPage(pageNumber);
            mPdf.setReadTime(new Date());
            Log.e(TAG, "save pdf : " + mPdf.toString());
            final PdfDAO pdfDAO = AppDatabase.getInstance(this).getPdfDAO();
            pdfDAO.insertPdf(mPdf)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onComplete() {
                            Log.e(TAG, "insert pdf success");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "insert pdf failed", e);
                        }
                    });
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (disposables != null) {
            disposables.dispose();
            disposables = null;
        }
        dismissPasswordDialog();
        super.onDestroy();
    }

    /**
     * 首先尝试从本地数据库中获取指定Uri的Pdf记录，如果没有记录则构造一个新的Pdf对象
     * fixme 这里假设查询语句没有问题
     */
    private void getPdfInformation(final Uri uri) {
        Disposable disposable = AppDatabase.getInstance(this)
                .getPdfDAO()
                .getPdf(uri.toString())
                .onErrorReturn(new Function<Throwable, Pdf>() {
                    @Override
                    public Pdf apply(Throwable throwable) {
                        return getPdfFromUri(uri);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pdf>() {
                    @Override
                    public void accept(Pdf pdf) {
                        Log.e(TAG, "get pdf info : " + pdf.toString());
                        mPdf = pdf;
                        setupPdfView();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "get pdf info error", throwable);
                    }
                });
        disposables.add(disposable);
    }

    private Pdf getPdfFromUri(Uri uri) {
        Pdf pdf = new Pdf();
        pdf.setFileName(getFileName(uri));
        pdf.setFilePath(uri.toString());
        pdf.setReadPage(0);
        return pdf;
    }

    private void setPdfModifiedTimeAndSize() {
        String filePath = mPdf.getFilePath();
        try {
            File file = new File(Uri.parse(filePath).getPath());
            mPdf.setFileSize(file.length());
            mPdf.setCreateTime(new Date(file.lastModified()));
        } catch (Exception e) {
            Log.e(TAG, "setPdfModifiedTimeAndSize err", e);
        }
    }

    private void setupPdfView() {
        mPdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolbar();
            }
        });

        loadDocument(Uri.parse(mPdf.getFilePath()), mPdf.getReadPage(), null);
    }

    private void toggleToolbar() {
        if (mToolbarVisible) {
            hideToolbar();
        } else {
            showSystemBars();
        }
    }

    private void hideToolbar() {
        mPdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                // 不隐藏导航栏
                /*| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*/);
        if (mActionBar != null) {
            mActionBar.hide();
        }
        mToolbarVisible = false;
    }

    private void showSystemBars() {
        mPdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        if (mActionBar != null) {
            mActionBar.show();
        }
        mToolbarVisible = true;
    }

    private void loadDocument(Uri uri, int startPage, String password) {
        mPdfView.fromUri(uri)
                .password(password)
                .enableAnnotationRendering(true)
                .defaultPage(startPage)
                .enableAntialiasing(true)
                .onPageChange(this)
                .swipeHorizontal(false)
                .onLoad(this)
                .onError(this)
                .enableDoubletap(false)
                .spacing(10) // in dp
                .onPageError(this)
                .onPageScroll(this)
                .nightMode(false)
                .load();
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = mPdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        // printBookmarksTree(mPdfView.getTableOfContents(), "-");

        mPdfView.getTableOfContents();
        mDocumentOpened = true;
        dismissPasswordDialog();
    }

    @Override
    public void onError(Throwable t) {
        Log.e(TAG, "load document failed", t);
        mDocumentOpened = false;
        if (t instanceof PdfPasswordException) {
            // 文件加密，需要输入密码
            showPasswordDialog();
        } else {
            // 文件加载失败
            showErrorDialog();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page, t);
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {
            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));
            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;

        if (TextUtils.equals(uri.getScheme(), "file")) {
            return uri.getLastPathSegment();
        }

        if (TextUtils.equals(uri.getScheme(), "content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void onPageScrolled(int page, float positionOffset) {
    }

    private TextInputEditText mPasswordEdit;
    private TextInputLayout mPasswordLayout;

    private void showPasswordDialog() {
        if (mPasswordDialog != null && mPasswordDialog.isShowing()) {
            mPasswordLayout.setErrorEnabled(true);
            mPasswordLayout.setError(getText(R.string.dialog_password_retry));
            return;
        }
        mPasswordDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_password_message)
                .setNegativeButton(R.string.dialog_password_button_cancel, null)
                .setPositiveButton(R.string.dialog_password_button_ok, null)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (!mDocumentOpened) {
                            ReaderActivity.this.finish();
                        }
                    }
                })
                .create();
        Window dialogWindow = mPasswordDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        mPasswordLayout = (TextInputLayout) getLayoutInflater().inflate(R.layout.dialog_password, null);
        mPasswordEdit = mPasswordLayout.findViewById(R.id.passwordEdit);
        mPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPasswordLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mPasswordDialog.setView(mPasswordLayout);
        mPasswordDialog.setCanceledOnTouchOutside(false);
        mPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mPasswordDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                mPasswordDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mPasswordDialog.dismiss();
                                if (!mDocumentOpened) {
                                    ReaderActivity.this.finish();
                                }
                            }
                        });

                mPasswordDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String password = mPasswordEdit.getEditableText().toString();
                                loadDocument(mFileUri, 0, password);
                            }
                        });
            }
        });
        mPasswordDialog.show();
    }

    private void dismissPasswordDialog() {
        if (mPasswordDialog != null) {
            mPasswordDialog.dismiss();
            mPasswordDialog = null;
            mPasswordEdit = null;
            mPasswordLayout = null;
        }
    }

    private void showErrorDialog() {
        AlertDialog errDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_error_title)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ReaderActivity.this.finish();
                    }
                })
                .setPositiveButton(R.string.dialog_error_button_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReaderActivity.this.finish();
                            }
                        })
                .create();
        errDialog.setCanceledOnTouchOutside(false);
        errDialog.show();
    }
}
