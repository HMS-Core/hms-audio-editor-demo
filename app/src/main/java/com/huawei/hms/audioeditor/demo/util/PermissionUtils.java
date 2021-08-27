/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    /**
     * Check Permissions
     * @param context  context
     * @param permission permission
     * @return true：authorized； false：unauthorized；
     */
    public static boolean checkPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Detect Multiple Permissions
     * @param context context
     * @param permissions permissions
     * @return Unauthorized Permission
     */
    public static List<String> checkMorePermissions(Context context, String[] permissions) {
        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (!checkPermission(context, permissions[i])) {
                permissionList.add(permissions[i]);
            }
        }
        return permissionList;
    }

    /**
     * Request Permissions
     * @param context context
     * @param permission permission
     * @param requestCode requestCode
     */
    public static void requestPermission(Context context, String permission, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, new String[] {permission}, requestCode);
    }

    /**
     * Request Multiple Permissions
     * @param context context
     * @param permissionList permissionList
     * @param requestCode requestCode
     */
    public static void requestMorePermissions(Context context, List permissionList, int requestCode) {
        String[] permissions = (String[]) permissionList.toArray(new String[0]);
        requestMorePermissions(context, permissions, requestCode);
    }

    /**
     * Request Multiple Permissions
     * @param context context
     * @param permissions permissions
     * @param requestCode requestCode
     */
    public static void requestMorePermissions(Context context, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
    }

    /**
     * Determine whether the permission has been denied.
     * @param context context
     * @param permission permission
     * @return Returns the status of the permission
     */
    public static boolean judgePermission(Context context, String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Detect permission and request permission: If no permission, request permission
     * @param context context
     * @param permission permission
     * @param requestCode requestCode
     */
    public static void checkAndRequestPermission(Context context, String permission, int requestCode) {
        if (!checkPermission(context, permission)) {
            requestPermission(context, permission, requestCode);
        }
    }

    /**
     * Detect and request multiple permissions
     * @param context context
     * @param permissions permissions
     * @param requestCode requestCode
     */
    public static void checkAndRequestMorePermissions(Context context, String[] permissions, int requestCode) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        requestMorePermissions(context, permissionList, requestCode);
    }

    /**
     * Check Permissions
     * @param context    context
     * @param permission Permission
     * @param callBack   Callback Listening
     */
    public static void checkPermission(Context context, String permission, PermissionCheckCallBack callBack) {
        if (checkPermission(context, permission)) { // 用户已授予权限
            callBack.onHasPermission();
        } else {
            if (judgePermission(context, permission)) {
                // 用户之前已拒绝过权限申请
                callBack.onUserHasAlreadyTurnedDown(permission);
            } else {
                // 用户之前已拒绝并勾选了不在询问、用户第一次申请权限。
                callBack.onUserHasAlreadyTurnedDownAndDontAsk(permission);
            }
        }
    }

    /**
     * Detect Multiple Permissions
     *
     * @param context     context
     * @param permissions Permission
     * @param callBack    Callback Listening
     */
    public static void checkMorePermissions(Context context, String[] permissions, PermissionCheckCallBack callBack) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) { // User Granted Permissions
            callBack.onHasPermission();
        } else {
            boolean isFirst = true;
            for (int i = 0; i < permissionList.size(); i++) {
                String permission = permissionList.get(i);
                if (judgePermission(context, permission)) {
                    isFirst = false;
                    break;
                }
            }
            String[] unauthorizedMorePermissions = (String[]) permissionList.toArray(new String[0]);
            if (isFirst) {
                // The user has rejected the permission application before.
                callBack.onUserHasAlreadyTurnedDownAndDontAsk(unauthorizedMorePermissions);
            } else {
                // The user has previously rejected and selected Do not ask, and the user applies for permission for the
                // first time.
                callBack.onUserHasAlreadyTurnedDown(unauthorizedMorePermissions);
            }
        }
    }

    /**
     * Detect and Apply for Permissions
     * @param context context
     * @param permission permission
     * @param requestCode requestCode
     * @param callBack callBack
     */
    public static void checkAndRequestPermission(
            Context context, String permission, int requestCode, PermissionRequestSuccessCallBack callBack) {
        if (checkPermission(context, permission)) { // User Granted Permissions
            callBack.onHasPermission();
        } else {
            requestPermission(context, permission, requestCode);
        }
    }

    /**
     * Detect and apply for multiple permissions
     * @param context context
     * @param permissions permissions
     * @param requestCode requestCode
     * @param callBack callBack
     */
    public static void checkAndRequestMorePermissions(
            Context context, String[] permissions, int requestCode, PermissionRequestSuccessCallBack callBack) {
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) { // User Granted Permissions
            callBack.onHasPermission();
        } else {
            requestMorePermissions(context, permissionList, requestCode);
        }
    }

    /**
     * Check whether the permission is successfully applied for.
     * @param grantResults grantResults
     * @return true:success false:false
     */
    public static boolean isPermissionRequestSuccess(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The user applies for permission and returns the request.
     * @param context context
     * @param permission permission
     * @param grantResults grantResults
     * @param callback callback
     */
    public static void onRequestPermissionResult(
            Context context, String permission, int[] grantResults, PermissionCheckCallBack callback) {
        if (PermissionUtils.isPermissionRequestSuccess(grantResults)) {
            callback.onHasPermission();
        } else {
            if (PermissionUtils.judgePermission(context, permission)) {
                callback.onUserHasAlreadyTurnedDown(permission);
            } else {
                callback.onUserHasAlreadyTurnedDownAndDontAsk(permission);
            }
        }
    }

    /**
     * The user applies for multiple permissions.
     * @param context context
     * @param permissions permissions
     * @param callback callback
     */
    public static void onRequestMorePermissionsResult(
            Context context, String[] permissions, PermissionCheckCallBack callback) {
        boolean isBannedPermission = false;
        List<String> permissionList = checkMorePermissions(context, permissions);
        if (permissionList.size() == 0) {
            callback.onHasPermission();
        } else {
            for (int i = 0; i < permissionList.size(); i++) {
                if (!judgePermission(context, permissionList.get(i))) {
                    isBannedPermission = true;
                    break;
                }
            }
            // 　Re-ask permission disabled
            if (isBannedPermission) {
                callback.onUserHasAlreadyTurnedDownAndDontAsk(permissions);
            } else {
                // Deny Permissions
                callback.onUserHasAlreadyTurnedDown(permissions);
            }
        }
    }

    /**
     * The permission setting page is displayed.
     * @param context context
     */
    public static void toAppSetting(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }

    public interface PermissionRequestSuccessCallBack {
        /**
         * User Granted Permissions
         */
        void onHasPermission();
    }

    public interface PermissionCheckCallBack {
        /**
         * User Granted Permissions
         */
        void onHasPermission();

        /**
         * User Denied Permissions
         *
         * @param permission Denied Permissions
         */
        void onUserHasAlreadyTurnedDown(String... permission);

        /**
         * The user has rejected the request and selected Do not ask again.
         * The user applies for permission for the first time.
         *
         * @param permission Denied Permissions
         */
        void onUserHasAlreadyTurnedDownAndDontAsk(String... permission);
    }
}
