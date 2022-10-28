/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import com.huawei.hms.audioeditor.sdk.engine.dubbing.HAEAiDubbingSpeaker;

/**
 * Content: text-to-speech
 * @since 2021/8/10
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
