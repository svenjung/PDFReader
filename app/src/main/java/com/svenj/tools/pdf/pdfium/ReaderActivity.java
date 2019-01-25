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
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

// TODO 使用BottomSheetBehavior实现目录预览
public class ReaderActivity extends AppCompatActivity implements OnPageChangeListener,
        OnLoadCompleteListener, OnPageErrorListener, OnPageScrollListener, OnErrorListener {
    private static final String TAG = "PdfReaderActivity";

    private PDFView mPdfView;
    private TextView mPageNumberView;
    private int pageNumber = 0;

    private Pdf mPdf;
    private Uri mFileUri;

    private AlertDialog mPasswordDialog = null;
    private boolean mDocumentOpened = false;

    /**
     * 只保存本地文件的阅读记录，即uri以file开头
     * 因为像第三方通过ACTION_VIEW传过来的uri可能是本地FileProvider
     */
    private boolean mPdfCanSave = false;

    RxPermissions permissions;
    private CompositeDisposable disposables = new CompositeDisposable();

    private ToolbarAnimator mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        // layout 透到状态栏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        permissions = new RxPermissions(this);
        initView();
        // load document
        tryLoadDocument();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);
        return true;
    }

    @Override
    protected void onStop() {
        // save read record
        savePdf();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mAnimator.cancel();
        if (disposables != null) {
            disposables.dispose();
            disposables = null;
        }
        dismissPasswordDialog();
        mPdfView.recycle();
        super.onDestroy();
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
        mPageNumberView.setVisibility(View.VISIBLE);

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

        mPageNumberView.setText(page + " / " + pageCount);
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page, t);
    }

    @Override
    public void onPageScrolled(int page, float positionOffset) {
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.topMargin = SystemBarUtils.getStatusBarHeight(this);
        toolbar.setLayoutParams(lp);

        View topBar = findViewById(R.id.topBar);
        View bottomBar = findViewById(R.id.bottomBar);
        mPageNumberView = findViewById(R.id.pageNumber);

        mAnimator = new ToolbarAnimator(topBar, bottomBar, mPageNumberView);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPdfView = findViewById(R.id.pdfView);
    }

    private void tryLoadDocument() {
        Intent intent = getIntent();
        mFileUri = intent.getData();

        if (mFileUri != null) {
            Log.e(TAG, "uri : " + mFileUri.toString());
            mPdfCanSave = TextUtils.equals(mFileUri.getScheme(), "file");
            Disposable disposable = permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(aBoolean -> {
                        if (aBoolean) {
                            getPdfInformation(mFileUri);
                        } else {
                            showErrorDialog();
                        }
                    });
            disposables.add(disposable);
        } else {
            showErrorDialog();
        }
    }
    /**
     * 首先尝试从本地数据库中获取指定Uri的Pdf记录，如果没有记录则构造一个新的Pdf对象
     * fixme 这里假设查询语句没有问题
     */
    private void getPdfInformation(final Uri uri) {
        Disposable disposable = AppDatabase.getInstance(this)
                .getPdfDAO()
                .getPdf(uri.toString())
                .onErrorReturn(throwable -> getPdfFromUri(uri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pdf -> {
                    Log.e(TAG, "get pdf info : " + pdf.toString());
                    mPdf = pdf;
                    setupPdfView();
                }, throwable -> Log.e(TAG, "get pdf info error", throwable));
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
        mPdfView.setOnClickListener(v -> mAnimator.toggleToolbar());

        loadDocument(Uri.parse(mPdf.getFilePath()), mPdf.getReadPage(), null);
    }

    private void loadDocument(Uri uri, int startPage, String password) {
        mPdfView.fromUri(uri)
                .password(password)
                .enableAnnotationRendering(true)
                .defaultPage(startPage)
                .enableAntialiasing(false)
                .onPageChange(this)
                .swipeHorizontal(false)
                .onLoad(this)
                .onError(this)
                .enableDoubletap(false)
                .spacing(2) // in dp
                .onPageError(this)
                .onPageScroll(this)
                .nightMode(false)
                .load();
    }

    private void savePdf() {
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

    // ------------- 密码输入对话框 --------------
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
                .setOnCancelListener(dialog -> {
                    if (!mDocumentOpened) {
                        ReaderActivity.this.finish();
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
        mPasswordDialog.setOnShowListener(onShowListener -> {
            mPasswordDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
            mPasswordDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> {
                        mPasswordDialog.dismiss();
                        if (!mDocumentOpened) {
                            ReaderActivity.this.finish();
                        }
                    });

            mPasswordDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String password = mPasswordEdit.getEditableText().toString();
                        loadDocument(mFileUri, 0, password);
                    });
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

    // ------------ 错误提示对话框 -------------
    private void showErrorDialog() {
        AlertDialog errDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_error_title)
                .setOnCancelListener(dialog -> ReaderActivity.this.finish())
                .setPositiveButton(R.string.dialog_error_button_ok,
                        (dialog, which) -> ReaderActivity.this.finish())
                .create();
        errDialog.setCanceledOnTouchOutside(false);
        errDialog.show();
    }
}
