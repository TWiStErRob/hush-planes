package dft.hushplanes.android.AR_Tests.GL;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.slf4j.*;

import android.hardware.SensorManager;
import android.opengl.*;
import android.util.Log;

import dft.hushplanes.android.AR_Tests.Camera.CameraPreview;
import dft.hushplanes.android.AR_Tests.NavigationMath.NavigationMath;
import dft.hushplanes.model.*;

/**
 * Created by hackathon on 25/03/2017.
 */

public class OverlayRenderer implements GLSurfaceView.Renderer {
    private static final Logger LOG = LoggerFactory.getLogger(CameraPreview.class);

    private TriangleStrip mTriangleStrip;
    private Triangle mTriangle;
    private Square mSquare;
    private Cube mCube;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private final float[] mtempMVPMatrix = new float[16];

    private float mAngle;
    private Flights flights;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mTriangle = new Triangle();
        mTriangleStrip = new TriangleStrip();
        mSquare = new Square();
        mCube = new Cube();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    public void setFlights(Flights flights) {
        this.flights = flights;
    }
    float dl = 0;
    @Override

    public void onDrawFrame(GL10 unused) {
        dl += 0.001;
        //float[] scratch = new float[16];

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


        float[] eyeR = {0, 0, -5, 1};
        float[] forwardR = {0f, 0f, 0f, 1f};
        float[] upR = {0f, 1f, 0.0f, 1f};

        float[] translationM = new float[16];
        Matrix.setIdentityM(translationM,0);
        float[] negtranslationM = new float[16];
        Matrix.translateM(translationM, 0, 0, 0, 2);

        //Matrix.multiplyMV(lookat,0,mRotationMatrix,0,straight,0);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, eyeR[0], eyeR[1], eyeR[2], forwardR[0], forwardR[1], forwardR[2], upR[0], upR[1], upR[2]);

        // Calculate the projection and view transformation
        Matrix.translateM(mViewMatrix, 0, 0, 5, 0);
        Matrix.multiplyMM(mViewMatrix, 0, mRotationMatrix, 0, mViewMatrix, 0);
        Matrix.translateM(mViewMatrix, 0, 0, -5, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        //Matrix.multiplyMM(mMVPMatrix, 0, mtempMVPMatrix, 0, mRotationMatrix, 0);
        //Matrix.setRotateM(mRotationMatrix, 0, mCubeRotation, 1.0f, 1.0f, 1.0f);
        // Combine the rotation matrix with the projection and camera view
        float[] mCubeMVPMatrix = new float[16];
        //Matrix.multiplyMM(mFinalMVPMatrix, 0, mMVPMatrix, 0, translationM, 0);
        //mCube.draw(mMVPMatrix);


        float[] id = new float[16];
        Matrix.setIdentityM(id,0);
        float[] mMVPMatrix2 = new float[16];
        Matrix.translateM(mMVPMatrix, 0, 0, 0, -5);

        Matrix.rotateM(mMVPMatrix,0,180,1,0,0);

        //Matrix.rotateM(mMVPMatrix,0,150,1,0,0);
        Matrix.translateM(mMVPMatrix, 0, 0, 0, 5);
        if (flights == null) return;
        for(Flight f : flights.flights){
            Location p = f.current;

                Matrix.multiplyMM(mMVPMatrix2, 0, mMVPMatrix, 0, id, 0);
                Matrix.translateM(mMVPMatrix2, 0, 0, 0, -5);
                float[] q2 = new float[4];
                float[] h2 = {51.4775f, -0.461389f, 0};
                float[] p2 = {(float)p.latitude, (float)p.longitude, (float)p.altitude / 100000f};
                NavigationMath.getDirection(q2, p2, h2);
                Matrix.rotateM(mMVPMatrix2, 0, q2[0], q2[1], q2[2], q2[3]);

                //Matrix.rotateM(mMVPMatrix,0,150,1,0,0);
                Matrix.translateM(mMVPMatrix2, 0, 0, 0, 5);
                float s = (float)Math.atan((1-NavigationMath.dist(h2, p2)/0.8)/2) + 1;
                //LOG.debug("Directio2n {} {} {} {}", (float)p.latitude, (float)p.longitude, (float)p.altitude, NavigationMath.dist(h2, p2));

                Matrix.scaleM(mMVPMatrix2, 0, s/4, s/4, s/4);
                mTriangle.draw(mMVPMatrix2);


        }

        mTriangleStrip.draw(mMVPMatrix);




    }

    /**
     * Utility method for compiling a OpenGL shader.
     * <p>
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     * <p>
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     * <p>
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("Overlay Renderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }


    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float[] angle) {
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, angle);

    }
}
