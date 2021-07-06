releaseVersionCode=
VERSIONARR=(${releaseVersion//./ })

if [ ${#VERSIONARR[1]} == 1 ]; then
    SECOND=0${VERSIONARR[1]}
else
    SECOND=${VERSIONARR[1]}
fi

if [ ${#VERSIONARR[2]} == 1 ]; then
    THRID=0${VERSIONARR[2]}
else
    THRID=${VERSIONARR[2]}
fi

releaseVersionCode=${VERSIONARR[0]}${SECOND}${THRID}${VERSIONARR[3]}
echo $releaseVersionCode
export ml_sdk_versionCode=$releaseVersionCode
export ml_sdk_versionName=$releaseVersion
