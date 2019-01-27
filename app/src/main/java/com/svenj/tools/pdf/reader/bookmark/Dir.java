package com.svenj.tools.pdf.reader.bookmark;

import com.svenj.tools.pdf.R;

public class Dir extends Item {

    public Dir(String title, int page) {
        super(title, page);
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_list_item_dir;
    }
}
