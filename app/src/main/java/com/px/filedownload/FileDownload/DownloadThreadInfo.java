package com.px.filedownload.FileDownload;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadThreadInfo implements Serializable {
    public int threadId;
    public long startPosition;
    public long endPosition;
    public long finishedPosition;
    public String downloadUrl;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public long getFinishedPosition() {
        return finishedPosition;
    }

    public void setFinishedPosition(long finishedPosition) {
        this.finishedPosition = finishedPosition;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    @Override
    public String toString() {
        return "DownloadThreadInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", threadId=" + threadId +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", finishedPosition=" + finishedPosition +
                '}';
    }
}
