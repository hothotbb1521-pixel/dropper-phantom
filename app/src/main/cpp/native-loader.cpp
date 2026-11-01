#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <vector>

#define LOG_TAG "NativeLoader"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Chave de descriptografia (igual ao engine.py)
const char* KEY = "S3cr3tK3yPh4nt0m2025!@#";

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_phantom_loader_NativeLoader_loadPayload(JNIEnv* env, jobject /* this */, jobject context) {
    LOGD("Iniciando leitura e descriptografia no C++...");

    // 1. Pega o AssetManager
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getAssetsMethod = env->GetMethodID(contextClass, "getAssets", "()Landroid/content/res/AssetManager;");
    jobject assetManager = env->CallObjectMethod(context, getAssetsMethod);
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

    // 2. Abre o arquivo payload.enc
    AAsset* asset = AAssetManager_open(mgr, "payload.enc", AASSET_MODE_BUFFER);
    if (asset == nullptr) {
        LOGD("Erro: payload.enc nao encontrado.");
        return nullptr;
    }

    // 3. Lê os bytes
    off_t length = AAsset_getLength(asset);
    std::vector<char> buffer(length);
    AAsset_read(asset, buffer.data(), length);
    AAsset_close(asset);

    // 4. Descriptografa (XOR simples para exemplo, o ideal é AES em C++)
    // NOTA: Para fazer AES em C++, precisaríamos da OpenSSL. Para evitar dependências e compilar de primeira, vamos usar um XOR reversível.
    // Na VPS, o Python precisa usar o mesmo XOR.
    for (int i = 0; i < length; i++) {
        buffer[i] ^= 0x42; // XOR com chave fixa
    }

    // 5. Retorna o array de bytes para o Java
    jbyteArray result = env->NewByteArray(length);
    env->SetByteArrayRegion(result, 0, length, (jbyte*)buffer.data());
    return result;
}
