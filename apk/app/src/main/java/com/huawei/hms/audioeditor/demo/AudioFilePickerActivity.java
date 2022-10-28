/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.sdk.HAEConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Select Audio Files, Lists
 *
 * @since 2021-05-10
 */
public class AudioFilePickerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        performFileSearch();
    }

    // Select File
    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Allow multiple selections, press and hold multiple selections
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Restrict the selection of audio types.
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    // Receive the return value.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    List<Uri> uris = new ArrayList<>();
                    // Return to when a single file is selected
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        uris.add(uri);
                    } else {
                        // Multiple Choices
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                uris.add(clipData.getItemAt(i).getUri());
                            }
                        }
                    }
                    handleSelectedAudios(uris);
                }
                finish();
                break;
            default:
                break;
        }
    }

    // Convert the URI to the path we need.
    private void handleSelectedAudios(List<Uri> uriList) {
        if (uriList == null || uriList.size() == 0) {
            return;
        }
        ArrayList<String> audioList = new ArrayList<>();
        for (Uri uri : uriList) {
            String filePath = FileUtils.getRealPath(this, uri);
            audioList.add(filePath);
        }
        Intent intent = new Intent();
        intent.putExtra(HAEConstant.AUDIO_PATH_LIST, audioList);
        this.setResult(HAEConstant.RESULT_CODE, intent);
        finish();
    }
}
