package dev.ragnarok.fenrir.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.view.media.MaterialPlayPauseFab;
import dev.ragnarok.fenrir.view.media.MediaActionDrawable;

public class PlayPauseButton extends MaterialPlayPauseFab implements OnClickListener {

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MusicUtils.playOrPause();
        updateState();
    }

    public void updateState() {
        if (MusicUtils.getCurrentAudio() == null) {
            setIcon(MediaActionDrawable.ICON_EMPTY, true);
        } else if (MusicUtils.isPlaying()) {
            setIcon(MediaActionDrawable.ICON_PAUSE, true);
        } else {
            setIcon(MediaActionDrawable.ICON_PLAY, true);
        }
    }

}
