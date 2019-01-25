package com.svenj.tools.pdf.repositories;

import android.content.Context;
import android.util.Log;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DocumentLiveData extends LiveData<List<Pdf>> {
    private boolean mRecentLivaData;
    private PdfDAO mPdfDao;
    private Disposable mDisposable;

    public DocumentLiveData(Context context, boolean recent) {
        mRecentLivaData = recent;
        mPdfDao = AppDatabase.getInstance(context).getPdfDAO();
    }

    @Override
    protected void onActive() {
        super.onActive();
        Flowable<List<Pdf>> flowable;
        if (mRecentLivaData) {
            flowable = mPdfDao.getRecentPdf();
        } else {
            flowable = mPdfDao.getAllPdf();
        }

        mDisposable = flowable.subscribeOn(Schedulers.io())
                .subscribe(this::postValue,
                        t -> Log.e("PdfRepository", "Query pdf items failed", t));
    }

    @Override
    protected void onInactive() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        super.onInactive();
    }
}
