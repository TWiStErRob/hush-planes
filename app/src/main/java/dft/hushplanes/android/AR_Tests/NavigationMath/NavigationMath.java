package dft.hushplanes.android.AR_Tests.NavigationMath;

import android.opengl.Matrix;

/**
 * Created by hackathon on 25/03/2017.
 */

public class NavigationMath {
    public static void getDirection(float[] result, float [] human, float [] plane){
        float dir[] = new float[3];
        dir[0] = human[0]-plane[0];
        dir[1] = human[1]-plane[1];
        dir[2] = human[2]-plane[2];
        float normdir[] = new float[3];
        normalize(dir,normdir);

        float [] v = new float[3];
        float [] down = {0,0,1};
        cross(normdir, down, v);
        float c = dot(normdir, down);

        result[0] = (float)Math.toDegrees((float)Math.acos(c));
        result[1] = v[0];
        result[2] = v[1];
        result[3] = v[2];

    }
    public static void cross(float[] p1, float[] p2, float[] result) {
        result[0] = p1[1] * p2[2] - p2[1] * p1[2];
        result[1] = p1[2] * p2[0] - p2[2] * p1[0];
        result[2] = p1[0] * p2[1] - p2[0] * p1[1];
    }
    public static void normalize(float[] p1, float[] result){
        float mod = (magnitute(p1));
        result[0] = p1[0]/mod;
        result[1] = p1[1]/mod;
        result[2] = p1[2]/mod;
    }
    public static float magnitute(float[] p1){
        return (float)Math.sqrt(p1[0]*p1[0] + p1[1] *p1[1] + p1[2]*p1[2]);
    }
    public static float dot(float[] v1, float[] v2) {
        float res = 0;
        for (int i = 0; i < v1.length; i++)
            res += v1[i] * v2[i];
        return res;
    }
    public static float dist(float[] p1, float[] p2){
        float [] p = new  float[3];
        p[0] = p1[0]-p2[0];
        p[1] = p1[1]-p2[1];
        p[2] = p1[2]-p2[2];
        return (float)Math.sqrt(p[0]*p[0] + p[1] *p[1] + p[2]*p[2]);
    }
}
