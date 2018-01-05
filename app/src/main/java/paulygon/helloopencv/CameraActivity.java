package paulygon.helloopencv;

import android.nfc.Tag;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static String TAG = "CameraActivity";
    JavaCameraView javaCameraView;
    Mat mRgba, imgGray, imgCanny, imgCircles;


    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    javaCameraView.setOnTouchListener(CameraActivity.this);
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String validate(long matAddrGr, long matAddrRgba);


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");

        if(OpenCVLoader.initDebug()){
            Log.i(TAG, " OpenCV loaded successfully");
        }
        else{
            Log.i(TAG, " OpenCV not loaded.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);



//        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
//
//        if(!OpenCVLoader.initDebug()){
//            tv.setText(tv.getText() + "\n OpenCVLoader.initDebug() not working.");
//        } else{
//            tv.setText(tv.getText() + "\n OpenCVLoader.initDebug() working!");
//            tv.setText(tv.getText() + "\n" + validate(0L, 0L));
//        }




        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setCameraIndex(0); //0=rear 1=front
        javaCameraView.setVisibility(SurfaceView.VISIBLE);

        javaCameraView.setCvCameraViewListener(this);

    }


    @Override
    protected void onPause(){
        super.onPause();

        if (javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (javaCameraView!=null){
            javaCameraView.disableView();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();

        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallBack);
        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4); //8UC 4 the 4 because 4 channel; r,g,b,a
        imgGray =  new Mat(height, width, CvType.CV_8UC1);
        imgCanny =  new Mat(height, width, CvType.CV_8UC1);
        imgCircles = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        circleCounterHelper();

        return mRgba;
        //return inputFrame.rgba();

        /*
        mRgba = inputFrame.rgba();

        int circles = 0;
        Log.i(TAG, " Circle Count (pre function call) " + circles);

        imgCircles.empty();

        circles = OpenCVNativeClass.circles(mRgba.getNativeObjAddr(), imgCircles.getNativeObjAddr());

        Log.i(TAG, " Circle Count " + circles);
        return imgCircles;
        */

        //SystemClock.sleep(100);

        /*
        Mat input = inputFrame.gray();
        Mat blur = new Mat();
        Mat circles = new Mat();

        //Imgproc.blur(input, blur, new Size(7,7), new Point(2,2));
        Imgproc.blur(input, blur, new Size(7,7)); //using default anchor of -1,-1 (kernel center)
        Imgproc.HoughCircles(blur, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 90, 0, 1000);

        Log.i(TAG, String.valueOf("size: " + circles.cols()) + ", " + String.valueOf(circles.rows()));

        int circleCount = 0;

        if(circles.cols() >0){
            //for (int i = 0 ; i < Math.min(circles.cols(), 5) ; i++){
            for (int i = 0 ; i < circles.cols() ; i++){
                double circleVec[] = circles.get(0,i);

                circleCount++;
                //circleCount = circleVec[2]

                if (circleVec == null){
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 3, new Scalar(255,255,255),5);
                Imgproc.circle(input, center, radius, new Scalar(255,255,255), 2);

            }

            Log.i(TAG, " Count of circles: " + circleCount);
        }


        circles.release();
        input.release();
        return inputFrame.rgba();
        */

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        Log.i(TAG, "onTouch event");
        circleCounterHelper();
        /*
        //Mat input = inputFrame.gray();
        Mat input = mRgba;
        Mat blur = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY,0);

        //Imgproc.blur(input, blur, new Size(7,7), new Point(2,2));
        Imgproc.blur(imgGray, blur, new Size(7,7)); //using default anchor of -1,-1 (kernel center)

        Imgproc.HoughCircles(blur, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 25, 100, 90, 0, 100);

        Log.i(TAG, String.valueOf("size: " + circles.cols()) + ", " + String.valueOf(circles.rows()));

        int circleCount = 0;

        if(circles.cols() >0){
            //for (int i = 0 ; i < Math.min(circles.cols(), 5) ; i++){
            for (int i = 0 ; i < circles.cols() ; i++){
                double circleVec[] = circles.get(0,i);

                circleCount++;
                //circleCount = circleVec[2]

                if (circleVec == null){
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 3, new Scalar(255,255,255),5);
                Imgproc.circle(input, center, radius, new Scalar(255,255,255), 2);

            }

            Log.i(TAG, " Count of circles: " + circleCount);
        }
*/

        return false;
    }

    public void circleCounterHelper(){
        //Mat input = inputFrame.gray();
        Mat input = mRgba;
        Mat blur = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY,0);

        //Imgproc.blur(input, blur, new Size(7,7), new Point(2,2));
        Imgproc.blur(imgGray, blur, new Size(7,7)); //using default anchor of -1,-1 (kernel center)

        Imgproc.HoughCircles(blur, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 90, 0, 100);

        Log.i(TAG, String.valueOf("size: " + circles.cols()) + ", " + String.valueOf(circles.rows()));

        int circleCount = 0;

        if(circles.cols() >0){
            //for (int i = 0 ; i < Math.min(circles.cols(), 5) ; i++){
            for (int i = 0 ; i < circles.cols() ; i++){
                double circleVec[] = circles.get(0,i);

                circleCount++;
                //circleCount = circleVec[2]

                if (circleVec == null){
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 3, new Scalar(255,255,255),5);
                Imgproc.circle(input, center, radius, new Scalar(255,255,255), 2);

            }

            Log.i(TAG, " Count of circles: " + circleCount);
        }
    }
}
