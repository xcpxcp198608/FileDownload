package com.px.filedownload.FileDownload;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadTask  {

    private Context context;
    private DownloadFileInfo downloadFileInfo;
    public static boolean isDownloading = false;
    private DownloadSQLiteDao downloadSQLiteDao;
    private static DownloadStatusListener downloadStatusListener = null;
    private long finishedPosition = -1;
    public boolean isPauseDownload = false;
    public boolean isCancelDownload = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DownloadService.MSG_START_DOWNLOAD :
                    Log.d(DownloadManager.DOWNLOAD_TAG , "msg_start");
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onStartDownload(true);
                    }
                    break;
                case DownloadService.MSG_PAUSE_DOWNLOAD :
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onPauseDownload(true , (Long) msg.obj);
                    }
                    break;
                case DownloadService.MSG_FINISHED_DOWNLOAD :
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onFinishedDownload((int)msg.obj ,true);
                    }
                    break;
                case DownloadService.MSG_FILE_NO_FOUND:
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onFailDownload((Exception) msg.obj);
                    }
                    break;
                case DownloadService.MSG_URL_ERROR_DOWNLOAD:
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onFailDownload((Exception) msg.obj);
                    }
                    break;
                case DownloadService.MSG_IO_ERROR_DOWNLOAD:
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onFailDownload((Exception) msg.obj);
                    }
                    break;
                case DownloadService.MSG_PROGRESS_CHANGE:
                    if(downloadStatusListener != null) {
                        long finished = (long) msg.obj;
                        Log.d(DownloadManager.DOWNLOAD_TAG , finished+"");
                        int progress = (int) (finished*100 /downloadFileInfo.getFileSize());
                        downloadStatusListener.onProgressChanged(progress , finished );
                    }
                    break;
                case DownloadService.MSG_CANCEL_DOWNLOAD:
                    if(downloadStatusListener != null) {
                        downloadStatusListener.onCancelDownload(true);
                    }
                    break;
            }
        }
    } ;

    public DownloadTask(Context context, DownloadFileInfo downloadFileInfo) {
        this.context = context;
        this.downloadFileInfo = downloadFileInfo;
        downloadSQLiteDao = DownloadSQLiteDao.getInstance(context);
    }


    public static void setListener (DownloadStatusListener downloadStatusListener1) {
        downloadStatusListener = downloadStatusListener1;
    }

    public void startDownload () {
        List<DownloadThreadInfo> dataList = downloadSQLiteDao.queryData(downloadFileInfo.getDownloadUrl());
        DownloadThreadInfo downloadThreadInfo = null;
        if (dataList.size() == 0) {
            downloadThreadInfo = new DownloadThreadInfo();
            downloadThreadInfo.setDownloadUrl(downloadFileInfo.getDownloadUrl());
            downloadThreadInfo.setStartPosition(0);
            downloadThreadInfo.setEndPosition(downloadFileInfo.getFileSize());
            downloadThreadInfo.setFinishedPosition(0);
            downloadThreadInfo.setThreadId(0);
            dataList.add(downloadThreadInfo);
            new DownloadThread(downloadThreadInfo).start();
        }else {
            for (DownloadThreadInfo downloadThreadInfo1 :dataList) {
                if(downloadThreadInfo1.getDownloadUrl().equals(downloadFileInfo.getDownloadUrl())){
                    new DownloadThread(downloadThreadInfo1).start();
                }
            }
        }

    }


    class DownloadThread extends Thread {
        private DownloadThreadInfo downloadThreadInfo;

        public DownloadThread(DownloadThreadInfo downloadThreadInfo) {
            this.downloadThreadInfo = downloadThreadInfo;
        }

        @Override
        public void run() {
            Log.d(DownloadManager.DOWNLOAD_TAG , "start task run");
            HttpURLConnection httpURLConnection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream inputStream = null;
            //读取数据库内的下载线程信息
            if( !downloadSQLiteDao.isDataExists(downloadThreadInfo.getDownloadUrl() , downloadThreadInfo.getThreadId())) {
                downloadSQLiteDao.insertData(downloadThreadInfo);
            }
            //设置下载开始位置
            long startPosition = downloadThreadInfo.getStartPosition() + downloadThreadInfo.getFinishedPosition();
            try {
                URL url = new URL(downloadThreadInfo.getDownloadUrl());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Range" , "bytes="+startPosition+"-"+downloadThreadInfo.getEndPosition());
            } catch (MalformedURLException e) {
                Log.d(DownloadManager.DOWNLOAD_TAG , e.getMessage());
            } catch (IOException e) {
                Log.d(DownloadManager.DOWNLOAD_TAG , e.getMessage());
            }
            //设置文件写入位置
            File file = new File(DownloadService.FILE_PATH , downloadFileInfo.getFileName());
            try {
                randomAccessFile = new RandomAccessFile(file , "rwd");
                randomAccessFile.seek(startPosition);
            } catch (FileNotFoundException e) {
                Log.d(DownloadManager.DOWNLOAD_TAG , e.getMessage());
            } catch (IOException e) {
                Log.d(DownloadManager.DOWNLOAD_TAG , e.getMessage());
            }
            //开始下载
            handler.sendEmptyMessage(DownloadService.MSG_START_DOWNLOAD);
            finishedPosition += downloadThreadInfo.getFinishedPosition();
            try {
                Log.d(DownloadManager.DOWNLOAD_TAG , "file seek");
                if(httpURLConnection.getResponseCode() == HttpsURLConnection.HTTP_PARTIAL) {
                    Log.d(DownloadManager.DOWNLOAD_TAG , "file seek1");
                    inputStream = httpURLConnection.getInputStream();
                    byte [] buffer = new byte [1024*4];
                    int length = -1;
                    long time = System.currentTimeMillis();
                    while((length = inputStream.read(buffer)) != -1) {
                        randomAccessFile.write(buffer ,0 , length);
                        //发送下载进度
                        finishedPosition += length;
                        if (System.currentTimeMillis() - time > 1000 ){
                            time = System.currentTimeMillis();
                            handler.obtainMessage(DownloadService.MSG_PROGRESS_CHANGE , finishedPosition).sendToTarget();
                            Log.d(DownloadManager.DOWNLOAD_TAG , finishedPosition+"");
                        }
                        //判断是否暂停下载，暂停时将下载的线程信息更新到数据库
                        if(isPauseDownload) {
                            handler.obtainMessage(DownloadService.MSG_PAUSE_DOWNLOAD ,finishedPosition).sendToTarget();
                            downloadSQLiteDao.updateData(downloadThreadInfo.getDownloadUrl() ,downloadThreadInfo.getThreadId() ,finishedPosition);
                            return;
                        } else if(isCancelDownload) {
                            handler.sendEmptyMessage(DownloadService.MSG_CANCEL_DOWNLOAD);
                            downloadSQLiteDao.deleteData(downloadThreadInfo.getDownloadUrl() , downloadThreadInfo.getThreadId());
                            Log.d(DownloadManager.DOWNLOAD_TAG , "delete");
                            return;
                        }
                    }
                    //下载完成后删除下载线程信息
                    int downloadProgress = 100;
                    handler.obtainMessage(DownloadService.MSG_FINISHED_DOWNLOAD , downloadProgress).sendToTarget();
                    downloadSQLiteDao.deleteData(downloadThreadInfo.getDownloadUrl() , downloadThreadInfo.getThreadId());
                }
            } catch (IOException e) {
                Log.d(DownloadManager.DOWNLOAD_TAG , e.getMessage());
            }finally {
                try {
                    if(httpURLConnection!= null){
                        httpURLConnection.disconnect();
                    }
                    if(randomAccessFile!= null){
                        randomAccessFile.close();
                    }
                    if(inputStream!= null){
                        inputStream.close();
                    }
                } catch (IOException e) {
                    handler.obtainMessage(DownloadService.MSG_IO_ERROR_DOWNLOAD , e).sendToTarget();
                }
            }
        }
    }
}
