package com.mytest.imageloader.bean;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mytest.imageloader.R;
import com.mytest.imageloader.util.ImageLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {

    private Set<String> mSelectedImg = new HashSet<>();

    private Context context;
    private List<String> mImgPaths;
    private String dirPath;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
        this.context = context;
        this.mImgPaths = mDatas;
        this.dirPath = dirPath;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public Object getItem(int i) {
        return mImgPaths.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_gridview, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.id_item_image);
            viewHolder.button = (ImageButton) convertView.findViewById(R.id.id_item_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 重置状态
        viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
        viewHolder.button.setImageResource(R.mipmap.ic_launcher);
        viewHolder.imageView.setColorFilter(null);

        ImageLoader.getInstance().loadImage(dirPath + "/" + mImgPaths.get(position), viewHolder.imageView);

        final String filePath = dirPath + "/" + mImgPaths.get(position);
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectedImg.contains(filePath)) {
                    mSelectedImg.remove(filePath);
                    viewHolder.imageView.setColorFilter(null);
                } else {
                    mSelectedImg.add(filePath);
                    viewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
                }
//                notifyDataSetChanged();
            }
        });
        if (mSelectedImg.contains(filePath)) {
            viewHolder.imageView.setColorFilter(Color.parseColor("#77000000"));
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        ImageButton button;
    }
}