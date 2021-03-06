package com.karhades.tag_it.main.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.karhades.tag_it.main.controller.fragment.TrackTagFragment;
import com.karhades.tag_it.main.model.NfcTag;

import java.util.List;

/**
 * FragmentStatePagerAdapter that handles fragment paging for a ViewPager.
 */
public class TrackTagPagerAdapter extends FragmentStatePagerAdapter {

    private List<NfcTag> mNfcTags;
    private TrackTagFragment fragment;

    public TrackTagPagerAdapter(FragmentManager fragmentManager, List<NfcTag> nfcTags) {
        super(fragmentManager);
        mNfcTags = nfcTags;
    }

    @Override
    public Fragment getItem(int position) {
        NfcTag nfcTag = mNfcTags.get(position);

        return TrackTagFragment.newInstance(nfcTag.getTagId());
    }

    @Override
    public int getCount() {
        return (mNfcTags == null) ? 0 : mNfcTags.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        fragment = (TrackTagFragment) object;
    }

    public TrackTagFragment getCurrentFragment() {
        return fragment;
    }
}
