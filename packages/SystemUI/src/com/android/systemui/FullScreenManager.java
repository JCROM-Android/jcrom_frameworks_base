/*
 * Copyright (C) 2013 Japanese Custom ROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PanelBar;

import android.widget.FrameLayout;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.drawable.Drawable;
import android.view.Surface;
import android.os.Environment;
import java.io.File;
import android.view.ViewGroup;
import com.android.systemui.R;
import android.graphics.drawable.PaintDrawable;
import android.graphics.Color;

import java.util.List;

public class FullScreenManager {

    private static final String TAG = FullScreenManager.class.getSimpleName();

    NavigationBarView mNavbar;
    PanelBar mStatusbar;

    final Context mContext;

    Handler mHandler = new Handler();

    boolean mIsHomeShowing;
    boolean mIsKeyguardShowing;

    KeyguardManager km;
    ActivityManager am;

    Drawable mStatusBarDrawable = null;
    Drawable mStatusBarLandDrawable = null;
    Drawable mFullStatusBarDrawable = null;
    Drawable mFullStatusBarLandDrawable = null;

    Drawable mNaviBarDrawable = null;
    Drawable mNaviBarLandDrawable = null;
    Drawable mFullNaviBarDrawable = null;
    Drawable mFullNaviBarLandDrawable = null;

    private int mState1 = 0;
    private int mState2 = 0;

    String forceHobby = null;
    String fullWallpaper = null;

    private final Runnable updateTransparencyRunnable = new Runnable() {
        @Override
        public void run() {
            doTransparentUpdate();
        }
    };

    public FullScreenManager(Context context) {
        mContext = context;

        forceHobby = SystemProperties.get("persist.sys.force.hobby");
        fullWallpaper = SystemProperties.get("persist.sys.full.wallpaper");

        mState1 = 0;
        mState2 = 0;

        km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        if(checkFullWallpaper()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentFilter.addAction("android.intent.action.JcromFullScreen");
            
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals("android.intent.action.JcromFullScreen")) {
                        String check_home = intent.getStringExtra("check_home");
                        if(check_home.equals("true")) {
                            mIsHomeShowing = true;
                        }else {
                            mIsHomeShowing = false;
                        }
                        update();
                    }else if(action.equals(Intent.ACTION_SCREEN_OFF)) {
                        update();
                    }else if(action.equals(Intent.ACTION_USER_PRESENT)) {
                        update();
                    }
                }
            }, intentFilter);

            prepareStatusBarBackground();
            prepareNaviBarBackground();
        }
    }

    public boolean checkFullWallpaper() {
        if((fullWallpaper.equals("true")) && (forceHobby.equals("true"))) {
            return true;
        }else {
            return false;
        }
    }

    public void update() {
        if(checkFullWallpaper()) {
            mHandler.removeCallbacks(updateTransparencyRunnable);
            mHandler.postDelayed(updateTransparencyRunnable, 200);
        }
    }

    public void setNavbar(NavigationBarView n) {
        mNavbar = n;
    }

    public void setStatusbar(PanelBar s) {
        mStatusbar = s;
    }

    public void setTempDisableStatusbarState(boolean state) {
    }

    public void setTempNavbarState(boolean state) {
    }


    private void setStatusbarBackground() {

        FrameLayout f = (FrameLayout) mStatusbar.findViewById(R.id.status_bar_background);
        PaintDrawable paintDrawable = new PaintDrawable(Color.argb(0,0,0,0));

        if(mIsHomeShowing && !mIsKeyguardShowing) {
            if (requiresRotation()) {
                if(mFullStatusBarLandDrawable != null){
                    f.setBackgroundDrawable(mFullStatusBarLandDrawable);
                }else if(mFullStatusBarDrawable != null){
                    f.setBackgroundDrawable(mFullStatusBarDrawable);
                }else if(mStatusBarLandDrawable != null){
                    f.setBackgroundDrawable(mStatusBarLandDrawable);
                }else if(mStatusBarDrawable != null){
                    f.setBackgroundDrawable(mStatusBarDrawable);
                }else{
                    f.setBackgroundColor(0xff000000);
                }
            }else{
                if(mFullStatusBarDrawable != null){
                    f.setBackgroundDrawable(mFullStatusBarDrawable);
                }else if(mStatusBarDrawable != null){
                    f.setBackgroundDrawable(mStatusBarDrawable);
                }else{
                    f.setBackgroundColor(0xff000000);
                }
            }
        }else {
            if (requiresRotation()) {
                if(mStatusBarLandDrawable != null){
                    f.setBackgroundDrawable(mStatusBarLandDrawable);
                }else if(mStatusBarDrawable != null){
                    f.setBackgroundDrawable(mStatusBarDrawable);
                }else{
                    f.setBackgroundColor(0xff000000);
                }
            }else{
                if(mStatusBarDrawable != null){
                    f.setBackgroundDrawable(mStatusBarDrawable);
                }else{
                    f.setBackgroundColor(0xff000000);
                }
            }
        }
    }

    private void setNavbarBackground() {

        View[] mRotatedViews = new View[4];

        mRotatedViews[Surface.ROTATION_0] =
        mRotatedViews[Surface.ROTATION_180] = mNavbar.findViewById(R.id.rot0);

        mRotatedViews[Surface.ROTATION_90] = mNavbar.findViewById(R.id.rot90);

        boolean NAVBAR_ALWAYS_AT_RIGHT = true;
        mRotatedViews[Surface.ROTATION_270] = NAVBAR_ALWAYS_AT_RIGHT
                    ? mNavbar.findViewById(R.id.rot90)
                    : mNavbar.findViewById(R.id.rot270);

        for (View v : mRotatedViews) {
            ViewGroup group = (ViewGroup) v.findViewById(R.id.nav_buttons);
            group.setMotionEventSplittingEnabled(false);
        }

        FrameLayout f_port = (FrameLayout) mRotatedViews[Surface.ROTATION_0];
        FrameLayout f_land = (FrameLayout) mRotatedViews[Surface.ROTATION_90];
        PaintDrawable paintDrawable = new PaintDrawable(Color.argb(0,0,0,0));

        if (mIsHomeShowing && !mIsKeyguardShowing) {
            {//port
                if (mFullNaviBarDrawable != null) {
                    f_port.setBackgroundDrawable(mFullNaviBarDrawable);
                }else if (mNaviBarDrawable != null) {
                    f_port.setBackgroundDrawable(mNaviBarDrawable);
                }else{
                    f_port.setBackgroundColor(0xff000000);
                }
            }
            {//land
                if (mFullNaviBarLandDrawable != null) {
                    f_land.setBackgroundDrawable(mFullNaviBarLandDrawable);
                }else if (mNaviBarLandDrawable != null) {
                    f_land.setBackgroundDrawable(mNaviBarLandDrawable);
                }else{
                    f_land.setBackgroundColor(0xff000000);
                }
            }
        }else {
            {//port
                if (mNaviBarDrawable != null) {
                    f_port.setBackgroundDrawable(mNaviBarDrawable);
                }else{
                    f_port.setBackgroundColor(0xff000000);
                }
            }
            {//land
                if (mNaviBarLandDrawable != null) {
                    f_land.setBackgroundDrawable(mNaviBarLandDrawable);
                }else{
                    f_land.setBackgroundColor(0xff000000);
                }
            }
        }
    }

    private void doTransparentUpdate() {
        mIsKeyguardShowing = isKeyguardShowing();

        if(homeCheck()) {
            mIsHomeShowing = true;
        }else {
            mIsHomeShowing = false;
        }

        if(!isLauncherShowing()) {
            mIsHomeShowing = false;
        }

        if(null != mStatusbar) {
            prepareStatusBarBackground();
            setStatusbarBackground();
        }
        if(null != mNavbar) {
            prepareNaviBarBackground();
            setNavbarBackground();            
        }

    }

    private boolean isLauncherShowing() {
        try {
            final List<ActivityManager.RecentTaskInfo> recentTasks = am
                    .getRecentTasksForUser(
                            1, ActivityManager.RECENT_WITH_EXCLUDED,
                            UserHandle.CURRENT.getIdentifier());
            if (recentTasks.size() > 0) {
                ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(0);
                Intent intent = new Intent(recentInfo.baseIntent);
                if (recentInfo.origActivity != null) {
                    intent.setComponent(recentInfo.origActivity);
                }
                if (isCurrentHomeActivity(intent.getComponent(), null)) {
                    return true;
                }
            }
        } catch(Exception ignore) {
        }
        return false;
    }

    private boolean isKeyguardShowing() {
        if (km == null)
            return false;
        return km.isKeyguardLocked();
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    .resolveActivityInfo(mContext.getPackageManager(), 0);
        }
        return homeInfo != null
                && homeInfo.packageName.equals(component.getPackageName())
                && homeInfo.name.equals(component.getClassName());
    }



    public boolean requiresRotation() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display dp = wm.getDefaultDisplay();

        return dp.getRotation()==Surface.ROTATION_90 || dp.getRotation()==Surface.ROTATION_270;
    }

    public void prepareStatusBarBackground() {
        mStatusBarDrawable = getDrawableFromFile("statusbar", "status_bar_background");
        mStatusBarLandDrawable = getDrawableFromFile("statusbar", "status_bar_background_land");
        mFullStatusBarDrawable = getDrawableFromFile("wallpaper", "full_status_bar_background" + getFullWallpaperString());
        mFullStatusBarLandDrawable = getDrawableFromFile("wallpaper", "full_status_bar_background" + getFullWallpaperString() + "_land");
    }

    public void prepareNaviBarBackground() {
        mNaviBarDrawable = getDrawableFromFile("navibar", "navibar_background_port");
        mNaviBarLandDrawable = getDrawableFromFile("navibar", "navibar_background_land");
        mFullNaviBarDrawable = getDrawableFromFile("wallpaper", "full_navibar_background" + getFullWallpaperString() + "_port");
        mFullNaviBarLandDrawable = getDrawableFromFile("wallpaper", "full_navibar_background" + getFullWallpaperString() + "_land");
    }

    public Drawable getDrawableFromFile(String DIR, String MY_FILE_NAME) {
        StringBuilder builder = new StringBuilder();
        builder.append(Environment.getDataDirectory().toString() + "/theme/"+DIR+"/");
        builder.append(File.separator);
        builder.append(MY_FILE_NAME);
        String filePath = builder.toString();
        String extension = checkThemeFile(filePath);
        return Drawable.createFromPath(filePath + extension);
    }

    private String checkThemeFile(String filename) {
        String extension = ".png";
        File file = null;

        file = new File(filename + ".png");
        if(file.exists()) {
            extension = ".png";
        }else {
            file = new File(filename + ".jpg");
            if(file.exists()) {
                extension = ".jpg";
            }
        }

        return extension;
    }

    private boolean homeCheck() {
        boolean flg = false;

        try{
            Context ctxt = mContext.createPackageContext("com.android.launcher", 0);
            SharedPreferences pref =
                ctxt.getSharedPreferences("jcrom_home", Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE|Context.MODE_MULTI_PROCESS);
            flg = pref.getBoolean("home", false);
        }catch (Exception e){
            e.printStackTrace();
        }

        return flg;
    }

    private String getFullWallpaperString() {
        boolean battery = false;
        boolean time = false;
        String fullString = "";

        try{
            Context ctxt = mContext.createPackageContext("net.jcrom.jcwallpaper", 0);
            SharedPreferences pref =
                ctxt.getSharedPreferences("full_home", Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE|Context.MODE_MULTI_PROCESS);
            battery = pref.getBoolean("battery", false);
            time = pref.getBoolean("time", false);

            if(battery) {
                if(time) {
                    fullString = "_time_battery";
                }else {
                    fullString = "_battery";
                }
            }else {
                if(time) {
                    fullString = "_time";
                }else {
                    fullString = "";
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return fullString;
    }

}
