package com.example.team8capstone.glassbow;

import com.google.android.glass.widget.CardBuilder;
/**
 * Class that holds all the information required to build a card
 * Field names are self explanatory
 */
public class CardInfo {
    public int slideNumber;
    public CardBuilder.Layout layout;
    public int xmlLayout;
    public int imageResource;
    public int audioResource;
    public int videoResource;
    public int goTo;
    public boolean hasXmlLayout = false;
    public boolean hasImage = false;
    public boolean hasAudio = false;
    public boolean hasVideo = false;
    public boolean hasHeader = false;
    public boolean hasText = false;
    public boolean hasBullets = false;
    public String header;
    public int headerTextSize = 16;
    public int textSize = 12;
    public String text = "";
    public int offset = 20;


    public CardInfo(int _slideNumber, int _xmlLayout) {
        slideNumber = _slideNumber;
        layout = CardBuilder.Layout.EMBED_INSIDE;
        xmlLayout = _xmlLayout;
        hasXmlLayout = true;
        goTo = _slideNumber + offset;

    }

    public CardInfo addBullet(String _text) {
        if (!hasBullets) {
            _text = "• " + _text;
        }
        else {
            _text = "\n• " + _text;
        }

        text += _text;
        hasBullets = true;
        hasText = true;

        return this;
    }

    public CardInfo setXmlLayout(int _xmlLayout){
        xmlLayout = _xmlLayout;
        hasXmlLayout = true;
        return this;
    }

    public CardInfo setImageResource(int _imageResource){
        imageResource = _imageResource;
        hasImage = true;
        return this;
    }

    public CardInfo setAudioResource(int _audioResource){
        audioResource = _audioResource;
        hasAudio = true;
        return this;
    }

    public CardInfo setVideoResource(int _videoResource){
        videoResource = _videoResource;
        hasVideo = true;
        return this;
    }

    public CardInfo setHeader(String _header){
        header = _header;
        hasHeader = true;
        return this;
    }

    public CardInfo setText(String _text){
        text = _text;
        hasText = true;
        return this;
    }

    public CardInfo setTextSize(int _textSize){
        textSize = _textSize;
        return this;
    }

}
