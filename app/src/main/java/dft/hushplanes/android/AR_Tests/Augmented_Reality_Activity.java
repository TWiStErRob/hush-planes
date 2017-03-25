package dft.hushplanes.android.AR_Tests;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;

import dft.hushplanes.android.AR_Tests.Camera.Preview;
import dft.hushplanes.android.AR_Tests.GL.OverlayView;

/**
 * Created by hackathon on 25/03/2017.
 */

public class Augmented_Reality_Activity extends Activity {
    private GLSurfaceView GLView;
    private Preview preview;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        GLView = new OverlayView(this);
        preview = new Preview(this);
        setContentView(preview);
        addContentView(GLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        GLView.setZOrderOnTop(true);
    }
}
