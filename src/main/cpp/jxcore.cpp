#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "JXCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C"

JNIEXPORT jstring JNICALL
Java_io_jxcore_node_JXCore_getString(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
