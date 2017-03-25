package dft.hushplanes.android.AR_Tests.GL;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.*;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class OverlayView extends GLSurfaceView implements SensorEventListener{
    private final OverlayRenderer renderer;

	public OverlayView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

        SensorManager sm = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR).size()!=0){
            Sensor s = sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
            sm.registerListener(this,s,SensorManager.SENSOR_DELAY_GAME);
        }

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new OverlayRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

    }
	private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	private static final float ROT_SCALE_FACTOR = 100;
    private float mPreviousX;
    private float mPreviousY;

    //TODO replace this with an accelerometer listener
    @Override
    public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                renderer.setAngle(e.values);
                requestRender();
        }
    }
    public void onSensorChanged(SensorEvent e, int accuracy){
        onSensorChanged(e);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

   @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }


                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

}
