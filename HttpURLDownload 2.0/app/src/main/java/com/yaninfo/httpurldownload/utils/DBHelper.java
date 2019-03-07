package com.yaninfo.httpurldownload.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库工具类
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "downad.db";
    private static DBHelper dbHelper = null;
    private static final int VERSION = 1;
    private static final String SQL_CREATE = "create table thread_info(id integer primary key autoincrement," +
            "thread_id integer, thread_url text, thread_start integer, thread_end integer, thread_finished integer)";
    private static final String SQL_DROP = "drop table if exists thread_info";

    /**
     * 构造方法,定义成私有
     * @param context
     */
    public DBHelper(Context context) {
        //context上下文对象, name数据库名称, factory游标工厂, version数据库版本
        super(context, DB_NAME, null, VERSION);
    }

    /**
     * 获得类的对象
     * dbHelper对象只创建一次
     */
    public static DBHelper getInstance(Context context) {
        if(dbHelper == null) {
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    /**
     * 创建数据库
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    /**
     * 更新数据库
     * @param db
     * @param i
     * @param i1
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
