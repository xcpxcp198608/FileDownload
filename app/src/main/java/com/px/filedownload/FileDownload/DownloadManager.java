package com.px.filedownload.FileDownload;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadManager {

    public static final String DOWNLOAD_TAG = "FileDownload";
    public static final String ACTION_START_DOWNLOAD = "1";
    public static final String ACTION_PAUSE_DOWNLOAD = "2";
    public static final String ACTION_CANCEL_DOWNLOAD = "3";

    public static final String ERROR_URL_ERROR = "Download url error!";

    private Context context;
    private DownloadFileInfo downloadFileInfo;
    private DownloadTask downloadTask;

    public DownloadManager(Context context ,DownloadFileInfo downloadFileInfo) {
        this.context = context;
        this.downloadFileInfo = downloadFileInfo;
    }

    public void startDownload () {
        Intent intent = new Intent(context , DownloadService.class);
        intent.setAction(ACTION_START_DOWNLOAD);
        intent.putExtra("downloadFileInfo" , downloadFileInfo);
        context.startService(intent);
    }

    public void pauseDownload () {
        Intent intent = new Intent(context , DownloadService.class);
        intent.setAction(ACTION_PAUSE_DOWNLOAD);
        context.startService(intent);
    }

    public void cancelDownload () {
        Intent intent = new Intent(context , DownloadService.class);
        intent.setAction(ACTION_CANCEL_DOWNLOAD);
        intent.putExtra("downloadFileInfo" , downloadFileInfo);
        context.startService(intent);
    }

    public void setOnDownloadStatusListener (DownloadStatusListener downloadStatusListener){
        DownloadTask.setListener(downloadStatusListener);
    }
}
