package com.yaninfo.httpurldownload;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yaninfo.httpurldownload.models.FileInfo;
import com.yaninfo.httpurldownload.services.DownloadService;

import java.io.File;
import java.util.List;

/**
 * ListView对应的文件列表适配器
 */
public class FileListAdapter extends BaseAdapter {

    private Context context = null;
    private List<FileInfo> mFileList = null;
    ViewHolder holder = null;

    /**
     * 实现构造方法
     * @param context
     * @param mFileList
     */
    public FileListAdapter(Context context, List<FileInfo> mFileList) {
        this.context = context;
        this.mFileList = mFileList;
    }

    /**
     * 返回文件列表显示文件的数量
     * @return
     */
    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 视图的显示
     * @param position
     * @param view
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        //设置视图中的控件
        final FileInfo fileInfo = mFileList.get(position);

        //判断视图是否为空,则缓存控件
        if(view == null ) {
            //加载视图
            view = LayoutInflater.from(context).inflate(R.layout.listitem, null);
            //获得布局中的控件
            holder = new ViewHolder();
            holder.textView = view.findViewById(R.id.textView);
            holder.btnStart = view.findViewById(R.id.btnStart);
            holder.btnStop = view.findViewById(R.id.btnStop);
            holder.progressBar = view.findViewById(R.id.progressBar);
            holder.textView.setText(fileInfo.getFileName());
            holder.progressBar.setMax(100);

            //开始按钮,通知Service开始下载
            holder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    //启动上下文
                    context.startService(intent);

                }
            });

            //暂停按钮,通知Service停止下载
            holder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileInfo", fileInfo);
                    //启动上下文
                    context.startService(intent);
                }
            });
            //保存holder到视图中
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.progressBar.setProgress(fileInfo.getFinished());

        return view;
    }

    /**
     * ViewHolder就是一个临时存储器,会把getView()的view缓存起来,这样就不用每次加载对应的控件了
     * 静态类只用加载一次
     */
    static class ViewHolder{
        TextView textView;
        Button btnStart;
        Button btnStop;
        ProgressBar progressBar;

    }

    /**
     * 更新列表进度条UI
     * @param id
     * @param progress
     */
    public void updateProgress (int id, int progress) {
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        //刷新界面,重写调用getView()
        notifyDataSetChanged();
    }
}
