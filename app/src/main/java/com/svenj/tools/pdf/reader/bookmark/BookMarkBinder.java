package com.svenj.tools.pdf.reader.bookmark;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.svenj.tools.pdf.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookMarkBinder extends TreeViewBinder<BookMarkBinder.BookMarkViewHolder> {

    @Override
    public BookMarkViewHolder provideViewHolder(View itemView) {
        return new BookMarkViewHolder(itemView);
    }

    @Override
    public void bindView(BookMarkViewHolder holder, int position, TreeNode node) {
        if (node.isExpand()) {
            holder.ivArrow.setImageResource(R.drawable.node_open);
        } else {
            holder.ivArrow.setImageResource(R.drawable.node_close);
        }
        BookmarkItem item = (BookmarkItem) node.getContent();
        holder.tvTitle.setText(item.title);
        holder.tvPageNumber.setText(String.valueOf(item.pageNumber));

        if (node.isLeaf()) {
            holder.ivArrow.setVisibility(View.INVISIBLE);
        } else {
            holder.ivArrow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item;
    }

    public static class BookMarkViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivArrow;
        public TextView tvTitle;
        public TextView tvPageNumber;

        public BookMarkViewHolder(@NonNull View itemView) {
            super(itemView);

            ivArrow = itemView.findViewById(R.id.arrow);
            tvTitle = itemView.findViewById(R.id.title);
            tvPageNumber = itemView.findViewById(R.id.pageNumber);
        }
    }
}
