/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
 * 带编辑框的弹窗
 * @since 2021/9/9
 */
public class EditDialogFragment extends DialogFragment implements View.OnClickListener {
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
        /* *  设置Dialog没有标题。需在setContentView之前设置，在之后设置会报错  * */
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /* *  设置Dialog背景透明效果  * */
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

        initListener();

        return view;
    }

    public void setEditTextStr(String text){
        if (!TextUtils.isEmpty(text) && etName != null) {
            etName.setText(text);
        }
    }

    @Override
    public void onStart() {
        /* *  设置宽度为屏宽、位置在屏幕中间  * */
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
     * 绑定点击事件
     **/
    private void initListener() {
        tvCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) { // 取消
            dismiss();
        } else if (v.getId() == R.id.tv_confirm) { // 确认
            String name = etName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(
                                getContext(),
                                getContext().getResources().getString(R.string.input_name_hint),
                                Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (callBack != null) {
                callBack.onConfirm(name, EditDialogFragment.this);
            }
        }
    }

    public interface RenameCallBack {
        void onConfirm(String newName, EditDialogFragment dialog);
    }
}
