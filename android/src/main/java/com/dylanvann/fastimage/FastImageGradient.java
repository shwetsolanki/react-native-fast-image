package com.dylanvann.fastimage;

import android.content.Context;
import android.graphics.PorterDuff;

import java.util.Arrays;

public class FastImageGradient {
    public PorterDuff.Mode mBlendMode;
    public int[] mColors;
    public float[] mLocations;
    public int mAngle;

    public FastImageGradient(Context context, int[] colors, PorterDuff.Mode blendMode, float[] locations, int angle) {
        super();
        mColors = colors;
        mBlendMode = blendMode;
        mLocations = locations;
        mAngle = angle;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mColors) ^ Arrays.hashCode(mLocations) ^ mAngle ^ mBlendMode.hashCode();
    }
}
