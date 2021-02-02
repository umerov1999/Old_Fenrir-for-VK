package dev.ragnarok.fenrir.media.video;

import android.content.Context;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.video.VideoListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.media.exo.ExoUtil;
import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.VideoSize;
import dev.ragnarok.fenrir.util.Utils;

public class ExoVideoPlayer implements IVideoPlayer {

    private final SimpleExoPlayer player;
    private final OnVideoSizeChangedListener onVideoSizeChangedListener = new OnVideoSizeChangedListener(this);
    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);
    private MediaSource source;
    private boolean supposedToBePlaying;
    private boolean prepareCalled;

    public ExoVideoPlayer(Context context, String url, ProxyConfig config, @InternalVideoSize int size) {
        player = createPlayer(context);
        player.addVideoListener(onVideoSizeChangedListener);
        source = createMediaSource(context, url, config, size == InternalVideoSize.SIZE_HLS || size == InternalVideoSize.SIZE_LIVE);
    }

    private static MediaSource createMediaSource(Context context, String url, ProxyConfig proxyConfig, boolean isHLS) {
        String userAgent = Constants.USER_AGENT(Account_Types.BY_TYPE);
        if (!isHLS) {
            if (url.contains("file://") || url.contains("content://")) {
                return new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(context, userAgent)).createMediaSource(Utils.makeMediaItem(url));
            }
            return new ProgressiveMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem(url));
        } else {
            return new HlsMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem(url));
        }
    }

    @Override
    public void updateSource(Context context, String url, ProxyConfig config, @InternalVideoSize int size) {
        source = createMediaSource(context, url, config, size == InternalVideoSize.SIZE_HLS || size == InternalVideoSize.SIZE_LIVE);
        player.setMediaSource(source);
        player.prepare();
        ExoUtil.startPlayer(player);
    }

    private SimpleExoPlayer createPlayer(Context context) {
        SimpleExoPlayer ret = new SimpleExoPlayer.Builder(context, new DefaultRenderersFactory(context)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)).build();
        ret.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).setUsage(C.USAGE_MEDIA).build(), true);
        return ret;
    }

    @Override
    public void play() {
        if (supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = true;

        if (!prepareCalled) {
            player.setMediaSource(source);
            player.prepare();
            prepareCalled = true;
        }

        ExoUtil.startPlayer(player);
    }

    @Override
    public void pause() {
        if (!supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = false;
        ExoUtil.pausePlayer(player);
    }

    @Override
    public void release() {
        player.removeVideoListener(onVideoSizeChangedListener);
        player.release();
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public boolean isPlaying() {
        return supposedToBePlaying;
    }

    @Override
    public int getBufferPercentage() {
        return player.getBufferedPercentage();
    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        player.setVideoSurfaceHolder(holder);
    }

    private void onVideoSizeChanged(int w, int h) {
        for (IVideoSizeChangeListener listener : videoSizeChangeListeners) {
            listener.onVideoSizeChanged(this, new VideoSize(w, h));
        }
    }

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        videoSizeChangeListeners.remove(listener);
    }

    private static final class OnVideoSizeChangedListener implements VideoListener {

        final WeakReference<ExoVideoPlayer> ref;

        private OnVideoSizeChangedListener(ExoVideoPlayer player) {
            ref = new WeakReference<>(player);
        }

        @Override
        public void onVideoSizeChanged(int i, int i1, int i2, float v) {
            ExoVideoPlayer player = ref.get();
            if (player != null) {
                player.onVideoSizeChanged(i, i1);
            }
        }

        @Override
        public void onRenderedFirstFrame() {

        }
    }
}
