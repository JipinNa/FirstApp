package com.example.firstapp.bean;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MMuxer {
    private static final String TAG = "MMuxer";

    private String mPath;
    private MediaMuxer mMediaMuxer;

    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;

    private boolean mIsAudioTrackAdd = false;
    private boolean mIsVideoTrackAdd = false;

    private boolean mIsStart = false;

    public MMuxer() {
        String fileName = "LVideo_" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + ".mp4";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/";
        mPath = filePath + fileName;
        try {
            mMediaMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addVideoTrack(MediaFormat mediaFormat) {
        if (mMediaMuxer != null) {
            mVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            mIsVideoTrackAdd = true;
            startMuxer();
        }
    }

    public void addAudioTrack(MediaFormat mediaFormat) {
        if (mMediaMuxer != null) {
            mAudioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            mIsAudioTrackAdd = true;
            startMuxer();
        }
    }

    private void startMuxer() {
        if (mIsAudioTrackAdd && mIsVideoTrackAdd) {
            mMediaMuxer.start();
            mIsStart = true;
            Log.i(TAG, "启动混合器，等待数据输入....");
        }
    }

    public void setNoAudio() {
        if (mIsAudioTrackAdd) {
            return;
        }
        mIsAudioTrackAdd = true;
        startMuxer();
    }

    public void setNoVideo() {
        if (mIsVideoTrackAdd) {
            return;
        }
        mIsVideoTrackAdd = true;
        startMuxer();
    }

    public void writeVideoData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart) {
            mMediaMuxer.writeSampleData(mVideoTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void writeAudioData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart) {
            mMediaMuxer.writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void release() {
        mIsVideoTrackAdd = false;
        mIsAudioTrackAdd = false;
        mMediaMuxer.stop();
        mMediaMuxer.release();
        Log.i(TAG, "混合器退出...");
    }
}
