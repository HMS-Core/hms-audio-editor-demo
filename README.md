# Audio Editor Kit Sample
English | [中文](README_ZH.md)

## Table of Contents

* [Introduction](#introduction)
* [Project directory structure](#project-directory-structure)
* [Running Procedure](#running-procedure)
* [Supported environment](#supported-environment)
* [License](#license)


## Introduction
HUAWEI Audio Editor Kit provides a wide range of audio editing capabilities, including audio import/expert/editing/extracting and format conversion. You can easily integrate them into your app as needed through the kit's open, excellent, yet easy-to-use APIs. This sample code is used to describe how to use the Audio Editor Kit SDK.

- Imports audio files in batches, and generates and previews the audio wave for a single audio or multiple audios.
- Supports basic audio editing operations such as changing the volume, adjusting the tempo or pitch, and copying and deleting audio.
- Adds one or more special effects to audio, including the music style, sound field, equalizer, fade-in/out, voice changer, sound effect, scene effect, and spatial audio
- Supports audio recording and importing.
- Separates audio sources for an audio file.
- Reduces the quasi-steady-state noises and sudden loud noises in audio collected from two microphones, and repairs and enhances the human voice to improve the audio quality.
- Extracts audio from video files in formats like MP4.
- Converts audio format to MP3, WAV, or FLAC.


## Project directory structure

```
|-- com.huawei.audioeditor.demo
    |-- Activity
    	|-- MainActivity // Audio Editor UI SDK entry, format conversion entry, and audio extraction entry
	    |-- AudioFormatActivity  // Demo Format conversion
		|-- AudioFilePickerActivity  // Demo Select Audio Files, Lists
		|-- FileApiActivity  // demo Function File Interfaces
		|-- StreamApiActivity  // demo Streaming Interfaces of Each Function
```


## Running Procedure
- Clone the code base to the local host.

- If you haven't already registered as a developer, register and create an app on [AppGalleryConnect](https://developer.huawei.com/consumer/cn/service/josp/agc/index.html).
- Replacing the sample-agconnect-services.json File in the Project
- Compile and run on an Android device or emulator.

## Supported Environment
Android SDK 21 or later and JDK 1.8 or later are recommended.

## Question or issues
If you want to evaluate more about HMS Core,
[r/HMSCore on Reddit](https://www.reddit.com/r/HuaweiDevelopers/) is for you to keep up with latest news about HMS Core, and to exchange insights with other developers.

If you have questions about how to use HMS samples, try the following options:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services?tab=Votes) is the best place for any programming questions. Be sure to tag your question with 
  `huawei-mobile-services`.
- [Huawei Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001) HMS Core Module is great for general questions, or seeking recommendations and opinions.

If you run into a bug in our samples, please submit an [issue](https://github.com/HMS-Core/hms-audio-editor-demo/issues) to the Repository. Even better you can submit a [Pull Request](https://github.com/HMS-Core/hms-audio-editor-demo/pulls) with a fix.

Note：

The package name in this project cannot be used to apply for agconnect-services.json. You can use a customized package name to apply for agconnect-services.json.
You only need to change applicationId in application-level build.gradle to the same package name as the applied agconnect-services.json to experience the Audio Editor Kit cloud-side service.

## License

This sample code has obtained [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
