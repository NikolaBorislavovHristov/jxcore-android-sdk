#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "JXCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

static const jint JAVA_VERSION = JNI_VERSION_1_4;

typedef struct {
    JNIEnv *env;
    jclass clazz;
    jmethodID methodID;
} JniMethodInfo;

static pthread_key_t sKey;
static JavaVM *sJavaVm = NULL;
static jobject sClassloader;
static JniMethodInfo sLoadClassMethodInfo;

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

bool getMethodInfo(JniMethodInfo &methodInfo, const char *className, const char *methodName, const char *methodSignature) {
    if (className == NULL || methodName == NULL || methodSignature == NULL) {
        return false;
    }

    JNIEnv *env = ::getEnv();
    if (env == NULL) {
        return false;
    }

    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGD("Failed to find class %s", className);
        return false;
    }

    jmethodID methodID = env->GetMethodID(clazz, methodName, methodSignature);
    if (methodID == NULL) {
        LOGD("Failed to find method id of %s", methodName);
        return false;
    }

    methodInfo.env = env;
    methodInfo.clazz = clazz;
    methodInfo.methodID = methodID;
    return true;
}

JNIEXPORT void JNICALL
Java_io_jxcore_node_JXCore_setNativeContext(JNIEnv *env, jobject thiz, jobject context) {
    JniMethodInfo getClassLoaderMethodInfo;
    ::getMethodInfo(getClassLoaderMethodInfo, "android/content/Context", "getClassLoader", "()Ljava/lang/ClassLoader;");
    jobject classLoader = env->CallObjectMethod(context, getClassLoaderMethodInfo.methodID);
    ::sClassloader = env->NewGlobalRef(classLoader);
    
    JniMethodInfo loadClassMethodInfo;
    ::getMethodInfo(loadClassMethodInfo, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    ::sLoadClassMethodInfo = loadClassMethodInfo;
}

}
