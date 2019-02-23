package com.android.toseefkhan.pandog.Utils;

import android.app.Application;
import com.android.toseefkhan.pandog.R;
import com.squareup.leakcanary.LeakCanary;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class InitialSetup extends Application {

    private static final String TAG = "InitialSetup";
    public boolean wait = true;
    public boolean isFirstTimeStart = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Comic Neue.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

}
