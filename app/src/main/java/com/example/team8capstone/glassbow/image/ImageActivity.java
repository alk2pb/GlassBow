package com.example.team8capstone.glassbow.image;

import com.example.team8capstone.glassbow.R;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
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
import android.widget.AdapterView;
import android.widget.ImageView;

// Class that handles displaying full screen images
public final class ImageActivity extends Activity {

    private CardScrollView mCardScroller;
    private boolean mVoiceMenuEnabled = true;
    private View mView;
    private int resource;

    private static final String TAG = "LogEntryStart";

    Time time = new Time();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            resource = extras.getInt("imageResource");
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

        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
        });

        setContentView(mCardScroller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "ImageActivity activated" + " LogEntryEnd");
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "ImageActivity deactivated" + " LogEntryEnd");
        super.onPause();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.image_menu, menu);
            return true;
        }
        // Good practice to pass through, for options menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            // Dynamically decides between enabling/disabling voice menu.
            return mVoiceMenuEnabled;
        }

        // Good practice to pass through, for options menu.
        return super.onPreparePanel(featureId, view, menu);
    }

    // Set Menu Options
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
        View view = vi.inflate(R.layout.image_layout, null);

        // Inflate the ImageView with the correct resource
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setImageResource(resource);

        return view;
    }

}
