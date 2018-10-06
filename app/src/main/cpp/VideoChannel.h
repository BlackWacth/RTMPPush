//
// Created by bruce-hua on 2018/10/3.
//

#ifndef RTMPPUSH_VIDEOCHANNEL_H
#define RTMPPUSH_VIDEOCHANNEL_H


#include <cstdint>
#include <sys/types.h>
#include <x264.h>
#include "librtmp/rtmp.h"

class VideoChannel {

    typedef void (*VideoCallback)(RTMPPacket *packet);

public:
    VideoChannel();

    ~VideoChannel();

    void setVideoEncodeInfo(int width, int height, int fps, int bitrate);

    void encodeData(int8_t *data);

    void setVideoCallback(VideoCallback callback);


private:

    pthread_mutex_t mutex;

    x264_t *videoCodec = 0;
    x264_picture_t *pic_in = 0;

    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;

    int ySize;
    int uvSize;
    int index = 0;
    VideoCallback  callback;

    void sendSpsPps(uint8_t *sps, uint8_t *pps, int len, int pps_len);

    void sendFrame(int type, int payload, uint8_t *p_payload);


};


#endif //RTMPPUSH_VIDEOCHANNEL_H
