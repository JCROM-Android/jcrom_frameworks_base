/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.systemui.R;

/**
 * The view to manage the header area in the expanded status bar.
 */
public class StatusBarHeaderView extends RelativeLayout {

    private boolean mExpanded;
    private View mBackground;
    private ViewGroup mSystemIconsContainer;
    private View mDateTime;
    private View mKeyguardCarrierText;

    private int mCollapsedHeight;
    private int mExpandedHeight;
    private int mKeyguardHeight;

    private boolean mKeyguardShowing;

    public StatusBarHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackground = findViewById(R.id.background);
        mSystemIconsContainer = (ViewGroup) findViewById(R.id.system_icons_container);
        mDateTime = findViewById(R.id.datetime);
        mKeyguardCarrierText = findViewById(R.id.keyguard_carrier_text);
        loadDimens();
    }

    private void loadDimens() {
        mCollapsedHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        mExpandedHeight = getResources().getDimensionPixelSize(
                R.dimen.status_bar_header_height_expanded);
        mKeyguardHeight = getResources().getDimensionPixelSize(
                R.dimen.status_bar_header_height_keyguard);
    }

    public int getCollapsedHeight() {
        return mKeyguardShowing ? mKeyguardHeight : mCollapsedHeight;
    }

    public int getExpandedHeight() {
        return mExpandedHeight;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        updateHeights();
    }

    private void updateHeights() {
        int height;
        if (mExpanded) {
            height = mExpandedHeight;
        } else if (mKeyguardShowing) {
            height = mKeyguardHeight;
        } else {
            height = mCollapsedHeight;
        }
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            setLayoutParams(lp);
        }
        int systemIconsContainerHeight = mKeyguardShowing ? mKeyguardHeight : mCollapsedHeight;
        lp = mSystemIconsContainer.getLayoutParams();
        if (lp.height != systemIconsContainerHeight) {
            lp.height = systemIconsContainerHeight;
            mSystemIconsContainer.setLayoutParams(lp);
        }
    }

    public void setExpansion(float height) {
        if (height < mCollapsedHeight) {
            height = mCollapsedHeight;
        }
        if (height > mExpandedHeight) {
            height = mExpandedHeight;
        }
        if (mExpanded) {
            mBackground.setTranslationY(-(mExpandedHeight - height));
        } else {
            mBackground.setTranslationY(0);
        }
    }

    public View getBackgroundView() {
        return mBackground;
    }

    public void attachSystemIcons(LinearLayout systemIcons) {
        mSystemIconsContainer.addView(systemIcons);
    }

    public void setKeyguardShowing(boolean keyguardShowing) {
        mKeyguardShowing = keyguardShowing;
        mBackground.setVisibility(keyguardShowing ? View.INVISIBLE : View.VISIBLE);
        mDateTime.setVisibility(keyguardShowing ? View.INVISIBLE : View.VISIBLE);
        mKeyguardCarrierText.setVisibility(keyguardShowing ? View.VISIBLE : View.GONE);
        if (keyguardShowing) {
            setZ(0);
        } else {
            setTranslationZ(0);
        }
        updateHeights();
    }
}