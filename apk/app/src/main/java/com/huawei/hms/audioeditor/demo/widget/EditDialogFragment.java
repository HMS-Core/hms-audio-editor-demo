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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.huawei.hms.audioeditor.demo.R;

/**
 * Pop-up window with edit box
 * @since 2021/9/9
 */
public class EditDialogFragment extends DialogFragment {
    private static final String TAG = "EditDialogFragment";
    private String title;

    private RenameCallBack callBack;
    private TextView tvTitle;
    private EditText etName;
    private TextView tvCancel;
    private TextView tvConfirm;

    private String name;

    private EditDialogFragment(String title, String name, RenameCallBack callBack) {
        this.title = title;
        this.name = name;
        this.callBack = callBack;
    }

    public static EditDialogFragment newInstance(String title, String name, RenameCallBack callBack) {
        return new EditDialogFragment(title, name, callBack);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();

        // The dialog does not have a title.This parameter needs to be set before setContentView.
        // An error will be reported after setContentView.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_edit_dialog, null);
        tvTitle = view.findViewById(R.id.title);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        etName = view.findViewById(R.id.et_name);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        if (!TextUtils.isEmpty(name)) {
            etName.setText(name);
        }

        initEvent();

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
        super.onStart();
    }

    /**
     * Bind a click event.
     **/
    private void initEvent() {
        tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        tvConfirm.setOnClickListener(v -> {
            String nameStr = etName.getText().toString();
            if (TextUtils.isEmpty(nameStr)) {
                Toast.makeText(
                    getContext(),
                    getContext().getResources().getString(R.string.input_name_hint),
                    Toast.LENGTH_SHORT)
                    .show();
                return;
            }
            if (callBack != null) {
                callBack.onConfirm(nameStr, EditDialogFragment.this);
            }
        });
    }

    public interface RenameCallBack {
        void onConfirm(String newName, EditDialogFragment dialog);
    }
}
