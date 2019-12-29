package com.example.firstapp.bean;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class Frame {
    public ByteBuffer buffer = null;
    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public void setBufferInfo(MediaCodec.BufferInfo info) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags);
    }
}
