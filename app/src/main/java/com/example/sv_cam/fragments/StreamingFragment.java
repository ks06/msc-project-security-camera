package com.example.sv_cam.fragments;

import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getSupportedResolutions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSource;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSourceConfiguration;
import com.amazonaws.regions.Regions;
import com.example.sv_cam.R;


public class StreamingFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private AndroidCameraMediaSource mCameraMediaSource;
    private KinesisVideoClient mKinesisVideoClient;
    private final String mStreamName = AWSMobileClient.getInstance().getUsername() + "-Stream";

    private static final String TAG = StreamingFragment.class.getSimpleName();




    public StreamingFragment() {
        // Required empty public constructor
    }

    public static StreamingFragment newInstance(Activity activity) {
        return new StreamingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{android.Manifest.permission.CAMERA}, 12);
        }
        final View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        TextureView textureView = view.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        return view;
    }

    private void startStreaming(final SurfaceTexture previewTexture) {
        try {
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    Regions.EU_WEST_2,
                    AWSMobileClient.getInstance());

            mCameraMediaSource = (AndroidCameraMediaSource) mKinesisVideoClient
                    .createMediaSource(mStreamName, getCurrentConfiguration());

            mCameraMediaSource.setPreviewSurfaces(new Surface(previewTexture));

            mCameraMediaSource.start();
        } catch (KinesisVideoException e) {
            Log.e(TAG, "unable to start streaming");
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        resumeStreaming();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseStreaming();
    }


    private void resumeStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }
            mCameraMediaSource.start();
        } catch (KinesisVideoException e) {
            Log.e(TAG, "unable to resume streaming");
            e.printStackTrace();
        }
    }

    private void pauseStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }
            mCameraMediaSource.stop();
        } catch (KinesisVideoException e) {
            Log.e(TAG, "unable to pause streaming");
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        surfaceTexture.setDefaultBufferSize(width, height);
        startStreaming(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        try {
            mCameraMediaSource.stop();
            mKinesisVideoClient.stopAllMediaSources();
            KinesisVideoAndroidClientFactory.freeKinesisVideoClient();
        } catch (KinesisVideoException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

    private AndroidCameraMediaSourceConfiguration getCurrentConfiguration() {
        return new AndroidCameraMediaSourceConfiguration(
                AndroidCameraMediaSourceConfiguration.builder()
                        .withCameraId(getBackFacingCameraId())
                        .withEncodingMimeType("video/avc")
                        .withHorizontalResolution(getSupportedResolutions(getActivity(), getBackFacingCameraId()).get(0).getWidth())
                        .withVerticalResolution(getSupportedResolutions(getActivity(), getBackFacingCameraId()).get(0).getHeight())
                        .withCameraFacing(CameraCharacteristics.LENS_FACING_BACK)
                        .withIsEncoderHardwareAccelerated(
                                true)
                        .withFrameRate(20)
                        .withRetentionPeriodInHours(48)
                        .withEncodingBitRate(384 * 1024)
                        .withCameraOrientation(0)
                        .withNalAdaptationFlags(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
                        .withIsAbsoluteTimecode(false));
    }


    private String getBackFacingCameraId() {
        try {
            CameraManager camManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            for(String cameraId : camManager.getCameraIdList()){
                CameraCharacteristics chars = camManager.getCameraCharacteristics(cameraId);
                int camOrientation = chars.get(CameraCharacteristics.LENS_FACING);
                if (camOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "failed to fetch back facing camera ID");
            e.printStackTrace();
        }
        return null;
    }


}