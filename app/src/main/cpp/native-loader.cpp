#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "NativeLoader"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_phantom_loader_MainActivity_decryptDex(JNIEnv* env, jobject /* this */, jbyteArray encryptedDex) {
    jsize len = env->GetArrayLength(encryptedDex);
    jbyte* data = env->GetByteArrayElements(encryptedDex, nullptr);
    
    // Simulação de descriptografia (O ideal aqui é usar AES em C++, mas para compilar e testar o fluxo, vamos apenas copiar)
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, data);
    
    env->ReleaseByteArrayElements(encryptedDex, data, JNI_ABORT);
    LOGD("DEX Descriptografado no C++!");
    return result;
}
