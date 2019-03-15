package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.toseefkhan.pandog.Profile.PostsProfileRVAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LottieFontViewGroupI extends FrameLayout {
    private static final String TAG = "LottieFontViewGroup";

    public LottieFontViewGroupI(Context context) {
        super(context);
    }

    public LottieFontViewGroupI(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "LottieFontViewGroup: hey m called context attr");
        init();
    }

    public LottieFontViewGroupI(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private  void init() {

        OnCompositionLoadedListener c = new OnCompositionLoadedListener() {
                    @Override
                    public void onCompositionLoaded(LottieComposition composition) {
                        LottieAnimationView cursorView = new LottieAnimationView(getContext());
                        cursorView.setLayoutParams(new LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        cursorView.setComposition(composition);
                        cursorView.playAnimation();
                        addView(cursorView);
                    }
                };

        LottieComposition.Factory.fromAssetFileName(getContext(), "Mobilo/I.json", c);
    }
}
