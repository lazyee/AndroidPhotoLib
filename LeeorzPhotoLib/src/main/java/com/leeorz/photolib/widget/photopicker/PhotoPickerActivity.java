package com.leeorz.photolib.widget.photopicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.immersionbar.ImmersionBar;
import com.leeorz.photolib.R;
import com.leeorz.photolib.widget.photopicker.entity.Photo;
import com.leeorz.photolib.widget.photopicker.event.OnItemCheckListener;
import com.leeorz.photolib.widget.photopicker.fragment.ImagePagerFragment;
import com.leeorz.photolib.widget.photopicker.fragment.PhotoPickerFragment;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class PhotoPickerActivity extends AppCompatActivity implements ImagePagerFragment.AddImagePagerFragment{

  private PhotoPickerFragment pickerFragment;
  private ImagePagerFragment imagePagerFragment;

  public final static String EXTRA_MAX_COUNT     = "MAX_COUNT";
  public final static String EXTRA_SHOW_CAMERA   = "SHOW_CAMERA";
  public final static String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";


  public final static int DEFAULT_MAX_COUNT = 9;
  private int maxCount = DEFAULT_MAX_COUNT;
  private TextView tvDone;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ImmersionBar.with(this).barColor(R.color.header_background).init();
    setContentView(R.layout.activity_photo_picker);

    maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
    boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
    tvDone = findViewById(R.id.tvDone);
    tvDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clickDone();
      }
    });

    findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPickerFragment);
    pickerFragment.getPhotoGridAdapter().setShowCamera(showCamera);
    pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
      @Override public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

        int total = selectedItemCount + (isCheck ? -1 : 1);

        tvDone.setEnabled(total > 0);

        if (maxCount <= 1) {
          List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
          if (!photos.contains(photo)) {
            photos.clear();
            pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
          }
          return true;
        }

        if (total > maxCount) {
          Toast.makeText(getActivity(), getString(R.string.over_max_count_tips, maxCount),
              LENGTH_LONG).show();
          return false;
        }
        tvDone.setText(getString(R.string.done_with_count, total, maxCount));
        return true;
      }
    });

  }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
  @Override public void onBackPressed() {
    if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
      imagePagerFragment.runExitAnimation(new Runnable() {
        public void run() {
          if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            setDoneEnable(pickerFragment.getPhotoGridAdapter().getSelectedPhotos().size() > 0);
          }
        }
      });
    } else {
      super.onBackPressed();
    }
  }


  @Override
  public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
    this.imagePagerFragment = imagePagerFragment;
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, this.imagePagerFragment)
        .addToBackStack(null)
        .commit();
        setDoneEnable(true);
  }

  private void setDoneEnable(boolean isEnable){
      tvDone.setEnabled(isEnable);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * 点击done按钮
   */
  private void clickDone(){
    Intent intent = new Intent();
    ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();

    //获取当前的预览图中的图像并且关闭页面，将数据带回上一个页面
    if(selectedPhotos.isEmpty()){
      selectedPhotos.add(this.imagePagerFragment.getPaths().get(this.imagePagerFragment.getCurrentItem()));
    }
    intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
    setResult(RESULT_OK, intent);
    finish();
  }

  public PhotoPickerActivity getActivity() {
    return this;
  }
}
