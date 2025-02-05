#include <string>
#include <android/log.h>
#include "android/asset_manager.h"
#include "android/asset_manager_jni.h"
#include "../../../jxcore-binaries/jx.h"

#ifndef LOGD
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "JXCore", __VA_ARGS__)
#endif

extern "C" {

AAssetManager *mAssetManager;
std::string mAssetsFilesTree;

void assetExistsSync(JXValue *argv, int argc) {
    char *filename = JX_GetString(&argv[0]);
    AAsset *asset = AAssetManager_open(mAssetManager, filename, AASSET_MODE_UNKNOWN);
    bool exists = asset != NULL;
    JX_SetBoolean(&argv[argc], exists);
    free(filename);
    if (exists) {
        AAsset_close(asset);
    }
}

void assetReadSync(JXValue *argv, int argc) {
    char *filename = JX_GetString(&argv[0]);
    AAsset *asset = AAssetManager_open(mAssetManager, filename, AASSET_MODE_UNKNOWN);
    if (asset != NULL) {
        off_t assetSize = AAsset_getLength(asset);
        char *assetBuffer = (char*) malloc(sizeof(char)*assetSize);
        int readLength = AAsset_read(asset, assetBuffer, assetSize);
        JX_SetBuffer(&argv[argc], assetBuffer, readLength);
        AAsset_close(asset);
        free(assetBuffer);
    } else {
        const char *err = "File does not exist";
        JX_SetError(&argv[argc], err, strlen(err));
    }

    free(filename);
}

void assetReadDirSync(JXValue *argv, int argc) {
    JX_SetJSON(&argv[argc], mAssetsFilesTree.c_str(), mAssetsFilesTree.length());
}

JNIEXPORT void JNICALL
Java_io_jxcore_node_JXCore_initializeEngine(JNIEnv *env, jobject thiz, jobject assetManager_, jstring assetsPath_, jstring assetsFilesTree_, jstring mainFileContent_) {
    const char *assetsPath = env->GetStringUTFChars(assetsPath_, 0);
    const char *assetsFilesTree = env->GetStringUTFChars(assetsFilesTree_, 0);
    const char *mainFileContent = env->GetStringUTFChars(mainFileContent_, 0);

    mAssetsFilesTree = assetsFilesTree;
    mAssetManager = AAssetManager_fromJava(env, assetManager_);

    JX_InitializeOnce(assetsPath);
    JX_InitializeNewEngine();
    JX_DefineExtension("assetExistsSync", assetExistsSync);
    JX_DefineExtension("assetReadSync", assetReadSync);
    JX_DefineExtension("assetReadDirSync", assetReadDirSync);
    JX_DefineMainFile(mainFileContent);
    
    if (JX_IsSpiderMonkey()) {
        LOGD("Engine initialized with spidermonkey");
    } else if (JX_IsV8()) {
        LOGD("Engine initialized with v8");
    }

    env->ReleaseStringUTFChars(assetsPath_, assetsPath);
    env->ReleaseStringUTFChars(assetsFilesTree_, assetsFilesTree);
    env->ReleaseStringUTFChars(mainFileContent_, mainFileContent);
}

JNIEXPORT void JNICALL
Java_io_jxcore_node_JXCore_stopEngine(JNIEnv *env, jobject thiz) {
    JX_StopEngine();
}

JNIEXPORT void JNICALL
Java_io_jxcore_node_JXCore_startEngine(JNIEnv *env, jobject thiz) {
    JX_StartEngine();
}

JNIEXPORT void JNICALL
Java_io_jxcore_node_JXCore_loopOnce(JNIEnv *env, jobject thiz) {
    JX_LoopOnce();
}

}