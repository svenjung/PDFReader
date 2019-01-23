package com.svenj.tools.pdf.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.util.Log;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PdfRepository {
    private PdfDAO pdfDAO;

    private RecentLiveData recentLiveData;

    public PdfRepository(PdfDAO pdfDAO) {
        this.pdfDAO = pdfDAO;

        recentLiveData = new RecentLiveData();
    }

    public void addObserver(Observer<List<Pdf>> observer) {
        recentLiveData.observeForever(observer);
    }

    public void removeObserver(Observer<List<Pdf>> observer) {
        recentLiveData.removeObserver(observer);
    }

    private class RecentLiveData extends LiveData<List<Pdf>> {
        private Disposable disposable;

        @Override
        protected void onActive() {
            disposable = pdfDAO.getAllPdfs()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Pdf>>() {
                        @Override
                        public void accept(List<Pdf> pdfBeans) {
                            setValue(pdfBeans);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Log.e("PdfRepository", "Query pdf items failed", throwable);
                        }
                    });
        }

        @Override
        protected void onInactive() {
            Log.e("LiveData", "onInactive");
            if (disposable != null) {
                disposable.dispose();
                disposable = null;
            }
        }
    }
}