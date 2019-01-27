package com.svenj.tools.pdf.reader.bookmark;

import com.svenj.tools.pdf.R;

public class BookmarkItem implements TreeLayoutItemType {
    public String title;
    public int pageNumber;

    public BookmarkItem(String title, int pageNumber) {
        this.title = title;
        this.pageNumber = pageNumber;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item;
    }
}
