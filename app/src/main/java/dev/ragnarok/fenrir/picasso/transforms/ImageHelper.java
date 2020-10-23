package dev.ragnarok.fenrir.picasso.transforms;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;

public class ImageHelper {

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        Path path = new Path();
        path.addOval(rect, Path.Direction.CCW);
        canvas.drawPath(path, paint);

        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }

    public static Bitmap getElpsedBitmap(Bitmap bitmap, int percentage_x, int percentage_y) {
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float cfx = (percentage_x / 100f);
        float cfy = (percentage_y / 100f);

        RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        Path path = new Path();
        path.addRoundRect(rect, (bitmap.getWidth() / 2f) * cfx, (bitmap.getHeight() / 2f) * cfy, Path.Direction.CW);
        canvas.drawPath(path, paint);

        if (bitmap != output) {
            bitmap.recycle();
        }
        return output;
    }

    public static Bitmap getCorneredBitmap(Bitmap bitmap, int top_percentage_x, int top_percentage_y, int bottom_percentage_x, int bottom_percentage_y) {
        if (bitmap == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && bitmap.getConfig() == Bitmap.Config.HARDWARE) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float top_cfx = (top_percentage_x / 100f) * (bitmap.getWidth() / 2f);
        float top_cfy = (top_percentage_y / 100f) * (bitmap.getHeight() / 2f);

        float bottom_cfx = (bottom_percentage_x / 100f) * (bitmap.getWidth() / 2f);
        float bottom_cfy = (bottom_percentage_y / 100f) * (bitmap.getHeight() / 2f);

        float[] radii = {top_cfx, top_cfx, top_cfy, top_cfy, bottom_cfy, bottom_cfy, bottom_cfx, bottom_cfx};

        RectF rect = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        Path path = new Path();
        path.addRoundRect(rect, radii, Path.Direction.CW);
        canvas.drawPath(path, paint);

        if (bitmap != output) {
            bitmap.recycle();
        }

        return output;
    }
}
