package dev.ragnarok.fenrir.media.voice;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import org.jetbrains.annotations.NotNull;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.media.exo.ExoUtil;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Utils;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class ExoVoicePlayer implements IVoicePlayer {

    private final Context app;
    private final ProxyConfig proxyConfig;
    private SimpleExoPlayer exoPlayer;
    private int status;
    private AudioEntry playingEntry;
    private boolean supposedToBePlaying;
    private IPlayerStatusListener statusListener;
    private IErrorListener errorListener;

    public ExoVoicePlayer(Context context, ProxyConfig config) {
        app = context.getApplicationContext();
        proxyConfig = config;
        status = STATUS_NO_PLAYBACK;
    }

    @Override
    public boolean toggle(int id, VoiceMessage audio) {
        if (nonNull(playingEntry) && playingEntry.getId() == id) {
            setSupposedToBePlaying(!isSupposedToPlay());
            return false;
        }

        release();

        playingEntry = new AudioEntry(id, audio);
        supposedToBePlaying = true;

        preparePlayer();
        return true;
    }

    private void setStatus(int status) {
        if (this.status != status) {
            this.status = status;

            if (nonNull(statusListener)) {
                statusListener.onPlayerStatusChange(status);
            }
        }
    }

    private void preparePlayer() {
        setStatus(STATUS_PREPARING);

        int extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        switch (Settings.get().other().getFFmpegPlugin()) {
            case 0:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                break;
            case 1:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON;
                break;
            case 2:
                extensionRenderer = DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
                break;
        }
        exoPlayer = new SimpleExoPlayer.Builder(app, new DefaultRenderersFactory(app).setExtensionRendererMode(extensionRenderer)).build();
        exoPlayer.setWakeMode(C.WAKE_MODE_NETWORK);

        String userAgent = Constants.USER_AGENT(Account_Types.BY_TYPE);

        String url = playingEntry.getAudio().getLinkMp3();

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem(url));
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                onInternalPlayerStateChanged(state);
            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException error) {
                onExoPlayerException(error);
            }
        });

        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
        exoPlayer.setPlayWhenReady(supposedToBePlaying);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
    }

    private void onExoPlayerException(ExoPlaybackException e) {
        if (nonNull(errorListener)) {
            errorListener.onPlayError(new PrepareException(e));
        }
    }

    private void onInternalPlayerStateChanged(@Player.State int state) {
        Logger.d("ExoVoicePlayer", "onInternalPlayerStateChanged, state: " + state);

        switch (state) {
            case Player.STATE_READY:
                setStatus(STATUS_PREPARED);
                break;
            case Player.STATE_ENDED:
                setSupposedToBePlaying(false);
                exoPlayer.seekTo(0);
                break;
            case Player.STATE_BUFFERING:
            case Player.STATE_IDLE:
                break;
        }
    }

    private void setSupposedToBePlaying(boolean supposedToBePlaying) {
        this.supposedToBePlaying = supposedToBePlaying;

        if (supposedToBePlaying) {
            ExoUtil.startPlayer(exoPlayer);
        } else {
            ExoUtil.pausePlayer(exoPlayer);
        }
    }

    @Override
    public float getProgress() {
        if (isNull(exoPlayer)) {
            return 0f;
        }

        if (status != STATUS_PREPARED) {
            return 0f;
        }

        long duration = exoPlayer.getDuration();
        long position = exoPlayer.getCurrentPosition();
        return (float) position / (float) duration;
    }

    @Override
    public void setCallback(@Nullable IPlayerStatusListener listener) {
        statusListener = listener;
    }

    @Override
    public void setErrorListener(@Nullable IErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public Optional<Integer> getPlayingVoiceId() {
        return isNull(playingEntry) ? Optional.empty() : Optional.wrap(playingEntry.getId());
    }

    @Override
    public boolean isSupposedToPlay() {
        return supposedToBePlaying;
    }

    @Override
    public void release() {
        if (nonNull(exoPlayer)) {
            exoPlayer.stop();
            exoPlayer.release();
        }
    }
}
