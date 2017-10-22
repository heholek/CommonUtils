package com.gianlu.commonutils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseBottomSheet<E> extends BottomSheetBehavior.BottomSheetCallback {
    protected final TextView title;
    protected final Context context;
    protected final Handler mainHandler;
    protected final ProgressBar loading;
    protected final FrameLayout content;
    private final View mask;
    private final int layoutRes;
    private final BottomSheetBehavior behavior;
    private final boolean forceLayoutInflating;
    protected E current;

    public BaseBottomSheet(View parent, @LayoutRes int layoutRes, boolean forceLayoutInflating) {
        this.layoutRes = layoutRes;
        this.forceLayoutInflating = forceLayoutInflating;
        View sheet = parent.findViewById(R.id.bottomSheet_container);
        context = sheet.getContext();
        behavior = BottomSheetBehavior.from(sheet);
        behavior.setBottomSheetCallback(this);
        behavior.setPeekHeight(0);
        behavior.setHideable(true);

        mask = parent.findViewById(R.id.bottomSheet_mask);
        mask.setVisibility(View.VISIBLE);
        mask.setBackgroundColor(Color.BLACK);
        mask.setClickable(false);
        mask.setAlpha(0);

        title = sheet.findViewById(R.id.bottomSheet_title);
        loading = sheet.findViewById(R.id.bottomSheet_loading);
        content = sheet.findViewById(R.id.bottomSheet_content);
        LayoutInflater.from(context).inflate(layoutRes, content, true);

        ImageButton close = sheet.findViewById(R.id.bottomSheet_close);
        close.setBackground(CommonUtils.resolveAttrAsDrawable(context, R.attr.selectableItemBackgroundBorderless));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collapse();
            }
        });

        mainHandler = new Handler(Looper.getMainLooper());

        bindViews();
        collapse();
    }

    private void showMask() {
        mask.animate().cancel();
        mask.setAlpha(0);
        mask.setClickable(true);
        mask.animate().alpha(.2f).setDuration(200).start();
    }

    private void hideMask() {
        mask.animate().cancel();
        mask.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                mask.setOnClickListener(null);
                mask.setClickable(false);
            }
        }).start();
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setPeekHeight(0);
            hideMask();
        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
            mask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapse();
                }
            });
        }
    }

    public void expandLoading() {
        if (forceLayoutInflating) {
            content.removeAllViews();
            LayoutInflater.from(context).inflate(layoutRes, content, true);
            bindViews();
        }

        loading.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        showMask();
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
    }

    public boolean shouldUpdate() {
        return behavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public abstract void bindViews();

    public void expand(E item) {
        if (forceLayoutInflating) {
            content.removeAllViews();
            LayoutInflater.from(context).inflate(layoutRes, content, true);
            bindViews();
        }

        current = item;
        setupViewInternal(item);
        updateViewInternal(item);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        showMask();
    }

    private void setupViewInternal(E item) {
        if (item == null) return;
        loading.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        setupView(item);
    }

    protected abstract void setupView(@NonNull E item);

    protected abstract void updateView(@NonNull E item);

    private void updateViewInternal(E item) {
        if (item == null) return;
        loading.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        current = item;
        updateView(item);
    }

    public void update(E item) {
        if (item == null) return;
        updateViewInternal(item);
    }

    public void collapse() {
        current = null;
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        hideMask();
    }
}
