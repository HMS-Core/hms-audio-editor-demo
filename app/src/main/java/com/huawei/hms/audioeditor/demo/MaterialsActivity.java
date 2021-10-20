/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.huawei.hms.audioeditor.demo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
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
import com.huawei.hms.audioeditor.sdk.materials.network.inner.bean.MaterialMenu;
import com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutColumn;
import com.huawei.hms.audioeditor.sdk.materials.network.response.MaterialsCutContent;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;

import com.google.android.material.tabs.TabLayout;

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
                            // 下载url改为根据素材id动态获取
                            materialsManageFile.getDownLoadUrlById(
                                    content.getContentId(),
                                    new MaterialsCallBack<String>() {
                                        @Override
                                        public void onFinish(String downloadUrl) {
                                            String saveDirectory =
                                                    FileUtil.getAudioExtractStorageDirectory(getBaseContext());
                                            String saveName = "material-" + System.currentTimeMillis();
                                            materialsManageFile.downloadResource(
                                                    downloadUrl,
                                                    saveDirectory,
                                                    saveName,
                                                    new MaterialsDownloadCallBack() {
                                                        @Override
                                                        public void onDownloadSuccess(File file) {
                                                            runOnUiThread(
                                                                    () -> {
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
                                                        public void onDownloading(int progress) {}

                                                        @Override
                                                        public void onDownloadFailed(int errorCode) {
                                                            runOnUiThread(
                                                                    () -> {
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
                                            runOnUiThread(
                                                    () -> {
                                                        Toast.makeText(
                                                                        MaterialsActivity.this,
                                                                        "DownloadFail"+errorCode,
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
                        // 获取素材栏目
                        fatherId = (String) tab.getTag();
                        getColumns(fatherId);
                        if (mMediaPlayer != null) {
                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

        tabLayoutFatherSecond.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // 获取子栏目
                        initBottomTab(mColumnsList, fatherId);
                        if (mMediaPlayer != null) {
                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // 获取素材列表
                        getMaterials(fatherId,(String) tab.getTag());

                        if (mMediaPlayer != null) {
                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                            }
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
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

        // 获取素材类型
        materialsManageFile.getFatherId(new MaterialsCallBack<List<MaterialMenu>>() {
            @Override
            public void onFinish(List<MaterialMenu> list) {

                for (int i = 0; i < list.size(); i++) {
                    TabLayout.Tab tab = tabLayoutFather.newTab();
                    tab.setText(list.get(i).getMenuName());
                    tab.setTag(list.get(i).getMenuId());
                    tabLayoutFather.addTab(tab);
                }

                // 目前有两个大类，0是音效，1是乐段
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
        // 获取素材栏目列表
        materialsManageFile.getColumnsByFatherColumnId(
                fatherId,
                new MaterialsCallBack<List<MaterialsCutColumn>>() {
                    @Override
                    public void onFinish(List<MaterialsCutColumn> response) {
                        tabLayout.removeAllTabs();
                        tabLayoutFatherSecond.removeAllTabs();
                        mColumnsList = response;
                        for (int i = 0; i < response.size(); i++) {
                            // 乐段分 乐器，风格类型
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
        // 乐器或者风格，类型下面的素材分类
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
            Log.e("material", "prepare fail RuntimeException");
        } catch (Exception e) {
            Log.e("material", "prepare fail RuntimeException");
        }
    }

    private void getMaterials(String fatherId,String columnId) {
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
    public void onCompletion(MediaPlayer mp) {}

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
