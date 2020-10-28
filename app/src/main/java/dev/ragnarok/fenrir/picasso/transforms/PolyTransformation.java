package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

public class PolyTransformation implements Transformation {

    private static final String TAG = PolyTransformation.class.getSimpleName();

    @NotNull
    @Override
    public String key() {
        return TAG + "()";
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) {
            return null;
        }
        return ImageHelper.getElpsedBitmap(source, 20, 20);
    }
}
