/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.common.utils.GsonUtils;
import com.huawei.hms.audioeditor.demo.restful.TtsingCloudManager;
import com.huawei.hms.audioeditor.demo.restful.TtsingConstant;
import com.huawei.hms.audioeditor.demo.restful.TtsingTaskListener;
import com.huawei.hms.audioeditor.demo.restful.bean.SongConfigBean;
import com.huawei.hms.audioeditor.ui.common.utils.AudioEditText;

import androidx.appcompat.app.AppCompatActivity;

public class SongSynthesisActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener  {
    private static final String TAG = "SongSynthesisActivity";
    private static final String SONG_JSON_NAME = "song_config.json";
    private TextView timbreTxt;
    private Button ttsingFile;
    private RadioGroup rgFileSingType;
    private RadioGroup rgFileSingTimbre;
    private RadioGroup rgSongName;
    private ProgressDialog progressDialog;
    private TextView mBack;
    private AudioEditText audioEditText;
    private LinearLayout presetLayout;
    private TtsingCloudManager ttsingCloudManager = new TtsingCloudManager();;

    // sing compose, compose type
    private int composeType = TtsingConstant.SING_COMPOSE_TYPE_XML;

    // sing compose, timbre type
    private int timbreType = TtsingConstant.SING_TIMBRE_LYRIC_POP_FEMALE;

    // sing compose, song id
    private String songId = TtsingConstant.SONG_ACCOMPANIMEN_ID_1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_synthesis);
        timbreTxt = findViewById(R.id.timbre_text);
        rgFileSingType = findViewById(R.id.sing_type);
        rgFileSingType.setOnCheckedChangeListener(this);
        rgFileSingTimbre = findViewById(R.id.timbre);
        rgFileSingTimbre.setOnCheckedChangeListener(this);
        rgSongName = findViewById(R.id.song_name);
        rgSongName.setOnCheckedChangeListener(this);
        presetLayout = findViewById(R.id.preset_layout);
        ttsingFile = findViewById(R.id.begin_sing);
        ttsingFile.setOnClickListener(this);
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        audioEditText = findViewById(R.id.et_lyrics_content);
    }

    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setTitle("loading");
        progressDialog.setMax(100);
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postTtsingCancle();
                progressDialog.dismiss();
            }
        });
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    public void postTtsingCancle() {
        ttsingCloudManager.postTtsingCancle(new TtsingTaskListener() {
            @Override
            public void onResult(Object result) {
                runOnUiThread(() -> {
                    hideProgress();
                });
            }

            @Override
            public void onFail(String taskId, String errorCode) {
                runOnUiThread(() -> {
                    hideProgress();
                    Toast.makeText(SongSynthesisActivity.this, "errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.begin_sing) {
            if (progressDialog != null && progressDialog.isShowing()) {
                return;
            }
            showProgress();
            String cachePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            String cacheName = System.currentTimeMillis() + ".wav";
            if (composeType == TtsingConstant.SING_COMPOSE_TYPE_PRESET && TextUtils.isEmpty(audioEditText.getText().toString())) {
                Toast.makeText(SongSynthesisActivity.this, getResources().getString(R.string.song_lyrics_null), Toast.LENGTH_SHORT).show();
                return;
            }
            ttsingCloudManager.postTtsingAsync(songId, audioEditText.getText().toString(), cachePath, cacheName, composeType, timbreType, new TtsingTaskListener() {
                @Override
                public void onResult(Object result) {
                    runOnUiThread(() -> {
                        hideProgress();
                        Toast.makeText(SongSynthesisActivity.this, "Success: " + result, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFail(String taskId, String errorCode) {
                    runOnUiThread(() -> {
                        hideProgress();
                        Toast.makeText(SongSynthesisActivity.this, "errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else if (v.getId() == R.id.back) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        postTtsingCancle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postTtsingCancle();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.sing_by_preset:
                composeType = TtsingConstant.SING_COMPOSE_TYPE_PRESET;
                rgFileSingTimbre.setVisibility(View.GONE);
                timbreTxt.setVisibility(View.GONE);
                timbreType = TtsingConstant.SING_TIMBRE_LYRIC_POP_FEMALE;
                presetLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.sing_by_xml:
                composeType = TtsingConstant.SING_COMPOSE_TYPE_XML;
                rgFileSingTimbre.setVisibility(View.VISIBLE);
                timbreTxt.setVisibility(View.VISIBLE);
                presetLayout.setVisibility(View.GONE);
                break;
            case R.id.sing_lyric_pop_female:
                timbreType = TtsingConstant.SING_TIMBRE_LYRIC_POP_FEMALE;
                break;
            case R.id.sing_national_style_female:
                timbreType = TtsingConstant.SING_TIMBRE_NATIONAL_STYLE_FEMALE;
                break;
            case R.id.sing_folk_male:
                timbreType = TtsingConstant.SING_TIMBRE_FOLK_MALE;
                break;
            case R.id.song_name1:
                songId = TtsingConstant.SONG_ACCOMPANIMEN_ID_1;
                timbreType = TtsingConstant.SING_TIMBRE_LYRIC_POP_FEMALE;
                break;
            case R.id.song_name2:
                songId = TtsingConstant.SONG_ACCOMPANIMEN_ID_2;
                timbreType = TtsingConstant.SING_TIMBRE_LYRIC_POP_FEMALE;
                break;
            case R.id.song_name3:
                songId = TtsingConstant.SONG_ACCOMPANIMEN_ID_3;
                timbreType = TtsingConstant.SING_TIMBRE_NATIONAL_STYLE_FEMALE;
                break;
            default:
                break;
        }
    }
}


