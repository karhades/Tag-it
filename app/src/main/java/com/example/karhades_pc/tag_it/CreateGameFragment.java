package com.example.karhades_pc.tag_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class CreateGameFragment extends Fragment {

    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    public static final String EXTRA_POSITION = "com.example.karhades_pc.tag_it.position";

    private ArrayList<NfcTag> nfcTags;

    private ActionButton actionButton;
    private RecyclerView recyclerView;
    private LinearLayout emptyLinearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcTags = MyTags.get(getActivity()).getNfcTags();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW) {
            if (resultCode == Activity.RESULT_OK) {
                recyclerView.getAdapter().notifyItemInserted(nfcTags.size());
            }
        } else if (requestCode == REQUEST_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                recyclerView.getAdapter().notifyItemChanged(data.getIntExtra(EXTRA_POSITION, -1));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        emptyLinearLayout = (LinearLayout) view.findViewById(R.id.create_empty_linear_layout);

        setupRecyclerView(view);
        setupFloatingActionButton(view);

        // Listen for list changes to hide or show widgets.
        MyTags.get(getActivity()).setOnListChangeListener(new MyTags.onListChangeListener() {
            @Override
            public void onListChanged() {
                hideRecyclerViewIfEmpty();
            }
        });
        hideRecyclerViewIfEmpty();

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.create_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new RiddleAdapter());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If scrolling down (dy > 0). The faster the scrolling
                // the bigger the dy.
                if (dy > 0) {
                    actionButton.hide();
                } else if (dy < -15) {
                    actionButton.show();
                }
            }
        });
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
//                actionButton.hide();
//                actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);

                // Start CreateTagActivity.
                Intent intent = new Intent(getActivity(), CreateTagActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    private void hideRecyclerViewIfEmpty() {
        if (nfcTags.size() == 0) {
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

        startupAnimation();
    }

    private void startupAnimation() {
        // Floating Action Button animation on show after a period of time.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (actionButton.isHidden()) {
                    actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
                    actionButton.show();
                    actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
                }
            }
        }, 750);
    }

    /**
     * TODO
     */
    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        // Create new views (invoked by the layout manager).
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //Log.d("CreateGameFragment", "onCreateViewHolder called");
            View view;
            if (Build.VERSION.SDK_INT < 21)
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_pre_lollipop, viewGroup, false);
            else
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_game_fragment, viewGroup, false);

            return new RiddleHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager).
        @Override
        public void onBindViewHolder(RiddleHolder riddleHolder, int position) {
            //Log.d("CreateGameFragment", "onBindViewHolder called " + position);
            NfcTag nfcTag = nfcTags.get(position);
            riddleHolder.bindRiddle(nfcTag);
        }

        @Override
        public int getItemCount() {
            return nfcTags.size();
        }
    }

    /**
     * TODO
     */
    private class RiddleHolder extends RecyclerView.ViewHolder {
        private NfcTag nfcTag;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private ImageButton moreImageButton;

        public RiddleHolder(final View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start CreateTagPagerActivity.
                    Intent intent = new Intent(getActivity(), CreateTagPagerActivity.class);
                    intent.putExtra(CreateTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
                    startActivityForResult(intent, REQUEST_EDIT);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClickListener.onItemLongClicked();

                    // TODO: Select the NfcTags.

                    MainActivity.setOnContexDeleteListener(new MainActivity.OnContextDeleteListener() {
                        @Override
                        public void onContextDeleted() {
                            NfcTag nfcTag = nfcTags.get(getAdapterPosition());
                            Toast.makeText(getActivity(), "Deleted items: " + nfcTag.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }
            });

            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            imageView = (ImageView) view.findViewById(R.id.row_create_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_create_title_text_view);
            titleTextView.setTypeface(typefaceBold);

            difficultyTextView = (TextView) view.findViewById(R.id.row_create_difficulty_text_view);
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));

            moreImageButton = (ImageButton) view.findViewById(R.id.row_create_more_image_view);
            moreImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupPopupWindow(moreImageButton);
                }
            });
        }

        public void bindRiddle(final NfcTag nfcTag) {
            this.nfcTag = nfcTag;
            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
        }

        private void setupPopupWindow(View view) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window, null);

            final PopupWindow popupWindow = new PopupWindow();
            popupWindow.setContentView(layout);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);

            TextView deleteTextView = (TextView) layout.findViewById(R.id.popup_delete_text_view);
            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete the selected NfcTag.
                    MyTags.get(getActivity()).deleteNfcTag(nfcTag);

                    // Refresh the list.
                    recyclerView.getAdapter().notifyItemRemoved(getAdapterPosition());

                    popupWindow.dismiss();
                }
            });

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= 21) {
                popupWindow.setElevation(24);
            }
            popupWindow.showAsDropDown(view, 25, -265);
        }
    }

    private static OnItemLongClickListener onItemLongClickListener;

    public static void setOnItemLongClickListener(OnItemLongClickListener newOnItemLongClickListener) {
        onItemLongClickListener = newOnItemLongClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked();
    }
}