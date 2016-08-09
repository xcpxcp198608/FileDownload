package com.px.filedownload.FileDownload;

/**
 * Created by Administrator on 2016/8/8.
 */
public interface DownloadStatusListener  {
    void onStartDownload(boolean isStart);
    void onPauseDownload(boolean isPause , int progress);
    void onFailDownload(Exception e);
    void onProgressChanged (int progress , long finishedPosition);
    void onFinishedDownload(int progress , boolean isFinished);
    void onCancelDownload( boolean isCancel);
}
