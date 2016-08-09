package com.px.filedownload.FileDownload;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/8/8.
 */
public class DownloadSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DownloadInfo.db";
    public static final String TABLE_NAME = "DownloadInfo";
    private static final int VERSION = 1;
    private static final String CREATE_TABLE = "create table if not exists "+TABLE_NAME +"(_id integer primary key autoincrement," +
            "threadId integer,startPosition real,endPosition real,finishedPosition real ,downloadUrl text)";
    private static final String DROP_TABLE = "drop table if exists "+TABLE_NAME;

    public DownloadSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        this.onCreate(db);
    }
}
