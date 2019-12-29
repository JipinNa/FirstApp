package com.example.firstapp.bean;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class AudioExtractor implements IExtractor {
    private String path;
    private MMExtractor mMediaExtractor;

    public AudioExtractor(String path) {
        this.path = path;
        mMediaExtractor = new MMExtractor(this.path);
    }

    @Override
    public MediaFormat getFormat() {
        return mMediaExtractor.getVideoFormat();
    }

    @Override
    public int readBuffer(ByteBuffer byteBuffer) {
        return mMediaExtractor.readBuffer(byteBuffer);
    }

    @Override
    public long getCurrentTimestamp() {
        return mMediaExtractor.getCurrentTimestamp();
    }

    @Override
    public long seek(long pos) {
        return mMediaExtractor.seek(pos);
    }

    @Override
    public void setStartPos(long pos) {
        mMediaExtractor.setStartPos(pos);
    }

    @Override
    public void stop() {
        mMediaExtractor.stop();
    }
}
