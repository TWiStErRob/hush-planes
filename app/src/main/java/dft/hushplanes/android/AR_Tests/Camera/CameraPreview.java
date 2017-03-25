package dft.hushplanes.android.AR_Tests.Camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.Collection;
import java.util.Collections;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.*;
import android.os.*;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.Toast;




@UiThread
// Deprecation warnings are constrained to methods by using FQCNs, because suppression doesn't work on imports.
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public interface CameraPreviewListener {
        void onCreate(CameraPreview preview);
        void onResume(CameraPreview preview);
        void onShutter(CameraPreview preview);
        void onPause(CameraPreview preview);
        void onDestroy(CameraPreview preview);
    }

    public static class CameraPreviewListenerAdapter implements CameraPreviewListener {
        @Override public void onCreate(CameraPreview preview) {
            // optional override
        }
        @Override public void onResume(CameraPreview preview) {
            // optional override
        }
        @Override public void onShutter(CameraPreview preview) {
            // optional override
        }
        @Override public void onPause(CameraPreview preview) {
            // optional override
        }
        @Override public void onDestroy(CameraPreview preview) {
            // optional override
        }
    }

    @WorkerThread
    public interface CameraPictureListener {
        /**
         * @param success whether auto-focus succeeded.
         *                If there's no auto-focus it always succeeds with {@code true}.
         *                If the focus call failed with an exception it'll be {@code false}.
         * @return whether you want to continue by taking a picture, {@code return success;} is a reasonable implementation
         */
        boolean onFocus(boolean success);
        void onTaken(@Nullable byte... image);
    }

    private final MissedSurfaceEvents missedEvents = new MissedSurfaceEvents();
    private final CameraThreadHandler thread = new CameraThreadHandler();
    private @Nullable CameraHolder cameraHolder = null;
    private @NonNull CameraPreviewListener listener = new CameraPreviewListenerAdapter();

    public CameraPreview(Context context) {
        super(context);
        Log.d("CameraPreview", "");
        getHolder().addCallback(this);
        getHolder().addCallback(thread);
        initCompat();
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    private void initCompat() {
        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    public void setListener(@Nullable CameraPreviewListener listener) {
        this.listener = listener != null? listener : new CameraPreviewListenerAdapter();
    }

    public @SuppressWarnings("deprecation") android.hardware.Camera getCamera() {
        return cameraHolder != null? cameraHolder.camera : null;
    }

    public boolean isRunning() {
        return cameraHolder != null;
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        Log.d("CameraPreview","{} surfaceCreated({}) {}" + cameraHolder + holder);
        if (cameraHolder != null) {
            usePreview();
        } else {
            missedEvents.surfaceCreated(holder);
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d("CameraPreview","{} surfaceChanged({}, format={}, w={}, h={}) {}" + cameraHolder + holder + format + w + h);
        if (cameraHolder != null) {
            stopPreview();
            updatePreview(w, h);
            startPreview();
        } else {
            missedEvents.surfaceChanged(holder, format, w, h);
        }
    }

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("CameraPreview","{} surfaceDestroyed({})" +  cameraHolder + holder);
        if (cameraHolder != null) {
            stopPreview();
            releaseCamera();
        } else {
            missedEvents.surfaceDestroyed(holder);
        }
    }

    private void usePreview() {
        Log.d("CameraPreview","{} Using preview"+ cameraHolder);
        try {
            if (cameraHolder != null) {
                Log.d("CameraPreview","setPreviewDisplay {}" + getHolder());
                cameraHolder.camera.setPreviewDisplay(getHolder());
            }
        } catch (RuntimeException | IOException ex) {
            Log.d("CameraPreview", "Error setting up camera preview" + ex);
        }
    }

    private void updatePreview(int w, int h) {
        Log.d("CameraPreview", "{} Updating preview" + cameraHolder);
        if (cameraHolder == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int displayDegrees = calculateDisplayOrientation(getContext(), cameraHolder.cameraInfo);
        int cameraDegrees = calculateRotation(getContext(), cameraHolder.cameraInfo);
        boolean landscape = displayDegrees % 180 == 0;
        if (!landscape) {
            int temp = width;
            //noinspection SuspiciousNameCombination we need to swap them when orientation is portrait
            width = height;
            height = temp;
        }

        // if (cameraHolder.cameraInfo.facing == CAMERA_FACING_FRONT) setScaleX(-1); doesn't work
        // @see http://stackoverflow.com/a/10390407/253468#comment63748074_10390407

        @SuppressWarnings("deprecation") android.hardware.Camera.Size previewSize =
                getOptimalSize(cameraHolder.params.getSupportedPreviewSizes(), width, height);
        @SuppressWarnings("deprecation") android.hardware.Camera.Size pictureSize =
                getOptimalSize(cameraHolder.params.getSupportedPictureSizes(), width, height);

        cameraHolder.params.setPreviewSize(previewSize.width, previewSize.height);
        cameraHolder.params.setPictureSize(pictureSize.width, pictureSize.height);
        cameraHolder.params.setRotation(cameraDegrees);
        cameraHolder.params.set("orientation", landscape? "landscape" : "portrait");
        cameraHolder.camera.setParameters(cameraHolder.params);
        cameraHolder.camera.setDisplayOrientation(displayDegrees);
    }

    private void releaseCamera() {
        Log.d("CameraPreview", "{} Releasing camera"+ cameraHolder);
        if (cameraHolder != null) {
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            Log.d("CameraPreview", "Releasing {}" + cameraHolder.camera);
            cameraHolder.camera.release();
            finished();
        }
    }

    private void startPreview() {
        Log.d("CameraPreview", "{} Starting preview" + cameraHolder);
        try {
            if (cameraHolder != null) {
                cameraHolder.camera.startPreview();
                listener.onResume(this);
            }
        } catch (RuntimeException ex) {
            Log.d("CameraPreview", "Error starting camera preview" + ex);
        }
    }

    private void stopPreview() {
        Log.d("CameraPreview", "{} Stopping preview" + cameraHolder);
        try {
            if (cameraHolder != null) {
                cameraHolder.camera.stopPreview();
                listener.onPause(this);
            }
        } catch (RuntimeException ex) {
            Log.d("CameraPreview", "ignore: tried to stop a non-existent preview" + ex);
        }
    }

    private void started(CameraHolder holder) {
        cameraHolder = holder;
        missedEvents.replay();
        listener.onCreate(this);
    }

    private void finished() {
        cameraHolder = null;
        listener.onDestroy(this);
    }

    @SuppressWarnings("deprecation")
    public Boolean isFrontFacing() {
        return cameraHolder == null? null
                : cameraHolder.cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * Initiate taking a picture, if {@code autoFocus} is {@code false} the picture will be taken immediately;
     * otherwise an auto-focus will be triggered if possible.
     * {@link CameraPictureListener#onFocus(boolean)} will be only called if auto-focus was requested.
     * Taking the picture can be cancelled by {@link CameraPictureListener#onFocus(boolean) onFocus}.
     */
    @SuppressWarnings("deprecation")
    public void takePicture(final CameraPictureListener callback, boolean autoFocus) {
        Log.d("CameraPreview", "{} Taking picture" + cameraHolder);
        if (cameraHolder == null) {
            return;
        }
        if (autoFocus) {
            focus(callback);
            return;
        }
        try {
            cameraHolder.camera.takePicture(new android.hardware.Camera.ShutterCallback() {
                @Override public void onShutter() {
                    post(new Runnable() {
                        @Override public void run() {
                            listener.onShutter(CameraPreview.this);
                        }
                    });
                    post(new Runnable() {
                        @Override public void run() {
                            listener.onPause(CameraPreview.this);
                        }
                    });
                }
            }, null, null, new android.hardware.Camera.PictureCallback() {
                @Override public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                    callback.onTaken(data);
                }
            });
        } catch (RuntimeException ex) {
            Log.d("CameraPreview", ("Cannot take picture" + ex));
            thread.post(new Runnable() {
                @Override public void run() {
                    callback.onTaken((byte[])null);
                }
            });
        }
    }

    public void cancelTakePicture() {
        Log.d("CameraPreview", "Cancel take picture");
        cancelAutoFocus();
        startPreview();
    }

    private void cancelAutoFocus() {
        Log.d("CameraPreview", "{} Cancel auto-focus" + cameraHolder);
        if (cameraHolder != null) {
            cameraHolder.camera.cancelAutoFocus();
        }
    }

    /**
     * Make the camera's picture be focused (if supported) and take a picture if the callback returns true.
     * @see CameraPictureListener#onFocus(boolean)
     */
    @SuppressWarnings("deprecation")
    public void focus(final CameraPictureListener callback) {
        Log.d("CameraPreview", "{} Camera focus" + cameraHolder);
        if (cameraHolder == null) {
            return;
        }
        android.hardware.Camera.AutoFocusCallback cameraCallback = new android.hardware.Camera.AutoFocusCallback() {
            @Override public void onAutoFocus(boolean success, android.hardware.Camera camera) {
                if (callback.onFocus(success)) {
                    takePicture(callback, false);
                }
            }
        };
        String focusMode = cameraHolder.camera.getParameters().getFocusMode();
        if (android.hardware.Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)
                || android.hardware.Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
            try {
                cameraHolder.camera.autoFocus(cameraCallback);
            } catch (RuntimeException ex) {
                Log.d("CameraPreview", "Failed to autofocus" + ex);
                cameraCallback.onAutoFocus(false, cameraHolder.camera);
            }
        } else {
            cameraCallback.onAutoFocus(true, cameraHolder.camera);
        }
    }

    public void setFlash(boolean flash) {
        if (cameraHolder == null) {
            return;
        }
        @SuppressWarnings("deprecation")
        String flashMode = flash
                ? android.hardware.Camera.Parameters.FLASH_MODE_ON
                : android.hardware.Camera.Parameters.FLASH_MODE_OFF;
        cameraHolder.params.setFlashMode(flashMode);
        cameraHolder.camera.setParameters(cameraHolder.params);
    }

    @SuppressWarnings("deprecation")
    private static class CameraHolder {
        final int cameraID;
        final android.hardware.Camera camera;
        final android.hardware.Camera.CameraInfo cameraInfo;
        final android.hardware.Camera.Parameters params;

        public CameraHolder(int id) {
            cameraID = id;
            Log.d("CameraPreview", "Opening camera");
            camera = android.hardware.Camera.open(cameraID);
            Log.d("CameraPreview", "Opened camera");
            try {
                Log.d("CameraPreview", "setPreviewDisplay null");
                camera.setPreviewDisplay(null);
            } catch (RuntimeException | IOException ex) {
                Log.d("CameraPreview", "Error setting up camera preview" + ex);
            }
            cameraInfo = new android.hardware.Camera.CameraInfo();
            params = camera.getParameters();
            android.hardware.Camera.getCameraInfo(cameraID, cameraInfo);
        }

        @Override public String toString() {
            return String.format(Locale.ROOT, "Camera #%d (%s, %d°)",
                    cameraID, facing(cameraInfo.facing), cameraInfo.orientation);
        }
        private static String facing(int facing) {
            switch (facing) {
                case android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK:
                    return "back";
                case android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT:
                    return "front";
                default:
                    return "unknown";
            }
        }
    }

    private class CameraHandlerThread extends HandlerThread {
        private final Looper mLooper;
        private final Handler mHandler;

        @MainThread
        public CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mLooper = getLooper();
            mHandler = new Handler(mLooper);
        }

        public void startOpenCamera() {
            mHandler.post(new Runnable() {
                // TODEL ResourceTypes below http://b.android.com/207302
                @WorkerThread
                @Override public void run() {
                    int cameraID = findCamera();
                    try {
                        final CameraHolder holder = new CameraHolder(cameraID);
                        //noinspection ResourceType post should be safe to call from background
                        CameraPreview.this.post(new Runnable() {
                            @UiThread
                            public void run() {
                                CameraPreview.this.started(holder);
                            }
                        });
                    } catch (final RuntimeException ex) {
                        Log.e("CameraPreview", "Error setting up camera #{}" + cameraID + ex);
                        //noinspection ResourceType post should be safe to call from background
                        CameraPreview.this.post(new Runnable() {
                            @UiThread
                            public void run() {
                                Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                                setVisibility(View.INVISIBLE); // destroy surface for callbacks to trigger
                            }
                        });
                    }
                }

                @SuppressWarnings("deprecation")
                private int findCamera() {
                    int cameras = android.hardware.Camera.getNumberOfCameras();
                    int frontId = -1;
                    int backId = -1;
                    for (int i = 0; i < cameras; ++i) {
                        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(0, info);
                        if (backId == -1 && info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                            backId = i;
                        }
                        if (frontId == -1 && info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            frontId = i;
                        }
                    }
                    int id = backId != -1? backId : frontId;
                    return id != -1? id : 0;
                }
            });
        }

        public void stopThread() {
            mLooper.quit();
        }
        public void post(Runnable runnable) {
            mHandler.post(runnable);
        }
    }

    private class CameraThreadHandler implements SurfaceHolder.Callback {
        private CameraHandlerThread mCameraThread = null;
        @Override public void surfaceCreated(SurfaceHolder holder) {
            if (mCameraThread != null) {
                throw new IllegalStateException("Camera Thread already started");
            }
            mCameraThread = new CameraHandlerThread();
            Log.d("CameraPreview","Starting thread {}" + mCameraThread);
            mCameraThread.startOpenCamera();
        }
        @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }
        @Override public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCameraThread != null) {
                Log.d("CameraPreview", "Stopping thread {}" + mCameraThread);
                mCameraThread.stopThread();
                mCameraThread = null;
            }
        }
        public void post(Runnable runnable) {
            mCameraThread.post(runnable);
        }
    }

    private class MissedSurfaceEvents implements SurfaceHolder.Callback {
        private boolean surfaceCreated;
        private boolean surfaceChanged;
        private boolean surfaceDestroyed;
        private SurfaceHolder holder;
        private int format;
        private int w, h;

        public void surfaceCreated(SurfaceHolder holder) {
            this.surfaceCreated = true;
            this.holder = holder;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.surfaceChanged = true;
            this.holder = holder;
            this.format = format;
            this.w = width;
            this.h = height;
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            this.surfaceDestroyed = true;
            this.holder = holder;
        }

        public void replay() {
            if (surfaceDestroyed) {
                CameraPreview.this.surfaceDestroyed(holder);
                return;
            }
            if (surfaceCreated) {
                CameraPreview.this.surfaceCreated(holder);
            }
            if (surfaceChanged) {
                CameraPreview.this.surfaceChanged(holder, format, w, h);
            }
        }
    }
    /** * @see android.view.Display#getOrientation()
     * @see android.view.Display#getRotation()
     * @param displayOrientation one of {@code Surface.ROTATION_d} constants
     * @return The {@code d} in the constant name as an integer
     */
    private static int orientationToDegrees(/*@Surface.Rotation*/ int displayOrientation) {
        //return displayOrientation * 90;
        switch (displayOrientation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                throw new IllegalArgumentException("Display orientation " + displayOrientation + " is not recognized.");
        }
    }

    /**
     * @see android.hardware.Camera#setDisplayOrientation(int)
     * @return the display's orientation in 90-increment degrees (0, 90, 180, 270)
     */
    @SuppressWarnings("deprecation")
    public static int calculateDisplayOrientation(Context context, android.hardware.Camera.CameraInfo cameraInfo) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int displayOrientation = windowManager.getDefaultDisplay().getRotation();
        int degrees = orientationToDegrees(displayOrientation);

        int result;
        if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }



    /**
     * @see android.hardware.Camera.Parameters#setRotation(int)
     * @return the camera rotation to use in 90-increment degrees (0, 90, 180, 270)
     */
    @SuppressWarnings("deprecation")
    public static int calculateRotation(Context context, android.hardware.Camera.CameraInfo cameraInfo) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int displayOrientation = windowManager.getDefaultDisplay().getRotation();
        int degrees = orientationToDegrees(displayOrientation);

        int result;
        if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
        } else { // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;

    }


    @SuppressWarnings("deprecation")
    public static @NonNull android.hardware.Camera.Size getOptimalSize(

            @NonNull Collection<android.hardware.Camera.Size> sizes, int w, int h) {
        //noinspection ConstantConditions it's still possible the call the method with null
        if (sizes == null || sizes.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one size to choose from.");
        }
        List<android.hardware.Camera.Size> sorted = new ArrayList<>(sizes);
        Collections.sort(sorted, new CameraSizeComparator(w, h));
        android.hardware.Camera.Size optimalSize = sorted.get(0);
        Log.d("CameraPreview", "Optimal size selected is {}x{} from {}." +
                optimalSize.width + optimalSize.height + sorted.toString());

        return optimalSize;
    }
}
