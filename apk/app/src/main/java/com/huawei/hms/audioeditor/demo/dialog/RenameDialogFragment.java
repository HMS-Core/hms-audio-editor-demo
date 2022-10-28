/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.dialog;

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
import com.huawei.hms.audioeditor.sdk.util.SmartLog;
import com.huawei.hms.audioeditor.demo.MediaData;
import com.huawei.hms.audioeditor.demo.util.PathUtils;
import com.huawei.hms.audioeditor.demo.util.ToastWrapper;
import java.io.File;
import java.util.ArrayList;

/**
 * Draft renaming pop-up window
 *
 */
public class RenameDialogFragment extends DialogFragment {
    private static final String TAG = "RenameDialogFragment";
    private String title;

    private MediaData media;
    private RenameCallBack callBack;
    private TextView tvTitle;
    private EditText etName;
    private TextView tvCancel;
    private TextView tvConfirm;

    private ArrayList<String> brotherFileNames = new ArrayList<>();

    private RenameDialogFragment(String title, MediaData media, RenameCallBack callBack) {
        this.title = title;
        this.media = media;
        this.callBack = callBack;
    }

    public static RenameDialogFragment newInstance(String title, MediaData media, RenameCallBack callBack) {
        return new RenameDialogFragment(title, media, callBack);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_rename_dialog, null);
        tvTitle = view.findViewById(R.id.title);
        tvTitle.setText(title);
        etName = view.findViewById(R.id.et_name);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        etName.setText(PathUtils.getFileNameWithoutSuffix(media.getName()));

        initBrotherFileNames();
        initEvent();
        return view;
    }

    @Override
    public void onStart() {
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(R.color.transparent);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
        super.onStart();
    }

    private void initEvent() {
        tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        tvConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                ToastWrapper.makeText(
                        getContext(),
                        getContext().getResources().getString(R.string.input_name_hint),
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if (name.equals(PathUtils.getFileNameWithoutSuffix(media.getName()))) {
                dismiss();
            } else {
                if (brotherFileNames.contains(name)) {
                    ToastWrapper.makeText(
                            getContext(),
                            getContext().getResources().getString(R.string.have_the_same_file),
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (callBack != null) {
                    callBack.onRename(name + PathUtils.getFileSuffix(media.getPath()), RenameDialogFragment.this);
                }
            }
        });
    }

    private void initBrotherFileNames() {
        String path = media.getPath();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path).getParentFile();
        String suffix = PathUtils.getFileSuffix(path);
        File[] files = file.listFiles(pathname -> pathname.isFile() && pathname.getAbsolutePath().endsWith(suffix));
        if (files == null || files.length == 0) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            brotherFileNames.add(files[i].getName());
            SmartLog.i(TAG, files[i].getName());
        }
    }

    public interface RenameCallBack {
        void onRename(String newName, RenameDialogFragment dialog);
    }
}
