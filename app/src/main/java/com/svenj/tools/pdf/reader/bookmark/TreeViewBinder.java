package com.svenj.tools.pdf.reader.bookmark;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class TreeViewBinder<VH extends RecyclerView.ViewHolder>
        implements TreeLayoutItemType {
    public abstract VH provideViewHolder(View itemView);

    public abstract void bindView(VH holder, int position, TreeNode node);
}
