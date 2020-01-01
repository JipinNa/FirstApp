package com.example.firstapp.bean;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class MP4Repack {
    private static final String TAG = "MP4Repack";

    private AudioExtractor mAExtractor;
    private VideoExtractor mVExtractor;

    private MMuxer mMuxe;

    public MP4Repack(String path) {
        mAExtractor = new AudioExtractor(path);
        mVExtractor = new VideoExtractor(path);
        mMuxe = new MMuxer();
    }

    public void start() {
        MediaFormat audioFormat = mAExtractor.getFormat();
        MediaFormat videoFormat = mVExtractor.getFormat();

        if (audioFormat != null) {
            mMuxe.addAudioTrack(audioFormat);
        } else {
            mMuxe.setNoAudio();
        }

        if (videoFormat != null) {
            mMuxe.addVideoTrack(videoFormat);
        } else {
            mMuxe.setNoVideo();
        }

        new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            //音频数据分离与写入
            if (audioFormat != null) {
                int size = mAExtractor.readBuffer(buffer);
                while (size > 0) {
                    bufferInfo.set(0, size, mAExtractor.getCurrentTimestamp(), mAExtractor.getSimpleFlag());
                    mMuxe.writeAudioData(buffer, bufferInfo);
                    size = mAExtractor.readBuffer(buffer);
                }
            }

            //视频数据分离与写入
            if (videoFormat != null) {
                int size = mAExtractor.readBuffer(buffer);
                while (size > 0) {
                    bufferInfo.set(0, size, mVExtractor.getCurrentTimestamp(), mVExtractor.getSimpleFlag());
                    mMuxe.writeAudioData(buffer, bufferInfo);
                    size = mVExtractor.readBuffer(buffer);
                }
            }
            mAExtractor.stop();
            mVExtractor.stop();
            mMuxe.release();
            Log.i(TAG, "MP4打包完成");
        }
        ).start();
    }
}
