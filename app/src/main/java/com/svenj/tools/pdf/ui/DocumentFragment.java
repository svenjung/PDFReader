package com.svenj.tools.pdf.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svenj.tools.pdf.R;
import com.svenj.tools.pdf.reader.ReaderActivity;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.view.OnItemClickListener;
import com.svenj.tools.pdf.view.PdfListAdapter;
import com.svenj.tools.pdf.view.RecyclerDivider;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DocumentFragment extends Fragment {

    private DocumentViewModel mViewModel;
    private RecyclerView mRecyclerView;

    private PdfListAdapter mAdapter;

    private Observer<List<Pdf>> mDocumentObserver = new Observer<List<Pdf>>() {
        @Override
        public void onChanged(@Nullable List<Pdf> pdfBeans) {
            mAdapter.setData(pdfBeans);
        }
    };

    private OnItemClickListener itemClickListener =
            pdf -> {
                Intent intent = new Intent(getActivity(), ReaderActivity.class);
                // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); /* launch as a new document */
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(pdf.getFilePath()));
                startActivity(intent);
            };

    public static DocumentFragment newInstance() {
        return new DocumentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_document, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recent_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.addItemDecoration(new RecyclerDivider(view.getContext()));
        mAdapter = new PdfListAdapter(itemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.title_document);
        }
        mViewModel.addObserver(mDocumentObserver);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (getActivity() != null) {
                getActivity().setTitle(R.string.title_document);
            }
        }
    }

    @Override
    public void onStop() {
        mViewModel.removeObserver(mDocumentObserver);
        super.onStop();
    }
}
