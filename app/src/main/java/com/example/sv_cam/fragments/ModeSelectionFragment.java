package com.example.sv_cam.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.example.sv_cam.R;


public class ModeSelectionFragment extends Fragment {

    public ModeSelectionFragment() {
        // Required empty public constructor
    }

    public static ModeSelectionFragment newInstance() {
        return new ModeSelectionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mode_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        if (!AWSMobileClient.getInstance().isSignedIn()) {
            navController.navigate(R.id.action_modeSelectionFragment_to_logInFragment);
        }

        Button signOutButton = view.findViewById(R.id.signOut);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AWSMobileClient.getInstance().signOut();
                navController.navigate(R.id.action_modeSelectionFragment_to_logInFragment);
            }
        });


        Button cameraButton = view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_modeSelectionFragment_to_streamingFragment);
            }
        });

        Button viewerButton = view.findViewById(R.id.viewerButton);
        viewerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate((R.id.action_modeSelectionFragment_to_viewStreamingFragment));
            }
        });
    }
}