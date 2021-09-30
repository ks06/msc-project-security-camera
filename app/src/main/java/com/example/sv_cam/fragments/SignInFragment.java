package com.example.sv_cam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.example.sv_cam.R;
import com.google.android.material.snackbar.Snackbar;


public class SignInFragment extends Fragment {
    private static final String TAG = SignInFragment.class.getSimpleName();


    public SignInFragment() {
        // Required empty public constructor
    }


    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (AWSMobileClient.getInstance().isSignedIn()) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_logInFragment_to_modeSelectionFragment);
        } else {
            AWSMobileClient.getInstance().showSignIn(getActivity(),
                    SignInUIOptions.builder()
                            .logo(R.drawable.svcamlogo_vertical)
                            .backgroundColor(R.color.white)
                            .canCancel(false)
                            .build(),
                    new Callback<UserStateDetails>() {
                        @Override
                        public void onResult(UserStateDetails userStateDetails) {
                            Log.d(TAG, "onResult: User signed-in " + userStateDetails.getUserState());
                        }
                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: ", e);
                            Snackbar.make(getActivity().findViewById(android.R.id.content),"User sign-in error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }
}