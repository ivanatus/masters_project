package com.example.traffic_analysis_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    private static final String ARG_TEXT = "argText", TITLE_TEXT = "TitleText";

    public static InfoFragment newInstance(String text, String title) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putString(TITLE_TEXT, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        // Retrieve the text from the arguments
        String text = getArguments().getString(ARG_TEXT);
        String title = getArguments().getString(TITLE_TEXT);

        // Set the text on the TextViews
        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText(title);
        TextView infoText = view.findViewById(R.id.infoText);
        infoText.setText(text);

        return view;
    }
}