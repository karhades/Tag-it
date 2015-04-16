package com.example.karhades_pc.riddlehunting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListFragment extends ListFragment {

    private ArrayList<Riddle> riddles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.riddles_title);

        riddles = MyRiddles.get(getActivity()).getRiddles();

        RiddleAdapter riddleAdapter = new RiddleAdapter(riddles);
        setListAdapter(riddleAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Riddle riddle = (Riddle) getListAdapter().getItem(position);

        Intent intent = new Intent(getActivity(), RiddlePagerActivity.class);
        intent.putExtra(RiddleFragment.EXTRA_TAG_ID, riddle.getTagId());
        startActivity(intent);
    }

    //Reloads the List
    @Override
    public void onResume() {
        super.onResume();
        ((RiddleAdapter) getListAdapter()).notifyDataSetChanged();
    }

    //Custom ArrayAdapter class
    private class RiddleAdapter extends ArrayAdapter<Riddle> {
        public RiddleAdapter(ArrayList<Riddle> riddles) {
            super(getActivity(), 0, riddles);
        }

        //Creates the custom view from the layout and returns it as
        //a single view.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_riddle, null);
            }

            Riddle riddle = getItem(position);

            //List item Title TextView
            TextView titleTextView = (TextView) convertView.findViewById(R.id.list_item_title_text_view);
            titleTextView.setText(riddle.getTitle());

            //List item Difficulty TextView
            TextView difficultyTextView = (TextView) convertView.findViewById(R.id.list_item_difficulty_text_view);
            difficultyTextView.setText(riddle.getDifficulty());

            //List item Solved CheckBox
            CheckBox solvedCheckBox = (CheckBox) convertView.findViewById(R.id.list_item_solved_check_box);
            solvedCheckBox.setChecked(riddle.isSolved());

            return convertView;
        }
    }
}
