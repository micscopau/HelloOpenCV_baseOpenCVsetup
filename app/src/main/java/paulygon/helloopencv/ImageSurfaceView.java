package paulygon.helloopencv;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = "ImageSurfaceView";

    private Camera mCamera;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder mHolder;

    /*
    public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
    }

    */


    public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            this.mCamera.setPreviewDisplay(holder);
            setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //this.mCamera.startPreview();
            startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try{
            mCamera.stopPreview();
        }catch(Exception e){
            //do nothing, no preview to stop.
        }

        //Camera.Parameters parameters = mCamera.getParameters();

        int orientation = getResources().getConfiguration().orientation;
        int degrees = 0;

        switch(orientation) {
            case Surface.ROTATION_0:
                degrees = 0 ;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }


        degrees = degrees % 360;
        mCamera.setDisplayOrientation(degrees);


        setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //mCamera.startPreview();
        startPreview();
        surfaceCreated(this.surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mCamera.stopPreview();
        this.mCamera.release();
    }

    private void setFocus(String mParameter){
        Camera.Parameters mParameters = mCamera.getParameters();
        mParameters.setFocusMode(mParameter);
        mCamera.setParameters(mParameters);
    }


    private void startPreview() {
        try {
            /**
             * Orientation should be adjusted, see http://stackoverflow.com/questions/20064793/how-to-fix-camera-orientation/26979987#26979987
             */

            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = null;
            float closestRatio = Float.MAX_VALUE;

            int targetPreviewWidth = isLandscape() ? getWidth() : getHeight();
            int targetPreviewHeight = isLandscape() ? getHeight() : getWidth();
            float targetRatio = targetPreviewWidth / (float) targetPreviewHeight;

            Log.v(TAG, "target size: " + targetPreviewWidth + " / " + targetPreviewHeight + " ratio:" + targetRatio);
            for (Camera.Size candidateSize : previewSizes) {
                float whRatio = candidateSize.width / (float) candidateSize.height;
                if (previewSize == null || Math.abs(targetRatio - whRatio) < Math.abs(targetRatio - closestRatio)) {
                    closestRatio = whRatio;
                    previewSize = candidateSize;
                }
            }

            Log.v(TAG, "preview size: " + previewSize.width + " / " + previewSize.height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(mHolder);
            setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public boolean isLandscape(){
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

}
