# 华为音频编辑服务示例代码
中文 | [English](README.md)

## 目录

 * [介绍](#介绍)
 * [工程目录结构](#工程目录结构)
 * [运行步骤](#运行步骤)
 * [支持的环境](#支持的环境)
 * [许可证](#许可证)


## 介绍
华为音频编辑服务是快速构建音频编辑能力的服务，只需要通过简单的集成方法即可让应用获取音频编辑能力。提供导入音频、编辑音频、导出音频、音频提取、格式转换等一站式音频能力。为开发者提供性能优异、简单易用、开放性强的接口，帮助开发者在App中轻松高效地构建音频功能。本示例代码介绍Audio Editor Kit SDK的使用方法。

- 支持多音频导入，能够生成并预览单音频或多音频的波形。
- 支持音频编辑基本操作，例如调整音量、调整音速音调、复制和删除等。
- 支持对音频添加特效功能，可新增多个特效，例如音乐风格、声场、均衡器、淡入淡出、变声、添加音效、环境效果、空间方位渲染等。
- 支持录制和导入音频功能。
- 支持伴奏提取功能。
- 支持降噪功能，对双麦录制导入的音频中的稳态噪声或者突发噪声进行处理，并对语音进行修复和增强，提升语音信号质量。
- 支持音频提取功能，从MP4等格式的视频中提取出音频进行编辑。
- 支持格式转换功能，支持音频转换为MP3、WAV、FLAC的格式。
- 支持AI配音功能：将文本转换成不同音色的语音。
- 支持歌声合成功能：根据输入歌词生成优美歌曲（仅支持中国区调用）。

## 工程目录结构

```
|-- apk
    |-- app
        |-- com.huawei.hms.audioeditor.demo
            |-- MainActivity // Audio Editor UI SDK 入口,格式转换入口,音频提取入口
            |-- AudioFormatActivity  // Demo格式转换
            |-- AudioFilePickerActivity  // Demo选择音频文件、列表
            |-- FileApiActivity  // demo 各功能文件接口
            |-- StreamApiActivity  // demo 各功能流式接口
            |-- AudioBaseActivity  // demo 基础功能集成
            |-- AiDubbingAudioActivity  // demo AI配音集成
            |-- SongSynthesisActivity  // demo 歌声合成功能集成
    |-- base_audioeditor
        |-- com.huawei.hms.audioeditor.demo
            |-- AudioBaseActivity  // demo 基础功能集成
    |-- aidubbing_audioeditor
        |-- com.huawei.hms.audioeditor.demo
            |-- AiDubbingAudioActivity  // demo AI配音集成
```

## 运行步骤
 - 将本代码库克隆到本地。

 - 如果您还没有注册成为开发者，请在[AppGalleryConnect上注册并创建应用](https://developer.huawei.com/consumer/cn/service/josp/agc/index.html)。
 - 替换工程中的sample-agconnect-services.json文件。
 - 编译并且在安卓设备上运行。

## 要求环境
推荐使用的AndroidSDK版本为24及以上，JDK版本为1.8及以上。

##  技术支持

如果您对HMS Core还处于评估阶段，可在[Reddit社区](https://www.reddit.com/r/HuaweiDevelopers/)获取关于HMS Core的最新讯息，并与其他开发者交流见解。

如果您对使用HMS示例代码有疑问，请尝试：

- 开发过程遇到问题上[Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services?tab=Votes)，在\[huawei-mobile-services]标签下提问，有华为研发专家在线一对一解决您的问题。
- 到[华为开发者论坛](https://developer.huawei.com/consumer/cn/forum/blockdisplay?fid=18) HMS Core板块与其他开发者进行交流。

如果您在尝试示例代码中遇到问题，请向仓库提交[issue](https://github.com/HMS-Core/hms-audio-editor-demo/issues)，也欢迎您提交[Pull Request](https://github.com/HMS-Core/hms-audio-editor-demo/pulls)。

注意：

该项目中的package name不能用于申请agconnect-services.json，您可以使用自定义package name来申请agconnect-services.json。
您只需将应用级build.gradle中的applicationId修改为与所申请的agconnect-services.json相同的package name，即可体验Audio Editor Kit云侧服务。

##  许可证

此示例代码已获得[Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0)。

