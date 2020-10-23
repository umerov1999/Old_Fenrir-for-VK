package dev.ragnarok.fenrir.picasso;

import android.os.Build;

import com.squareup.picasso3.Picasso;
import com.squareup.picasso3.Request;
import com.squareup.picasso3.RequestHandler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import dev.ragnarok.fenrir.db.Stores;

public class LocalRequestHandler extends RequestHandler {

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri != null && data.uri.getScheme() != null && data.uri.getScheme().equals("content");
    }

    @Override
    public void load(@NotNull Picasso picasso, @NotNull Request request, @NotNull Callback callback) throws IOException {
        assert request.uri != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callback.onSuccess(new RequestHandler.Result.Bitmap(Stores.getInstance().localMedia().getThumbnail(request.uri, 256, 256), Picasso.LoadedFrom.DISK));
        } else {
            long contentId = Long.parseLong(request.uri.getLastPathSegment());
            @Content_Local int ret = Content_Local.PHOTO;
            if (request.uri.getPath().contains("videos")) {
                ret = Content_Local.VIDEO;
            } else if (request.uri.getPath().contains("images")) {
                ret = Content_Local.PHOTO;
            } else if (request.uri.getPath().contains("audios")) {
                ret = Content_Local.AUDIO;
            } else {
                callback.onError(new Exception("Picasso Thumb Not Support"));
            }
            callback.onSuccess(new RequestHandler.Result.Bitmap(Stores.getInstance().localMedia().getOldThumbnail(ret, contentId), Picasso.LoadedFrom.DISK));
        }
    }
}
