package com.mytest.imageloader;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mytest.imageloader.bean.FolderBean;
import com.mytest.imageloader.util.ImageLoader;

import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public class ListImageDirPopupWindow extends PopupWindow {

    private int width;
    private int height;
    private View convertView;
    private ListView listView;
    private List<FolderBean> mDatas;

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    public interface OnDirSelectedListener {
        void onSelected(FolderBean folderBean);
    }

    private OnDirSelectedListener mListener;

    public ListImageDirPopupWindow(Context context, List<FolderBean> datas) {
        calWidthAndHeight(context);

        convertView = LayoutInflater.from(context).inflate(R.layout.popup_main, null);
        mDatas = datas;

        setContentView(convertView);
        setWidth(width);
        setHeight(height);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        initViews(context);
        initEvent();
    }

    private void initEvent() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mListener.onSelected(mDatas.get(i));
                }
            }
        });
    }

    private void initViews(Context context) {
        listView = (ListView) convertView.findViewById(R.id.id_list_dir);
        listView.setAdapter(new ListDirAdapter(context, mDatas));
    }

    /**
     * 计算popupwindow的宽度和高度
     *
     * @param context
     */
    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        width = metrics.widthPixels;
        height = (int) (metrics.heightPixels * 0.7);
    }

    private class ListDirAdapter extends ArrayAdapter<FolderBean> {

        private LayoutInflater inflater;
        private List<FolderBean> mDatas;

        public ListDirAdapter(Context context, List<FolderBean> datas) {
            super(context, 0, datas);
            inflater = LayoutInflater.from(context);
            mDatas = datas;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_popup_main, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.id_id_dir_item_image);
                holder.dirName = (TextView) convertView.findViewById(R.id.id_dir_item_name);
                holder.dirCount = (TextView) convertView.findViewById(R.id.id_dir_item_count);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FolderBean bean = getItem(position);
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
            ImageLoader.getInstance().loadImage(bean.getFirstImgPath(), holder.imageView);
            holder.dirName.setText(bean.getName());
            holder.dirCount.setText(bean.getCount() + "");
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView dirName;
            TextView dirCount;
        }
    }
}
