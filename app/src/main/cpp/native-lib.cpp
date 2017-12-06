#include <jni.h>
#include <string>
#include <opencv2/core.hpp>

extern "C" {
JNIEXPORT jstring

JNICALL
Java_paulygon_helloopencv_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());

}


JNIEXPORT jstring JNICALL
Java_paulygon_helloopencv_MainActivity_validate(JNIEnv *env, jobject instance, jlong matAddrGr,
                                                jlong matAddrRgba) {

    cv::Rect();
    cv::Mat();
    std::string hello2 = "Hello from Validate!";
    return env->NewStringUTF(hello2.c_str());
    }
}