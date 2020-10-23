package dev.ragnarok.fenrir.picasso;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.squareup.picasso3.Picasso;

import java.io.File;
import java.io.IOException;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.ProxyUtil;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Objects;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class PicassoInstance {

    private static final String TAG = PicassoInstance.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static PicassoInstance instance;
    private final IProxySettings proxySettings;
    private final Context app;
    private Cache cache_data;
    private volatile Picasso singleton;

    @SuppressLint("CheckResult")
    private PicassoInstance(Context app, IProxySettings proxySettings) {
        this.app = app;
        this.proxySettings = proxySettings;
        this.proxySettings.observeActive()
                .subscribe(ignored -> onProxyChanged());
    }

    @NonNull
    public static Uri buildUriForPicasso(@Content_Local int type, long id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return buildUriForPicassoNew(type, id);
        }
        switch (type) {
            case Content_Local.PHOTO:
                return ContentUris.withAppendedId(Uri.parse("content://media/external/images/media/"), id);
            case Content_Local.VIDEO:
                return ContentUris.withAppendedId(Uri.parse("content://media/external/videos/media/"), id);
            case Content_Local.AUDIO:
                return ContentUris.withAppendedId(Uri.parse("content://media/external/audios/media/"), id);
        }
        return ContentUris.withAppendedId(Uri.parse("content://media/external/images/media/"), id);
    }

    @NonNull
    public static Uri buildUriForPicassoNew(@Content_Local int type, long id) {
        switch (type) {
            case Content_Local.PHOTO:
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            case Content_Local.VIDEO:
                return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            case Content_Local.AUDIO:
                return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
    }

    public static void init(Context context, IProxySettings proxySettings) {
        instance = new PicassoInstance(context.getApplicationContext(), proxySettings);
    }

    public static Picasso with() {
        return instance.getSingleton();
    }

    public static void clear_cache() throws IOException {
        instance.getCache_data();
        instance.cache_data.evictAll();
    }

    // from picasso sources
    private static long calculateDiskCacheSize(File dir) {
        long size = 5242880L;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long blockCount = statFs.getBlockCountLong();
            long blockSize = statFs.getBlockSizeLong();
            long available = blockCount * blockSize;
            size = available / 50L;
        } catch (IllegalArgumentException ignored) {

        }

        return Math.max(Math.min(size, 52428800L), 5242880L);
    }

    private void onProxyChanged() {
        synchronized (this) {
            if (Objects.nonNull(singleton)) {
                singleton.shutdown();
                singleton = null;
            }

            Logger.d(TAG, "Picasso singleton shutdown");
        }
    }

    private Picasso getSingleton() {
        if (Objects.isNull(singleton)) {
            synchronized (this) {
                if (Objects.isNull(singleton)) {
                    singleton = create();
                }
            }
        }


        return singleton;
    }

    private void getCache_data() {
        if (cache_data == null) {
            File cache = new File(app.getCacheDir(), "picasso-cache");

            if (!cache.exists()) {
                cache.mkdirs();
            }

            cache_data = new Cache(cache, calculateDiskCacheSize(cache));
        }
    }

    private Picasso create() {
        Logger.d(TAG, "Picasso singleton creation");
        getCache_data();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache_data)
                //.addNetworkInterceptor(chain -> chain.proceed(chain.request()).newBuilder().header("Cache-Control", "max-age=31536000,public").build())
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build();
                    return chain.proceed(request);
                });
        ProxyUtil.applyProxyConfig(builder, proxySettings.getActiveProxy());

        return new Picasso.Builder(app)
                .callFactory(builder.build())
                .addRequestHandler(new LocalRequestHandler())
                .build();
    }
}
