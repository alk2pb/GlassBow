package com.example.team8capstone.glassbow.video;

import com.example.team8capstone.glassbow.R;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.AdapterView;


/**
 * Class that handles displaying full screen videos
 */
public final class VideoActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener {

    private CardScrollView mCardScroller;
    private boolean mVoiceMenuEnabled = true;
    private View mView;
    private int resource;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPaused = false;
    private SurfaceHolder surfaceHolder;

    private static final String TAG = "LogEntryStart";

    Time time = new Time();

    private AudioManager am;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            resource = extras.getInt("videoResource");
        }

        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mView = buildView();


        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });

        // Set mediaPlayer volume to max
//        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
//        am.setStreamVolume(AudioManager.STREAM_SYSTEM,am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM),0);

        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                am.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
        });

        setContentView(mCardScroller);

        surfaceView = (SurfaceView) mView.findViewById(R.id.video);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    public void onCompletion(MediaPlayer mediaplayer) {
        getWindow().closePanel(WindowUtils.FEATURE_VOICE_COMMANDS);
        closeOptionsMenu();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "VideoActivity activated" + " LogEntryEnd");
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "VideoActivity deactivated" + " LogEntryEnd");
        super.onPause();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.video_menu, menu);
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "Menu created" + " LogEntryEnd");
            return true;
        }

        // Good practice to pass through, for options menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    // Set Menu Options
    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            if (!mediaPlayer.isPlaying() && !isPaused) {
                menu.add(Menu.NONE,1,Menu.NONE,"play video");
            }

            if (mediaPlayer.isPlaying() || isPaused){
                menu.addSubMenu(Menu.NONE,9,Menu.NONE,"video options");
                if (isPaused){
                    menu.findItem(9).getSubMenu().add(Menu.NONE,4,Menu.NONE,"resume");
                }
                else {
                    menu.findItem(9).getSubMenu().add(Menu.NONE,5,Menu.NONE,"pause");
                }
                menu.findItem(9).getSubMenu().add(Menu.NONE,6,Menu.NONE,"rewind");
                menu.findItem(9).getSubMenu().add(Menu.NONE,7,Menu.NONE,"fast forward");
                menu.findItem(9).getSubMenu().add(Menu.NONE,8,Menu.NONE,"play from beginning");
                menu.findItem(9).getSubMenu().add(Menu.NONE,R.id._cancel,Menu.NONE,"cancel");
//                menu.findItem(9).getSubMenu().add(Menu.NONE,15,Menu.NONE,"volume up");
//                menu.findItem(9).getSubMenu().add(Menu.NONE,16,Menu.NONE,"volume down");
            }
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "Menu populated" + " LogEntryEnd");

            // Dynamically decides between enabling/disabling voice menu.
            return mVoiceMenuEnabled;
        }

        // Good practice to pass through, for options menu.
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id._exit:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "Exit selected" + " LogEntryEnd");
                    finish();
                    break;
                case 1:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer started" + " LogEntryEnd");
                    mediaPlayer.start();
                    break;
                case 4:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer resumed" + " LogEntryEnd");
                    mediaPlayer.start();
                    isPaused = false;
                    break;
                case 5:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer paused" + " LogEntryEnd");
                    mediaPlayer.pause();
                    isPaused = true;
                    break;
                case 6:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer rewinded" + " LogEntryEnd");
                    if (mediaPlayer.getCurrentPosition() < 3000){
                        mediaPlayer.seekTo(0);
                    }
                    else {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
                    }
                    break;
                case 7:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer fast forwarded" + " LogEntryEnd");
                    if (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() < 3000){
                        mediaPlayer.seekTo(mediaPlayer.getDuration());
                    }
                    else {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
                    }
                    break;
                case 8:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer restarted" + " LogEntryEnd");
                    mediaPlayer.seekTo(0);
                    isPaused = false;
                    break;
//                case 15:
//                    am.adjustStreamVolume(AudioManager.STREAM_SYSTEM,AudioManager.ADJUST_RAISE,0);
//                    time.setToNow();
//                    Log.i(TAG,time.toString() + ", " + "Volume increased" + " LogEntryEnd");
//                    break;
//                case 16:
//                    am.adjustStreamVolume(AudioManager.STREAM_SYSTEM,AudioManager.ADJUST_LOWER,0);
//                    time.setToNow();
//                    Log.i(TAG,time.toString() + ", " + "Volume decreased" + " LogEntryEnd");
//                    break;
                default:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "Cancel selected" + " LogEntryEnd");
                    return true;
            }

            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onMenuOpened (int featureId, Menu menu) {
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "Menu opened" + " LogEntryEnd");
        return true;
    }

    @Override
    public void onPanelClosed (int featureId, Menu menu) {
        getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
        invalidateOptionsMenu();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "Menu closed" + " LogEntryEnd");
    }

    private View buildView() {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.video_layout, null);

        mediaPlayer = MediaPlayer.create(VideoActivity.this, resource);
        mediaPlayer.setOnCompletionListener(VideoActivity.this);

        return view;
    }

    private void playVideo() {
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "MediaPlayer started" + " LogEntryEnd");
        mediaPlayer.start();
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        mediaPlayer.setDisplay(surfaceHolder);
        playVideo();
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.release();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
}