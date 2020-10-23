package dev.ragnarok.fenrir.picasso.transforms;

import com.squareup.picasso3.RequestHandler;
import com.squareup.picasso3.Transformation;

import org.jetbrains.annotations.NotNull;

public class RoundTransformation implements Transformation {

    private static final String TAG = RoundTransformation.class.getSimpleName();

    @NotNull
    @Override
    public String key() {
        return TAG + "()";
    }

    @NotNull
    @Override
    public RequestHandler.Result.Bitmap transform(@NotNull RequestHandler.Result.Bitmap source) {
        return new RequestHandler.Result.Bitmap(ImageHelper.getRoundedBitmap(source.getBitmap()), source.loadedFrom, source.exifRotation);
    }
}
