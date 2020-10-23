package dev.ragnarok.fenrir.picasso.transforms;

import com.squareup.picasso3.RequestHandler;
import com.squareup.picasso3.Transformation;

import org.jetbrains.annotations.NotNull;

public class PolyTopTransformation implements Transformation {

    private static final String TAG = PolyTopTransformation.class.getSimpleName();

    @NotNull
    @Override
    public String key() {
        return TAG + "()";
    }

    @NotNull
    @Override
    public RequestHandler.Result.Bitmap transform(@NotNull RequestHandler.Result.Bitmap source) {
        return new RequestHandler.Result.Bitmap(ImageHelper.getCorneredBitmap(source.getBitmap(), 20, 20, 0, 0), source.loadedFrom, source.exifRotation);
    }
}
