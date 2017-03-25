package dft.hushplanes.android.AR_Tests.Camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dft.hushplanes.android.AR_Tests.GL.OverlayRenderer;

/**
 * Created by hackathon on 25/03/2017.
 */

public class Preview extends TextureView {
    private CameraDevice mCamera;
    private Surface previewSurface;  //The surface to which the preview will be drawn.
    private Size[] mSizes; //The sizes supported by the Camera. 1280x720, 1024x768, etc.  This must be set.
    private CaptureRequest.Builder mRequestBuilder;  //Builder to create a request for a camera capture.
    private Context context;

    //mTextureView.setSurfaceTextureListener(surfaceTextureListener);

    public Preview(Context context){
        super(context);
        this.context = context;
        setSurfaceTextureListener(surfaceTextureListener);

    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        /*The surface texture is available, so this is where we will create and open the camera, as
        well as create the request to start the camera preview.
         */
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            previewSurface = new Surface(surface);

            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            try {
                CameraCharacteristics characteristics =
                        cameraManager.getCameraCharacteristics("0");

                /*A map that contains all the supported sizes and other information for the camera.
                Check the documentation for more information on what is available.
                 */
                StreamConfigurationMap streamConfigurationMap = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);

                /*Request that the manager open and create a camera object.
                cameraDeviceCallback.onOpened() is called now to do this.
                 */
                if ( ContextCompat.checkSelfPermission( context, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {

                    ActivityCompat.requestPermissions((Activity)context, new String[] {  Manifest.permission.CAMERA  }, 4
                            );
                }
                cameraManager.openCamera("0", cameraDeviceCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * Callbacks to notify us of the status of the Camera device.
     */
    CameraDevice.StateCallback cameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            /*
            This where we create our capture session.  Our Camera is ready to go.
             */
            mCamera = camera;

            try {
                //Used to create the surface for the preview.
                SurfaceTexture surfaceTexture = getSurfaceTexture();

                                     /*VERY IMPORTANT.  THIS MUST BE SET FOR THE APP TO WORK.  THE CAMERA NEEDS TO KNOW ITS PREVIEW SIZE.*/
                surfaceTexture.setDefaultBufferSize(mSizes[2].getWidth(), mSizes[2].getHeight());

                /*A list of surfaces to which we would like to receive the preview.  We can specify
                more than one.*/
                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(previewSurface);

                /*We humbly forward a request for the camera.  We are telling it here the type of
                capture we would like to do.  In this case, a live preview.  I could just as well
                have been CameraDevice.TEMPLATE_STILL_CAPTURE to take a singe picture.  See the CameraDevice
                docs.*/
                mRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mRequestBuilder.addTarget(previewSurface);

                //A capture session is now created. The capture session is where the preview will start.
                camera.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, new Handler());

            } catch (CameraAccessException e) {
                Log.e("Camera Exception", e.getMessage());
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    /**
     * The CameraCaptureSession.StateCallback class  This is where the preview request is set and started.
     */
    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new android.hardware.camera2.CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                /* We humbly set a repeating request for images.  i.e. a preview. */
                session.setRepeatingRequest(mRequestBuilder.build(), cameraCaptureSessionCallback, new Handler());
            } catch (CameraAccessException e) {
                Log.e("Camera Exception", e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    private CameraCaptureSession.CaptureCallback cameraCaptureSessionCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };


}
