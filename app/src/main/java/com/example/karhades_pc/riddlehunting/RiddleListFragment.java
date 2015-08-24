package com.example.karhades_pc.riddlehunting;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListFragment extends ListFragment {

    private ArrayList<Riddle> riddles;

    private ActionButton actionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);

        getActivity().setTitle(R.string.riddles_title);

        riddles = MyRiddles.get(getActivity()).getRiddles();

        RiddleAdapter riddleAdapter = new RiddleAdapter(riddles);
        setListAdapter(riddleAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riddle_list, container, false);

        setupFloatingActionButton(view);

        return view;
    }

    private void setupFloatingActionButton(View view)
    {
        actionButton = (ActionButton) view.findViewById(R.id.floating_action_button);
        actionButton.setButtonColor(getResources().getColor(R.color.colorPrimary));
        actionButton.setButtonColorPressed(getResources().getColor(R.color.colorPrimaryDark));
        actionButton.setImageResource(R.mipmap.puzzle);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Floating action button pressed!", Toast.LENGTH_SHORT).show();
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_riddle_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_item_new_riddle:
                if(actionButton.isHidden())
                    actionButton.show();
                else
                    actionButton.hide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(23)
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Riddle riddle = (Riddle) getListAdapter().getItem(position);

        Intent intent = new Intent(getActivity(), RiddlePagerActivity.class);
        intent.putExtra(RiddleFragment.EXTRA_TAG_ID, riddle.getTagId());
        startActivity(intent);
    }

    // Reloads the List
    @Override
    public void onResume() {
        super.onResume();
        ((RiddleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    // Custom ArrayAdapter class
    private class RiddleAdapter extends ArrayAdapter<Riddle> {
        public RiddleAdapter(ArrayList<Riddle> riddles) {
            super(getActivity(), 0, riddles);
        }

        // Creates the custom view from the layout and returns it as
        // a single view.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_riddle, null);
            }

            Riddle riddle = getItem(position);

            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            // List item Title TextView
            TextView titleTextView = (TextView) convertView.findViewById(R.id.list_item_title_text_view);
            titleTextView.setText(riddle.getTitle());
            titleTextView.setTypeface(typefaceBold);

            // List item Difficulty Label TextView
            TextView difficultyLabelTextView = (TextView) convertView.findViewById(R.id.list_item_difficulty_label_text_view);
            difficultyLabelTextView.setTypeface(typefaceNormal);

            // List item Difficulty TextView
            TextView difficultyTextView = (TextView) convertView.findViewById(R.id.list_item_difficulty_text_view);
            difficultyTextView.setText(riddle.getDifficulty());
            difficultyTextView.setTypeface(typefaceNormal);

            // List item Solved CheckBox
            CheckBox solvedCheckBox = (CheckBox) convertView.findViewById(R.id.list_item_solved_check_box);
            solvedCheckBox.setChecked(riddle.isSolved());

            return convertView;
        }
    }
}
