#include <jni.h>
#include <string>
#include <android/log.h>
#include <assert.h>
#include <cstring>

#include "constants.h"

#define LOGV(TAG, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG,__VA_ARGS__)
#define LOGD(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)
#define LOGI(TAG, ...) __android_log_print(ANDROID_LOG_INFO   , TAG,__VA_ARGS__)
#define LOGW(TAG, ...) __android_log_print(ANDROID_LOG_WARN   , TAG,__VA_ARGS__)
#define LOGE(TAG, ...) __android_log_print(ANDROID_LOG_ERROR  , TAG,__VA_ARGS__)


#define JNIREG_CLASS "com/cylan/jiafeigou/support/Security"//指定要注册的类

extern "C" JNIEXPORT jstring JNICALL native_getQQKey(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = "";
    if (strcmp(content, "") == 0) {
        result = "1103156296";
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}

extern "C" jstring JNICALL native_getMtaKey(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    if (strcmp(content, "zhongxing") == 0) {
        std::string result = "ATQ1JV694AKT";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    } else {
        std::string result = "Aqc1103156296";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
}
extern "C" jstring JNICALL native_getWeChatKey(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = WECHAT_APP_KEY_YUN;
    if (strcmp(content, "test1") == 0) {
        result = "wx7b9d35f344db8841";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    if (strcmp(content, "yf") == 0) {
        result = "wx82878b30fc6c0a6d";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}
extern "C" jstring JNICALL native_getServerAddr(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = "yun";
    if (strcmp(content, "test1") == 0) {
        result = "test1";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}
extern "C" int JNICALL native_getServerPort(JNIEnv *env, jclass clazz, jstring content_) {
    return 443;
}
extern "C" jstring JNICALL native_getVKey(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = CYLAN_VKEY;
    if (strcmp(content, "zhongxing") == 0) {
        result = DOBY_VKEY;
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    if (strcmp(content, "cell_c") == 0) {
        result = CELL_C_VKEY;
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}
//根据包名的后缀,{"":官方版本} {"zhongxing":doby}
extern "C" jstring JNICALL native_getVId(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = CYLAN_VID;
    if (strcmp(content, "zhongxing") == 0) {
        result = DOBY_VID;
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    if (strcmp(content, "cell_c") == 0) {
        result = CELL_C_VID;
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}
extern "C" jstring JNICALL native_getSinaAppKey(JNIEnv *env, jclass clazz, jstring content_) {
    const char *content = env->GetStringUTFChars(content_, 0);
    if (content == NULL) {
        return NULL;
    }
    std::string result = "1315129656";
    if (strcmp(content, "") == 0) {
        result = "1315129656";
        env->ReleaseStringUTFChars(content_, content);
        return env->NewStringUTF(result.c_str());
    }
    env->ReleaseStringUTFChars(content_, content);
    return env->NewStringUTF(result.c_str());
}

//函数注册表
static JNINativeMethod gMethods[] = {
        {"getQQKey",        "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getQQKey},//绑定
        {"getMtaKey",       "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getMtaKey},//绑定
        {"getWeChatKey",    "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getWeChatKey},//绑定
        {"getServerPort",   "(Ljava/lang/String;)I",                  (void *) native_getServerPort},//绑定
        {"getServerPrefix", "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getServerAddr},//绑定
        {"getVKey",         "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getVKey},//绑定
        {"getVId",          "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getVId},//绑定
        {"getSinaAppKey",   "(Ljava/lang/String;)Ljava/lang/String;", (void *) native_getSinaAppKey},//绑定
};

/*
* Register several native methods for one class.
*/
static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("JNICALL", "clazz is null");
        return JNI_FALSE;
    }
    int ret = env->RegisterNatives(clazz, gMethods, numMethods);
    if (ret < 0) {
        LOGE("JNICALL", "RegisterNatives is null:%d", ret);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}


/*
* Register native methods for all classes we know about.
*/
static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

/*
* Set some test stuff up.
*
* Returns the JNI dpMsgVersion on success, -1 on failure.
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("JNICALL", "faied");
        return -1;
    }
    assert(env != NULL);

    if (!registerNatives(env)) {//注册
        LOGE("JNICALL", "registerNatives faied");
        return -1;
    }
    /* success -- return valid dpMsgVersion number */
    result = JNI_VERSION_1_4;

    return result;
}