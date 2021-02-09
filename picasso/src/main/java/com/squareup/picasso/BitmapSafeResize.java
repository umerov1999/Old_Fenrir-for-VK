package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

public class BitmapSafeResize {
    private static int maxResolution = -1;

    public static int getMaxResolution() {
        return maxResolution;
    }

    public static void setMaxResolution(int maxResolution) {
        if (maxResolution > 100) {
            BitmapSafeResize.maxResolution = maxResolution;
        } else {
            BitmapSafeResize.maxResolution = -1;
        }
    }

    public static boolean isOverflowCanvas(int Resolution) {
        Canvas canvas = new Canvas();
        int maxCanvasSize = Math.min(canvas.getMaximumBitmapWidth(), canvas.getMaximumBitmapHeight());
        if (maxCanvasSize > 0) {
            return maxCanvasSize < Resolution;
        }
        return false;
    }

    public static Bitmap checkBitmap(@NonNull Bitmap bitmap) {
        if (maxResolution < 0 || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0 || (bitmap.getWidth() <= maxResolution && bitmap.getHeight() <= maxResolution)) {
            return bitmap;
        }
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        float mCo = (float) Math.min(mHeight, mWidth) / Math.max(mHeight, mWidth);
        if (mWidth > mHeight) {
            mWidth = maxResolution;
            mHeight = (int) (maxResolution * mCo);
        } else {
            mHeight = maxResolution;
            mWidth = (int) (maxResolution * mCo);
        }
        if (mWidth <= 0 || mHeight <= 0) {
            return bitmap;
        }
        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, true);
        bitmap.recycle();
        return tmp;
    }
}