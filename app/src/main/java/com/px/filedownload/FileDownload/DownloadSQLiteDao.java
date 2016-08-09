package com.px.filedownload.FileDownload;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadSQLiteDao implements DownloadSQLiteDaoInterface {

    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public DownloadSQLiteDao(Context context) {
        this.context = context;
        sqLiteDatabase = new DownloadSQLiteHelper(context).getWritableDatabase();
    }

    private static DownloadSQLiteDao instance;
    public static synchronized DownloadSQLiteDao getInstance(Context context) {
        if(instance ==null) {
            synchronized (DownloadSQLiteDao.class){
                instance = new DownloadSQLiteDao(context);
            }
        }
        return instance;
    }

    @Override
    public boolean deleteData(String downloadUrl, int threadId) {
        sqLiteDatabase.delete(DownloadSQLiteHelper.TABLE_NAME ,
                "downloadUrl=? and threadId=?" , new String [] {downloadUrl , threadId+""});
        return !this.isDataExists(downloadUrl , threadId);
    }

    @Override
    public void insertData(DownloadThreadInfo downloadThreadInfo) {
        if(isDataExists(downloadThreadInfo.getDownloadUrl() ,downloadThreadInfo.getThreadId())) {
            return;
        }else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("threadId" , downloadThreadInfo.getThreadId());
            contentValues.put("startPosition" , downloadThreadInfo.getStartPosition());
            contentValues.put("endPosition" , downloadThreadInfo.getEndPosition());
            contentValues.put("finishedPosition" , downloadThreadInfo.getFinishedPosition());
            contentValues.put("downloadUrl" , downloadThreadInfo.getDownloadUrl());
            sqLiteDatabase.insert(DownloadSQLiteHelper.TABLE_NAME ,null , contentValues);
            if(contentValues != null) {
                contentValues = null;
            }
        }
    }

    @Override
    public List<DownloadThreadInfo> queryData(String downloadUrl ) {
        List<DownloadThreadInfo> dataList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(DownloadSQLiteHelper.TABLE_NAME , null ,
                "downloadUrl=? " , new String[] {downloadUrl } , null, null , null);
        while(cursor.moveToNext()) {
            DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
            downloadThreadInfo.setThreadId(cursor.getInt(cursor.getColumnIndex("threadId")));
            downloadThreadInfo.setDownloadUrl(downloadUrl);
            downloadThreadInfo.setStartPosition(cursor.getLong(cursor.getColumnIndex("startPosition")));
            downloadThreadInfo.setEndPosition(cursor.getLong(cursor.getColumnIndex("endPosition")));
            downloadThreadInfo.setFinishedPosition(cursor.getLong(cursor.getColumnIndex("finishedPosition")));
            dataList.add(downloadThreadInfo);
        }
        if(cursor != null){
            cursor.close();
        }
        return dataList;
    }

    @Override
    public void updateData(String downloadUrl, int threadId, long finishedPosition) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("finishedPosition" , finishedPosition);
        sqLiteDatabase.update(DownloadSQLiteHelper.TABLE_NAME , contentValues ,
                "downloadUrl=? and threadId=?" , new String [] {downloadUrl , threadId+""});
    }

    @Override
    public boolean isDataExists(String downloadUrl, int threadId) {
        Cursor cursor = sqLiteDatabase.query(DownloadSQLiteHelper.TABLE_NAME , null ,
                "downloadUrl=? and threadId=?" , new String[] {downloadUrl , threadId+""} , null, null , null);
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }
}
