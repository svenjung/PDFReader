package com.svenj.tools.pdf;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.util.Log;

import com.svenj.tools.pdf.repositories.AppDatabase;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.repositories.PdfRepository;

import java.util.List;

public class RecentViewModel extends AndroidViewModel {

    private PdfRepository repository;

    public RecentViewModel(@NonNull Application application) {
        super(application);

        repository = new PdfRepository(AppDatabase.getInstance(application).getPdfDAO());
    }

    public void addObserver(Observer<List<Pdf>> observer) {
        repository.addObserver(observer);
    }

    public void removeObserver(Observer<List<Pdf>> observer) {
        repository.removeObserver(observer);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
