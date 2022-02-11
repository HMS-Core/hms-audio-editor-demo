/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.common.Constants;
import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.demo.util.PermissionUtils;
import com.huawei.hms.audioeditor.sdk.HAEConstant;
import com.huawei.hms.audioeditor.sdk.HAETimeLine;
import com.huawei.hms.audioeditor.sdk.HuaweiAudioEditor;
import com.huawei.hms.audioeditor.sdk.asset.HAEAsset;
import com.huawei.hms.audioeditor.sdk.asset.HAEAudioAsset;
import com.huawei.hms.audioeditor.sdk.asset.HAEAudioVolumeCallback;
import com.huawei.hms.audioeditor.sdk.bean.HAEAudioProperty;
import com.huawei.hms.audioeditor.sdk.bean.HAEAudioVolumeObject;
import com.huawei.hms.audioeditor.sdk.engine.audio.thumbnail.WaveformManager;
import com.huawei.hms.audioeditor.sdk.lane.HAEAudioLane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Content: basic function integration (This code is only used as an example. To clearly display interface
 * invoking, parameters are not protected and out-of-bounds array exists.)
 *
 * @date 2021/11/8
 * @since 2021/11/8
 */
public class AudioBaseActivity extends AppCompatActivity
        implements View.OnClickListener {
    private int REQUEST_CODE_FOR_SELECT_AUDIO = 1000;
    private String filePath = "";
    private TextView mBack;
    private Button mChoiceFile;
    private TextView tvFilePath;
    private HuaweiAudioEditor mEditor;
    private HAETimeLine mTimeLine;
    private HAEAudioLane audioLane;
    private HAEAudioAsset audioAsset;
    protected GetThumbNailTask getThumbNailTask;
    private CountDownLatch latchCountdown;
    private TextView mTvDateLength;
    private Button mGetWaveData;
    // Number of imported audio files (single path in this example)
    private List<String> validPath = new ArrayList<>();
    private boolean isThumbNailTaskEnd = false;

    private TextView mTvAssetLength;
    private Button mWaveLength;

    private TextView mTvAssetLength1;
    private Button mAssetDel;

    private Button mBtnExport;
    private int audioFormat = Constants.AV_CODEC_ID_MP3;
    private int audioRate = Constants.SAMPLE_RATE_44100;
    private int audioChannel = 2;

    private String musicPath;

    private MediaPlayer mPlayer;
    private Button mAudioResumePlay;
    private Button mAudioPlay;
    private Button mAudioStop;

    private SeekBar mSbVolume;
    private Button mAssetVolume;
    private TextView mTvVolume;
    private int mProgress_volume = 100;

    private SeekBar mSbSpeed;
    private SeekBar mSbPitch;
    private TextView mTvSpeed;
    private TextView mTvPitch;
    private Button mBtnSpeedAndPitch;
    private float mProgress_speed = 1.0F;
    private float mProgress_pitch = 1.0F;

    private SeekBar mSbFadeIn;
    private SeekBar mSbFadeOut;
    private TextView mFadeIn;
    private TextView mFadeOut;
    private Button mBtnFade;
    private int fadeInTime = 0;
    private int fadeOutTime = 0;

    private final String[] PERMISSIONS =
            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // Permission Request Code
    private static final int PERMISSION_REQUESTS = 1;

    private ProgressDialog progressDialog;
    private volatile boolean isProcessing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_base);
        initBase();
        initView();
        initListener();
        PermissionUtils.checkMorePermissions(
                AudioBaseActivity.this,
                PERMISSIONS,
                new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {

                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        PermissionUtils.requestMorePermissions(AudioBaseActivity.this, PERMISSIONS, PERMISSION_REQUESTS);
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        PermissionUtils.requestMorePermissions(AudioBaseActivity.this, PERMISSIONS, PERMISSION_REQUESTS);
                    }
                });
        initProgress();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.choice_file && v.getId() != R.id.back) {
            if (mTimeLine == null || audioLane == null || audioAsset == null) {
                Toast.makeText(this, "Please import Audio", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        switch (v.getId()) {
            case R.id.choice_file:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("audio/*");
                startActivityForResult(intent, REQUEST_CODE_FOR_SELECT_AUDIO);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.get_wave_data:
                if (isThumbNailTaskEnd){
                    getWaveData();
                }else {
                    Toast.makeText(this, getResources().getString(R.string.wait_for_wave),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.get_splits_data:
                long splitPoint = audioAsset.getEndTime()/2;
                boolean isSuccess = audioLane.splitAsset(audioAsset.getIndex(), splitPoint);
                if (isSuccess){
                    mTvAssetLength.setText(audioLane.getAssets().size()+"");
                }
                break;
            case R.id.asset_del:
                if (audioLane.getAssets().size()>0){
                    HAEAsset haeAsset = audioLane.getAssets().get(0);
                    audioLane.removeAsset(haeAsset.getIndex());
                    mTvAssetLength1.setText(audioLane.getAssets().size()+"");
                }else {
                    Toast.makeText(this, getResources().getString(R.string.no_delete_audio),Toast.LENGTH_SHORT).show();
                }
               break;
            case R.id.btn_export:
                if(isProcessing){
                    if (progressDialog != null && !progressDialog.isShowing() ){
                        progressDialog.show();
                    }
                    return;
                }
                isProcessing = true;
                showProgress();
                musicPath = Environment.getExternalStorageDirectory().getPath() + "/export"+System.currentTimeMillis()+ ".mp3";
                HuaweiAudioEditor.getInstance().setExportAudioCallback(exportAudioCallback);
                // Setting Audio Attributes
                HAEAudioProperty audioProperty = new HAEAudioProperty();
                audioProperty.setEncodeFormat(audioFormat);
                audioProperty.setSampleRate(audioRate);
                audioProperty.setChannels(audioChannel);
                // Exporting an Audio File (Time-consuming operation. You are advised to process it in a subthread.)
                new Thread(
                        () -> {
                            HuaweiAudioEditor.getInstance().exportAudio(audioProperty, musicPath);
                        })
                        .start();
                break;
            case R.id.audio_resume_play:
                if (HuaweiAudioEditor.getInstance().getState() == HuaweiAudioEditor.State.COMPILE) {
                    Toast.makeText(this, R.string.play_delay, Toast.LENGTH_SHORT).show();
                    return;
                }
                HuaweiAudioEditor.getInstance()
                        .playTimeLine(mTimeLine.getCurrentTime(), mTimeLine.getEndTime());
                break;
            case R.id.audio_play:
                if (HuaweiAudioEditor.getInstance().getState() == HuaweiAudioEditor.State.COMPILE) {
                    Toast.makeText(this, R.string.play_delay, Toast.LENGTH_SHORT).show();
                    return;
                }
                HuaweiAudioEditor.getInstance()
                        .playTimeLine(mTimeLine.getStartTime(), mTimeLine.getEndTime());
                break;
            case R.id.audio_stop:
                HuaweiAudioEditor.getInstance().pauseTimeLine();
                break;
            case R.id.asset_volume:
                float volumeValue;
                if (mProgress_volume > 100) {
                    volumeValue = mProgress_volume / 20f;
                } else {
                    volumeValue = mProgress_volume * 0.01f;
                }
                boolean success = audioAsset.setVolume(volumeValue);
                // Obtain the volume.
                float volume = audioAsset.getVolume();
                break;
            case R.id.asset_speed_pitch:
                boolean success1 = audioLane.changeAssetSpeed(audioAsset.getIndex(), mProgress_speed, mProgress_pitch);
                // Acquiring the speed of sound, pitch
                float speed = audioLane.getSpeed(audioAsset.getIndex());
                float pitch = audioLane.getPitch(audioAsset.getIndex());
                break;
            case R.id.asset_fade:
                // Set Fade-in and Fade-out
                boolean success2 = audioLane.setAudioAssetFade(audioAsset.getIndex(), fadeInTime, fadeOutTime);
                 // Get Fade-In and Fade-Out
                int inTime = audioAsset.getFadeInTimeMs();
                int outTime = audioAsset.getFadeOutTimeMs();
                break;
            default:
                break;
        }
    }
    HuaweiAudioEditor.ExportAudioCallback exportAudioCallback = new HuaweiAudioEditor.ExportAudioCallback() {
        @Override
        public void onCompileProgress(long time, long duration) {
            if (duration != 0) {
                int progress = (int) (time * 100 / duration);
                if (progress > 100) {
                    progress = 100;
                }
                int finalProgress = progress;
                runOnUiThread(
                        () -> {
                            Log.d("progress",finalProgress+"");
                            if (progressDialog != null) {
                                progressDialog.setProgress(finalProgress);
                            }
                        });
            }
        }
        @Override
        public void onCompileFinished() {
            Log.d("progress","finish");
            isProcessing = false;
            runOnUiThread(()->{
                hideProgress();
                Toast.makeText(AudioBaseActivity.this,"Finish",Toast.LENGTH_SHORT).show();
            });
        }
        @Override
        public void onCompileFailed(int errCode, String errorMsg) {
            isProcessing = false;
            runOnUiThread(()->{
                hideProgress();
                Toast.makeText(AudioBaseActivity.this,errorMsg,Toast.LENGTH_SHORT).show();
            });
        }
    };


    public void initView() {
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mChoiceFile = findViewById(R.id.choice_file);
        mChoiceFile.setOnClickListener(this);
        tvFilePath = findViewById(R.id.file_path);
        tvFilePath.setText(filePath);
        mTvDateLength = findViewById(R.id.tv_data_length);
        mGetWaveData = findViewById(R.id.get_wave_data);
        mGetWaveData.setOnClickListener(this);

        mTvAssetLength = findViewById(R.id.tv_asset_length);
        mWaveLength = findViewById(R.id.get_splits_data);
        mWaveLength.setOnClickListener(this);

        mTvAssetLength1 = findViewById(R.id.tv_asset_length1);
        mAssetDel = findViewById(R.id.asset_del);
        mAssetDel.setOnClickListener(this);

        mBtnExport = findViewById(R.id.btn_export);
        mBtnExport.setOnClickListener(this);

        mAudioResumePlay = findViewById(R.id.audio_resume_play);
        mAudioPlay = findViewById(R.id.audio_play);
        mAudioStop = findViewById(R.id.audio_stop);
        mAudioResumePlay.setOnClickListener(this);
        mAudioPlay.setOnClickListener(this);
        mAudioStop.setOnClickListener(this);

        mTvVolume = findViewById(R.id.tv4);
        mSbVolume = findViewById(R.id.sb_volume);
        mAssetVolume = findViewById(R.id.asset_volume);
        mAssetVolume.setOnClickListener(this);
        mSbVolume.setMax(100);
        mTvVolume.setText(getResources().getString(R.string.asset_volume));

        mSbSpeed = findViewById(R.id.sb_speed);
        mSbSpeed.setMax(95);
        mSbPitch = findViewById(R.id.sb_pitch);
        mSbPitch.setMax(49);
        mTvSpeed = findViewById(R.id.tv5);
        mTvPitch = findViewById(R.id.tv6);
        mBtnSpeedAndPitch = findViewById(R.id.asset_speed_pitch);
        mBtnSpeedAndPitch.setOnClickListener(this);

        mSbFadeIn = findViewById(R.id.sb_fade_in);
        mSbFadeOut = findViewById(R.id.sb_fade_out);
        mFadeIn = findViewById(R.id.tv7);
        mFadeOut = findViewById(R.id.tv8);
        mBtnFade = findViewById(R.id.asset_fade);
        mBtnFade.setOnClickListener(this);
        mSbFadeIn.setMax(100);
        mSbFadeOut.setMax(100);
    }

    private void initListener(){
        mSbVolume.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        mProgress_volume = i;
                        float volumeValue =  (float) mProgress_volume/10;
                        mTvVolume.setText(getResources().getString(R.string.asset_volume)+"-("+volumeValue+")");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        mSbSpeed.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        mProgress_speed = (i+5)/10;
                        mTvSpeed.setText(getResources().getString(R.string.asset_speed)+"-("+mProgress_speed+")");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        mSbPitch.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        mProgress_pitch = (i+1)/10;
                        mTvPitch.setText(getResources().getString(R.string.asset_pitch)+"-("+mProgress_pitch+")");
                        mTvSpeed.setText(getResources().getString(R.string.asset_speed)+"-("+mProgress_speed+")");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        mSbFadeIn.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        fadeInTime = (int)((float) (progress / 10.0));
                        mFadeIn.setText(getResources().getString(R.string.asset_fade_in)+"-("+fadeInTime + "s)");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        mSbFadeOut.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        fadeOutTime = (int)((float) (progress / 10.0));
                        mFadeOut.setText(getResources().getString(R.string.asset_fade_out)+"-("+fadeOutTime + "s)");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

    }

    public void initBase() {
        // Creating an Image Editing Management Class
        mEditor = HuaweiAudioEditor.create(AudioBaseActivity.this);
        mEditor.initEnvironment();
    }

    public void importIntegration(String path) {
        validPath.clear();
        validPath.add(path);
        // Creating a Timeline
        mTimeLine = mEditor.getTimeLine();
        // Create a swimlane
        audioLane = mTimeLine.appendAudioLane();
        audioAsset = audioLane.appendAudioAsset(path,mTimeLine.getCurrentTime());
        if (audioAsset == null){
            Toast.makeText(this,"Import invalid path",Toast.LENGTH_SHORT).show();
            return;
        }

        mSbFadeIn.setMax((int) ((audioAsset.getDuration() * 10) / (1000)));
        mSbFadeOut.setMax((int) ((audioAsset.getDuration() * 10) / (1000)));
        fadeInTime = (int) ((float) (mSbFadeIn.getProgress() / 10.0) * 1000);
        fadeOutTime = (int) ((float) (mSbFadeOut.getProgress() / 10.0) * 1000);
        mSbFadeIn.setProgress(audioAsset.getFadeInTimeMs() / 100);
        mSbFadeOut.setProgress(audioAsset.getFadeOutTimeMs() / 100);

        mTvVolume.setText(getResources().getString(R.string.asset_volume)+"-("+audioAsset.getVolume()+")");
        mProgress_pitch = audioAsset.getPitch();
        mProgress_speed = audioAsset.getSpeed();
        mTvPitch.setText(getResources().getString(R.string.asset_pitch)+"-("+mProgress_pitch+")");
        mTvSpeed.setText(getResources().getString(R.string.asset_speed)+"-("+mProgress_speed+")");


        mTvAssetLength.setText(audioLane.getAssets().size()+"");
        latchCountdown = new CountDownLatch(validPath.size());
        new Thread(
                () -> {
                    latchCountdown = new CountDownLatch(validPath.size());
                    updateAudioCache(validPath, latchCountdown);
                    try {
                        latchCountdown.await();
                        Log.i("AudioBase", "all the audio data load complete");

                    } catch (InterruptedException e) {
                        Log.e("AudioBase", "got exception " + e.getMessage());
                    }
                    if (getApplicationContext() == null) {
                        return;
                    }
                    isThumbNailTaskEnd = true;

                })
                .start();
    }
    private void updateAudioCache(final List<String> validPath, CountDownLatch latchCountdown) {
        WaveformManager.getInstance().generateWaveThumbnailCache(validPath, latchCountdown);
    }

    // Obtaining Waveform Data
    private void getWaveData(){
        if (getThumbNailTask != null) {
            getThumbNailTask.cancel(true);
            getThumbNailTask = null;
        }
        getThumbNailTask = new GetThumbNailTask(audioAsset.getThumbNailRequestId());
        getThumbNailTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    protected class GetThumbNailTask extends AsyncTask<Void, Void, Void> {
        private String currentRequestId;

        public GetThumbNailTask(String currentRequestId) {
            this.currentRequestId = currentRequestId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // update wave(intervalLevel 1-9)
            audioAsset.updateVolumeObjects(currentRequestId, HAEConstant.INTERVAL_TEN_FRAME,
                    audioAsset.getStartTime(), audioAsset.getEndTime(),
                    new HAEAudioVolumeCallback() {
                        @Override
                        public void onAudioAvailable(HAEAudioVolumeObject haeAudioVolumeObject) {

                        }

                        @Override
                        public void onAudioEnd() {
                            CopyOnWriteArrayList<HAEAudioVolumeObject> receivedVolumeObjects =
                                    audioAsset.getAudioList();
                            runOnUiThread(
                                    () -> {
                                        mTvDateLength.setText(receivedVolumeObjects.size()+"");
                                    });

                            Log.d("audioVolumeObject",receivedVolumeObjects.size()+"");

                        }
                    });
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_SELECT_AUDIO) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "No audio selected", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    String path = FileUtils.getRealPath(this, uri);
                    importIntegration(path);
                    if (!TextUtils.isEmpty(path)) {
                        this.filePath = path;
                        if (tvFilePath != null) {
                            tvFilePath.setText(filePath);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WaveformManager.getInstance().cleanWaveThumbnailCache(validPath);
        mEditor.stopEditor();
    }
    private void showProgress() {
        if (progressDialog != null) {
            progressDialog.setProgress(0);
            progressDialog.show();
        }
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setTitle(getString(R.string.title_progress));
        progressDialog.setMax(100);
    }
}
