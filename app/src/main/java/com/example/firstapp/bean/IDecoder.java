package com.example.firstapp.bean;

import android.media.MediaFormat;

public interface IDecoder extends Runnable {
    //暂停解码
    void pause();
    //继续解码
    void goOn();
    //跳转到pos
    long seekTo(long pos);
    //跳转到pos并播放
    long seekAndPlay(long pos);
    //停止解码
    void stop();
    //是否正在解码
    boolean isDecoding();
    //是否正在快进
    boolean isSeeking();
    //是否停止解码
    boolean isStop();
    //设置状态监听器
    void setStaeListener(IDecoderStateListener listener);
    //获取视频宽
    int getWidth();
    //获取视频高
    int getHeight();
    //获取视频长度
    long getDurating();
    //获取视频旋转角度
    int getRotationAngle();
    //获取视频对应的格式参数
    MediaFormat getMediaFormat();
    //获取视频对应的媒体轨道
    int getTrack();
    //获取解码文件路径
    String getFilePath();
    //取消同步
    IDecoder withoutSync();
}
