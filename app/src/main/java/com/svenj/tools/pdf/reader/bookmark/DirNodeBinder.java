package com.svenj.tools.pdf.reader.bookmark;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.svenj.tools.pdf.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DirNodeBinder extends TreeViewBinder<DirNodeBinder.DirViewHolder> {

    @Override
    public DirViewHolder provideViewHolder(View itemView) {
        return new DirViewHolder(itemView);
    }

    @Override
    public void bindView(DirViewHolder holder, int position, TreeNode node) {
        holder.ivArrow.setRotation(0);
        holder.ivArrow.setImageResource(R.drawable.node_close);
        int rotateDegree = node.isExpand() ? 90 : 0;
        holder.ivArrow.setRotation(rotateDegree);
        Dir dir = (Dir) node.getContent();
        holder.tvTitle.setText(dir.getTitle());
        holder.tvPage.setText(String.valueOf(dir.getPage()));

        if (node.isLeaf()) {
            holder.ivArrow.setVisibility(View.INVISIBLE);
        } else {
            holder.ivArrow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item_dir;
    }

    public static class DirViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArrow;
        TextView tvTitle;
        TextView tvPage;

        public ImageView getArrow() {
            return ivArrow;
        }

        DirViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArrow = itemView.findViewById(R.id.arrow);
            tvTitle = itemView.findViewById(R.id.title);
            tvPage = itemView.findViewById(R.id.pageNumber);
        }
    }
}
