#include <jni.h>
#include <string>

extern "C"

JNIEXPORT jstring JNICALL
Java_io_jxcore_node_JXCore_getString(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
