package com.yaninfo.httpurldownload;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yaninfo.httpurldownload.models.FileInfo;
import com.yaninfo.httpurldownload.services.DownloadService;
import com.yaninfo.httpurldownload.services.DownloadTask;
import com.yaninfo.httpurldownload.utils.DBHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyan
 */
public class MainActivity extends Activity {

   //文件列表
   private ListView mListFile = null;
   //文件集合
   private List<FileInfo> mFileList = null;
   //文件适配器
   private FileListAdapter mAdapter = null;
   private String myString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper helper=DBHelper.getInstance(MainActivity.this);
        //调用getWritableDatabase()或getReadableDatabase()才会真正创建或打开
        SQLiteDatabase db=helper.getWritableDatabase();
        db.close(); //操作完成后关闭数据库连接

        //初始化组件
        mListFile = findViewById(R.id.listView);
        mFileList = new ArrayList<>();


         //实例化
         FileInfo fileInfo = new FileInfo(0,"http://www.xunlegelei.com/susujia.apk",
                "jiajia",0,0);
         FileInfo fileInfo1 = new FileInfo(1,"http://www.xunlegelei.com/susujia.apk",
                "jiajia1",0,0);
         FileInfo fileInfo2 = new FileInfo(2,"http://www.xunlegelei.com/susujia.apk",
                "jiajia2",0,0);

         //添加到集合
         mFileList.add(fileInfo);
         mFileList.add(fileInfo1);
         mFileList.add(fileInfo2);

         //创建适配器
        mAdapter = new FileListAdapter(this, mFileList);
        mListFile.setAdapter(mAdapter);

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISH);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 更新UI广播接收器
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新进度
            if(DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished",0);
                int id = intent.getIntExtra("id",0);
                //更新进度
                mAdapter.updateProgress(id, finished);
            } else if(DownloadService.ACTION_FINISH.equals(intent.getAction())){
                //下载结束
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //更新进度
                mAdapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this,
                        mFileList.get(fileInfo.getId()).getFileName()+"下载完毕", Toast.LENGTH_SHORT).show();

            }
        }
    };

}
