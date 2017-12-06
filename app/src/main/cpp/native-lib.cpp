#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {

int toGray(Mat img, Mat& imgGray);

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


JNIEXPORT jint JNICALL
Java_paulygon_helloopencv_OpenCVNativeClass_convertGray(JNIEnv *env, jclass type, jlong matAddrRgba,
                                                        jlong matAddrGray) {

    //cv::Mat& mRgb = *(cv::Mat*)matAddrRgba; //this is what these lines would be without using namespace cv.
    Mat& mRgb = *(Mat*)matAddrRgba;
    Mat& mGray = *(Mat*)matAddrGray;

    int conv;

    jint retVal;

    conv = toGray(mRgb, mGray);

    retVal = (jint)conv;

    return retVal;
    }

int toGray(Mat img, Mat& imgGray){
    cvtColor(img, imgGray, CV_RGB2GRAY);

    if(imgGray.rows == img.rows && imgGray.cols == img.cols)
        return 1;

    return 0;
}

}

