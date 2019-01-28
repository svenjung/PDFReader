package com.svenj.tools.pdf.reader;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shockwave.pdfium.PdfDocument;
import com.svenj.tools.pdf.R;
import com.svenj.tools.pdf.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

public class DocInfoListAdapter extends BaseAdapter {
    final private List<DocMeta> metas;

    public DocInfoListAdapter(List<DocMeta> metas) {
        this.metas = metas;
    }

    @Override
    public int getCount() {
        return metas == null ? 0 : metas.size();
    }

    @Override
    public Object getItem(int position) {
        return metas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView != null) {
            vh = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_doc_info_item, parent, false);
            vh = new ViewHolder();
            vh.tvMetaTag = convertView.findViewById(R.id.meta_tag);
            vh.tvMetaInfo = convertView.findViewById(R.id.meta_info);
            convertView.setTag(vh);
        }

        DocMeta meta = metas.get(position);
        vh.tvMetaTag.setText(meta.getTag());
        vh.tvMetaInfo.setText(meta.getValue());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvMetaTag;
        TextView tvMetaInfo;
    }

    public static class DocMeta {
        private String tag;
        private String value;

        public DocMeta(String tag, String value) {
            this.tag = tag;
            this.value = value;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static List<DocMeta> buildMetaList(@NonNull Context context, PdfDocument.Meta meta,
                                              @NonNull String title) {
        Resources res = context.getResources();
        List<DocMeta> metas = new ArrayList<>();
        String metaTitle = meta.getTitle();
        if (TextUtils.isEmpty(metaTitle)) {
            metaTitle = title;
        }
        metas.add(new DocMeta(res.getString(R.string.document_title), metaTitle));
        metas.add(new DocMeta(res.getString(R.string.document_author), meta.getAuthor()));
        metas.add(new DocMeta(res.getString(R.string.document_subject), meta.getSubject()));
        metas.add(new DocMeta(res.getString(R.string.document_keywords), meta.getSubject()));
        metas.add(new DocMeta(res.getString(R.string.document_creator), meta.getCreator()));
        metas.add(new DocMeta(res.getString(R.string.document_producer), meta.getProducer()));

        Date metaDate = Utils.parsePdfMetaDate(meta.getCreationDate());
        String date = metaDate == null ? "" : Utils.formatDate(metaDate);
        metas.add(new DocMeta(res.getString(R.string.document_creation_date), date));
        metaDate = Utils.parsePdfMetaDate(meta.getModDate());
        date = metaDate == null ? "" : Utils.formatDate(metaDate);
        metas.add(new DocMeta(res.getString(R.string.document_mod_date), date));

        return metas;
    }
}
