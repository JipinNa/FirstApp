package com.example.firstapp.bean;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MMExtractor {
    private String path;
    private MediaExtractor mExtractor;
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;
    private long mCurSampleTime = 0;
    private int mCurSampleFlag = 0;
    private long mStartPos = 0;

    public MMExtractor(String path) {
        this.path = path;
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(this.path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaFormat getVideoFormat() {
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mVideoTrack = 1;
                break;
            }
        }
        return mAudioTrack >= 0 ? mExtractor.getTrackFormat(mVideoTrack) : null;
    }

    public MediaFormat getAudioFormat() {
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mAudioTrack = 1;
                break;
            }
        }
        return mAudioTrack >= 0 ? mExtractor.getTrackFormat(mAudioTrack) : null;
    }

    public int readBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        selectSOurceTrack();
        int readSampleCount = mExtractor.readSampleData(byteBuffer, 0);
        if (readSampleCount < 0) {
            return -1;
        }
        mCurSampleTime = mExtractor.getSampleTime();
        mCurSampleFlag = mExtractor.getSampleFlags();
        mExtractor.advance();
        return readSampleCount;
    }

    private void selectSOurceTrack() {
        if (mVideoTrack >= 0) {
            mExtractor.selectTrack(mVideoTrack);
        } else if (mAudioTrack >= 0) {
            mExtractor.selectTrack(mAudioTrack);
        }
    }

    public long seek(long pos) {
        mExtractor.seekTo(pos,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        return mExtractor.getSampleTime();
    }

    public void stop() {
        mExtractor.release();
        mExtractor = null;
    }

    public int getVideoTrack() {
        return mVideoTrack;
    }

    public int getAudioTrack() {
        return mAudioTrack;
    }

    public void setStartPos(long mStartPos) {
        this.mStartPos = mStartPos;
    }

    public long getCurrentTimestamp() {
        return mCurSampleTime;
    }

    public int getCurSampleFlag() {
        return mCurSampleFlag;
    }
}
