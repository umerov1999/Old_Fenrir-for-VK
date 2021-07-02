package dev.ragnarok.fenrir.view.natives.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.video.AnimatedFileDrawable;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnimatedShapeableImageView extends ShapeableImageView {

    private static final ThreadLocal<byte[]> bufferLocal = new ThreadLocal<>();
    private final NetworkCache cache;
    private final int defaultWidth;
    private final int defaultHeight;
    private AnimatedFileDrawable drawable;
    private boolean attachedToWindow;
    private boolean playing;
    private onDecoderInit decoderCallback;
    private Disposable mDisposable = Disposable.disposed();

    public AnimatedShapeableImageView(Context context) {
        this(context, null);
    }

    public AnimatedShapeableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cache = new NetworkCache(context);

        @SuppressLint("CustomViewStyleable") TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RLottieImageView);
        defaultWidth = (int) a.getDimension(R.styleable.RLottieImageView_w, 100);
        defaultHeight = (int) a.getDimension(R.styleable.RLottieImageView_h, 100);
        a.recycle();
    }

    public void setDecoderCallback(onDecoderInit decoderCallback) {
        this.decoderCallback = decoderCallback;
    }

    private void setAnimationByUrlCache(String url) {
        if (!FenrirNative.isNativeLoaded()) {
            decoderCallback.onLoaded(false);
            return;
        }
        File ch = cache.fetch(url);
        if (ch == null) {
            setImageDrawable(null);
            decoderCallback.onLoaded(false);
            return;
        }
        setAnimation(new AnimatedFileDrawable(ch, 0, defaultWidth, defaultHeight, () -> decoderCallback.onLoaded(false)));
        playAnimation();
    }

    private void setAnimationByResCache(@RawRes int res) {
        if (!FenrirNative.isNativeLoaded()) {
            decoderCallback.onLoaded(false);
            return;
        }
        File ch = cache.fetch(res);
        if (ch == null) {
            setImageDrawable(null);
            decoderCallback.onLoaded(false);
            return;
        }
        setAnimation(new AnimatedFileDrawable(ch, 0, defaultWidth, defaultHeight, () -> decoderCallback.onLoaded(false)));
        playAnimation();
    }

    public void fromNet(String url, OkHttpClient.Builder client) {
        if (!FenrirNative.isNativeLoaded() || url == null || url.isEmpty()) {
            decoderCallback.onLoaded(false);
            return;
        }
        clearAnimationDrawable();
        if (cache.isCachedFile(url)) {
            setAnimationByUrlCache(url);
            return;
        }
        mDisposable = Completable.create(u -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.build().newCall(request).execute();
                if (!response.isSuccessful()) {
                    u.onError(new Throwable("Not success connection"));
                    return;
                }
                InputStream bfr = Objects.requireNonNull(response.body()).byteStream();
                BufferedInputStream input = new BufferedInputStream(bfr);
                cache.writeTempCacheFile(url, input);
                input.close();
                cache.renameTempFile(url);
            } catch (Exception e) {
                u.onError(e);
                return;
            }
            u.onComplete();
        }).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> setAnimationByUrlCache(url), e -> decoderCallback.onLoaded(false));
    }

    public void fromRes(@RawRes int res) {
        if (!FenrirNative.isNativeLoaded()) {
            decoderCallback.onLoaded(false);
            return;
        }
        clearAnimationDrawable();
        if (cache.isCachedRes(res)) {
            setAnimationByResCache(res);
            return;
        }
        mDisposable = Completable.create(u -> {
            try {
                if (!copyRes(res)) {
                    u.onError(new Throwable("Copy video res error"));
                    return;
                }
                cache.renameTempFile(res);
            } catch (Exception e) {
                u.onError(e);
                return;
            }
            u.onComplete();
        }).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> setAnimationByResCache(res), e -> {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            decoderCallback.onLoaded(false);
        });
    }

    private void setAnimation(@NonNull AnimatedFileDrawable videoDrawable) {
        if (decoderCallback != null) {
            decoderCallback.onLoaded(videoDrawable.isDecoded());
        }
        if (!videoDrawable.isDecoded())
            return;
        drawable = videoDrawable;
        drawable.setAllowDecodeSingleFrame(true);
        drawable.setParentView(this);
        setImageDrawable(drawable);
    }

    public void fromFile(@NonNull File file) {
        if (!FenrirNative.isNativeLoaded()) {
            decoderCallback.onLoaded(false);
            return;
        }
        clearAnimationDrawable();
        setAnimation(new AnimatedFileDrawable(file, 0, defaultWidth, defaultHeight, () -> decoderCallback.onLoaded(false)));
    }

    public void clearAnimationDrawable() {
        if (drawable != null) {
            drawable.stop();
            drawable.setParentView(null);
        }
        drawable = null;
        setImageDrawable(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        if (drawable != null) {
            drawable.setCallback(this);
            drawable.setParentView(this);
            if (playing) {
                drawable.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
        if (drawable != null) {
            drawable.stop();
            drawable.setParentView(null);
        }
        mDisposable.dispose();
    }

    public boolean isPlaying() {
        return drawable != null && drawable.isRunning();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable dr) {
        super.setImageDrawable(dr);
        if (!(dr instanceof AnimatedFileDrawable)) {
            if (drawable != null) {
                drawable.stop();
                drawable.setParentView(null);
            }
            mDisposable.dispose();
            drawable = null;
        }
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap bm) {
        super.setImageBitmap(bm);
        if (drawable != null) {
            drawable.stop();
            drawable.setParentView(null);
        }
        mDisposable.dispose();
        drawable = null;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (drawable != null) {
            drawable.stop();
            drawable.setParentView(null);
        }
        mDisposable.dispose();
        drawable = null;
    }

    public void playAnimation() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.start();
        }
    }

    public void resetFrame() {
        if (drawable == null) {
            return;
        }
        playing = true;
        if (attachedToWindow) {
            drawable.seekTo(0, true);
        }
    }

    public void stopAnimation() {
        if (drawable == null) {
            return;
        }
        playing = false;
        if (attachedToWindow) {
            drawable.stop();
        }
    }

    public @Nullable
    AnimatedFileDrawable getAnimatedDrawable() {
        return drawable;
    }

    private boolean copyRes(@RawRes int rawRes) {
        try (InputStream inputStream = FenrirNative.getAppContext().getResources().openRawResource(rawRes)) {
            File out = new File(NetworkCache.Companion.parentDir(getContext()), NetworkCache.Companion.filenameForRes(rawRes, true));
            FileOutputStream o = new FileOutputStream(out);
            byte[] buffer = bufferLocal.get();
            if (buffer == null) {
                buffer = new byte[4096];
                bufferLocal.set(buffer);
            }
            while (inputStream.read(buffer, 0, buffer.length) >= 0) {
                o.write(buffer);
            }
            o.flush();
            o.close();
        } catch (Throwable e) {
            if (Constants.IS_DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public interface onDecoderInit {
        void onLoaded(boolean success);
    }
}
