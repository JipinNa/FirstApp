package com.example.firstapp.bean;

import com.example.firstapp.base.BaseDecoder;

public interface IDecoderStateListener {
    void decoderPrepare(IDecoder decoder);

    void decoderError(IDecoder decoder, String msg);

    void decoderRunning(IDecoder decoder);

    void decoderPause(IDecoder decoder);

    void decoderFinish(IDecoder decoder);

    void decoderDestroy(IDecoder decoder);

    void decodeOneFrame(IDecoder decoder, Frame frame);
}
