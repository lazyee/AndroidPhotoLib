package com.lazyee.photolib.photopicker.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lazyee.photolib.R;
import com.lazyee.photolib.entity.Photo;
import com.lazyee.photolib.entity.PhotoDirectory;
import com.lazyee.photolib.photopicker.event.OnItemCheckListener;
import com.lazyee.photolib.photopicker.event.OnPhotoClickListener;
import com.lazyee.photolib.photopicker.util.MediaStoreHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder> {

    private LayoutInflater inflater;

    private Context mContext;

    private OnItemCheckListener onItemCheckListener = null;
    private OnPhotoClickListener onPhotoClickListener = null;
    private View.OnClickListener onCameraClickListener = null;

    public final static int ITEM_TYPE_CAMERA = 100;
    public final static int ITEM_TYPE_PHOTO = 101;

    private boolean hasCamera = true;
    private boolean isSelectSingle = false;

    public PhotoGridAdapter(Context mContext, List<PhotoDirectory> photoDirectories) {
        this.photoDirectories = photoDirectories;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getItemViewType(int position) {
        return (showCamera() && position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_photo, parent, false);
        PhotoViewHolder holder = new PhotoViewHolder(itemView);
        if (viewType == ITEM_TYPE_CAMERA) {
            holder.ivPhoto.setScaleType(ImageView.ScaleType.CENTER);
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onCameraClickListener != null) {
                        onCameraClickListener.onClick(view);
                    }
                }
            });
        }
        return holder;
    }


    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

            List<Photo> photos = getCurrentPhotos();
            final Photo photo;

            if (showCamera()) {
                photo = photos.get(position - 1);
            } else {
                photo = photos.get(position);
            }

            Glide.with(mContext)
                .load(new File(photo.getPath()))
                .centerCrop()
                .placeholder(R.drawable.shape_photo_bg)
                .error(R.drawable.ic_broken_image)
                .thumbnail(0.2f)
                .into(holder.ivPhoto);

            final boolean isChecked = isSelected(photo);


            if(this.isSelectSingle){
                holder.mask.setVisibility(View.GONE);
                holder.ivSelected.setVisibility(View.GONE);

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearSelection();
                        toggleSelection(photo);
                        notifyItemChanged(position);
                        if (onItemCheckListener != null) {
                            onItemCheckListener.OnItemCheck(position, photo, isChecked, getSelectedPhotos().size());

                        }
                    }
                });

            }else{
                holder.mask.setVisibility(isChecked?View.VISIBLE:View.GONE);
                holder.ivSelected.setImageResource(isChecked? R.drawable.ic_picker_checked: R.drawable.ic_picker_check);

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onPhotoClickListener != null) {
                            onPhotoClickListener.onClick(view, position, showCamera());
                        }
                    }
                });

                holder.ivSelected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isEnable = true;
                        if (onItemCheckListener != null) {
                            isEnable = onItemCheckListener.OnItemCheck(position, photo, isChecked,
                                    getSelectedPhotos().size());
                        }
                        if (isEnable) {
                            toggleSelection(photo);
                            notifyItemChanged(position);
                        }
                    }
                });
            }

        } else {
            holder.ivPhoto.setImageResource(R.drawable.camera);
        }
    }


    @Override
    public int getItemCount() {
        int photosCount =
                photoDirectories.size() == 0 ? 0 : getCurrentPhotos().size();
        if (showCamera()) {
            return photosCount + 1;
        }
        return photosCount;
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View mask;
        private ImageView ivSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            mask = itemView.findViewById(R.id.vSelectedMask);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }
    }


    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        this.onItemCheckListener = onItemCheckListener;
    }


    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }


    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
    }


    public ArrayList<String> getSelectedPhotoPaths() {
        ArrayList<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

        for (Photo photo : selectedPhotos) {
            selectedPhotoPaths.add(photo.getPath());
        }

        return selectedPhotoPaths;
    }


    public void setShowCamera(boolean hasCamera) {
        this.hasCamera = hasCamera;
    }

    /**
     * 是否是选择单个图片
     * @param selectSingle
     */
    public void setSelectSingle(boolean selectSingle) {
        isSelectSingle = selectSingle;
    }

    public boolean showCamera() {
        return (hasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }
}
