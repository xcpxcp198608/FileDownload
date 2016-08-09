package com.px.filedownload.FileDownload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadService extends Service{

    public static final int MSG_PREPARE_DOWNLOAD = 0;
    public static final int MSG_START_DOWNLOAD = 1;
    public static final int MSG_PAUSE_DOWNLOAD = 2;
    public static final int MSG_FINISHED_DOWNLOAD = 3;
    public static final int MSG_CONNECT_FAIL_DOWNLOAD = 4;
    public static final int MSG_URL_ERROR_DOWNLOAD = 5;
    public static final int MSG_IO_ERROR_DOWNLOAD = 6;
    public static final int MSG_FILE_NO_FOUND = 7;
    public static final int MSG_PROGRESS_CHANGE = 8;
    public static final int MSG_CANCEL_DOWNLOAD = 9;
    private static final String PREPARE_DOWNLOAD = "Prepare to download";
    private static final String NETWORK_CONNECT_FAIL = "Network connect fail , please check and try again!";
    private static final String URL_ERROR = "Download Url error!";
    private static final String IO_ERROR = "IO error!";

    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DDownload/";

    private DownloadTask downloadTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_PREPARE_DOWNLOAD :
                    Toast.makeText(getApplicationContext(),PREPARE_DOWNLOAD ,Toast.LENGTH_SHORT).show();
                    DownloadFileInfo downloadFileInfo = (DownloadFileInfo) msg.obj;
                    downloadTask = new DownloadTask(getApplicationContext(), downloadFileInfo);
                    downloadTask.startDownload();
                    break;
                case MSG_CONNECT_FAIL_DOWNLOAD :
                    Toast.makeText(getApplicationContext(),NETWORK_CONNECT_FAIL ,Toast.LENGTH_SHORT).show();
                    break;
                case MSG_URL_ERROR_DOWNLOAD :
                    Toast.makeText(getApplicationContext(),URL_ERROR ,Toast.LENGTH_SHORT).show();
                    break;
                case MSG_IO_ERROR_DOWNLOAD :
                    Log.d(DownloadManager.DOWNLOAD_TAG , (String) msg.obj);
                    Toast.makeText(getApplicationContext(),IO_ERROR ,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(DownloadManager.ACTION_START_DOWNLOAD.equals(intent.getAction())){
            //Log.d(DownloadManager.DOWNLOAD_TAG , "startService");
            DownloadFileInfo downloadFileInfo = (DownloadFileInfo) intent.getSerializableExtra("downloadFileInfo");
            new DownloadInitThread(downloadFileInfo).start();
        }else if (DownloadManager.ACTION_PAUSE_DOWNLOAD.equals(intent.getAction())){
            Log.d(DownloadManager.DOWNLOAD_TAG , "pauseService");
            if(downloadTask != null) {
                downloadTask.isPauseDownload = true ;
            }
        }else if (DownloadManager.ACTION_CANCEL_DOWNLOAD.equals(intent.getAction())){
            Log.d(DownloadManager.DOWNLOAD_TAG , "cancelService");
            if(downloadTask != null) {
                if(downloadTask.isPauseDownload){

                }else {
                    downloadTask.isCancelDownload = true ;
                    stopSelf();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(downloadTask != null) {
            downloadTask.isCancelDownload = true ;
        }
    }

    public boolean isNetworkConnected (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!= null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            return true;
        }else {
            return false;
        }
    }

    //初始化下载任务线程；
    class DownloadInitThread extends Thread {
        private DownloadFileInfo downloadFileInfo;

        public DownloadInitThread( DownloadFileInfo downloadFileInfo) {
            this.downloadFileInfo = downloadFileInfo;
        }

        @Override
        public void run() {
            if(!isNetworkConnected(DownloadService.this)) {
                mHandler.sendEmptyMessage(MSG_CONNECT_FAIL_DOWNLOAD);
                return;
            }
            HttpURLConnection httpURLConnection = null;
            RandomAccessFile randomAccessFile = null;
            try {
                int fileSize= -1;
                URL url = new URL(downloadFileInfo.getDownloadUrl());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();
                if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    fileSize = httpURLConnection.getContentLength();
                    Log.d(DownloadManager.DOWNLOAD_TAG , fileSize+"");
                }else {
                    Log.d(DownloadManager.DOWNLOAD_TAG , "connect error");
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAIL_DOWNLOAD);
                }
                if(fileSize < 0) {
                    return;
                }
                File dir = new File(FILE_PATH);
                if(!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir,downloadFileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file , "rwd");
                randomAccessFile.setLength(fileSize);
                downloadFileInfo.setFileSize(fileSize);
                mHandler.obtainMessage(MSG_PREPARE_DOWNLOAD ,downloadFileInfo).sendToTarget();
            } catch (MalformedURLException e) {
                mHandler.sendEmptyMessage(MSG_URL_ERROR_DOWNLOAD);
            } catch (IOException e) {
                mHandler.obtainMessage(MSG_IO_ERROR_DOWNLOAD ,e).sendToTarget();
            }finally {
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
                if(randomAccessFile != null){
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        mHandler.sendEmptyMessage(MSG_IO_ERROR_DOWNLOAD);
                    }
                }
            }
        }
    }

}
