#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "JXCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

static const jint JAVA_VERSION = JNI_VERSION_1_4;

static pthread_key_t sKey;
static JavaVM *sJavaVm = NULL;

jint JNI_OnLoad(JavaVM *javaVM, void *reserved) {
    LOGD("JNI_OnLoad");
    sJavaVm = javaVM;
    return JAVA_VERSION;
}

JNIEnv *getEnv() {
    JNIEnv *env = (JNIEnv *)pthread_getspecific(sKey);
    if (env == NULL) {
        jint result = sJavaVm->GetEnv(reinterpret_cast<void**>(&env), JAVA_VERSION);
        switch (result) {
            case JNI_OK:
                pthread_setspecific(sKey, env);
                break;
            case JNI_EDETACHED:
                if (sJavaVm->AttachCurrentThread(&env, NULL) < 0) {
                    LOGD("Failed to get the environment. Unable to attach thread.");
                } else {
                    pthread_setspecific(sKey, env);
                }
                break;
            case JNI_EVERSION:
                LOGD("Failed to get the environment. JAVA version 1.4 not supported.");
                break;
            default:
                LOGD("Failed to get the environment");
                break;
        }
    }

    return env;
}

JNIEXPORT jstring JNICALL
Java_io_jxcore_node_JXCore_getString(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

}
