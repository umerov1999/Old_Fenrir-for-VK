package dev.ragnarok.fenrir.picasso;

import android.graphics.Bitmap;
import android.os.Build;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.util.Objects;

public class PicassoLocalRequestHandler extends RequestHandler {

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getPath() != null && data.uri.getLastPathSegment() != null && data.uri.getScheme() != null && data.uri.getScheme().equals("content");
    }

    public RequestHandler.Result load(Request request, int networkPolicy) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Bitmap target = Stores.getInstance().localMedia().getThumbnail(request.uri, 256, 256);
            if (Objects.isNull(target)) {
                throw new IOException("Picasso Thumb Not Support");
            }
            return new RequestHandler.Result(target, Picasso.LoadedFrom.DISK);
        } else {
            long contentId = Long.parseLong(request.uri.getLastPathSegment());
            @Content_Local int ret;
            if (request.uri.getPath().contains("videos")) {
                ret = Content_Local.VIDEO;
            } else if (request.uri.getPath().contains("images")) {
                ret = Content_Local.PHOTO;
            } else if (request.uri.getPath().contains("audios")) {
                ret = Content_Local.AUDIO;
            } else {
                throw new IOException("Picasso Thumb Not Support");
            }
            Bitmap target = Stores.getInstance().localMedia().getOldThumbnail(ret, contentId);
            if (Objects.isNull(target)) {
                throw new IOException("Picasso Thumb Not Support");
            }
            return new RequestHandler.Result(target, Picasso.LoadedFrom.DISK);
        }
    }
}
