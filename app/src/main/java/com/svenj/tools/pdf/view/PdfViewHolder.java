package com.svenj.tools.pdf.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.svenj.tools.pdf.R;
import com.svenj.tools.pdf.repositories.Pdf;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.svenj.tools.pdf.Utils.buildSubtitle;

@SuppressLint("InflateParams")
class PdfViewHolder extends RecyclerView.ViewHolder {
    private TextView title;
    private TextView subtitle;

    static PdfViewHolder create(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_list_item, null);
        return new PdfViewHolder(view);
    }

    private PdfViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.title);
        subtitle = itemView.findViewById(R.id.subtitle);
    }

    void bindItem(final Pdf pdf, final OnItemClickListener itemClickListener) {
        title.setText(pdf.getFileName());
        subtitle.setText(buildSubtitle(pdf.getCreateTime(), pdf.getFileSize()));
        if (itemClickListener != null) {
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(pdf));
        }
    }
}
