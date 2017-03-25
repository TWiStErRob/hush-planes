package dft.hushplanes.android.AR_Tests;

import org.slf4j.*;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import dft.hushplanes.android.R;

public class AugmentedRealityActivity extends Activity {
	private static final Logger LOG =
			LoggerFactory.getLogger(AugmentedRealityActivity.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);

		GLSurfaceView glView = (GLSurfaceView)findViewById(R.id.overlay);
		glView.setZOrderOnTop(true);
	}
}
