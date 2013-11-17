/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;
import android.graphics.Color;
import android.util.Slog;
import android.os.Environment;

import com.android.systemui.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import libcore.icu.ICU;

public class DateView extends TextView {
    private static final String TAG = "DateView";

    private final Date mCurrentTime = new Date();

    private SimpleDateFormat mDateFormat;
    private String mLastText;

    public static final String THEME_DIRECTORY = "/theme/notification/";
    public static final String CONFIGURATION_FILE = "notification.conf";
    public static final String DATE_COLOR = "color.date";
    private final String mFilePath;
    private Properties prop;
    private String mColor = null;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                if (Intent.ACTION_LOCALE_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    // need to get a fresh date format
                    mDateFormat = null;
                }
                updateClock();
            }
        }
    };

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFilePath = Environment.getDataDirectory() + THEME_DIRECTORY + CONFIGURATION_FILE;
        loadConf(mFilePath, DATE_COLOR);
    }

    private void loadConf(String filePath, String propertyName) {
        prop = new Properties();
        try {
            prop.load(new FileInputStream(filePath));
            mColor = prop.getProperty(propertyName);
        } catch (IOException e) {
            mColor = null;
            return;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        mContext.registerReceiver(mIntentReceiver, filter, null, null);

        updateClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mDateFormat = null; // reload the locale next time
        mContext.unregisterReceiver(mIntentReceiver);
    }

    protected void updateClock() {
        if (mDateFormat == null) {
            final String dateFormat = getContext().getString(R.string.system_ui_date_pattern);
            final Locale l = Locale.getDefault();
            final String fmt = ICU.getBestDateTimePattern(dateFormat, l.toString());
            mDateFormat = new SimpleDateFormat(fmt, l);
        }

        if(null != mColor) {
            int color = (int)(Long.parseLong(mColor, 16));
            setTextColor(color);
        }

        mCurrentTime.setTime(System.currentTimeMillis());

        final String text = mDateFormat.format(mCurrentTime);
        if (!text.equals(mLastText)) {
            setText(text);
            mLastText = text;
        }
    }
}
