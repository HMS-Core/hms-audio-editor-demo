/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.hms.audioeditor.ui.common.listener.OnClickRepeatedListener;

/**
 * Draft dialog box on the home page
 */
public class HomeFragmentPopWindow extends PopupWindow {
    private Activity activity;
    private ActionOnClickListener mListener;
    private int popWidth = 0;
    private int popHeight = 0;

    public HomeFragmentPopWindow(Activity mActivity) {
        activity = mActivity;
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_popup_window, null, false);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        TextView deleteHomePopupWindow = view.findViewById(R.id.delete_home_popup_window);
        TextView renameHomePopupWindow = view.findViewById(R.id.rename_home_popup_window);
        TextView copyHomePopupWindow = view.findViewById(R.id.copy_home_popup_window);
        this.setContentView(view);
        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
        popWidth = view.getMeasuredWidth();
        popHeight = view.getMeasuredHeight();

        // Set External Clickable
        this.setOutsideTouchable(true);
        // Set the pop-up window to be clicked.
        this.setFocusable(true);
        view.setFocusableInTouchMode(true);

        renameHomePopupWindow.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (mListener != null) {
                                mListener.onRenameClick();
                            }
                            dismiss();
                        }));

        copyHomePopupWindow.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (mListener != null) {
                                mListener.onCopyClick();
                            }
                            dismiss();
                        }));

        deleteHomePopupWindow.setOnClickListener(
                new OnClickRepeatedListener(
                        v -> {
                            if (mListener != null) {
                                mListener.onDeleteClick();
                            }
                            dismiss();
                        }));

        view.setOnKeyListener(
                (v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                        return true;
                    }
                    return false;
                });
    }

    public int getPopWidth() {
        return popWidth;
    }

    public int getPopHeight() {
        return popHeight;
    }

    public void setOnActionClickListener(ActionOnClickListener listener) {
        mListener = listener;
    }

    public interface ActionOnClickListener {
        /**
         * change draft name
         */
        void onRenameClick();

        /**
         * copy draft
         */
        void onCopyClick();

        /**
         * delete draft
         */
        void onDeleteClick();
    }
}
