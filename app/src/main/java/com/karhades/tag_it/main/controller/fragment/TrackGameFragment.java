/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.TrackTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.List;

/**
 * Controller Fragment class that binds the tracking tab with the data set.
 */
public class TrackGameFragment extends Fragment {

    /**
     * Widget references.
     */
    private RecyclerView recyclerView;
    private LinearLayout emptyLinearLayout;

    /**
     * Instance variables.
     */
    private List<NfcTag> nfcTags;
    private NfcTagAdapter adapter;
    private Callbacks callbacks;

    public interface Callbacks {
        void onFragmentResumed(TrackGameFragment fragment);
    }

    public static TrackGameFragment newInstance() {
        return new TrackGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateUi() {
        // Updates the UI.
        nfcTags = MyTags.get(getActivity()).getNfcTags();
        // Refresh the NfcTag list.
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TrackGameFragment.Callbacks interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        callbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_game, container, false);

        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        adapter = new NfcTagAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                hideRecyclerViewIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);

                hideRecyclerViewIfEmpty();
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.tracking_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    private void setupEmptyView(View view) {
        emptyLinearLayout = (LinearLayout) view.findViewById(R.id.tracking_empty_linear_layout);
    }

    private void hideRecyclerViewIfEmpty() {
        boolean shouldHide = nfcTags == null || nfcTags.size() == 0;
        if (shouldHide) {
            recyclerView.setVisibility(View.INVISIBLE);
            emptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        callbacks.onFragmentResumed(this);
    }

    /**
     * Describes the view and it's child views that
     * will bind the data for an adapter item.
     */
    @SuppressWarnings("deprecation")
    private class NfcTagHolder extends RecyclerView.ViewHolder {

        /**
         * Instance variable.
         */
        private NfcTag nfcTag;

        /**
         * Widget references.
         */
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private CheckBox solvedCheckBox;

        /**
         * Constructor that registers any listeners and make calls
         * to findViewById() for each adapter item.
         *
         * @param view The view describing an adapter item (CardView).
         */
        public NfcTagHolder(View view) {
            super(view);

            setupTouchListener(view);
            setupClickListener(view);

            imageView = (ImageView) view.findViewById(R.id.row_tracking_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_tracking_title_text_view);

            difficultyTextView = (TextView) view.findViewById(R.id.row_tracking_difficulty_text_view);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));

            solvedCheckBox = (CheckBox) view.findViewById(R.id.row_tracking_solved_check_box);
        }

        /**
         * Card resting elevation is 2dp and Card raised elevation is 8dp. Animate the changes between them.
         *
         * @param view The CardView to animate.
         */
        private void setupTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                Animator startAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_elevate);
                Animator endAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_rest);

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startAnimator.setTarget(v);
                            startAnimator.start();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            endAnimator.setTarget(v);
                            endAnimator.start();
                            break;
                    }
                    return false;
                }
            });
        }

        private void setupClickListener(View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
                        startTrackingTagPagerActivityWithTransition();
                    }
                    // No transitions.
                    else {
                        startTrackingTagPagerActivity();
                    }
                }
            });
        }

        @TargetApi(21)
        @SuppressWarnings("unchecked")
        private void startTrackingTagPagerActivityWithTransition() {
            Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), getAdapterPosition());
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, imageView.getTransitionName()).toBundle();
            getActivity().startActivity(intent, bundle);
        }

        private void startTrackingTagPagerActivity() {
            Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), getAdapterPosition());
            startActivity(intent);
        }

        /**
         * Helper method for binding data on the adapter's
         * onBindViewHolder() method.
         *
         * @param nfcTag The NfcTag object to bind data to views.
         */
        public void bindNfcTag(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
                imageView.setTransitionName("image" + nfcTag.getTagId());
                imageView.setTag("image" + nfcTag.getTagId());
            }

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);
            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
            solvedCheckBox.setChecked(nfcTag.isSolved());
            if (solvedCheckBox.isChecked()) {
                solvedCheckBox.setVisibility(View.VISIBLE);
            } else {
                solvedCheckBox.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Wraps the data set and creates views for individual items. It's the
     * intermediate that sits between the RecyclerView and the data set.
     */
    private class NfcTagAdapter extends RecyclerView.Adapter<NfcTagHolder> {

        @Override
        public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_track_game_fragment_alt, viewGroup, false);

            return new NfcTagHolder(view);
        }

        @Override
        public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
            NfcTag nfcTag = nfcTags.get(position);
            nfcTagHolder.bindNfcTag(nfcTag);
        }

        @Override
        public int getItemCount() {
            return (nfcTags == null) ? 0 : nfcTags.size();
        }
    }
}
