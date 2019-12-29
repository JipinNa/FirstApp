package com.example.firstapp.base;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.firstapp.bean.DecodeState;
import com.example.firstapp.bean.Frame;
import com.example.firstapp.bean.IDecoder;
import com.example.firstapp.bean.IDecoderStateListener;
import com.example.firstapp.bean.IExtractor;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public abstract class BaseDecoder implements IDecoder {
    private static final String TAG = "BaseDecoder";
    private String mFilePath;
    //-----------线程相关------------
    //解码器是否在运行
    private boolean mIsRunning = true;
    //线程等待锁🔓
    private Object mLock = new Object();
    //是否可以进入解码
    private boolean mReadyForDecode = false;

    //--------------解码相关--------------
    //音视频解码器
    protected MediaCodec mCodec = null;
    //音视频数据读取器
    protected IExtractor mExtractor = null;
    //解码输入缓存区
    protected ByteBuffer[] mInputBuffers = null;
    //解码输出缓存区
    protected ByteBuffer[] mOutputBuffers = null;

    //---------------解码数据信息----------------
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private DecodeState mState = DecodeState.STOP;
    private IDecoderStateListener mStateListener = null;

    //----------------数据流是否结束-------------
    private boolean mIsEOS = false;
    private long mDuration = 0;
    protected int mVideoWidth = 0;
    protected int mVideoHeight = 0;
    private long mEndPos = 0;
    private long mStartTimeForSync = 0;
    private boolean mInRunning = false;
    private boolean mSyncRender = false;

    public BaseDecoder(@NonNull String filePath) {
        mFilePath = filePath;
    }

    @Override
    final public void run() {
        mState = DecodeState.START;
        if (mStateListener != null) {
            mStateListener.decoderPrepare(this);
        }
        if (!init()) {
            return;
        }
        Log.i(TAG, "开始解码");
        try {
            while (mIsRunning) {
                if (mState != DecodeState.START &&
                        mState != DecodeState.DECODING &&
                        mState != DecodeState.SEEKING) {
                    Log.i(TAG, "进入等待：$mState");
                    waitDecode();

                    mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp();
                }

                if (!mIsRunning || mState == DecodeState.STOP) {
                    mIsRunning = false;
                    break;
                }

                if (mStartTimeForSync == -1l) {
                    mStartTimeForSync = System.currentTimeMillis();
                }

                if (!mIsEOS) {
                    mIsEOS = pushBufferToDecoder();
                }

                int index = pullBufferFromDecoder();
                if (index >= 0) {
                    if (mSyncRender && mState == DecodeState.DECODING) {
                        sleepRender();
                    }

                    if (mSyncRender) {
                        render(mOutputBuffers[index], mBufferInfo);
                    }

                    Frame frame = new Frame();
                    frame.buffer = mOutputBuffers[index];
                    frame.setBufferInfo(mBufferInfo);
                    mStateListener.decodeOneFrame(this, frame);

                    mCodec.releaseOutputBuffer(index, true);

                    if (mState == DecodeState.START) {
                        mState = DecodeState.PAUSE;
                    }
                }
                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "解码结束");
                    mState = DecodeState.FINISH;
                    mStateListener.decoderFinish(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doneDecode();
            release();
        }
    }

    private boolean init() {
        if (mFilePath.isEmpty() || !new File(mFilePath).exists()) {
            Log.w(TAG, "文件路径为空");
            if (mStateListener != null) {
                mStateListener.decoderError(this, "文件路径为空");
            }
            return false;
        }
        if (!check()) {
            return false;
        }
        mExtractor = initExtractor(mFilePath);
        if (mExtractor == null || mExtractor.getFormat() == null) {
            return false;
        }
        if (!initParams()) {
            return false;
        }
        if (!initRender()) {
            return false;
        }
        if (!initCodec()) {
            return false;
        }
        return true;
    }

    private boolean initParams() {
        try {
            MediaFormat format = mExtractor.getFormat();
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
            if (mEndPos == 0l) {
                mEndPos = mDuration;
            }

            initSpecParams(mExtractor.getFormat());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean initCodec() {
        try {
            String type = mExtractor.getFormat().getString(MediaFormat.KEY_MIME);
            mCodec = MediaCodec.createDecoderByType(type);
            if (!configCodec(mCodec, mExtractor.getFormat())) {
                waitDecode();
            }
            mCodec.start();
            mInputBuffers = mCodec.getInputBuffers();
            mOutputBuffers = mCodec.getOutputBuffers();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void waitDecode() {
        try {
            if (mState == DecodeState.PAUSE) {
                if (mStateListener != null) {
                    mStateListener.decoderPause(this);
                }
            }
            synchronized (mLock) {
                mLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean pushBufferToDecoder() {
        int inputBufferIndex = mCodec.dequeueInputBuffer(2000);
        boolean isEndOfStream = false;
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
            int sampleSize = mExtractor.readBuffer(inputBuffer);
            if (sampleSize < 0) {
                mCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEndOfStream = true;
            } else {
                mCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mExtractor.getCurrentTimestamp(), 0);
            }
        }
        return isEndOfStream;
    }

    private int pullBufferFromDecoder() {
        int index = mCodec.dequeueOutputBuffer(mBufferInfo, 1000);
        switch (index) {
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
            case MediaCodec.INFO_TRY_AGAIN_LATER:
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                mOutputBuffers = mCodec.getOutputBuffers();
                index = -1;
                break;
            default:
                break;
        }
        return index;
    }

    private void sleepRender() {
        long passTime = System.currentTimeMillis() - mStartTimeForSync;
        long curTime = getCurTimeStamp();
        if (curTime > passTime) {
            try {
                Thread.sleep(curTime - passTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void release() {
        Log.i(TAG, "解码停止，释放解码器");
        mState = DecodeState.STOP;
        mIsEOS = false;
        mExtractor.stop();
        mCodec.stop();
        mCodec.release();
        mStateListener.decoderDestroy(this);
    }

    protected void notifyDecode() {
        synchronized (mLock) {
            mLock.notifyAll();
        }
        if (mState == DecodeState.DECODING) {
            if (mStateListener != null) {
                mStateListener.decoderRunning(this);
            }
        }
    }

    @Override
    public void pause() {
        mState = DecodeState.PAUSE;
    }

    @Override
    public void goOn() {
        mState = DecodeState.DECODING;
    }

    @Override
    public long seekTo(long pos) {
        return 0;
    }

    @Override
    public long seekAndPlay(long pos) {
        return 0;
    }

    @Override
    public void stop() {
        mState = DecodeState.STOP;
        mInRunning = false;
        notifyDecode();
    }

    @Override
    public boolean isDecoding() {
        return mState == DecodeState.DECODING;
    }

    @Override
    public boolean isSeeking() {
        return mState == DecodeState.SEEKING;
    }

    @Override
    public void setStaeListener(IDecoderStateListener listener) {
        mStateListener = listener;
    }

    @Override
    public int getWidth() {
        return mVideoWidth;
    }

    @Override
    public int getHeight() {
        return mVideoHeight;
    }

    @Override
    public long getDurating() {
        return mDuration;
    }

    @Override
    public int getRotationAngle() {
        return 0;
    }

    @Override
    public MediaFormat getMediaFormat() {
        return mExtractor.getFormat();
    }

    @Override
    public int getTrack() {
        return 0;
    }

    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public IDecoder withoutSync() {
        mSyncRender = false;
        return this;
    }

    private long getCurTimeStamp() {
        return mBufferInfo.presentationTimeUs / 1000;
    }

    //配置解码器
    abstract boolean configCodec(MediaCodec mCodec, MediaFormat format);

    //初始化渲染器
    abstract boolean initRender();

    //初始化子类特有的参数
    abstract void initSpecParams(MediaFormat format);

    //初始化数据提取器
    abstract IExtractor initExtractor(String filePath);

    //检查子类参数
    abstract boolean check();

    //渲染
    abstract void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo);

    //结束解码
    abstract void doneDecode();
}
