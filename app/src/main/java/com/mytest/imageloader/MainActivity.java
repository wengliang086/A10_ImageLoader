package com.mytest.imageloader;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mytest.imageloader.bean.FolderBean;
import com.mytest.imageloader.util.ImageLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private List<String> mImgs;
    private ImageAdapter imageAdapter;

    private RelativeLayout bottomLayout;
    private TextView dirName;
    private TextView dirCount;

    private File mCurrentDir;
    private int mMaxCount;

    private List<FolderBean> mFolderBeans = new ArrayList<>();

    private ProgressDialog progressDialog;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x110) {
                progressDialog.dismiss();
                // 绑定数据到View中
                data2View();
            }
        }
    };

    private void data2View() {
        if (mCurrentDir == null) {
            Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mImgs = Arrays.asList(mCurrentDir.list());
        imageAdapter = new ImageAdapter(this, mImgs, mCurrentDir.getAbsolutePath());
        gridView.setAdapter(imageAdapter);

        dirCount.setText(mMaxCount + "");
        dirName.setText(mCurrentDir.getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initEvent();
    }

    private void initEvent() {

    }

    /**
     * 利用ContentProvider扫描手机中的所有图片
     */
    private void initData() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(this, null, "正在加载...");
        new Thread() {
            @Override
            public void run() {
                super.run();
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = MainActivity.this.getContentResolver();

                String selection = MediaStore.Images.Media.MIME_TYPE + " = ? or " + MediaStore.Images.Media.MIME_TYPE + " = ? ";
                String[] selectionArgs = {"image/jpeg", "image/png"};
                Cursor cursor = cr.query(mImgUri, null, selection, selectionArgs, MediaStore.Images.Media.DATE_MODIFIED);

                Set<String> mDirPaths = new HashSet<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean = null;
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImgPath(path);
                    }
                    if (parentFile.list() == null) {
                        continue;
                    }
                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String fileName) {
                            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                                return true;
                            }
                            return false;
                        }
                    }).length;
                    folderBean.setCount(picSize);

                    mFolderBeans.add(folderBean);

                    if (picSize > mMaxCount) {
                        mMaxCount = picSize;
                        mCurrentDir = parentFile;
                    }
                }
                cursor.close();
                // 通知Handler扫描图片完成
                mHandler.sendEmptyMessage(0x110);
            }
        }.start();
    }

    private void initView() {
        gridView = (GridView) findViewById(R.id.id_gridView);
        bottomLayout = (RelativeLayout) findViewById(R.id.id_bottom_layout);
        dirName = (TextView) findViewById(R.id.id_dir_name);
        dirCount = (TextView) findViewById(R.id.id_dir_count);
    }

    private class ImageAdapter extends BaseAdapter {

        private Context context;
        private List<String> mDatas;
        private String dirPath;
        private LayoutInflater inflater;

        public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
            this.context = context;
            this.mDatas = mDatas;
            this.dirPath = dirPath;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mImgs.size();
        }

        @Override
        public Object getItem(int i) {
            return mImgs.get(i) ;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (view == null) {
                view = inflater.inflate(R.layout.item_gridview, viewGroup);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) view.findViewById(R.id.id_item_image);
                viewHolder.button = (ImageButton) view.findViewById(R.id.id_item_select);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            // 重置状态
            viewHolder.imageView.setImageResource(R.mipmap.ic_launcher);
            viewHolder.button.setImageResource(R.mipmap.ic_launcher);

            ImageLoader.getInstance().loadImage(dirPath + "/" + mImgs.get(i), viewHolder.imageView);
            return view;
        }

        private class ViewHolder {
            ImageView imageView;
            ImageButton button;
        }
    }
}
