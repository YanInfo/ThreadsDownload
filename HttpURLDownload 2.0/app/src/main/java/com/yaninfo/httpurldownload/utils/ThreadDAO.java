package com.yaninfo.httpurldownload.utils;

import com.yaninfo.httpurldownload.models.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 */
public interface ThreadDAO {

    /**
     * 插入
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除
     * @param url
     */
    public void deleteThread(String url );

    /**
     * 更新线程
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url, int thread_id, int finished);

    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 判断线程是否存在
     * @param url
     * @param thread_id
     * @return
     */
    public boolean isExists(String url, int thread_id);
}
