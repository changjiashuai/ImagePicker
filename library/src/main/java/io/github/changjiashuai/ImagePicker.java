package io.github.changjiashuai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.changjiashuai.bean.ImageFolder;
import io.github.changjiashuai.bean.ImageItem;
import io.github.changjiashuai.loader.ImageLoader;
import io.github.changjiashuai.widget.CropImageView;

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2017/2/22 16:29.
 */

public class ImagePicker {

    public static final int REQUEST_CODE_PICK = 1001;
    public static final int REQUEST_CODE_CROP = 1002;
    public static final int REQUEST_CODE_PREVIEW = 1003;
    public static final int REQUEST_CODE_ITEMS = 1004;
    public static final int REQUEST_CODE_BACK = 1005;

    public static final String EXTRA_RESULT_ITEMS = "extra_result_items";
    public static final String EXTRA_SELECTED_IMAGE_POSITION = "extra_selected_image_position";
    public static final String EXTRA_IMAGE_ITEMS = "extra_image_items";

    private boolean multiMode = true;               //图片选择模式
    private boolean crop = true;                    //裁剪
    private boolean saveRectangle = false;          //裁剪后的图片是否是矩形，否则跟随裁剪框的形状
    private boolean showCamera = true;              //显示相机
    private int selectLimit = 9;                    //最大选择图片数量
    private int outPutX = 800;                      //裁剪保存宽度
    private int outPutY = 800;                      //裁剪保存高度
    private int focusWidth = 280;                   //焦点框的宽度
    private int focusHeight = 280;                  //焦点框的高度

    private ImageLoader mImageLoader;               //图片加载器
    @CropImageView.Style
    private int style = CropImageView.RECTANGLE;    //裁剪框的形状
    private File cropCacheFolder;
    private File pickImageFile;

    private ArrayList<ImageItem> mSelectedImages = new ArrayList<>();   //选中的图片集合
    private List<ImageFolder> mImageFolders; //所有的图片文件夹
    private int mCurrentImageFolderPosition = 0;    //当前选中的文件夹位置， 0表示所有图片
    private List<OnImageSelectedListener> mImageSelectedListeners;   //图片选中的监听回调

    private static ImagePicker mInstance;

    private ImagePicker() {
    }

    public static ImagePicker getInstance() {
        if (mInstance == null) {
            synchronized (ImagePicker.class) {
                if (mInstance == null) {
                    mInstance = new ImagePicker();
                }
            }
        }
        return mInstance;
    }

    public boolean isMultiMode() {
        return multiMode;
    }

    public void setMultiMode(boolean multiMode) {
        this.multiMode = multiMode;
    }

    public boolean isCrop() {
        return crop;
    }

    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    public boolean isSaveRectangle() {
        return saveRectangle;
    }

    public void setSaveRectangle(boolean saveRectangle) {
        this.saveRectangle = saveRectangle;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public int getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(int selectLimit) {
        this.selectLimit = selectLimit;
    }

    public int getOutPutX() {
        return outPutX;
    }

    public void setOutPutX(int outPutX) {
        this.outPutX = outPutX;
    }

    public int getOutPutY() {
        return outPutY;
    }

    public void setOutPutY(int outPutY) {
        this.outPutY = outPutY;
    }

    public int getFocusWidth() {
        return focusWidth;
    }

    public void setFocusWidth(int focusWidth) {
        this.focusWidth = focusWidth;
    }

    public int getFocusHeight() {
        return focusHeight;
    }

    public void setFocusHeight(int focusHeight) {
        this.focusHeight = focusHeight;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(@CropImageView.Style int style) {
        this.style = style;
    }

    public File getCropCacheFolder(Context context) {
        if (cropCacheFolder == null) {
            cropCacheFolder = new File(context.getCacheDir() + "/ImagePicker/cropTemp/");
        }
        return cropCacheFolder;
    }

    public void setCropCacheFolder(File cropCacheFolder) {
        this.cropCacheFolder = cropCacheFolder;
    }

    public File getPickImageFile() {
        return pickImageFile;
    }

    public void setPickImageFile(File pickImageFile) {
        this.pickImageFile = pickImageFile;
    }

    public List<ImageFolder> getImageFolders() {
        return mImageFolders;
    }

    public void setImageFolders(ArrayList<ImageFolder> imageFolders) {
        mImageFolders = imageFolders;
    }

    public int getCurrentImageFolderPosition() {
        return mCurrentImageFolderPosition;
    }

    public void setCurrentImageFolderPosition(int currentImageFolderPosition) {
        mCurrentImageFolderPosition = currentImageFolderPosition;
    }

    public ArrayList<ImageItem> getCurrentImageFolderItems() {
        return mImageFolders.get(mCurrentImageFolderPosition).images;
    }

    public boolean isSelect(ImageItem imageItem) {
        return mSelectedImages.contains(imageItem);
    }

    public int getSelectImageCount() {
        if (mSelectedImages == null) {
            return 0;
        }
        return mSelectedImages.size();
    }

    public ArrayList<ImageItem> getSelectedImages() {
        return mSelectedImages;
    }

    public void clearSelectedImages() {
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
    }

    public void clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners.clear();
            mImageSelectedListeners = null;
        }
        if (mImageFolders != null) {
            mImageFolders.clear();
            mImageFolders = null;
        }
        if (mSelectedImages != null) {
            mSelectedImages.clear();
        }
        mCurrentImageFolderPosition = 0;
    }

    /**
     * 拍照的方法
     */
    public void takePicture(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            if (Utils.existSDCard()) {
                pickImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            } else {
                pickImageFile = Environment.getDataDirectory();
            }
            pickImageFile = createFile(pickImageFile, "IMG_", ".jpg");
            if (pickImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.parse("file://" + pickImageFile.getAbsolutePath()));
            }
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 扫描图片
     */
    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse("file://" + file.getAbsolutePath());
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 图片选中的监听
     */
    public interface OnImageSelectedListener {
        void onImageSelected(int position, ImageItem imageItem, boolean isAdd);
    }

    public void addOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) {
            mImageSelectedListeners = new ArrayList<>();
        }
        mImageSelectedListeners.add(l);
    }

    public void removeOnImageSelectedListener(OnImageSelectedListener l) {
        if (mImageSelectedListeners == null) {
            return;
        }
        mImageSelectedListeners.remove(l);
    }

    public void addSelectedImageItem(int position, ImageItem item, boolean isAdd) {
        if (isAdd) {
            mSelectedImages.add(item);
        } else {
            mSelectedImages.remove(item);
        }
        notifyImageSelectedChanged(position, item, isAdd);
    }

    private void notifyImageSelectedChanged(int position, ImageItem item, boolean isAdd) {
        if (mImageSelectedListeners == null) {
            return;
        }
        for (OnImageSelectedListener l : mImageSelectedListeners) {
            l.onImageSelected(position, item, isAdd);
        }
    }
}