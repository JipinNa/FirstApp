package com.example.firstapp.bean;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.firstapp.base.BaseDecoder;

import java.nio.ByteBuffer;

public class VideoDecoder extends BaseDecoder {
    private static final String TAG = "VideoDecoder";

    private SurfaceView mSurfaceView;
    private Surface mSurface;

    public VideoDecoder(@NonNull String filePath, SurfaceView sfv, Surface sf) {
        super(filePath);
        mSurfaceView = sfv;
        mSurface = sf;
    }

    @Override
    public boolean configCodec(final MediaCodec codec, final MediaFormat format) {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0);
            notifyDecode();
        } else {
            if (mSurfaceView != null) {
                mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
                    @Override
                    public void surfaceRedrawNeeded(SurfaceHolder holder) {

                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        mSurface = holder.getSurface();
                        configCodec(codec, format);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {

                    }
                });
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean initRender() {
        return true;
    }

    @Override
    public void initSpecParams(MediaFormat format) {

    }

    @Override
    public IExtractor initExtractor(String filePath) {
        return new VideoExtractor(filePath);
    }

    @Override
    public boolean check() {
        if (mSurfaceView == null && mSurface == null) {
            Log.w(TAG, "SurfaceView和Surface都为空，至少需要一个不为空");
            if (mStateListener != null) {
                mStateListener.decoderError(this, "显示器为空");
                return false;
            }
        }
        return true;
    }

    @Override
    public void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {

    }

    @Override
    public void doneDecode() {

    }

    @Override
    public boolean isStop() {
        return false;
    }
}
