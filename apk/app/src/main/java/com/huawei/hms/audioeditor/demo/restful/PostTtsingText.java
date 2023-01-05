/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.restful;

import java.io.Serializable;

public class PostTtsingText implements Serializable {
    private DataBean data;
    private ConfigBean config;

    public static class DataBean implements Serializable {

        private String lyric;
        private String language;
        public void setLyric(String lyric) {
            this.lyric = lyric;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    public static class ConfigBean implements Serializable {

        private int type;
        private int outputEncoderFormat;
        private String wordDurationForceAlign = "false";

        public void setWordDurationForceAlign(String wordDurationForceAlign) {
            this.wordDurationForceAlign = wordDurationForceAlign;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setOutputEncoderFormat(int outputEncoderFormat) {
            this.outputEncoderFormat = outputEncoderFormat;
        }
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public void setConfig(ConfigBean config) {
        this.config = config;
    }
}
