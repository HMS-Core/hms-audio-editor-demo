/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.ui.R;

public class ToastWrapper {
    private static CharSequence message;
    private TimeCount timeCount;
    private Handler mHandler = new Handler();
    private boolean canceled = true;
    Toast toast;
    private static TextView tvMsg;

    public ToastWrapper(Toast toast) {
        this.toast = toast;
    }

    public void show() {
        if (toast != null) {
            toast.show();
        }
    }

    public void show(int duration) {
        timeCount = new TimeCount(duration, 1000);
        if (canceled) {
            timeCount.start();
            canceled = false;
            showUntilCancel();
        }
    }

    public static ToastWrapper makeText(Context context, CharSequence text, int duration) {
        message = text;
        Toast t = new Toast(context);
        View v = LayoutInflater.from(context).inflate(R.layout.audio_toast_custom, null);
        tvMsg = v.findViewById(R.id.content);
        tvMsg.setText(text);
        t.setGravity(Gravity.BOTTOM, 0, SizeUtils.dp2Px(context, 30));
        t.setDuration(duration);
        t.setView(v);
        return new ToastWrapper(t);
    }

    public static ToastWrapper makeText(Context context, CharSequence text) {
        message = text;
        Toast t = new Toast(context);
        View v = LayoutInflater.from(context).inflate(R.layout.audio_toast_custom, null);
        tvMsg = v.findViewById(R.id.content);
        tvMsg.setText(text);
        t.setGravity(Gravity.BOTTOM, 0, SizeUtils.dp2Px(context, 30));
        t.setView(v);
        return new ToastWrapper(t);
    }

    public static ToastWrapper makeText(Context context, int res, int duration) {
        return makeText(context, context.getResources().getString(res), duration);
    }

    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvMsg.setText(message);
        }

        @Override
        public void onFinish() {
            hide();
        }
    }

    private void showUntilCancel() {
        if (canceled) {
            return;
        }
        toast.show();
        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        showUntilCancel();
                    }
                },
                Toast.LENGTH_LONG);
    }

    private void hide() {
        if (toast != null) {
            toast.cancel();
        }
        canceled = true;
    }
}
