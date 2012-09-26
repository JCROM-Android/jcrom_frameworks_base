
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
import android.view.WindowManager;
import android.view.Display;
import android.view.Surface;

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

            Drawable drawable = null;
            if (requiresRotation()) {
                drawable = getDrawableFromFile("lockscreen", "lockscreen_wallpaper_land.png");
                if (drawable == null) {
                    drawable = getDrawableFromFile("lockscreen", "lockscreen_wallpaper.png");
                }
            } else {
                drawable = getDrawableFromFile("lockscreen", "lockscreen_wallpaper.png");
            }

            if ( null != drawable ) {
                mLockScreenWallpaperImage.setImageDrawable(drawable);
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

    public boolean requiresRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display dp = wm.getDefaultDisplay();
        return dp.getRotation()==Surface.ROTATION_90 || dp.getRotation()==Surface.ROTATION_270;
    }

    public Drawable getDrawableFromFile(String DIR, String MY_FILE_NAME) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.getDataDirectory().toString() + "/theme/"+DIR+"/");
        builder.append(File.separator);
        builder.append(MY_FILE_NAME);
        String filePath = builder.toString();
        bitmapWallpaper = BitmapFactory.decodeFile(filePath);
        Drawable d = new BitmapDrawable(getResources(), bitmapWallpaper);
        return d;
    }
}
