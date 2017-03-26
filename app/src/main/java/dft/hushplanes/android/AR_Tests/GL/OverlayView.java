package dft.hushplanes.android.AR_Tests.GL;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.*;
import android.opengl.GLSurfaceView;
import android.support.v4.content.ContextCompat;
import android.text.*;
import android.text.style.*;
import android.util.AttributeSet;

import dft.hushplanes.android.R;
import dft.hushplanes.model.Flights;

public class OverlayView extends GLSurfaceView implements SensorEventListener{
	private final OverlayRenderer renderer = new OverlayRenderer();

	public OverlayView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		if (!isInEditMode()) {
			SensorManager sm = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			if (sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR).size() != 0) {
				Sensor s = sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
			}

			// Create an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);
			setEGLConfigChooser(8, 8, 8, 8, 16, 0);

			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(renderer);
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
	}


    @Override
    public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                float[] vec = { e.values[0], e.values[1], e.values[2], e.values[3]};
                renderer.setAngle(vec);
                requestRender();
        }
    }
    public void onSensorChanged(SensorEvent e, int accuracy){
        //onSensorChanged(e);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
	public SpannableStringBuilder msg;

	public void setFlights(Flights flights) {
		renderer.setFlights(flights);
		msg = new SpannableStringBuilder();
		msg.append("The closest plane is ");
		int start;
		start = msg.length();
		msg.append(String.valueOf(Math.round(renderer.mindist))).append("\u00a0feet away");
		color(msg, start, msg.length());
		msg.append(".\nThat's just a ");
		start = msg.length();
		msg.append(String.valueOf(Math.round(renderer.mindist / 300))).append("\u00a0minute walk");
		color(msg, start, msg.length());
		msg.append(" walk on the ground.");
	}
	private void color(Spannable msg, int start, int end) {
		int accent = ContextCompat.getColor(getContext(), R.color.colorAccent);
		msg.setSpan(new ForegroundColorSpan(0xFF000000 | accent), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		msg.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
}
