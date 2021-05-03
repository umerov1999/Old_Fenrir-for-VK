package dev.ragnarok.fenrir.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.util.Objects;

public class PicassoMediaMetadataHandler extends RequestHandler {

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getPath() != null && data.uri.getLastPathSegment() != null && data.uri.getScheme() != null && data.uri.getScheme().contains("share_");
    }

    private @Nullable
    Bitmap getMetadataAudioThumbnail(@NonNull Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(Injection.provideApplicationContext(), uri);
            byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
            if (cover == null) {
                return null;
            }
            InputStream is = new ByteArrayInputStream(cover);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RequestHandler.Result load(Request request, int networkPolicy) throws IOException {
        Bitmap target = getMetadataAudioThumbnail(Uri.parse(request.uri.toString().replace("share_", "")));
        if (Objects.isNull(target)) {
            throw new IOException("Picasso Thumb Not Support");
        }
        return new RequestHandler.Result(target, Picasso.LoadedFrom.DISK);
    }
}
