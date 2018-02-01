package paulygon.helloopencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static org.opencv.core.Core.BORDER_DEFAULT;


public class CapturedPictureActivity extends AppCompatActivity {

    private static String TAG = "CapturedPictureActivity";

    private Intent inIntent;
    private byte[] imageBytes;
    private float bitmapRotation;
    private ImageView imageView;
    private TextView textViewPipCount;

    Mat mRgba, imgGray, imgCanny, imgCircles;

    Mat mRgbaF, mRgbaT; //o

    int circleCountOriginal = 0;
    int circleCount = 0;
    int ellipseCount = 0;

    private Bitmap circleBitmap;
    private Bitmap ellipseBitmap;


    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:{
                    //javaCameraView.enableView();
                    //javaCameraView.setOnTouchListener(OpenCVActivity.this);
                    //mOpenCvCameraView.enableView(); //o
                    //mOpenCvCameraView.setOnTouchListener(OpenCVActivity.this);
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

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
        setContentView(R.layout.activity_captured_picture);

        imageView = (ImageView) findViewById(R.id.captured_image);
        assert imageView != null;

        imgGray = new Mat();

        textViewPipCount = (TextView) findViewById(R.id.textViewPipCount);

        inIntent = getIntent();

        String intentTag = inIntent.getStringExtra("TAG");

        if (intentTag.equals("Camera2Activity")) {
            // calling from Camera2activity (my code)
            imageBytes = inIntent.getByteArrayExtra(Camera2Activity.CAPTURED_IMAGE);
            bitmapRotation = inIntent.getFloatExtra(Camera2Activity.BITMAP_ROTATION, 0);

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Bitmap rotatedBitmap = RotateBitmap(bitmap, bitmapRotation);

            circleBitmap = circleCounterHelper(rotatedBitmap);

        }
        else if (intentTag.equals("Camera2BasicFragment")) {
        /*
        //Calling from Camera2BasicFragment
        imageBytes = inIntent.getByteArrayExtra(Camera2BasicFragment.CAPTURED_IMAGE);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        circleBitmap = circleCounterHelper(bitmap);
        */

            Uri imageUri = Uri.parse(inIntent.getStringExtra(Camera2BasicFragment.FILE_URI));
            int rotation = inIntent.getIntExtra(Camera2BasicFragment.ROTATION, 0);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap = RotateBitmap(bitmap, rotation);

                circleBitmap = circleCounterHelper(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        imageView.setImageBitmap(circleBitmap);
        updatePipCount();

    }


    public void fabAcceptClick(View view) {
        Toast.makeText(this, "Accepting Pip Count of :" + circleCount, Toast.LENGTH_LONG).show();
    }

    public void fabCameraClick(View view) {
        //Intent outIntent = new Intent(this, Camera2Activity.class);
        Intent outIntent = new Intent(this, CameraActivity.class);
        startActivity(outIntent);
    }

    public void bttnMinusFiftyClick(View view) {
        circleCount-=50;
        updatePipCount();
    }

    public void bttnMinusOneClick(View view) {
        circleCount--;
        updatePipCount();
    }

    public void bttnPlusOneClick(View view) {
        circleCount++;
        updatePipCount();
    }

    public void bttnPlusFiftyClick(View view) {
        circleCount+=50;
        updatePipCount();
    }

    public void bttnReset(View view) {
        circleCount = circleCountOriginal;
        updatePipCount();
    }

    public void updatePipCount(){
        textViewPipCount.setText(Integer.toString(circleCount));
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //public void circleCounterHelper(byte[] data){
    public Bitmap circleCounterHelper(Bitmap bitmap){

        //Mat input = new Mat();//Mat(width, height, CvType.CV_8UC4);
        //input.put(0,0,data);

        Mat input = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, input);

        Mat matBlur = new Mat();
        Mat matSharp = new Mat();
        Mat matFilter = new Mat();
        Mat matCircles = new Mat();
        Mat matThresh = new Mat();
        Mat matOpened = new Mat();
        Mat matClosed = new Mat();
        Mat matCanny = new Mat();
        Mat matOutput = new Mat();

        /*
        //Refinement for open/close morphology kernel (I think)
        int morph_elem = 0;
        int morph_size = 0;
        int morph_operator = 0;
        Mat kernel = Imgproc.getStructuringElement( morph_elem, Size( 2*morph_size + 1, 2*morph_size+1 ), Point( morph_size, morph_size ));
        */
        //Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
        int kernelLength = 99;


        //Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY,0);


        //Imgproc.Canny(imgGray, matCanny, 20, 200, 3, true);


        //Imgproc.blur(input, matBlur, new Size(7,7), new Point(2,2));
        //Imgproc.blur(imgGray, matBlur, new Size(5,5)); //using default anchor of -1,-1 (kernel center)

        /*
        //This combination worked!
        Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY,0);
        Imgproc.bilateralFilter(imgGray, matFilter, 30, 90, 5);
        */

        Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY,0);
        //Imgproc.bilateralFilter(imgGray, matFilter, -1, 60, 60);
        //Imgproc.bilateralFilter(imgGray, matFilter, kernelLength, kernelLength*2, kernelLength/2);

        //Imgproc.bilateralFilter(imgGray, matFilter, 60, 50, 25); //works but slowly
        //Imgproc.bilateralFilter(imgGray, matFilter, 40, 50, 25); //little bit quicker
        //Imgproc.bilateralFilter(imgGray, matFilter, 20, 100, 50); //works and decent speed
        //Imgproc.bilateralFilter(imgGray, matFilter, 20, 50, 75); //works and decent speed
        //Imgproc.bilateralFilter(imgGray, matFilter, 30, 50, 75); //works but a little slow
        Imgproc.bilateralFilter(imgGray, matFilter, 20, 50, 75);

        //Imgproc.bilateralFilter(imgGray, matFilter, 60, 100, 25); //works but slow
        /*
        doesn't work
        Imgproc.bilateralFilter(imgGray, matFilter, 90, 154, 25);
        Imgproc.bilateralFilter(imgGray, matFilter, 60, 154, 25);
         */

        //System.out.println("MSPDEBUG : pre thresholding");
        //Imgproc.threshold(imgGray, matThresh,127,255,Imgproc.THRESH_BINARY);
        //Imgproc.threshold(imgGray, matThresh,154,255,Imgproc.THRESH_BINARY_INV);
        //Imgproc.threshold(imgGray, matThresh,154,255,Imgproc.THRESH_TOZERO_INV);

        //Imgproc.threshold(matFilter, matThresh,154,255,Imgproc.THRESH_TOZERO);
        //Imgproc.threshold(matFilter, matThresh,127,255,Imgproc.THRESH_BINARY);

        //Imgproc.threshold(imgGray, matThresh,127,255,Imgproc.THRESH_TOZERO);

        //Imgproc.bilateralFilter(matThresh, matFilter, -1, 60, 60);

        //Imgproc.morphologyEx(matThresh, matOpened, Imgproc.MORPH_DILATE, kernel);
        //Imgproc.morphologyEx(matThresh, matOpened, Imgproc.MORPH_DILATE, kernel, new Point(-1,-1), 9);

        //Imgproc.morphologyEx(matThresh, matClosed, Imgproc.MORPH_ERODE, kernel);


        //Imgproc.morphologyEx(matThresh, matClosed, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), 9);
        //Imgproc.morphologyEx(matClosed, matOpened, Imgproc.MORPH_DILATE, kernel, new Point(-1,-1), 9);

        //Imgproc.morphologyEx(matThresh, matOpened, Imgproc.MORPH_DILATE, kernel, new Point(-1,-1), 9);
        //Imgproc.morphologyEx(matOpened, matClosed, Imgproc.MORPH_ERODE, kernel, new Point(-1,-1), 9);

        matOutput = matFilter;

        //Imgproc.adaptiveThreshold(imgGray, matThresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,11,2);
        //Imgproc.adaptiveThreshold(imgGray, matThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,11,2);
        //Imgproc.adaptiveThreshold(imgGray, matThresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,49,2);

        //System.out.println("MSPDEBUG : post thresholding");

        //Imgproc.morphologyEx(matThresh, matOpened, Imgproc.MORPH_OPEN, new Mat());
        //Imgproc.morphologyEx(matThresh, matClosed, Imgproc.MORPH_CLOSE, new Mat());

        //Imgproc.morphologyEx(matThresh, matOpened, Imgproc.MORPH_OPEN, kernel);
        //Imgproc.morphologyEx(matThresh, matClosed, Imgproc.MORPH_CLOSE, kernel);

        /*---------------------------*/

        //Imgproc.HoughCircles(matBlur, matCircles, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 90, 0, 100);

        Imgproc.HoughCircles(matOutput, matCircles, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 90, 1, 100);

        //Imgproc.HoughCircles(matThresh, matCircles, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 90, 0, 100);

        //Log.i(TAG, String.valueOf("size: " + circles.cols()) + ", " + String.valueOf(circles.rows()))

        circleCount = 0;

        System.out.println("MSPDEBUG : pre circle count");

        if(matCircles.cols() >0){
            //for (int i = 0 ; i < Math.min(circles.cols(), 5) ; i++){
            for (int i = 0 ; i < matCircles.cols() ; i++){
                double circleVec[] = matCircles.get(0,i);

                circleCount++;
                //circleCount = circleVec[2]

                if (circleVec == null){
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 1, new Scalar(255,255,255),110); //center
                Imgproc.circle(input, center, 60, new Scalar(0,0,0), 10); //ring

            }

            //Log.i(TAG, " Count of circles: " + circleCount);

        }

        System.out.println("MSPDEBUG : post circle count");
        circleCountOriginal = circleCount;

        Utils.matToBitmap(input, bmp32);
        //Utils.matToBitmap(matOutput, bmp32);

        return bmp32;

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

}
