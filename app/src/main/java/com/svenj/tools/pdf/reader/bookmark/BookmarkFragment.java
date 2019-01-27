package com.svenj.tools.pdf.reader.bookmark;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shockwave.pdfium.PdfDocument;
import com.svenj.tools.pdf.R;
import com.svenj.tools.pdf.SystemBarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkFragment extends Fragment {
    private BookmarkFactory mListener;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvContent = view.findViewById(R.id.tv_content);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
        lp.topMargin = SystemBarUtils.getStatusBarHeight(getActivity());
        tvContent.setLayoutParams(lp);

        RecyclerView recyclerView = view.findViewById(R.id.rv_bookmark);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<TreeNode> nodes = new ArrayList<>();
        List<PdfDocument.Bookmark> bookmarks = mListener.getBookmarks();
        initTreeNode(bookmarks, nodes);
        TreeViewAdapter adapter = new TreeViewAdapter(getContext(), nodes, Arrays.asList(new DirNodeBinder(), new ItemNodeBinder()));
        adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
            @Override
            public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
                if (node.isLeaf()) {
                    Item item = (Item) node.getContent();
                    mListener.onBookmarkClick(item.getPage());
                }
                return false;
            }

            @Override
            public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
                DirNodeBinder.DirViewHolder dirViewHolder = (DirNodeBinder.DirViewHolder) holder;
                final ImageView ivArrow = dirViewHolder.getArrow();
                int rotateDegree = isExpand ? -90 : 90;
                ivArrow.animate().rotationBy(rotateDegree)
                        .start();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookmarkFactory) {
            mListener = (BookmarkFactory) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookmarkFactory");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initTreeNode(List<PdfDocument.Bookmark> bookmarks, List<TreeNode> nodes) {
        for (PdfDocument.Bookmark b : bookmarks) {
            TreeNode node;
            if (b.hasChildren()) {
                node = new TreeNode(new Dir(b.getTitle(), (int) b.getPageIdx()));
                nodes.add(node);
                addChildNode(b.getChildren(), node);
            } else {
                node = new TreeNode(new Item(b.getTitle(), (int) b.getPageIdx()));
                nodes.add(node);
            }
        }
    }

    private void addChildNode(List<PdfDocument.Bookmark> bookmarks, TreeNode node) {
        for (PdfDocument.Bookmark b : bookmarks) {
            TreeNode child;
            if (b.hasChildren()) {
                child = new TreeNode(new Dir(b.getTitle(), (int) b.getPageIdx()));
                node.addChild(child);
                addChildNode(b.getChildren(), child);
            } else {
                child = new TreeNode(new Item(b.getTitle(), (int) b.getPageIdx()));
                node.addChild(child);
            }
        }
    }

    public interface BookmarkFactory {
        List<PdfDocument.Bookmark> getBookmarks();

        void onBookmarkClick(int selectedPage);
    }
}
