package com.example.firstapp.bean;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;

import com.example.firstapp.base.BaseDecoder;

import java.nio.ByteBuffer;

public class AudioDecoder extends BaseDecoder {
    private static final String TAG = "AudioDecoder";

    private int mSampleRate = -1;
    private int mChannels = 1;
    private int mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack mAudioTrack = null;
    private short[] mAudioOutTempBuf = null;

    public AudioDecoder(@NonNull String filePath) {
        super(filePath);
    }

    @Override
    public boolean configCodec(MediaCodec codec, MediaFormat format) {
        codec.configure(format, null, null, 0);
        return true;
    }

    @Override
    public boolean initRender() {
        int channel = mChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit);
        mAudioOutTempBuf = new short[minBufferSize / 2];
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,//播放类型
                mSampleRate,//采样率
                channel,//通道
                mPCMEncodeBit,//采样位数
                minBufferSize,//缓冲区大小
                AudioTrack.MODE_STREAM);//播放模式
        mAudioTrack.play();
        return true;
    }

    @Override
    public void initSpecParams(MediaFormat format) {
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            mPCMEncodeBit = format.containsKey(MediaFormat.KEY_PCM_ENCODING) ? format.getInteger(MediaFormat.KEY_PCM_ENCODING) : AudioFormat.ENCODING_PCM_16BIT;
        } catch (Exception e) {

        }
    }

    @Override
    public IExtractor initExtractor(String filePath) {
        return new AudioExtractor(filePath);
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mAudioOutTempBuf.length < bufferInfo.size / 2) {
            mAudioOutTempBuf = new short[bufferInfo.size / 2];
        }
        outputBuffer.position(0);
        outputBuffer.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size / 2);
        mAudioTrack.write(mAudioOutTempBuf, 0, bufferInfo.size / 2);
    }

    @Override
    public void doneDecode() {
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    @Override
    public boolean isStop() {
        return false;
    }
}
