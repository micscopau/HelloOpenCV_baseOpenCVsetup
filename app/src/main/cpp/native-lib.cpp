#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" {

int toGray(Mat img, Mat& imgGray);
int countCircles(Mat img, Mat& imgCircles);

JNIEXPORT jstring

JNICALL
Java_paulygon_helloopencv_CameraActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());

}


JNIEXPORT jstring JNICALL
Java_paulygon_helloopencv_CameraActivity_validate(JNIEnv *env, jobject instance, jlong matAddrGr,
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


JNIEXPORT jint JNICALL
Java_paulygon_helloopencv_OpenCVNativeClass_circles(JNIEnv *env, jclass type, jlong matAddrRgba, jlong matAddrCircles) {

    Mat& mRgb = *(Mat*)matAddrRgba;
    Mat& mCircles = *(Mat*)matAddrCircles;


    jint circleCount;
    int count;

    count = countCircles(mRgb,mCircles);

    circleCount = (jint)count;

    return circleCount;

}

int countCircles(Mat img, Mat& imgCircles){
    /*
    Mat src = img;
    Mat gray;


    cvtColor(src, gray, COLOR_RGB2GRAY);
    medianBlur(gray, gray,5);
    vector<Vec3f> circles;

    HoughCircles(gray, circles, HOUGH_GRADIENT, 1, gray.rows/16, 100,30,1,30);


   for(size_t i = 0; i < circles.size() ; i++){
       Vec3i c = circles[i];
       Point center = Point(c[0], c[1]);
       //circle center
       circle(src, center, 1, Scalar(0,100,100), 3, LINE_AA);
       int radius = c[2];
       circle(src, center, 1, Scalar(255,0,255), 3, LINE_AA);
   }
   imshow("detected circles", src);
    waitKey();
     */

    Mat gray;

    cvtColor(img, gray, COLOR_RGB2GRAY);
    medianBlur(gray, gray,5);
    vector<Vec3f> circles;

    HoughCircles(gray, circles, HOUGH_GRADIENT, 1, gray.rows/16, 100,30,1,30);


    for(size_t i = 0; i < circles.size() ; i++){
        Vec3i c = circles[i];
        Point center = Point(c[0], c[1]);
        //circle center
        circle(imgCircles, center, 1, Scalar(0,100,100), 3, LINE_AA);
        int radius = c[2];
        circle(imgCircles, center, 1, Scalar(255,0,255), 3, LINE_AA);
    }
    //imshow("detected circles", imgCircles);
    //waitKey();

    return circles.size();
}

}
