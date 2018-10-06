#include <jni.h>
#include <string>
#include "librtmp/rtmp.h"
#include "VideoChannel.h"
#include "safe_queue.h"
#include "macro.h"
#include <x264.h>

VideoChannel *videoChannel = 0;
SafeQueue<RTMPPacket *> packets;
uint32_t start_time;
int isStart = 0;
int readPushing = 0;

pthread_t pid;

void callback(RTMPPacket *packet) {
    if (packet) {
        packet->m_nTimeStamp = RTMP_GetTime() - start_time;
        packets.push(packet);
    }
}

void releasePackets(RTMPPacket *&packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = 0;
    }
}

void *start(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do {
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("rtmp创建失败");
            break;
        }

        RTMP_Init(rtmp);
        rtmp->Link.timeout = 5;
        int ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("rtmp设置地址失败:%s", url);
            break;
        }

        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("rtmp连接地址失败:%s", url);
            break;
        }
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("rtmp连接流失败:%s", url);
            break;
        }
        readPushing = 1;
        start_time = RTMP_GetTime();
        packets.setWork(1);
        RTMPPacket *packet = 0;
        while (isStart) {
            packets.pop(packet);
            if (!isStart) {
                break;
            }
            if (!packet) {
                continue;
            }
            packet->m_nInfoField2 = rtmp->m_stream_id;

            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePackets(packet);
            if (!ret) {
                LOGE("发送数据失败");
                break;
            }
        }
        releasePackets(packet);
    } while (0);
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    delete url;
    return 0;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1init(JNIEnv *env, jobject instance) {

    videoChannel = new VideoChannel;
    videoChannel->setVideoCallback(callback);
    packets.setReleaseCallback(releasePackets);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1setVideoEncodeInfo(JNIEnv *env, jobject instance, jint w,
                                                             jint h, jint fps, jint bitrate) {

    if (videoChannel) {
        videoChannel->setVideoEncodeInfo(w, h, fps, bitrate);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1start(JNIEnv *env, jobject instance, jstring path_) {

    if (isStart) {
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    char *url = new char[strlen(path) + 1];
    strcpy(url, path);
    isStart = 1;
    pthread_create(&pid, 0, start, url);
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1pushVideo(JNIEnv *env, jobject instance,
                                                    jbyteArray data_) {
    if (!videoChannel || !readPushing) {
        return;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    videoChannel->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1stop(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_hzw_rtmp_live_LivePusher_native_1release(JNIEnv *env, jobject instance) {

    // TODO

}