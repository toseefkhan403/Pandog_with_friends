package com.android.toseefkhan.pandog.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LottieFontViewGroupN2 extends FrameLayout {
    private static final String TAG = "LottieFontViewGroup";
    private final Map<String, LottieComposition> compositionMap = new HashMap<>();
    private final List<View> views = new ArrayList<>();


    public LottieFontViewGroupN2(Context context) {
        super(context);
        Log.d(TAG, "LottieFontViewGroup: hey m called context");
    //    init();
    }

    public LottieFontViewGroupN2(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "LottieFontViewGroup: hey m called context attr");
        init();
    }

    public LottieFontViewGroupN2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "LottieFontViewGroup: hey m called context attr defstyel");
    //    init();
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
                        cursorView.loop(true);
                        addView(cursorView);
                    }
                };

        LottieComposition.Factory.fromAssetFileName(getContext(), "Mobilo/N.json", c);
        LottieComposition.Factory.fromAssetFileName(getContext(), "Mobilo/N.json", c);
    }

    @Override
    public  void addView(View child, int index) {
        super.addView(child, index);
        Log.d(TAG, "addView: indices information " + index);
        if (index == -1) {
            views.add(child);
        } else {
            views.add(index, child);
        }
    }


}
