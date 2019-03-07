package com.yaninfo.httpurldownload.services;

import android.content.Context;
import android.content.Intent;
import com.yaninfo.httpurldownload.models.FileInfo;
import com.yaninfo.httpurldownload.models.ThreadInfo;
import com.yaninfo.httpurldownload.utils.ThreadDAO;
import com.yaninfo.httpurldownload.utils.ThreadDAOImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件下载任务类
 */
public class DownloadTask {

    //上下文
    private Context mContext = null;
    //文件信息
    private FileInfo mFileInfo = null;
    private ThreadDAOImpl mDao;
    private int mFinished = 0;
    public boolean isPause = false;
    //线程数量
    private int mThreadCount = 1;
    //线程集合
    private List<DownloadThread> mThreadList = null;
    //线程池,带缓存
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();

    /**
     * 有参构造方法
     *
     * @param mContext
     * @param mFileInfo
     */
    public DownloadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = mThreadCount;
        //实例化ThreadDAO
        mDao = new ThreadDAOImpl(mContext);
    }

    /**
     * 多线程下载,文件切快
     */
    public void download() {
        //读取数据库线程信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        if (threadInfos.size() == 0) {
            //获取每个线程下载的长度
            int length = (mFileInfo.getLength()) / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
                        length * i, (length * (i + 1) - 1), 0);
                //最后一个线程除不尽,就设置为文件的长度
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //添加到线程集合中
                threadInfos.add(threadInfo);
                //向数据库插入一条线程信息
                mDao.insertThread(threadInfo);

            }

        }

        //启动多个线程下载
        mThreadList = new ArrayList<>();
        for (ThreadInfo info : threadInfos) {
            DownloadThread thread = new DownloadThread(info);
            //  thread.start();
            DownloadTask.sExecutorService.execute(thread);
            //添加线程到集合中
            mThreadList.add(thread);
        }

    }

    /**
     * 判断所有线程是否都执行完毕
     */
    private synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;

        //遍历线程集合,判断线程是否都执行完毕
        for (DownloadThread thread : mThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            // 删除线程信息
            mDao.deleteThread(mFileInfo.getUrl());
            //发送广播通知UI下载任务结束
            Intent intent = new Intent(DownloadService.ACTION_FINISH);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {

        //线程实体类
        private ThreadInfo threadInfo = null;
        //线程是否结束
        public boolean isFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }


        /**
         * 下载进度保存到Activity,实时更新
         */
        @Override
        public void run() {

            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream input = null;

            //设置下载位置
            try {
                URL url = new URL(threadInfo.getUrl());
                //打开连接
                connection = (HttpURLConnection) url.openConnection();
                //设置超时
                connection.setConnectTimeout(3000);
                //设置请求方法
                connection.setRequestMethod("GET");
                //start等于开始位置加上结束位置
                int start = threadInfo.getStart() + threadInfo.getFinished();
                //设置下载位置
                connection.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                //设置一个文件写入位置,创建一个本地文件
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //seek(100)就是跳过100个字节,直接从第101个字节开始读写
                raf.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                //开始下载
                //累加线程完成的进度
                //读取数据,判断是否连接,这里状态码是206,即无条件响应GET请求返回状态码
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {

                    //输入流
                    input = connection.getInputStream();

                    //定义一下字节
                    byte[] buffer = new byte[1024 * 4];
                    int length = -1;
                    long time = System.currentTimeMillis();
                    //遍历读取
                    while ((length = input.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, length);
                        //把下载进度发送广播给Activity
                        //累加整个文件完成的进度
                        mFinished += length;
                        //累加每个线程下载的进度
                        threadInfo.setFinished(threadInfo.getFinished() + length);
                        //500毫秒发送一次广播
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            //广播发送数据
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时,保存下载进度
                        if (isPause) {
                            mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
                            return;
                        }
                    }
                    isPause = true;
                    //检查下载任务是否完成
                    checkAllThreadsFinished();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                try {
                    input.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
