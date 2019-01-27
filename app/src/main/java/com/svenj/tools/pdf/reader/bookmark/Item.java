package com.svenj.tools.pdf.reader.bookmark;

import com.svenj.tools.pdf.R;

public class Item implements TreeLayoutItemType {
    private String title;
    private int page;

    public Item(String title, int page) {
        this.title = title;
        this.page = page;
    }

    public String getTitle() {
        return title;
    }

    public int getPage() {
        return page;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item;
    }

}
