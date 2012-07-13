
package com.android.internal.policy.impl;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.os.SystemProperties;
import android.os.Environment;

class LockscreenWallpaper extends FrameLayout {

    private final String TAG = "LockscreenWallpaperUpdater";

    private ImageView mLockScreenWallpaperImage;

    Bitmap bitmapWallpaper;

    public LockscreenWallpaper(Context context, AttributeSet attrs) {
        super(context);

        setLockScreenWallpaper();
    }

    public void setLockScreenWallpaper() {
        String forceHobby = SystemProperties.get("persist.sys.force.hobby");
        if (forceHobby.equals("true")) {
            mLockScreenWallpaperImage = new ImageView(getContext());
            mLockScreenWallpaperImage.setScaleType(ScaleType.CENTER_CROP);
            addView(mLockScreenWallpaperImage, -1, -1);

            String MY_FRAME_FILE = "lockscreen_wallpaper.png";
            StringBuilder builder = new StringBuilder();
            builder.append(Environment.getDataDirectory().toString() + "/theme/lockscreen/");
            builder.append(File.separator);
            builder.append(MY_FRAME_FILE);
            String filePath = builder.toString();

            bitmapWallpaper = BitmapFactory.decodeFile(filePath);
            Drawable d = new BitmapDrawable(getResources(), bitmapWallpaper);
            if ( null != d ) {
                mLockScreenWallpaperImage.setImageDrawable(d);
            } else {
                removeAllViews();
            }
        } else {
            removeAllViews();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (bitmapWallpaper != null)
            bitmapWallpaper.recycle();

        System.gc();
        super.onDetachedFromWindow();
    }
}
