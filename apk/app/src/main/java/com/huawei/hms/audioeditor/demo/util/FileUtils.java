/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.huawei.hms.audioeditor.sdk.util.SmartLog;

/**
 * Tool class for obtaining the file path
 *
 * @since 2021-05-10
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    public static String getRealPath(Context context, Uri fileUri) {
        String realPath;
        if (Build.VERSION.SDK_INT < 19) {
            realPath = FileUtils.getRealPathFromURI_BelowAPI19(context, fileUri);
        } else {
            realPath = FileUtils.getRealPathFromURI_API19(context, fileUri);
        }
        return realPath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_BelowAPI19(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(columnIndex);
            cursor.close();
        }
        return result;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            } else if (isDownloadsDocument(uri)) {
                String fileName = getFilePath(context, uri);
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                }

                String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:", "");
                    File file = new File(id);
                    if (file.exists()) {
                        return id;
                    }
                }

                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    public static String getFilePath(Context context, Uri uri) {

        Cursor cursor = null;
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Check is external document storage.
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Check is download document storage.
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Check is dedia document storage.
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Writes data stream to the file.
     * @param buffer Cache to be written
     * @param strFilePath Destination Save File Address
     * @param append Indicates whether to write in append mode.
     */
    public static void writeBufferToFile(byte[] buffer, String strFilePath, boolean append) {
        File file = new File(strFilePath);
        RandomAccessFile randomAccessFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (append) {
                // Appending write mode.
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(file.length());
                randomAccessFile.write(buffer);
            } else {
                // Overwrite mode.
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(buffer);
                fileOutputStream.flush();
            }
        } catch (IOException e) {
            SmartLog.e(TAG, e.getMessage());
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                SmartLog.e("Failed to close stream.", e.getMessage());
            }
        }
    }

    /**
     * Create a directory to store the voice files generated by the AiDubbing.
     * @param context context
     * @return file path
     */
    public static String initFile(Context context) {
        String filePath = context.getExternalFilesDir("wav").getPath();
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                SmartLog.i(TAG, "mkdirs failed");
            }
            SmartLog.i("initFile", "Create a directory to store the voice files generated by the AIDubbing.");
        }
        return filePath;
    }

    /**
     * Delete a file.
     * @param filePath File path
     */
    public static void deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        deleteFile(new File(filePath));
    }

    /**
     * Delete all contents of the folder
     * @param file File
     */
    public static void deleteFile(File file) {
        if (file != null && file.exists()) { // Check whether the file exists.
            if (file.isDirectory()) { // Otherwise if it's a directory
                File[] files = file.listFiles(); // Declares all files in the directory. files[];
                if (files != null) {
                    for (File childFile : files) { // Traverse all files in the directory.
                        deleteFile(childFile); // Use this method to iterate each file.
                    }
                }
            }

            // Safely delete files
            deleteFileSafely(file);
        }
    }

    /**
     * Safely delete files.Prevents the system from reporting an error when a file is re-created after being deleted. open failed: EBUSY (Device or resource busy)
     * @param file File
     * @return true  false
     */
    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            boolean renameTo = file.renameTo(tmp);
            if (!renameTo) {
                SmartLog.e(TAG, "deleteFileSafely file.renameTo fail!");
            }
            return tmp.delete();
        }
        return false;
    }
}
