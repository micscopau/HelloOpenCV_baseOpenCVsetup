package paulygon.helloopencv;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

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
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();

    }


    public void buttonOpenCVCameraClick(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);

    }

    public void buttonNativeCameraClick(View view) {
        Intent intent = new Intent(this, CameraActivityNative.class);
        startActivity(intent);
    }

    public void buttonCamera2apiClick(View view) {
        Intent intent = new Intent(this, Camera2Activity.class);
        startActivity(intent);
    }
}
