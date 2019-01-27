package com.svenj.tools.pdf.reader.bookmark;

import android.view.View;
import android.widget.TextView;

import com.svenj.tools.pdf.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemNodeBinder extends TreeViewBinder<ItemNodeBinder.ItemViewHolder> {

    @Override
    public ItemViewHolder provideViewHolder(View itemView) {
        return new ItemViewHolder(itemView);
    }

    @Override
    public void bindView(ItemViewHolder holder, int position, TreeNode node) {
        Item item = (Item) node.getContent();
        holder.tvTitle.setText(item.getTitle());
        holder.tvPage.setText(String.valueOf(item.getPage()));
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvPage;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvPage = itemView.findViewById(R.id.pageNumber);
        }
    }
}
