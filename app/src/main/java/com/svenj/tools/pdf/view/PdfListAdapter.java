package com.svenj.tools.pdf.view;

import android.view.ViewGroup;

import com.svenj.tools.pdf.repositories.Pdf;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PdfListAdapter extends RecyclerView.Adapter<PdfViewHolder> {
    List<Pdf> mPdfs = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public PdfListAdapter(OnItemClickListener l) {
        onItemClickListener = l;
    }

    public void setData(List<Pdf> pdfs) {
        mPdfs = pdfs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return PdfViewHolder.create(viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder pdfViewHolder, int i) {
        pdfViewHolder.bindItem(mPdfs.get(i), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mPdfs == null ? 0 : mPdfs.size();
    }
}
