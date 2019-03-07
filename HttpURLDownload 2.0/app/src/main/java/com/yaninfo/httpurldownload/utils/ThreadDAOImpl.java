package com.yaninfo.httpurldownload.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yaninfo.httpurldownload.models.ThreadInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现数据访问借口
 */
public class ThreadDAOImpl implements ThreadDAO {

    private DBHelper mHelper = null;

    /**
     * 构造方法
     * @param context
     */
    public ThreadDAOImpl(Context context) {

        mHelper = DBHelper.getInstance(context);
    }
    /**
     * 增
     * @param threadInfo
     */
    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id, thread_url, thread_start, thread_end, thread_finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
        db.close();
    }

    /**
     * 删
     * @param url
     */
    @Override
    public synchronized void deleteThread(String url ) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where thread_url= ? ",
                new Object[]{url});
        db.close();
    }

    /**
     * 改
     * @param url
     * @param thread_id
     * @param finished
     */
    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set thread_finished = ? where  thread_url=? and thread_id = ?",
                new Object[]{finished, url, thread_id});
        db.close();
    }

    /**
     * 查
     * @param url
     * @return
     */
    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        //Cursor游标理解为结果集
        Cursor cursor = db.rawQuery("select * from thread_info where thread_url = ?" ,
                new String[]{url});
        //遍历结果集
        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo();
            //根据列号来取值
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("thread_url")));
            threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("thread_start")));
            threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("thread_end")));
            threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("thread_finished")));
            list.add(threadInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where thread_url = ? and thread_id =? " ,
                new String[]{url, thread_id+""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        db.close();
        //返回布尔类型
        return exists;
    }
}
