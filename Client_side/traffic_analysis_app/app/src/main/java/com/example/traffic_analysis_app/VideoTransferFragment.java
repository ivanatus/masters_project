package com.example.traffic_analysis_app;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoTransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoTransferFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnVideoTransferListener videoTransferListener;

    public interface OnVideoTransferListener {
        void onVideoTransferProgress(String progress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoTransferListener) {
            videoTransferListener = (OnVideoTransferListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVideoTransferListener");
        }
    }

    public VideoTransferFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoTransferFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoTransferFragment newInstance() {
        VideoTransferFragment fragment = new VideoTransferFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_transfer, container, false);

        Button ok_button = view.findViewById(R.id.transfer_button);
        ok_button.setOnClickListener(v -> {
            // Dismiss the dialog and proceed with the main activity content
            dismiss();
        });

        return view;
    }

    public void updateProgressText(String progress) {
        if (getView() != null) {
            TextView transfer_text = getView().findViewById(R.id.transfer_text);
            Button ok_button = getView().findViewById(R.id.transfer_button);
            transfer_text.setText(progress);
            if(!progress.equals("Snimka se obrađuje, molim pričekajte...")){
                ok_button.setVisibility(View.VISIBLE);
            } else {
                ok_button.setVisibility(View.GONE);
            }
        }
    }
}