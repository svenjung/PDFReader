package com.svenj.tools.pdf.ui;

import android.app.Application;

import com.svenj.tools.pdf.repositories.AppDatabase;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.repositories.PdfRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;

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
