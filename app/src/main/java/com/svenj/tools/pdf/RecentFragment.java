package com.svenj.tools.pdf;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.svenj.tools.pdf.pdfium.ReaderActivity;
import com.svenj.tools.pdf.repositories.Pdf;
import com.svenj.tools.pdf.view.RecyclerDivider;

import java.util.List;

import static com.svenj.tools.pdf.Utils.buildSubtitle;

public class RecentFragment extends Fragment {

    private RecentViewModel mViewModel;

    private RecyclerView mRecyclerView;
    private RecentListAdapter mAdapter;

    private Observer<List<Pdf>> observer = new Observer<List<Pdf>>() {
        @Override
        public void onChanged(@Nullable List<Pdf> pdfBeans) {
            mAdapter.setPdfList(pdfBeans);
        }
    };

    private RecentListAdapter.OnItemClickListener itemClickListener =
            new RecentListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Pdf pdf) {
                    Intent intent = new Intent(getActivity(), ReaderActivity.class);
                    // API>=21: intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); /* launch as a new document */
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); /* launch as a new document */
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(pdf.getFilePath()));
                    startActivity(intent);
                }
            };

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RecentViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Use the ViewModel
        mViewModel.addObserver(observer);
    }

    @Override
    public void onStop() {
        if (mViewModel != null) {
            mViewModel.removeObserver(observer);
        }
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recent_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.addItemDecoration(new RecyclerDivider(view.getContext()));
        mAdapter = new RecentListAdapter();
        mAdapter.setOnItemClickListener(itemClickListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    private static class RecentListAdapter
            extends RecyclerView.Adapter<RecentListAdapter.PdfItemViewHolder> {
        private List<Pdf> pdfList = null;

        public interface OnItemClickListener {
            void onItemClick(Pdf pdf);
        }

        private OnItemClickListener mOnItemClickListener;

        public void setPdfList(List<Pdf> pdfList) {
            this.pdfList = pdfList;
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener l) {
            mOnItemClickListener = l;
        }

        @NonNull
        @Override
        public PdfItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.pdf_list_item, viewGroup, false);
            return new PdfItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PdfItemViewHolder pdfItemViewHolder, int i) {
            pdfItemViewHolder.bindView(pdfList.get(i));
        }

        @Override
        public int getItemCount() {
            return pdfList == null ? 0 : pdfList.size();
        }

        private class PdfItemViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView subtitle;

            PdfItemViewHolder(@NonNull View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.title);
                subtitle = itemView.findViewById(R.id.subtitle);
            }

            void bindView(final Pdf pdf) {
                title.setText(pdf.getFileName());
                subtitle.setText(buildSubtitle(pdf.getCreateTime(), pdf.getFileSize()));

                if (mOnItemClickListener != null) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnItemClickListener.onItemClick(pdf);
                        }
                    });
                }
            }

        }

    }

}
