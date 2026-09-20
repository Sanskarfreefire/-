#include <jni.h>
#include <string>
#include <cmath>
#include <android/log.h>
#include "offsets.h"

#define LOG_TAG "LUNAR"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_tools_systeminfo7_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "SR MODS ESP/Aimbot Engine Active";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_tools_systeminfo7_service_OverlayService_calculateAimbot(
        JNIEnv* env,
        jobject /* this */,
        jfloat crossX, jfloat crossY,
        jfloat targetX, jfloat targetY,
        jfloat fov, jfloat smooth) {
    float dx = targetX - crossX;
    float dy = targetY - crossY;
    float dist = sqrtf(dx * dx + dy * dy);
    if (dist <= fov && smooth > 0.0f) {
        float moveX = dx / smooth;
        float moveY = dy / smooth;
        long packed = (((long)(moveX * 1000)) & 0xFFFFFFFFL) << 32 | (((long)(moveY * 1000)) & 0xFFFFFFFFL);
        return packed;
    }
    return 0L;
}
