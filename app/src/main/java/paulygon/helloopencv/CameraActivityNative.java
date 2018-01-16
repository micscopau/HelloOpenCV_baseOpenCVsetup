package paulygon.helloopencv;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class CameraActivityNative extends AppCompatActivity {

    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;

    private FrameLayout cameraPreviewLayout;
    private ImageView capturedImageHolder;

    private static String TAG = "CameraActivity";
    Mat mRgba, imgGray, imgCanny, imgCircles;
    Mat mRgbaF, mRgbaT; //o
    int circleCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_native);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraPreviewLayout = (FrameLayout)findViewById(R.id.camera_preview);
        capturedImageHolder = (ImageView)findViewById(R.id.captured_image);

        camera = checkDeviceCamera();
        mImageSurfaceView = new ImageSurfaceView(CameraActivityNative.this, camera);

        cameraPreviewLayout.addView(mImageSurfaceView);

        Button captureButton = (Button)findViewById(R.id.button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });
    }

    private Camera checkDeviceCamera(){
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if(bitmap==null){
                Toast.makeText(CameraActivityNative.this, "Captured image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            //capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, 300, 200 ));

            Camera.Parameters parameters;

            parameters = camera.getParameters();

            capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, parameters.getPreviewSize().width, parameters.getPreviewSize().height ));
            //capturedImageHolder.setImageBitmap(bitmap);

            capturedImageHolder.setVisibility(View.VISIBLE);
            camera.stopPreview();

        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    /*
    //Refer to AndroidCameraAPITutorial on utelization and implementation of this menu feature
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    public void circleCounterHelper(Mat in) {

        Mat input = in;

        Mat blur = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(input, imgGray, Imgproc.COLOR_RGB2GRAY, 0);

        //Imgproc.blur(input, blur, new Size(7,7), new Point(2,2));
        Imgproc.blur(imgGray, blur, new Size(7, 7)); //using default anchor of -1,-1 (kernel center)

        Imgproc.HoughCircles(blur, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 50, 100, 90, 0, 100);

        Log.i(TAG, String.valueOf("size: " + circles.cols()) + ", " + String.valueOf(circles.rows()));

        circleCount = 0;

        if (circles.cols() > 0) {
            //for (int i = 0 ; i < Math.min(circles.cols(), 5) ; i++){
            for (int i = 0; i < circles.cols(); i++) {
                double circleVec[] = circles.get(0, i);

                circleCount++;
                //circleCount = circleVec[2]

                if (circleVec == null) {
                    break;
                }

                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int radius = (int) circleVec[2];

                Imgproc.circle(input, center, 3, new Scalar(255, 255, 255), 5);
                Imgproc.circle(input, center, radius, new Scalar(255, 255, 255), 2);

            }

            Log.i(TAG, " Count of circles: " + circleCount);

        }
    }
}