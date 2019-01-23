package com.svenj.tools.pdf;

import android.content.Context;
import android.content.res.Resources;

public class SystemBarUtils {

    private static Integer statusBarHeight = null;

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != null) {
            return statusBarHeight;
        }
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight = dpToPx(context, 24);
        }

        return statusBarHeight;
    }

    public static int dpToPx(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * dp + 0.5f);
    }
}
