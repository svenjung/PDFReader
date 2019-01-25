package com.svenj.tools.pdf.repositories;

import android.content.Context;

import java.util.List;

import androidx.lifecycle.Observer;

public class DocumentRepository {
    private DocumentLiveData mDocumentLiveData;

    public DocumentRepository(Context context, boolean recent) {
        mDocumentLiveData = new DocumentLiveData(context, recent);
    }

    public void addObserver(Observer<List<Pdf>> observer) {
        mDocumentLiveData.observeForever(observer);
    }

    public void removeObserver(Observer<List<Pdf>> observer) {
        mDocumentLiveData.removeObserver(observer);
    }
}
