package dev.ragnarok.fenrir.picasso;

import android.os.Build;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import dev.ragnarok.fenrir.db.Stores;

public class LocalRequestHandler extends RequestHandler {

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getScheme() != null && data.uri.getScheme().equals("content");
    }

    public RequestHandler.Result load(Request request, int arg1) throws IOException {
        assert request.uri != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new RequestHandler.Result(Stores.getInstance().localMedia().getThumbnail(request.uri, 256, 256), Picasso.LoadedFrom.DISK);
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
            return new RequestHandler.Result(Stores.getInstance().localMedia().getOldThumbnail(ret, contentId), Picasso.LoadedFrom.DISK);
        }
    }
}
