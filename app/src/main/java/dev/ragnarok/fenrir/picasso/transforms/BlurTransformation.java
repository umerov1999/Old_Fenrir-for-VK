package dev.ragnarok.fenrir.picasso.transforms;

/**
 * Copyright (C) 2018 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

public class BlurTransformation implements Transformation {

    private static final String TAG = BlurTransformation.class.getSimpleName();

    private final int mRadius;
    private final int mSampling;

    private final Context mContext;

    public BlurTransformation(int radius, int sampling, Context mContext) {
        mRadius = radius;
        mSampling = sampling;
        this.mContext = mContext;
    }

    public static Bitmap blur(Bitmap image, Context context, float radius) {
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        RenderScript renderScript = RenderScript.create(context);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    @NotNull
    @Override
    public String key() {
        return TAG + "(radius=" + mRadius + ", sampling=" + mSampling + ")";
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && source.getConfig() == Bitmap.Config.HARDWARE) {
            source = source.copy(Bitmap.Config.ARGB_8888, true);
        }

        Bitmap bitmap = blur(source, mContext, mRadius);
        if (source != bitmap) {
            source.recycle();
        }

        return bitmap;
    }
}
