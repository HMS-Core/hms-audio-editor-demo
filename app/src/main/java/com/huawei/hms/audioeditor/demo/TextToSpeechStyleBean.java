/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import com.huawei.hms.audioeditor.sdk.engine.dubbing.HAEAiDubbingSpeaker;

/**
 * 内容：文字转语音
 *
 * @author Zwx1040641
 * @date 2021/7/7
 * @since 2021/7/7
 */
public class TextToSpeechStyleBean {
    private HAEAiDubbingSpeaker speaker;
    private boolean isSelect;

    public TextToSpeechStyleBean(HAEAiDubbingSpeaker name, boolean isSelect) {
        this.speaker = name;
        this.isSelect = isSelect;
    }

    public HAEAiDubbingSpeaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(HAEAiDubbingSpeaker speaker) {
        this.speaker = speaker;
    }

    public boolean isChecked() {
        return isSelect;
    }

    public void setChecked(boolean select) {
        isSelect = select;
    }
}
