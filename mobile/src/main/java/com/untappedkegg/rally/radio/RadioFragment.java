package com.untappedkegg.rally.radio;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.untappedkegg.rally.R;

public class RadioFragment extends Fragment implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {

    public RadioFragment() {
        // TODO Auto-generated constructor stub
    }

    private Button btn_play, btn_pause, btn_stop;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private int lengthOfAudio;
    //private final String URL = AppState.WR_WRC_RADIO;
    private final Handler handler = new Handler();
    private final Runnable r = new Runnable() {
        @Override
        public void run() {
            updateSeekProgress();
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.radio, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        btn_play = (Button) getActivity().findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);
        btn_pause = (Button) getActivity().findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(this);
        btn_pause.setEnabled(false);
        btn_stop = (Button) getActivity().findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
        btn_stop.setEnabled(false);

        seekBar = (SeekBar) getActivity().findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mediaPlayer.isPlaying()) {
            SeekBar tmpSeekBar = (SeekBar) v;
            mediaPlayer.seekTo((lengthOfAudio / 100) * tmpSeekBar.getProgress());
        }
        return false;
    }

    @Override
    public void onClick(View view) {

        try {
            //mediaPlayer.setDataSource(URL);
            mediaPlayer.prepare();
            lengthOfAudio = mediaPlayer.getDuration();
        } catch (Exception e) {
            //Log.e("Error", e.getMessage());
        }

        switch (view.getId()) {
            case R.id.btn_play:
                playAudio();
                break;
            case R.id.btn_pause:
                pauseAudio();
                break;
            case R.id.btn_stop:
                stopAudio();
                break;
            default:
                break;
        }

        updateSeekProgress();
    }

    private void updateSeekProgress() {
        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / lengthOfAudio) * 100));
            handler.postDelayed(r, 1000);
        }
    }

    private void stopAudio() {
        mediaPlayer.stop();
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
        seekBar.setProgress(0);
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        btn_play.setEnabled(true);
        btn_pause.setEnabled(false);
    }

    private void playAudio() {
        mediaPlayer.start();
        btn_play.setEnabled(false);
        btn_pause.setEnabled(true);
        btn_stop.setEnabled(true);
    }

}
