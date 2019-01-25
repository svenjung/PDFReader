package com.svenj.tools.pdf.pdfium;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ViewPropertyAnimatorCompatSet;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;

/**
 * 处理工具栏动画效果
 */
@SuppressLint("RestrictedApi")
public class ToolbarAnimator {
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    private static final long DURATION = 200;

    // Toolbar
    private View toolbar;
    // BottomBar
    private View bottomBar;
    // Page index
    private View pageIndex;

    private ViewPropertyAnimatorCompatSet mAnimatorSet;
    private boolean mToolbarVisible = true;

    public ToolbarAnimator(@NonNull View toolbar, View bottomBar, View pageIndex) {
        this.toolbar = toolbar;
        this.bottomBar = bottomBar;
        this.pageIndex = pageIndex;

        mAnimatorSet = new ViewPropertyAnimatorCompatSet();
    }

    public void toggleToolbar() {
        if (mToolbarVisible) {
            hide();
        } else {
            show();
        }
    }

    private void show() {
        // TopBar
        ViewPropertyAnimatorCompat toolbarAnimator =
                ViewCompat.animate(toolbar).translationY(0);
        mAnimatorSet.play(toolbarAnimator);

        // BottomBar
        ViewPropertyAnimatorCompat bottomBarAnimator =
                ViewCompat.animate(bottomBar).translationY(0);
        mAnimatorSet.play(bottomBarAnimator);

        // PageNumber
        ViewPropertyAnimatorCompat pageNumberAnimator =
                ViewCompat.animate(pageIndex).alpha(1F);
        mAnimatorSet.play(pageNumberAnimator);

        mAnimatorSet.setInterpolator(INTERPOLATOR);
        mAnimatorSet.setDuration(DURATION);

        mAnimatorSet.setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
            }

            @Override
            public void onAnimationEnd(View view) {
                mToolbarVisible = true;
                showStatusBar();
            }

            @Override
            public void onAnimationCancel(View view) {

            }
        });

        mAnimatorSet.start();
    }

    private void hide() {
        int toolbarEndHeight = -toolbar.getMeasuredHeight();
        ViewPropertyAnimatorCompat toolbarAnimator =
                ViewCompat.animate(toolbar).translationY(toolbarEndHeight);
        toolbarAnimator.setInterpolator(INTERPOLATOR);
        toolbarAnimator.setDuration(DURATION);
        mAnimatorSet.play(toolbarAnimator);

        // BottomBar
        int bottomBarHeight = bottomBar.getMeasuredHeight();
        ViewPropertyAnimatorCompat bottomBarAnimator =
                ViewCompat.animate(bottomBar).translationY(bottomBarHeight);
        mAnimatorSet.play(bottomBarAnimator);

        // PageNumber
        ViewPropertyAnimatorCompat pageNumberAnimator =
                ViewCompat.animate(pageIndex).alpha(0);
        mAnimatorSet.play(pageNumberAnimator);

        mAnimatorSet.setInterpolator(INTERPOLATOR);
        mAnimatorSet.setDuration(DURATION);

        mAnimatorSet.setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
                hideStatusBar();
            }

            @Override
            public void onAnimationEnd(View view) {
                mToolbarVisible = false;
            }

            @Override
            public void onAnimationCancel(View view) {

            }
        });

        mAnimatorSet.start();
    }

    private void hideStatusBar() {
        toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        /*| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/
                // 不隐藏导航栏
                /*| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION*/);
    }

    private void showStatusBar() {
        toolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                /*| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/);
    }

    public void cancel() {
        mAnimatorSet.cancel();
        toolbar = null;
        bottomBar = null;
        pageIndex = null;
    }
}
