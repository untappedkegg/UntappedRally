package com.untappedkegg.rally.radio;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.untappedkegg.rally.R;

public class RadioVideoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.video, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        VideoView videoView = (VideoView) getActivity().findViewById(R.id.videoView);
        //	        videoView.setVideoPath("http://www.pocketjourney.com/downloads/pj/video/famous.3gp");
        //	        videoView.setMediaController(new MediaController(getActivity()));
        //	        videoView.requestFocus();
        //
        //	        videoView.youTubeStart();


        //	        videoView.setVideoURI(Uri.parse(AppState.WR_WRC_RADIO)); // feed.getUrl returns the url
        //	        videoView.youTubeStart();// this on UiThread
    }

}
