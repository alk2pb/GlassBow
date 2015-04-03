package com.example.team8capstone.glassbow;

import com.example.team8capstone.glassbow.image.ImageActivity;
import com.example.team8capstone.glassbow.video.VideoActivity;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;
import java.lang.Runtime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener {
    // Array of Card Infos
    private ArrayList<CardInfo> cardInfos = new ArrayList<CardInfo>();

    // Other variables used (name is self explanatory)
    private CardScrollView mCardScroller;
    private boolean mVoiceMenuEnabled = true;
    private CardScrollAdapter mAdapter;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPaused = false;

    private Intent image;
    private Intent video;

    private static final String TAG = "LogEntryStart";

    private StringBuilder log=new StringBuilder();

    private Time time = new Time();

//    private boolean hasPlayed = false;

    private int currentSlide = 0;

    private AudioManager am;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Sets Card Info
        setCardInfo();

        // Instantiates a new intent for the ImageActivity that will be activated when
        // the user wishes to view a picture
        image = new Intent(this, ImageActivity.class);

        // Instantiates a new intent for the VideoActivity that will be activated when
        // the user wishes to view a video
        video = new Intent(this, VideoActivity.class);

        // Requests a voice menu on this activity. As for any other window feature,
        // be sure to request this before setContentView() is called
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);

        // Ensure screen stays on during demo.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCardScroller = new CardScrollView(this);
        mAdapter = new CardAdapter(createCards(this), this, cardInfos);
        mCardScroller.setAdapter(mAdapter);
        setCardScrollerListener();
        setContentView(mCardScroller);

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));


            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }

        } catch (IOException e) {
        }

        // Set mediaPlayer volume to max
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM,am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM),0);

    }

    // When the MediaPlayer finishes, close and refresh the menu
    public void onCompletion(MediaPlayer mediaplayer) {
        getWindow().closePanel(WindowUtils.FEATURE_VOICE_COMMANDS);
        closeOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "MainActivity activated" + " LogEntryEnd");
        Log.i(TAG,time.toString() + ", " + "Current Slide:" + mCardScroller.getSelectedItemPosition() + " LogEntryEnd");
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }

        mCardScroller.deactivate();
        time.setToNow();
        Log.i(TAG, time.toString() + ", " + "MainActivity deactivated" + " LogEntryEnd");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Release the MediaPlayer when the activity finishes
        mediaPlayer.release();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "MainActivity destroyed" + " LogEntryEnd");
        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOWNLOADS + "/log_" + time.toString().replace("/","_") + ".txt");

            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(log.toString());
            output.close();
        }
        catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.voice_menu, menu);
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "Menu created" + " LogEntryEnd");
            return true;
        }
        // Good practice to pass through, for options menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {

            int position = mCardScroller.getSelectedItemPosition();

            addDefaultMenuOptions(menu, position);
            addCustomMenuOptions(menu, position);

            // If the MediaPlayer is playing, add media options
            if (mediaPlayer.isPlaying() || isPaused){
                addMediaMenuOptions(menu);

            }

            collapseMenu(menu,1);
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "Menu populated" + " LogEntryEnd");

            // Dynamically decides between enabling/disabling voice menu.
            return mVoiceMenuEnabled;
        }

        // Good practice to pass through, for options menu.
        return super.onPreparePanel(featureId, view, menu);
    }

    // Add default menu options
    private void addDefaultMenuOptions(Menu menu, int position){
        menu.add(Menu.NONE,10,Menu.NONE,"next");
        menu.add(Menu.NONE,11,Menu.NONE,"back");
        menu.addSubMenu(Menu.NONE,12,Menu.NONE,"goto");
        setGotoMenuOptions(menu, position);
        menu.addSubMenu(Menu.NONE,13,Menu.NONE,"exit");
        menu.findItem(13).getSubMenu().add(Menu.NONE,14,Menu.NONE,"yes");
    }

    private void setGotoMenuOptions(Menu menu, int position) {
        // Add menu options based on slide position
        for (CardInfo cardInfo : cardInfos) {
            if (!cardInfo.hasHeader) {
                menu.findItem(12).getSubMenu().add(Menu.NONE, cardInfo.goTo, Menu.NONE, Integer.toString(cardInfo.slideNumber + 1));
            }
            else {
                menu.findItem(12).getSubMenu().add(Menu.NONE, cardInfo.goTo, Menu.NONE, cardInfo.header);
            }
        }
    }

    private void addCustomMenuOptions(Menu menu, int position){

        // Add and adjust menu options based on slide content
        menu.findItem(12).getSubMenu().removeItem(cardInfos.get(0).offset + position);

        if (position == 0){
            menu.removeItem(11);
        }

        if (position == cardInfos.size() - 1){
            menu.removeItem(10);
        }

        if (cardInfos.get(position).hasAudio){
            if (!mediaPlayer.isPlaying() && !isPaused) {
                menu.add(Menu.NONE,1,Menu.NONE,"play audio");
            }
        }

        if (cardInfos.get(position).hasVideo){
            menu.add(Menu.NONE,0,Menu.NONE,"play video");
        }

        if (cardInfos.get(position).hasImage){
            menu.add(Menu.NONE,2,Menu.NONE,"view picture");
        }

    }

    private void addMediaMenuOptions(Menu menu){
        menu.add(Menu.NONE,3,Menu.NONE,"stop media");
        menu.addSubMenu(Menu.NONE,9,Menu.NONE,"media options");
        if (isPaused){
            menu.findItem(9).getSubMenu().add(Menu.NONE,4,Menu.NONE,"resume");
        }
        else {
            menu.findItem(9).getSubMenu().add(Menu.NONE,5,Menu.NONE,"pause");
        }
        menu.findItem(9).getSubMenu().add(Menu.NONE,6,Menu.NONE,"rewind");
        menu.findItem(9).getSubMenu().add(Menu.NONE,7,Menu.NONE,"fast forward");
        menu.findItem(9).getSubMenu().add(Menu.NONE,8,Menu.NONE,"play from beginning");
//        menu.findItem(9).getSubMenu().add(Menu.NONE,15,Menu.NONE,"volume up");
//        menu.findItem(9).getSubMenu().add(Menu.NONE,16,Menu.NONE,"volume down");
    }

    // Collapse a menu to prevent menu options from going off the viewable screen
    private void collapseMenu(Menu menu, int level){
//        if (level == 1){
//            if (menu.size() > 4){
//                menu.addSubMenu(Menu.NONE,level*100,Menu.NONE,"more options");
//            }
//        }
//        else {
//            if (menu.size() > 3){
//                menu.addSubMenu(Menu.NONE,level*100,Menu.NONE,"more options");
//            }
//        }
//
//        if (level == 1){
//            if (menu.size() > 4){
//                for (int i = 3; i < menu.size(); i++){
//                    if (!(menu.getItem(i).getItemId()/100 >= 1)){
//                        if(menu.getItem(i).hasSubMenu()){
//                            reMenu(menu.findItem(level*100).getSubMenu(), menu.getItem(i));
//                        }
//                        else {
//                            menu.findItem(level*100).getSubMenu().add(Menu.NONE,menu.getItem(i).getItemId(),Menu.NONE,menu.getItem(i).getTitle());
//                        }
//                        menu.removeItem(menu.getItem(i).getItemId());
//                    }
//                }
//            }
//        }
//        else {
//            if (menu.size() > 3){
//                for (int i = 2; i < menu.size(); i++){
//                    if (!(menu.getItem(i).getItemId()/100 >= 1)){
//                        if(menu.getItem(i).hasSubMenu()){
//                            reMenu(menu.findItem(level*100).getSubMenu(), menu.getItem(i));
//                        }
//                        else {
//                            menu.findItem(level*100).getSubMenu().add(Menu.NONE,menu.getItem(i).getItemId(),Menu.NONE,menu.getItem(i).getTitle());
//                        }
//                        menu.removeItem(menu.getItem(i).getItemId());
//                    }
//                }
//            }
//        }

        for (int i = 0; i < menu.size(); i++){
            if (menu.getItem(i).hasSubMenu()){
                collapseMenu(menu.getItem(i).getSubMenu(),level+1);
            }
        }
        menu.add(Menu.NONE,99,Menu.NONE,"cancel");
    }

    // Add a pre-populated SubMenu to another Menu
    private void reMenu(Menu menu, MenuItem reMenu){
        menu.addSubMenu(Menu.NONE,reMenu.getItemId(),Menu.NONE,reMenu.getTitle());
        for (int i = 0; i < reMenu.getSubMenu().size(); i++){
            if (reMenu.getSubMenu().getItem(i).hasSubMenu()){
                menu.findItem(reMenu.getItemId()).getSubMenu().addSubMenu(Menu.NONE,reMenu.getSubMenu().getItem(i).getItemId(),Menu.NONE,reMenu.getSubMenu().getItem(i).getTitle());
                reMenu(menu.findItem(reMenu.getItemId()).getSubMenu().findItem(reMenu.getSubMenu().getItem(i).getItemId()).getSubMenu(),reMenu.getSubMenu().getItem(i));
            }
            else{
                menu.findItem(reMenu.getItemId()).getSubMenu().add(Menu.NONE,reMenu.getSubMenu().getItem(i).getItemId(),Menu.NONE,reMenu.getSubMenu().getItem(i).getTitle());
            }
        }
    }

    // Set menu item actions
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case 2:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "ImageActivity started" + " LogEntryEnd");
                    startActivity(image);
                    break;
                case 1:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer started" + " LogEntryEnd");
                    mediaPlayer.start();
                    break;
                case 0:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "VideoActivity started" + " LogEntryEnd");
                    startActivity(video);
                    break;
                case 3:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "MediaPlayer stopped" + " LogEntryEnd");
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                    isPaused = false;
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
                    Log.i(TAG,time.toString() + ", " + "ImageActivity fast forwarded" + " LogEntryEnd");
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
                case 10:
                    if (mCardScroller.getSelectedItemPosition() < cardInfos.size())
                    {
                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() + 1);
                    }
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "Next slide selected" + " LogEntryEnd");
                    break;
                case 11:
                    if (mCardScroller.getSelectedItemPosition() > 0)
                    {
                        mCardScroller.setSelection(mCardScroller.getSelectedItemPosition() - 1);
                    }
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "Previous slide selected" + " LogEntryEnd");
                    break;
                case 14:
                    time.setToNow();
                    Log.i(TAG,time.toString() + ", " + "Exit selected" + " LogEntryEnd");
                    finish();
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
                    for (CardInfo cardInfo : cardInfos){
                        if (item.getItemId() == cardInfo.goTo){
                            mCardScroller.setSelection(cardInfo.slideNumber);
                            time.setToNow();
                            Log.i(TAG,time.toString() + ", " + "Goto selected" + " LogEntryEnd");
                            return true;
                        }
                    }
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

    // When the menu is closed, refresh it
    @Override
    public void onPanelClosed (int featureId, Menu menu) {
        getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
        invalidateOptionsMenu();
        time.setToNow();
        Log.i(TAG,time.toString() + ", " + "Menu closed" + " LogEntryEnd");
    }

    // Set media resources based on slide position
    private void setMediaResources(int position){
        if (cardInfos.get(position).hasImage){
            image.removeExtra("imageResource");
            image.putExtra("imageResource", cardInfos.get(position).imageResource);
        }

        if (cardInfos.get(position).hasAudio){
            if (!mediaPlayer.isPlaying() && !isPaused) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, cardInfos.get(position).audioResource);
                mediaPlayer.setOnCompletionListener(MainActivity.this);
            }
        }

        if (cardInfos.get(position).hasVideo){
            video.removeExtra("videoResource");
            video.putExtra("videoResource", cardInfos.get(position).videoResource);
        }
    }

    // Starts audio and video when card is selected
    private void startMedia(int position) {
        if (cardInfos.get(position).hasAudio){
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "MediaPlayer started" + " LogEntryEnd");
            mediaPlayer.start();
        }

        if (cardInfos.get(position).hasVideo){
            time.setToNow();
            Log.i(TAG,time.toString() + ", " + "VideoActivity started" + " LogEntryEnd");
            startActivity(video);
        }
//        hasPlayed = true;
    }

    private void setCardScrollerListener() {
        // When an item is selected, refresh the menu
        mCardScroller.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
                invalidateOptionsMenu();
                setMediaResources(mCardScroller.getSelectedItemPosition());
                time.setToNow();
                Log.i(TAG,time.toString() + ", " + "New slide selected" + " LogEntryEnd");
                Log.i(TAG,time.toString() + ", " + "Current slide: " + mCardScroller.getSelectedItemPosition() + " LogEntryEnd");
//                if (currentSlide != mCardScroller.getSelectedItemPosition()) {
//                    hasPlayed = false;
//                    currentSlide = mCardScroller.getSelectedItemPosition();
//                }
//                if (!hasPlayed) {
//                    startMedia(mCardScroller.getSelectedItemPosition());
//                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Add sound effect when an slide is clicked
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int soundEffect = Sounds.TAP;
                time.setToNow();
                Log.i(TAG,time.toString() + ", " + "Slide " + mCardScroller.getSelectedItemPosition() + " tapped" + " LogEntryEnd");
                // Play sound.
                am.playSoundEffect(soundEffect);
                openOptionsMenu();
            }
        });
    }

    // Create the slides for the "powerpoint" view
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();

        for (CardInfo cardInfo : cardInfos){
            if (cardInfo.hasXmlLayout){
                cards.add(cardInfo.slideNumber, new CardBuilder(context,cardInfo.layout)
                        .setEmbeddedLayout(cardInfo.xmlLayout));
            }
            else {
                CardBuilder cardBuilder = new CardBuilder(context,cardInfo.layout);

                if (cardInfo.hasText){
                    cardBuilder.setText(cardInfo.text);
                }

                cards.add(cardInfo.slideNumber, cardBuilder);
            }
        }
        return cards;
    }

    // Set Card Info
    private void setCardInfo() {
        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Getting to know the bow tie")
                .addBullet("Both sides are the same size")
                .addBullet("The length is adjustable on the inside of the bow tie")
                .addBullet("Adjust the length to your neck size, adding a ½ inch may be helpful while learning")
                .setVideoResource(R.raw.bvid1)
                .setImageResource(R.drawable.bpic1));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Tie Orientation")
                .addBullet("Place the tie with the inside against your neck")
                .addBullet("The Right Side should be about 1 ½ “ longer than the Left.")
                .setVideoResource(R.raw.bvid2)
                .setImageResource(R.drawable.bpic2));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("The Cross Over")
                .addBullet("Take the long side and cross it over the front of the short side")
                .addBullet("Then slip the long side behind the short side and up towards your face")
                .addBullet("This will look similar to tying a shoe")
                .addBullet("Keep this snug against your neck")
                .setVideoResource(R.raw.bvid3)
                .setImageResource(R.drawable.bpic3));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("The First Bow")
                .addBullet("The bottom end forms the first bow")
                .addBullet("Pinch the narrowest portion of the bottom end with your right hand")
                .addBullet("Bring the your pinched tie to the middle of the cross over")
                .addBullet("The bow should form with a fold on the left side")
                .setVideoResource(R.raw.bvid4)
                .setImageResource(R.drawable.bpic4));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Middle Stripe")
                .addBullet("Pinch First bow and Knot with right hand")
                .addBullet("Lower top side in front of the first bow")
                .addBullet("Make sure the tie remains flat in front of the bow")
                .addBullet("This creates a middle stripe")
                .setVideoResource(R.raw.bvid5)
                .setImageResource(R.drawable.bpic5));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Pinch and pull")
                .addBullet("Pinch the two ends of the first bow, surrounding the middle stripe with your right hand")
                .addBullet("Pull this forward, and notice the Back Loop is formed behind the bow")
                .setVideoResource(R.raw.bvid6)
                .setImageResource(R.drawable.bpic6));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Back Loop")
                .addBullet("This is where the second bow will go through")
                .addBullet("The size of the loop is adjustable by loosening the middle stripe")
                .addBullet("This loop will be tightened later")
                .setVideoResource(R.raw.bvid7)
                .setImageResource(R.drawable.bpic7));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Start Second Bow ")
                .addBullet("Pinch bottom portion, where the tie is widest, with your left hand")
                .addBullet("Push pinched bow through Back Loop")
                .addBullet("The tie should naturally wrap your left pointer finger as you make this motion")
                .setVideoResource(R.raw.bvid8)
                .setImageResource(R.drawable.bpic8));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Finish Second Bow ")
                .addBullet("Carefully pull the folded portion of the tie through the Back loop")
                .addBullet("Be careful not to pull the free end through the loop too")
                .addBullet("The tie can now be adjusted for looks")
                .setVideoResource(R.raw.bvid9)
                .setImageResource(R.drawable.bpic9));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Adjusting the Bow")
                .addBullet("Carefully pull on the folded ends of the bow to tighten the Middle Stripe")
                .addBullet("Then adjust both the folded and free ends to make a symmetrical bow")
                .setVideoResource(R.raw.bvid10)
                .setImageResource(R.drawable.bpic10));

        cardInfos.add(new CardInfo(cardInfos.size(), R.layout.left_column_layout)
                .setHeader("Common Problems")
                .addBullet("Second bow is too small")
                .addBullet("Tie is loose around neck")
                .addBullet("Often  wrong tie length or too loose of a knot")
                .setVideoResource(R.raw.bvid11)
                .setImageResource(R.drawable.bpic11));
    }
}
