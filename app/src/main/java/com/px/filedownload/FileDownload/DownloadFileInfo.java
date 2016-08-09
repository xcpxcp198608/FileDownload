package com.px.filedownload.FileDownload;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadFileInfo implements Serializable{
    public String fileName;
    public String filePackageName;
    public long fileSize;
    public String downloadUrl;
    public long finishedPosition;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePackageName() {
        return filePackageName;
    }

    public void setFilePackageName(String filePackageName) {
        this.filePackageName = filePackageName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFinishedPosition() {
        return finishedPosition;
    }

    public void setFinishedPosition(long finishedPosition) {
        this.finishedPosition = finishedPosition;
    }


    @Override
    public String toString() {
        return "DownloadFileInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePackageName='" + filePackageName + '\'' +
                ", fileSize=" + fileSize +
                ", finishedPosition=" + finishedPosition +
                '}';
    }
}
