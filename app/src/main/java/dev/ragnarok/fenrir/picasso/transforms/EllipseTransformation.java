package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;

public class EllipseTransformation implements Transformation {

    private static final String TAG = EllipseTransformation.class.getSimpleName();

    @NonNull
    @Override
    public String key() {
        return TAG + "()";
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (source == null) {
            return null;
        }
        return ImageHelper.getEllipseBitmap(source, 0.35f);
    }
}
