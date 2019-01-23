package com.svenj.tools.pdf;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 解决7.0以上Uri.fromFile报错
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

}
