package com.svenj.tools.pdf.ui;


import android.app.Application;

import com.svenj.tools.pdf.repositories.DocumentRepository;
import com.svenj.tools.pdf.repositories.Pdf;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;

public class DocumentViewModel extends AndroidViewModel {
    private DocumentRepository repository;

    public DocumentViewModel(@NonNull Application application) {
        super(application);
        repository = new DocumentRepository(application, true);
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
