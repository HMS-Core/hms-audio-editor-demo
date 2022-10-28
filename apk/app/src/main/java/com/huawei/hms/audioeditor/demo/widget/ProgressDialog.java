/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.huawei.hms.audioeditor.demo.R;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;

public class ProgressDialog extends DialogFragment implements View.OnClickListener {
    public ProgressBar rdProgress;
    private TextView tvProgress;
    private TextView tvMessage;
    private TextView tvCancel;

    public static ProgressDialog newInstance(String message) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        ProgressDialog fragment = new ProgressDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setProgress(int progress) {
        rdProgress.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();

        // The dialog does not have a title.This parameter needs to be set before setContentView.
        // An error will be reported after setContentView.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        View view = requireActivity().getLayoutInflater().inflate(R.layout.progress_dialog, null);
        rdProgress = view.findViewById(R.id.pb_progress);
        tvProgress = view.findViewById(R.id.tv_progress);
        tvMessage = view.findViewById(R.id.tv_message);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);
        if (getArguments() != null) {
            String message = getArguments().getString("message");
            if (!TextUtils.isEmpty(message)) {
                tvMessage.setText(getArguments().getString("message"));
            }
        } else {
            tvMessage.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onStart() {
        /* *  Set the width to the screen width and position to the middle of the screen.  * */
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(R.color.transparent);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        setProgress(0);
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            HAEAudioExpansion.getInstance().cancelExtractAudio();
        }
    }
}
