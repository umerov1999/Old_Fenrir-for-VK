package dev.ragnarok.fenrir.media.video;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.VideoSize;


public interface IVideoPlayer {
    void play();

    void pause();

    void release();

    int getDuration();

    int getCurrentPosition();

    void seekTo(int position);

    boolean isPlaying();

    int getBufferPercentage();

    void setSurfaceHolder(SurfaceHolder holder);

    void addVideoSizeChangeListener(IVideoSizeChangeListener listener);

    void removeVideoSizeChangeListener(IVideoSizeChangeListener listener);

    interface IVideoSizeChangeListener {
        void onVideoSizeChanged(@NonNull IVideoPlayer player, VideoSize size);
    }
}