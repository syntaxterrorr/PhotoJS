#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_atharva_photo_1c_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
/*extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_atharva_photo_1js_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO


    return env->NewStringUTF(returnValue);
}*/