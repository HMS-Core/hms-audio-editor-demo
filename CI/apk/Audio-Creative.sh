#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0);pwd)

TYPE=apk
MODULE=app
RELEASE_DIR=build/outputs/apk/release
DEBUG_DIR=build/outputs/apk/debug

functionBuild(){

  if [[  ${WORKSPACE} == "" ]]; then
    echo "this is in local!"
  else
      echo "this is in remote!"
      export needHwSign=true
      if [[  $releaseVersion == "" ]]; then
          echo "this is snapshot version!"
      else
          echo "this is release version!"
          export useRemoteImplementation=true
          source ${SCRIPT_DIR}/../getVersionCode.sh
      fi
	#流水线上local.properties里面配置的cmake不对，需要自己修改。
	echo "get the workspace!"
	ls ${WORKSPACE}
	cd ${WORKSPACE}/HMSCoreAudioEditorSDKDemo/audioEditKit_example/
	rm -rf local.properties
	touch local.properties
	chmod 777 local.properties
	echo "cmake.dir=/opt/buildtools/android-sdk-linux/cmake/3.16.5" > local.properties
  fi

  cd ${SCRIPT_DIR}/../../${MODULE}
  gradle assembleRelease
  gradle assembleDebug
}

functionCopyApk(){

    cd ${SCRIPT_DIR}/../../${MODULE}
    cd ${RELEASE_DIR}

    if [[  $releaseVersion == "" ]]; then
      timestap=$(date "+%Y%m%d%H%M%S")
      PACKAGE_PRODUCT=AudioCreative-product-debug-Demo-$timestap
      PACKAGE_MIRROR=AudioCreative-mirror-debug-Demo-$timestap
	    PACKAGE_NOGARD=AudioCreative-nogard-debug-Demo-$timestap
	    PACKAGE_STAGING=AudioCreative-staging-debug-Demo-$timestap
    else
      PACKAGE_PRODUCT=AudioCreative-product-release-Demo-${releaseVersion}
      PACKAGE_MIRROR=AudioCreative-mirror-release-Demo-${releaseVersion}
	    PACKAGE_NOGARD=AudioCreative-nogard-release-Demo-${releaseVersion}
	    PACKAGE_STAGING=AudioCreative-staging-release-Demo-${releaseVersion}
    fi

    PKNAME=`ls | grep AudioCreative.apk`
    cp ${PKNAME} ${PACKAGE_PRODUCT}.apk
    cp ${PKNAME} ${PACKAGE_NOGARD}.apk
    cp ${PKNAME} ${PACKAGE_STAGING}.apk


    cd ${SCRIPT_DIR}/../../${MODULE}
    cd ${DEBUG_DIR}
    PKNAMEDEBUG=`ls | grep AudioCreative.apk`
    cp ${PKNAMEDEBUG} ../release/${PACKAGE_MIRROR}.apk

}

functionBuild || true
functionCopyApk
bash ${WORKSPACE}/Script/clouddragon/build2.0/service/getPackageInfo.sh "HMSCoreAudioEditorSDKDemo/audioEditKit_example/app/build/outputs/apk/release" "AudioCreative-*.apk" "" "" ""
