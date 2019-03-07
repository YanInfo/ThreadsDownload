package com.yaninfo.httpurldownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.yaninfo.httpurldownload.models.FileInfo;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author zhangyan
 */
public class DownloadService extends Service {

    /* 路径标识 */
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    /* 开始标识 */
    public static final String ACTION_START = "ACTION_START";
    /* 结束标识 */
    public static final String ACTION_STOP = "ACTION_STOP";
    /* 下载完成标识 */
    public static final String ACTION_FINISH = "ACTION_FINISH";
    /*  更新进度  */
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    /*  handler传递标识  */
    public static final int MSG_INIT = 0;
    /*  下载任务的集合     */
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();
    /*   初始化下载  */
    private Runnable mInitThread;

    /**
     * 获取Activity传来的参数
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得Activity传来的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            //任务为空时启动线程,这里一定要做判断,不然会重复下载启动多个线程
             mInitThread = new InitThread(fileInfo);
             DownloadTask.sExecutorService.execute(mInitThread);

        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            //从集合中取出下载任务
            Log.i("test", "Stop:" + fileInfo.getFileName());
            DownloadTask tasks = mTasks.get(fileInfo.getId());
            if (tasks != null) {
                tasks.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    //输出一下数据
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i("test", "Init:" + fileInfo);

                    //启动下载任务
                    DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 3);
                    task.download();
                    //添加下载任务到集合中
                    mTasks.put(fileInfo.getId(), task);
                    break;
            }
        }
    };


    /**
     * 初始化子线程,网络操作.这里只开一条线程
     */
    class InitThread extends Thread {

        private FileInfo mFileInfo = null;

        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        /**
         * 连接网络,获得文件长度回传到Handler,最终打印出来
         */
        @Override
        public void run() {
            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;

            try {
                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length = -1;

                //url检验
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //在本地创建文件
                File file = new File(dir, mFileInfo.getFileName());
                //随机文件操作,rwd表示读取写入和删除权限都给了
                randomAccessFile = new RandomAccessFile(file, "rwd");
                //设置文件长度
                randomAccessFile.setLength(length);
                mFileInfo.setLength(length);
                //发送给Handler
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭连接
                connection.disconnect();
                try {
                    //关闭流
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
