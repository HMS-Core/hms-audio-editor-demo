/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.audioeditor.sdk.HAEMaterialsManageFile;
import com.huawei.hms.audioeditor.sdk.materials.network.MaterialsCallBack;
import com.huawei.hms.audioeditor.sdk.materials.network.MaterialsDownloadCallBack;
import com.huawei.hms.audioeditor.sdk.materials.bean.MaterialMenu;
import com.huawei.hms.audioeditor.sdk.materials.bean.MaterialsCutColumn;
import com.huawei.hms.audioeditor.sdk.materials.bean.MaterialsCutContent;

import com.google.android.material.tabs.TabLayout;

import com.huawei.hms.audioeditor.sdk.util.SmartLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MaterialsActivity extends AppCompatActivity
    implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private TextView mBack;
    private RecyclerView mRecyclerView;
    private TabLayout tabLayout;
    private TabLayout tabLayoutFather;
    private TabLayout tabLayoutFatherSecond;
    private ProgressBar indicator;
    private MediaPlayer mMediaPlayer;
    private List<MaterialsCutContent> mList;
    private List<MaterialsCutColumn> mColumnsList;
    private MaterialAdapter mMaterialAdapter;
    private String fatherId;

    HAEMaterialsManageFile materialsManageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materials);
        initView();
        initData();
        initEvent();
    }

    private void initEvent() {
        mBack.setOnClickListener(v -> finish());
        mMaterialAdapter.setOnItemClickListener(
            new MaterialAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, int dataPosition) {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        } else {
                            playAudio(mList.get(dataPosition).getLocalPath());
                        }
                    }
                }

                @Override
                public void onDownloadClick(int position, final int dataPosition) {
                    MaterialsCutContent content = mList.get(dataPosition);
                    // The download URL is dynamically obtained based on the material ID.
                    materialsManageFile.getDownLoadUrlById(
                        content.getContentId(),
                        new MaterialsCallBack<String>() {
                            @Override
                            public void onFinish(String downloadUrl) {
                                String saveDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
                                String saveName = "material-" + System.currentTimeMillis();
                                materialsManageFile.downloadResource(
                                    downloadUrl,
                                    saveDirectory,
                                    saveName,
                                    new MaterialsDownloadCallBack() {
                                        @Override
                                        public void onDownloadSuccess(File file) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(
                                                    MaterialsActivity.this,
                                                    "DownloadSuccess: "
                                                        + file.getPath(),
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                                content.setStatus(
                                                    MaterialsCutContent.DOWNLOADED);
                                                content.setLocalPath(file.getPath());
                                                mMaterialAdapter.notifyDataSetChanged();
                                            });
                                        }

                                        @Override
                                        public void onDownloading(int progress) {
                                        }

                                        @Override
                                        public void onDownloadFailed(int errorCode) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(
                                                    MaterialsActivity.this,
                                                    "errorCode: " + errorCode,
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                                content.setStatus(
                                                    MaterialsCutContent.UNDOWNLOAD);
                                                mMaterialAdapter.notifyDataSetChanged();
                                            });
                                        }
                                    });
                            }

                            @Override
                            public void onError(int errorCode) {
                                runOnUiThread(() -> {
                                    Toast.makeText(
                                        MaterialsActivity.this,
                                        "DownloadFail" + errorCode,
                                        Toast.LENGTH_SHORT)
                                        .show();
                                    content.setStatus(MaterialsCutContent.UNDOWNLOAD);
                                    mMaterialAdapter.notifyDataSetChanged();
                                });
                            }
                        });

                }

                @Override
                public void onPlayClick(int position, int dataPosition) {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        } else {
                            playAudio(mList.get(dataPosition).getLocalPath());
                        }
                    }
                }
            });

        tabLayoutFather.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // Obtaining Material Columns
                    fatherId = (String) tab.getTag();
                    getColumns(fatherId);
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

        tabLayoutFatherSecond.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // Obtaining a Subcategory
                    initBottomTab(mColumnsList, fatherId);
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

        tabLayout.addOnTabSelectedListener(
            new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // Obtaining the Material List
                    getMaterials(fatherId, (String) tab.getTag());

                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
    }

    private void initView() {
        mBack = findViewById(R.id.back);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayoutFather = (TabLayout) findViewById(R.id.tab_layout_father);
        tabLayoutFatherSecond = (TabLayout) findViewById(R.id.tab_layout_father_second);
        mRecyclerView = findViewById(R.id.pager_recycler_view);
        indicator = findViewById(R.id.indicator);
    }

    private void initData() {
        materialsManageFile = new HAEMaterialsManageFile();
        initMediaPlayer();

        mList = new ArrayList<>();
        mMaterialAdapter = new MaterialAdapter(this, mList, R.layout.adapter_material_item);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mMaterialAdapter);

        // Obtain the material type.
        materialsManageFile.getFatherId(new MaterialsCallBack<List<MaterialMenu>>() {
            @Override
            public void onFinish(List<MaterialMenu> list) {

                for (int i = 0; i < list.size(); i++) {
                    TabLayout.Tab tab = tabLayoutFather.newTab();
                    tab.setText(list.get(i).getMenuName());
                    tab.setTag(list.get(i).getMenuId());
                    tabLayoutFather.addTab(tab);
                }

                // Currently, there are two categories: 0 for sound effects and 1 for music segments.
                fatherId = list.get(0).getMenuId();
                getColumns(fatherId);
            }

            @Override
            public void onError(int errorCode) {
                indicator.setVisibility(View.GONE);
                Toast.makeText(MaterialsActivity.this, "errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getColumns(String fatherId) {
        // Obtains the material column list.
        materialsManageFile.getColumnsByFatherColumnId(
            fatherId,
            new MaterialsCallBack<List<MaterialsCutColumn>>() {
                @Override
                public void onFinish(List<MaterialsCutColumn> response) {
                    tabLayout.removeAllTabs();
                    tabLayoutFatherSecond.removeAllTabs();
                    mColumnsList = response;
                    for (int i = 0; i < response.size(); i++) {
                        // Segments are divided into instruments, style types.
                        TabLayout.Tab tab = tabLayoutFatherSecond.newTab();
                        tab.setText(response.get(i).getColumnName());
                        tab.setTag(response.get(i).getColumnId());
                        tabLayoutFatherSecond.addTab(tab);
                    }
                    initBottomTab(response, fatherId);
                }

                @Override
                public void onError(int errorCode) {
                    indicator.setVisibility(View.GONE);
                    Toast.makeText(MaterialsActivity.this, "errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initBottomTab(List<MaterialsCutColumn> response, String fatherId) {
        int fatherSecondIndex = tabLayoutFatherSecond.getSelectedTabPosition();
        tabLayout.removeAllTabs();
        // Musical instrument or style, material classification under type
        if (response.get(fatherSecondIndex).getChildren() != null) {
            tabLayout.setVisibility(View.VISIBLE);
            for (int j = 0; j < response.get(fatherSecondIndex).getChildren().size(); j++) {
                TabLayout.Tab tabChild = tabLayout.newTab();
                tabChild.setText(response.get(fatherSecondIndex).getChildren().get(j).getColumnName());
                tabChild.setTag(response.get(fatherSecondIndex).getChildren().get(j).getColumnId());
                tabLayout.addTab(tabChild);
            }
            getMaterials(fatherId, response.get(fatherSecondIndex).getChildren().get(0).getColumnId());
        } else {
            tabLayout.setVisibility(View.GONE);
            getMaterials(fatherId, response.get(fatherSecondIndex).getColumnId());
        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        }
    }

    private void playAudio(String path) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepareAsync();
            }
        } catch (RuntimeException e) {
            SmartLog.e("material", "prepare fail RuntimeException");
        } catch (Exception e) {
            SmartLog.e("material", "prepare fail RuntimeException");
        }
    }

    private void getMaterials(String fatherId, String columnId) {
        indicator.setVisibility(View.VISIBLE);
        materialsManageFile.getMaterialsByColumnId(fatherId,
            columnId,
            0,
            20,
            new MaterialsCallBack<List<MaterialsCutContent>>() {
                @Override
                public void onFinish(List<MaterialsCutContent> response) {
                    mList.clear();
                    mList.addAll(response);
                    mMaterialAdapter.notifyDataSetChanged();
                    indicator.setVisibility(View.GONE);
                }

                @Override
                public void onError(int errorCode) {
                    Toast.makeText(MaterialsActivity.this, "errorCode: " + errorCode, Toast.LENGTH_SHORT).show();
                    indicator.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }
}
