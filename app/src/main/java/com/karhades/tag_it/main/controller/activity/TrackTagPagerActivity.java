/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.activity;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.adapter.TrackTagPagerAdapter;
import com.karhades.tag_it.main.controller.fragment.TrackTagFragment;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.List;
import java.util.Map;

/**
 * Controller Activity class that hosts TrackTagFragment and enables paging.
 */
public class TrackTagPagerActivity extends AppCompatActivity implements ViewPager.PageTransformer {

    /**
     * Extras constants.
     */
    private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";
    private static final String EXTRA_CURRENT_TAG_POSITION = "com.karhades.tag_it.current_tag_position";
    private static final String EXTRA_OLD_TAG_POSITION = "com.karhades.tag_it.old_tag_position";

    /**
     * ViewPager.PageTransformer constant.
     */
    private static final float MIN_SCALE = 0.75f;

    /**
     * Widget reference.
     */
    private ViewPager mViewPager;

    /**
     * Instance variables.
     */
    private List<NfcTag> mNfcTags;
    private TrackTagPagerAdapter mTrackTagPagerAdapter;
    private String mTagId;

    /**
     * Transition variables.
     */
    private int mCurrentTagPosition;
    private int mOldTagPosition;
    private boolean mIsReturning;

    public static Intent newIntent(Context context, String tagId, int position) {
        Intent intent = new Intent(context, TrackTagPagerActivity.class);
        intent.putExtra(EXTRA_TAG_ID, tagId);
        intent.putExtra(EXTRA_CURRENT_TAG_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_tag_pager);

        // Gets the NfcTag ID from TrackGameFragment.
        mTagId = getIntent().getStringExtra(EXTRA_TAG_ID);

        // Gets the NFC tags list.
        mNfcTags = MyTags.get(this).getNfcTags();

        setupViewPager();
        setCurrentTagPage();

        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            enableTransitions();

            supportPostponeEnterTransition();
        }
    }

    @SuppressWarnings("deprecation")
    private void setupViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.track_tag_pager_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mTrackTagPagerAdapter = new TrackTagPagerAdapter(fragmentManager, mNfcTags);

        mViewPager.setAdapter(mTrackTagPagerAdapter);
        mViewPager.setPageTransformer(true, this);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // DO NOTHING
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentTagPosition = position;

                NfcTag nfcTag = mNfcTags.get(position);
                setTitle(nfcTag.getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                // DO NOTHING
            }
        });
    }

    private void setCurrentTagPage() {
        NfcTag nfcTag = MyTags.get(this).getNfcTag(mTagId);
        int position = MyTags.get(this).getNfcTagPosition(nfcTag.getTagId());

        if (position != -1) {
            mViewPager.setCurrentItem(position);
        }
    }

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();

        // [-Infinity,-1)
        if (position < -1) {
            // This page is way off-screen to the left.
            page.setAlpha(0);

        }
        // [-1,0]
        else if (position <= 0) {
            // Use the default slide transition when moving to the left page.
            page.setAlpha(1);
            page.setTranslationX(0);
            page.setScaleX(1);
            page.setScaleY(1);

        }
        // (0,1]
        else if (position <= 1) {
            // Fade the page out.
            page.setAlpha(1 - position);

            // Counteract the default slide transition.
            page.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1).
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

        }
        // (1,+Infinity]
        else {
            // This page is way off-screen to the right.
            page.setAlpha(0);
        }
    }

    // Used for transitions.
    @Override
    @TargetApi(21)
    public void finishAfterTransition() {
        if (!TransitionHelper.isTransitionSupportedAndEnabled()) {
            super.finishAfterTransition();
            return;
        }

        TrackTagFragment fragment = mTrackTagPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            mIsReturning = true;
            // Hide the fragment's action button and pass a runnable to run after
            // the animation ends.
            fragment.hideActionButtonOnExit(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_OLD_TAG_POSITION, mOldTagPosition);
                    intent.putExtra(EXTRA_CURRENT_TAG_POSITION, mCurrentTagPosition);
                    setResult(RESULT_OK, intent);

                    TrackTagPagerActivity.super.finishAfterTransition();
                }
            });
        } else {
            super.finishAfterTransition();
        }
    }

    @TargetApi(21)
    private void enableTransitions() {
        mCurrentTagPosition = getIntent().getIntExtra(EXTRA_CURRENT_TAG_POSITION, -1);
        mOldTagPosition = mCurrentTagPosition;

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                // If it's returning to MainActivity (TrackGameFragment).
                if (mIsReturning) {
                    // Get shared view.
                    View sharedView = mTrackTagPagerAdapter.getCurrentFragment().getSharedElement();

                    // If shared view was recycled.
                    if (sharedView == null) {
                        // If shared view is null, then it has likely been scrolled off screen and
                        // recycled. In this case we cancel the shared element transition by
                        // removing the shared elements from the shared elements map.
                        names.clear();
                        sharedElements.clear();
                    }
                    // If user swiped to another tag.
                    else if (mCurrentTagPosition != mOldTagPosition) {
                        // Clear all the previous registrations.
                        names.clear();
                        sharedElements.clear();

                        // Add the correct transition name for the current view.
                        names.add(sharedView.getTransitionName());
                        sharedElements.put(sharedView.getTransitionName(), sharedView);
                    }

                    // If bundle is null, then the activity is exiting.
                    View statusBar = findViewById(android.R.id.statusBarBackground);
                    // Add the NavigationBar to shared elements to avoid blinking.
                    View navigationBar = findViewById(android.R.id.navigationBarBackground);

                    if (statusBar != null) {
                        names.add(statusBar.getTransitionName());
                        sharedElements.put(statusBar.getTransitionName(), statusBar);
                    }
                    if (navigationBar != null) {
                        names.add(navigationBar.getTransitionName());
                        sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                    }
                }
            }
        });
    }
}
