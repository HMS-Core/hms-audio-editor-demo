/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.audioeditor.common.agc.HAEApplication;
import com.huawei.hms.audioeditor.demo.adapter.MainRecyclerViewAdapter;
import com.huawei.hms.audioeditor.demo.dialog.RenameDialogFragment;
import com.huawei.hms.audioeditor.demo.recycle.PageIndicatorView;
import com.huawei.hms.audioeditor.demo.recycle.PageRecyclerView;
import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.demo.util.PermissionUtils;
import com.huawei.hms.audioeditor.demo.viewmodel.MainViewModel;
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.demo.widget.ProgressDialog;
import com.huawei.hms.audioeditor.sdk.AudioExtractCallBack;
import com.huawei.hms.audioeditor.sdk.DraftCallback;
import com.huawei.hms.audioeditor.sdk.FailContent;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.LaunchCallback;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.sdk.util.SmartLog;
import com.huawei.hms.audioeditor.ui.api.AudioEditorLaunchOption;
import com.huawei.hms.audioeditor.ui.api.DraftInfo;
import com.huawei.hms.audioeditor.ui.api.HAEUIManager;
import com.huawei.hms.audioeditor.ui.common.bean.Constant;
import com.huawei.hms.audioeditor.ui.common.listener.OnClickRepeatedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS_AUDIO_IMPORT = 1;
    private static final int PERMISSION_REQUESTS_MY_FILES = 2;

    // Permission Request Code
    private static final int PERMISSION_REQUESTS = 1;

    // Request code for selecting a video
    private static final int REQUEST_CODE_FOR_SELECT_VIDEO = 1000;

    // edits
    private static final int PERMISSION_TYPE_EDIT = 1;

    // extract
    private static final int PERMISSION_TYPE_EXTRACT = 2;

    // Format conversion
    private static final int PERMISSION_TYPE_FORMAT = 3;

    // file
    private static final int PERMISSION_TYPE_FILE = 4;

    // streamed
    private static final int PERMISSION_TYPE_FLOW = 5;

    // ai dubbing
    private static final int PERMISSION_TYPE_TEXT_TO_AUDIO = 6;

    // Spatial Rendering
    private static final int PERMISSION_TYPE_SPACE = 8;

    // Material download
    private static final int PERMISSION_MATERIAL = 7;

    // Basic Functions
    private static final int PERMISSION_BASE = 9;

    // Current Permission Request Type
    private int currentPermissionType = PERMISSION_TYPE_EDIT;

    private static final String[] PERMISSIONS =
        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String DRAFT_ID = "draftId";

    private LinearLayout clipMain;
    private TextView draftMain;
    private RecyclerView recyclerviewMain;
    public ProgressDialog fragmentDialog;
    private MainViewModel mainViewModel;
    private List<DraftInfo> mDraftList;
    private MainRecyclerViewAdapter mainRecyclerViewAdapter;
    private HomeFragmentPopWindow homeFragmentPopWindow;
    private Intent intentToActivity;
    private PageRecyclerView mRecycleMenu;
    private PageIndicatorView pageIndicatorView;
    private List<MenuBean> menuBeanList = new ArrayList<>();
    private PageRecyclerView.PageAdapter myAdapter = null;
    private Context mContext;
    protected ViewModelProvider.AndroidViewModelFactory mFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mFactory = new ViewModelProvider.AndroidViewModelFactory(getApplication());
        setContentView(R.layout.activity_main);
        initView();
        initObject();
        initData();
        initEvent();
        // scan new files in storage
        MediaScannerConnection.scanFile(this, new String[]{Environment
            .getExternalStorageDirectory().getAbsolutePath()}, null, null);

        // Setting the APIkey of the SDK
        HAEApplication.getInstance().setApiKey("Set your APIKey");
    }

    protected void initObject() {
        mainViewModel = new ViewModelProvider(this, mFactory).get(MainViewModel.class);
        mDraftList = new ArrayList<>();
        recyclerviewMain.setHasFixedSize(true);
        mainRecyclerViewAdapter = new MainRecyclerViewAdapter(mContext, mDraftList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        recyclerviewMain.setLayoutManager(layoutManager);
        recyclerviewMain.setNestedScrollingEnabled(false);
        recyclerviewMain.setAdapter(mainRecyclerViewAdapter);
    }

    protected void initData() {
        mainViewModel
            .getDraftProjects()
            .observe(
                this,
                draftProjects -> {
                    mDraftList.clear();
                    if (draftProjects.size() > 0) {
                        draftMain.setVisibility(View.VISIBLE);
                        mDraftList.addAll(draftProjects);
                    } else {
                        draftMain.setVisibility(View.GONE);
                    }
                    mainRecyclerViewAdapter.notifyDataSetChanged();
                });
    }

    protected void initEvent() {
        clipMain.setOnClickListener(
            new OnClickRepeatedListener(
                v -> {
                    intentToActivity = new Intent();
                    intentToActivity.putExtra(
                        Constant.EXTRA_AUDIO_CLIP_SHOW_TYPE, Constant.AUDIO_CLIP_SHOW_TYPE_SELECT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(PERMISSIONS, PERMISSION_REQUESTS_AUDIO_IMPORT);
                    }
                }));
        mainRecyclerViewAdapter.setOnItemClickListener(
            new MainRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    DraftInfo draftInfo = mDraftList.get(position);
                    // just use the name AudioFormatActivity, in fact will not launch thi activity
                    intentToActivity = new Intent(mContext, AudioFormatActivity.class);
                    intentToActivity.putExtra(
                        Constant.EXTRA_AUDIO_CLIP_SHOW_TYPE, Constant.AUDIO_CLIP_SHOW_TYPE_CLIP_FROM_DRAFT);
                    intentToActivity.putExtra(DRAFT_ID, draftInfo.getDraftId());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(PERMISSIONS, PERMISSION_REQUESTS_AUDIO_IMPORT);
                    }
                }

                @Override
                public void onMoreClick(View view, int position) {
                    SmartLog.i(TAG, "onMoreClick: more" + position);
                    showPopWindow(view, position);
                }
            });
    }

    protected void initView() {
        recyclerviewMain = findViewById(R.id.recyclerview_main);
        clipMain = findViewById(R.id.clip_main);
        draftMain = findViewById(R.id.draft_main);
        recyclerviewMain = findViewById(R.id.recyclerview_main);
        mRecycleMenu = findViewById(R.id.recycle_menu);
        pageIndicatorView = findViewById(R.id.indicator);
        menuBeanList.clear();

        MenuBean mainFormat = new MenuBean();
        mainFormat.setName(getResources().getString(R.string.main_format));
        mainFormat.setImg(R.mipmap.icon_home_format);
        mainFormat.setNameId(R.string.main_format);
        menuBeanList.add(mainFormat);

        MenuBean changeSound = new MenuBean();
        changeSound.setName(getResources().getString(R.string.main_change_sound));
        changeSound.setImg(R.mipmap.icon_home_format);
        changeSound.setNameId(R.string.main_change_sound);
        menuBeanList.add(changeSound);

        MenuBean audioExtract = new MenuBean();
        audioExtract.setName(getResources().getString(R.string.main_withdraw));
        audioExtract.setImg(R.mipmap.icon_home_withdraw);
        audioExtract.setNameId(R.string.main_withdraw);
        menuBeanList.add(audioExtract);

        MenuBean fileApiTitle = new MenuBean();
        fileApiTitle.setName(getResources().getString(R.string.file_api_title));
        fileApiTitle.setImg(R.mipmap.icon_home_format);
        fileApiTitle.setNameId(R.string.file_api_title);
        menuBeanList.add(fileApiTitle);

        MenuBean textToAudio = new MenuBean();
        textToAudio.setName(getResources().getString(R.string.text_to_audio));
        textToAudio.setImg(R.mipmap.icon_home_withdraw);
        textToAudio.setNameId(R.string.text_to_audio);
        menuBeanList.add(textToAudio);

        MenuBean materialTag = new MenuBean();
        materialTag.setName(getResources().getString(R.string.material_tag));
        materialTag.setImg(R.mipmap.icon_home_withdraw);
        materialTag.setNameId(R.string.material_tag);
        menuBeanList.add(materialTag);

        MenuBean spaceRender = new MenuBean();
        spaceRender.setName(getResources().getString(R.string.space_render));
        spaceRender.setImg(R.mipmap.icon_home_space_render);
        spaceRender.setNameId(R.string.space_render);
        menuBeanList.add(spaceRender);

        MenuBean audioBase = new MenuBean();
        audioBase.setName(getResources().getString(R.string.title_audio_base));
        audioBase.setImg(R.mipmap.icon_home_format);
        audioBase.setNameId(R.string.title_audio_base);
        menuBeanList.add(audioBase);

        mRecycleMenu.setIndicator(pageIndicatorView);
        mRecycleMenu.setPageSize(1, 3);
        myAdapter = mRecycleMenu.new PageAdapter(menuBeanList, new PageRecyclerView.CallBack() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.home_menu_item, parent, false);
                return new HomeViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((HomeViewHolder) holder).mTvMenu.setText(menuBeanList.get(position).getName());
                ((HomeViewHolder) holder).mImg.setImageDrawable(getResources().getDrawable(menuBeanList.get(position).getImg()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intentToActivity(menuBeanList.get(position).getNameId());
                    }
                });
            }
        });
        mRecycleMenu.setAdapter(myAdapter);
    }

    private void intentToActivity(int position) {
        switch (position) {
            case R.string.main_format:
                currentPermissionType = PERMISSION_TYPE_FORMAT;
                intentToActivity = new Intent(mContext, AudioFormatActivity.class);
                requestPermission();
                break;
            case R.string.main_change_sound:
                currentPermissionType = PERMISSION_TYPE_FLOW;
                intentToActivity = new Intent(mContext, StreamApiActivity.class);
                requestPermission();
                break;
            case R.string.main_withdraw:
                currentPermissionType = PERMISSION_TYPE_EXTRACT;
                intentToActivity = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intentToActivity.addCategory(Intent.CATEGORY_OPENABLE);
                intentToActivity.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intentToActivity.setType("video/*");
                requestPermission();
                break;
            case R.string.file_api_title:
                currentPermissionType = PERMISSION_TYPE_FILE;
                intentToActivity = new Intent(mContext, FileApiActivity.class);
                requestPermission();
                break;
            case R.string.text_to_audio:
                currentPermissionType = PERMISSION_TYPE_TEXT_TO_AUDIO;
                intentToActivity = new Intent(mContext, AiDubbingAudioActivity.class);
                requestPermission();
                break;
            case R.string.material_tag:
                currentPermissionType = PERMISSION_MATERIAL;
                intentToActivity = new Intent(mContext, MaterialsActivity.class);
                requestPermission();
                break;
            case R.string.space_render:
                currentPermissionType = PERMISSION_TYPE_SPACE;
                intentToActivity = new Intent(mContext, SpaceRenderActivity.class);
                requestPermission();
                break;
            case R.string.title_audio_base:
                currentPermissionType = PERMISSION_BASE;
                intentToActivity = new Intent(mContext, AudioBaseActivity.class);
                requestPermission();
                break;
            default:
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, PERMISSION_REQUESTS_AUDIO_IMPORT);
        } else {
            toActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    /**
     * Display Go to App Settings Dialog
     */
    private void showToAppSettingDialog() {
        new AlertDialog.Builder(this)
            .setMessage("")
            .setPositiveButton(
                getString(R.string.setting), (dialog, which) -> PermissionUtils.toAppSetting(mContext))
            .setNegativeButton(getString(R.string.cancels), null)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUESTS) {
            PermissionUtils.onRequestMorePermissionsResult(mContext, PERMISSIONS,
                new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {
                        /* *  获取读权限之后跳转到音频导入  * */
                        if (requestCode == PERMISSION_REQUESTS_AUDIO_IMPORT) {
                            toActivity();
                        } else if (requestCode == PERMISSION_REQUESTS_MY_FILES) { // 跳转到我的文件页面
                            startActivity(intentToActivity);
                        }
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        showToAppSettingDialog();
                    }
                });
        }
    }

    /**
     * Extracting audio files from videos through video file paths
     *
     * @param path Full path of the video file
     */
    private void beginExtractAudio(String path) {
        fragmentDialog = ProgressDialog.newInstance("Extracting");
        fragmentDialog.show(getSupportFragmentManager(), "ProgressDialogFragment");
        String outPutDir = FileUtil.getAudioExtractStorageDirectory(this);
        String name = "audio_extract";
        if (path != null) {
            int slashIndex = path.lastIndexOf("/");
            if (slashIndex == -1) {
                name = path;
            } else {
                name = path.substring(slashIndex + 1);
            }
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex >= 0) {
                name = name.substring(0, dotIndex);
            }
        }
        realExtractAudio(path, outPutDir, name);
    }

    private void realExtractAudio(String path, String outPutDir, String name) {
        // Start fetching audio
        HAEAudioExpansion.getInstance().extractAudio(this, path, outPutDir, name,
            new AudioExtractCallBack() {
                @Override
                public void onSuccess(String audioPath) {
                    SmartLog.d(TAG, "ExtractAudio onSuccess : " + audioPath);
                    runOnUiThread(() -> {
                        fragmentDialog.dismiss();
                        String format = getResources().getString(R.string.extract_success);
                        Toast.makeText(
                            MainActivity.this,
                            String.format(format, audioPath),
                            Toast.LENGTH_LONG)
                            .show();
                    });
                }

                @Override
                public void onProgress(int progress) {
                    SmartLog.d(TAG, "ExtractAudio onProgress : " + progress);
                    runOnUiThread(() -> fragmentDialog.setProgress(progress));
                }

                @Override
                public void onFail(int errCode) {
                    SmartLog.i(TAG, "ExtractAudio onFail : " + errCode);
                    runOnUiThread(() -> {
                        fragmentDialog.dismiss();
                        if (errCode == HAEErrorCode.FAIL_FILE_EXIST) {
                            Toast.makeText(
                                MainActivity.this,
                                getResources().getString(R.string.file_exists),
                                Toast.LENGTH_LONG)
                                .show();
                            EditDialogFragment.newInstance(
                                "",
                                name,
                                (newName, dialog) -> {
                                    realExtractAudio(path, outPutDir, newName);
                                    dialog.dismiss();
                                })
                                .show(getSupportFragmentManager(), "EditDialogFragment");
                        } else {
                            Toast.makeText(
                                MainActivity.this,
                                getResources().getString(R.string.extract_fail)
                                    + " , errCode : "
                                    + errCode,
                                Toast.LENGTH_LONG)
                                .show();
                        }
                    });
                }

                @Override
                public void onCancel() {
                    SmartLog.d(TAG, "ExtractAudio onCancel.");
                    runOnUiThread(() -> {
                        Toast.makeText(
                            MainActivity.this,
                            getResources().getString(R.string.dm_extract_cancel),
                            Toast.LENGTH_LONG)
                            .show();
                        fragmentDialog.dismiss();
                    });
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FOR_SELECT_VIDEO) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getResources().getString(R.string.select_none_video), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    String filePath = FileUtils.getRealPath(this, uri);
                    if (!TextUtils.isEmpty(filePath)) {
                        beginExtractAudio(filePath);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.file_not_avable), Toast.LENGTH_SHORT)
                            .show();
                    }
                }
            }
        }
    }

    private void toActivity() {
        if (intentToActivity.getIntExtra(Constant.EXTRA_AUDIO_CLIP_SHOW_TYPE, -1) == Constant.AUDIO_CLIP_SHOW_TYPE_SELECT
            || intentToActivity.getIntExtra(Constant.EXTRA_AUDIO_CLIP_SHOW_TYPE, -1) == Constant.AUDIO_CLIP_SHOW_TYPE_CLIP_FROM_DRAFT) {
            AudioEditorLaunchOption option = new AudioEditorLaunchOption.Builder()
                .setDraftId(intentToActivity.getStringExtra(DRAFT_ID))
                .setDraftMode(AudioEditorLaunchOption.DraftMode.SAVE_DRAFT)
                .build();
            try {
                HAEUIManager.getInstance().launchEditorActivity(mContext, option, new LaunchCallback() {
                    @Override
                    public void onFailed(int errCode, String errMsg) {
                        Toast.makeText(mContext, errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException exception) {
                SmartLog.e(TAG, "got exception: " + exception.getMessage());
            }
        } else {
            if (currentPermissionType == PERMISSION_TYPE_EXTRACT) {
                startActivityForResult(intentToActivity, REQUEST_CODE_FOR_SELECT_VIDEO);
            } else {
                startActivity(intentToActivity);
            }
        }
    }

    private void showPopWindow(View view, int pos) {
        int width;
        int height;
        if (homeFragmentPopWindow == null) {
            homeFragmentPopWindow = new HomeFragmentPopWindow(this);
            width = homeFragmentPopWindow.getPopWidth();
            height = homeFragmentPopWindow.getPopHeight();
            SmartLog.i(TAG, "showActionPopWindow1: width==" + width + "   height==" + height);
        } else {
            width = homeFragmentPopWindow.getContentView().getWidth();
            height = homeFragmentPopWindow.getContentView().getHeight();
            SmartLog.i(TAG, "showActionPopWindow2: width==" + width + "   height==" + height);
        }

        homeFragmentPopWindow.setOnActionClickListener(
            new HomeFragmentPopWindow.ActionOnClickListener() {
                @Override
                public void onRenameClick() {
                    DraftInfo project = mDraftList.get(pos);
                    MediaData wrapper = new MediaData();
                    wrapper.setName(project.getDraftName());
                    String title = getResources()
                        .getString(com.huawei.hms.audioeditor.ui.R.string.draft_rename_title);
                    RenameDialogFragment fragment = RenameDialogFragment.newInstance(
                        title,
                        wrapper,
                        (newName, dialog) -> {
                            HAEUIManager.getInstance().updateProjectName(project.getDraftId(), newName, new DraftCallback() {
                                @Override
                                public void onFailed(Object object) {
                                    FailContent failContent = (FailContent) object;
                                    Toast.makeText(mContext, failContent.errList.get(0).getErrMsg(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            refresh();
                            dialog.dismiss();
                        });
                    fragment.show(getSupportFragmentManager(), "rename");
                }

                @Override
                public void onCopyClick() {
                    DraftInfo project = mDraftList.get(pos);
                    String newProjectId =
                        HAEUIManager.getInstance()
                            .copyProjectById(project.getDraftId(), "", new DraftCallback() {
                                @Override
                                public void onFailed(Object object) {
                                    FailContent failContent = (FailContent) object;
                                    Toast.makeText(mContext, failContent.errList.get(0).getErrMsg(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    boolean result = !TextUtils.isEmpty(newProjectId);
                    if (result) {
                        refresh();
                    }
                }

                @Override
                public void onDeleteClick() {
                    List<String> tobeDeleteProject = new ArrayList<>();
                    tobeDeleteProject.add(mDraftList.get(pos).getDraftId());
                    int deleteNum = HAEUIManager.getInstance().deleteDrafts(tobeDeleteProject, new DraftCallback() {
                        @Override
                        public void onFailed(Object object) {
                            FailContent failContent = (FailContent) object;
                            int num = failContent.errList.size();
                            for (int i = 0; i < num; i++) {
                                Toast.makeText(mContext, failContent.errList.get(i).getErrMsg(), Toast.LENGTH_SHORT).show();
                                SmartLog.e(TAG, "the draft delete failed, id is:" + failContent.errList.get(i).getDraftId());
                                SmartLog.e(TAG, "errcode is:" + failContent.errList.get(i).getErrCode() + "  , errMsg is: " + failContent.errList.get(i).getErrMsg());
                            }
                        }
                    });
                    SmartLog.i(TAG, "delete draft number is:" + deleteNum);
                    mDraftList.remove(pos);
                    refresh();
                }
            });
        int off = 40;
        homeFragmentPopWindow.showAsDropDown(view, -width + view.getWidth(), -height - view.getHeight() - off);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void refresh() {
        mainViewModel.initDraftProjects();
        mainRecyclerViewAdapter.notifyDataSetChanged();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTvMenu;
        private final ImageView mImg;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvMenu = itemView.findViewById(R.id.tv_menu);
            mImg = itemView.findViewById(R.id.img);
        }
    }
}
