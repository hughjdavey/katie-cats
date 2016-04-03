package com.hugh.katiecats.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.hugh.katiecats.R;

public class Animations {

    /**
     * Interface for callers wishing to have some actions executed after the animation
     */
    public interface PostAnimAction {
        void execute();
    }

    private final static int CONTRACT_ANIM_ID = R.anim.contract;
    private final static int EXPAND_ANIM_ID = R.anim.expand;

    private static Animation contract;
    private static Animation expand;

    public static void contract(Context context, @NonNull View view) {
        contract(context, view, null);
    }

    public static void expand(Context context, @NonNull View view) {
        expand(context, view, null);
    }

    public static void contract(Context context, @NonNull View view, @Nullable final PostAnimAction action) {
        contract = AnimationUtils.loadAnimation(context, CONTRACT_ANIM_ID);
        contract.reset();

        if (action != null) {
            contract.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    action.execute();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        view.bringToFront();
        view.startAnimation(contract);
    }

    public static void expand(Context context, @NonNull View view, @Nullable final PostAnimAction action) {
        expand = AnimationUtils.loadAnimation(context, EXPAND_ANIM_ID);
        expand.reset();

        if (action != null) {
            expand.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    action.execute();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        view.bringToFront();
        view.startAnimation(expand);
    }
}
