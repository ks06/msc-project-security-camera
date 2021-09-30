package com.example.sv_cam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient;
import com.amazonaws.services.kinesisvideo.model.APIName;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointRequest;
import com.amazonaws.services.kinesisvideoarchivedmedia.AWSKinesisVideoArchivedMediaClient;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.GetHLSStreamingSessionURLRequest;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.GetHLSStreamingSessionURLResult;
import com.amazonaws.services.kinesisvideoarchivedmedia.model.HLSPlaybackMode;
import com.example.sv_cam.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ViewStreamingFragment extends Fragment implements Player.Listener {

    private final String mStreamName = AWSMobileClient.getInstance().getUsername() + "-Stream";
    private AWSKinesisVideoArchivedMediaClient mAWSKinesisVideoArchivedMediaClient;
    private final GetHLSStreamingSessionURLRequest hlsUrlRequest = new GetHLSStreamingSessionURLRequest();
    private GetHLSStreamingSessionURLResult hlsUrlResult = new GetHLSStreamingSessionURLResult();

    private static final String TAG = ViewStreamingFragment.class.getSimpleName();

    private ProgressBar progressBar;
    private SimpleExoPlayer player;


    public ViewStreamingFragment() {
        // Required empty public constructor
    }


    public static ViewStreamingFragment newInstance(String param1, String param2) {

        return new ViewStreamingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_streaming, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PlayerView playerView = view.findViewById(R.id.videoView);
        progressBar = view.findViewById(R.id.loading);
        buildPlayer();
        playerView.setPlayer(player);

    }

    @Override
    public void onStart() {
        super.onStart();
        getStreamDataEndPoint();
//        String streamingURL = getHlsUrl(streamEndPoint);
//        initializeAndStartPlayer();
    }

    private void getHlsUrl(String streamEndPoint) {
        Single<String> observable = Single.create(obs -> {
            try {
                mAWSKinesisVideoArchivedMediaClient =
                        new AWSKinesisVideoArchivedMediaClient(AWSMobileClient
                                .getInstance());
                Log.d(TAG, mAWSKinesisVideoArchivedMediaClient.toString());
                mAWSKinesisVideoArchivedMediaClient.setEndpoint(streamEndPoint);
                Log.d(TAG, "Stream end point: " + streamEndPoint);
                hlsUrlResult = mAWSKinesisVideoArchivedMediaClient
                        .getHLSStreamingSessionURL(hlsUrlRequest
                                .withStreamName(mStreamName)
                                .withPlaybackMode(HLSPlaybackMode.LIVE));
                String hlsURL = hlsUrlResult.getHLSStreamingSessionURL();
                Log.d(TAG, "obtained HLS URL: " + hlsURL);
                obs.onSuccess(hlsURL);
            } catch (Exception e) {
                Log.e(TAG, "unable to fetch HLS URL" + hlsUrlResult.toString());
                obs.onError(e);
            }

        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    startPlayer(s);
                    Log.i(TAG, "HLS URL emitting successful");},
                        throwable -> Log.e(TAG, "error: %s" + throwable.getMessage()));
    }


    private void getStreamDataEndPoint() {
        Single<String> observable = Single.create(obs -> {
            try {
                AWSKinesisVideoClient c = new AWSKinesisVideoClient(AWSMobileClient.getInstance());
                c.setRegion(Region.getRegion(Regions.EU_WEST_2));
                GetDataEndpointRequest request = new GetDataEndpointRequest()
                        .withAPIName(APIName.GET_HLS_STREAMING_SESSION_URL)
                        .withStreamName(mStreamName);
                String dataEndpoint = c.getDataEndpoint(request).getDataEndpoint();
                Log.d(TAG,"Data end point set" + ": " + dataEndpoint);
                obs.onSuccess(dataEndpoint);
            } catch (Exception e) {
                obs.onError(e);
            }
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    getHlsUrl(s);
                    Log.i(TAG, "end point emitting successful" + s);
                    }, throwable -> Log.e(TAG, "error: %s" + throwable.getMessage()));
    }

    private void buildPlayer() {
        player = new SimpleExoPlayer.Builder(getActivity()).build();
    }

    private void startPlayer(String url) {
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
        HlsMediaSource hlsMediaSource =
                new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(url));
        player.setMediaSource(hlsMediaSource);
        player.prepare();
        player.play();
        Log.d(TAG, "exoPlayer played");
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        player.release();
    }

    @Override
    public void onStop() {
        super.onStop();
        player.release();
    }

}