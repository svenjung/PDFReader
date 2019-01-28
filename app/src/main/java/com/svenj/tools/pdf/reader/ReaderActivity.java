package com.svenj.tools.pdf.reader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import com.svenj.tools.pdf.reader.bookmark.BookmarkFragment;
import com.svenj.tools.pdf.repositories.AppDatabase;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.repositories.PdfDAO;

import java.io.File;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

// TODO 使用BottomSheetBehavior实现目录预览
public class ReaderActivity extends AppCompatActivity implements OnPageChangeListener,
        OnLoadCompleteListener, OnPageErrorListener, OnPageScrollListener, OnErrorListener,
        BookmarkFragment.BookmarkFactory {
    private static final String TAG = "PdfReaderActivity";

    private PDFView mPdfView;
    private DrawerLayout mDrawerLayout;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.doc_information:
                showDocInfoDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        // onDetachedFromWindow called this method
        // mPdfView.recycle();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        mPdfView.getTableOfContents();
        mDocumentOpened = true;
        setTitle(mPdf.getFileName());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        mPageNumberView.setVisibility(View.VISIBLE);

        dismissPasswordDialog();

        prepareBookmarkFragment();
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

        mPageNumberView.setText((page + 1) + " / " + pageCount);
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page, t);
    }

    @Override
    public void onPageScrolled(int page, float positionOffset) {
    }

    // 提供Bookmark列表
    @Override
    public List<PdfDocument.Bookmark> getBookmarks() {
        if (mDocumentOpened) {
            return mPdfView.getTableOfContents();
        }
        return null;
    }

    // Bookmark点击回调
    @Override
    public void onBookmarkClick(int selectedPage) {
        if (mDocumentOpened) {
            closeDrawer();
            mPdfView.jumpTo(selectedPage, false);
        }
    }

    private void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void openDrawer() {
        if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
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
        mPdfView.useBestQuality(true);

        // 底栏拦截点击事件，避免穿透到PdfView
        bottomBar.setOnClickListener(v -> {
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.bookMarks).setOnClickListener(mBottomBarClickListener);
        findViewById(R.id.share).setOnClickListener(mBottomBarClickListener);
        findViewById(R.id.settings).setOnClickListener(mBottomBarClickListener);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void tryLoadDocument() {
        Intent intent = getIntent();
        mFileUri = intent.getData();

        if (mFileUri != null) {
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
                .enableAntialiasing(true)
                .onPageChange(this)
                .swipeHorizontal(false)
                .onLoad(this)
                .onError(this)
                .enableDoubletap(false)
                .spacing(1) // in dp
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

    private void prepareBookmarkFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.navigation_content, new BookmarkFragment());
        ft.commit();

        closeDrawer();
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

    private View.OnClickListener mBottomBarClickListener = v -> {
        int id = v.getId();
        switch (id) {
            case R.id.bookMarks:
                openDrawer();
                break;
            case R.id.share:
                break;
            case R.id.settings:
                break;
        }
    };

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

    // ------------ 文件信息对话框 -------------
    private void showDocInfoDialog() {
        List<DocInfoListAdapter.DocMeta> metas = DocInfoListAdapter
                .buildMetaList(this, mPdfView.getDocumentMeta(), mPdf.getFileName());
        DocInfoListAdapter adapter = new DocInfoListAdapter(metas);
        AlertDialog docInfo = new AlertDialog.Builder(this)
                .setAdapter(adapter, null)
                .create();
        docInfo.show();
    }
}
