package com.px.filedownload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.px.filedownload.FileDownload.DownloadFileInfo;
import com.px.filedownload.FileDownload.DownloadManager;
import com.px.filedownload.FileDownload.DownloadSQLiteDao;
import com.px.filedownload.FileDownload.DownloadStatusListener;

public class Demo extends AppCompatActivity {
    private TextView tv_FileName;
    private Button bt_Download ,bt_PauseDownload , bt_CancelDownload;
    private ProgressBar pb_Download;
    private DownloadManager downloadManager;

    private DownloadSQLiteDao downloadSQLiteDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_FileName = (TextView) findViewById (R.id.tv_fileName);
        bt_Download = (Button) findViewById(R.id.bt_download);
        bt_PauseDownload = (Button) findViewById(R.id.bt_pauseDownload);
        bt_CancelDownload = (Button) findViewById(R.id.bt_cancelDownload);
        pb_Download = (ProgressBar) findViewById(R.id.pb_download);

        final DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
        downloadFileInfo.setDownloadUrl("http://p.gdown.baidu.com/801e1a694a33e3c61b0db4a38c11a39b0ebdd5d13c6c6607f454873aa70e3df9128b5224598c1c10bf0c08e7273a61c16ed9e80187e992a9d4c314b05cf58a55c58f29b5df2eeb112f2cc0365081f59151b6e26689499eb057ffa1e413148282aba11c01b83bed7f117f4feb9616bb008c67f18fb208eee174a2cc8669c750cf02a7ef4fba9ee1657485d8354a4bfa04fe2a41a0b9f167d3");
        downloadFileInfo.setFileName("file.apk");

        downloadManager = new DownloadManager(Demo.this , downloadFileInfo);
        bt_Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.startDownload();
            }
        });
        bt_PauseDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager.pauseDownload();
            }
        });
        bt_CancelDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //downloadManager.cancelDownload();
            }
        });

        downloadManager.setOnDownloadStatusListener(new DownloadStatusListener() {
            @Override
            public void onStartDownload(boolean isStart) {
                tv_FileName.setText("downloading");
            }

            @Override
            public void onPauseDownload(boolean isPause, int progress) {
                tv_FileName.setText("pause"+"---"+progress+"%");
            }

            @Override
            public void onFailDownload(Exception e) {
                tv_FileName.setText(e.getMessage());
            }

            @Override
            public void onProgressChanged(int progress, long finishedPosition) {
                pb_Download.setProgress(progress);
            }

            @Override
            public void onFinishedDownload(int progress, boolean isFinished) {
                pb_Download.setProgress(progress);
                tv_FileName.setText("finish"+progress);
            }

            @Override
            public void onCancelDownload(boolean isCancel) {
                tv_FileName.setText("cancel");
            }
        });


    }
}
