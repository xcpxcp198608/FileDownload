package com.px.filedownload.FileDownload;

import java.util.List;

/**
 * Created by Administrator on 2016/8/8.
 */
public interface DownloadSQLiteDaoInterface {
    void insertData (DownloadThreadInfo downloadThreadInfo) ;
    boolean deleteData (String downloadUrl , int threadId) ;
    List<DownloadThreadInfo> queryData (String downloadUrl ) ;
    void updateData (String downloadUrl ,int threadId ,long finishedPosition) ;
    boolean isDataExists (String downloadUrl ,int threadId);
}
