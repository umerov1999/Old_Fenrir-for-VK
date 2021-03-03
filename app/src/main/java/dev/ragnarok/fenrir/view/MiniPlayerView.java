package dev.ragnarok.fenrir.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.Objects;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.module.rlottie.RLottieImageView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.Disposable;

import static dev.ragnarok.fenrir.player.util.MusicUtils.mService;
import static dev.ragnarok.fenrir.player.util.MusicUtils.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

public class MiniPlayerView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {

    private static final int REFRESH_TIME = 1;
    private Disposable mPlayerDisposable = Disposable.disposed();
    private int mAccountId;
    private RLottieImageView visual;
    private ImageView play_cover;
    private TextView Title;
    private SeekBar mProgress;
    private boolean mFromTouch;
    private long mPosOverride = -1;
    private View lnt;
    private TimeHandler mTimeHandler;
    private long mLastSeekEventTime;

    public MiniPlayerView(Context context) {
        super(context);
        init();
    }

    public MiniPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.mini_player, this);
        View play = root.findViewById(R.id.item_audio_play);
        play_cover = root.findViewById(R.id.item_audio_play_cover);
        visual = root.findViewById(R.id.item_audio_visual);
        lnt = root.findViewById(R.id.miniplayer_layout);
        lnt.setVisibility(MusicUtils.getMiniPlayerVisibility() ? View.VISIBLE : View.GONE);
        ImageButton mPClosePlay = root.findViewById(R.id.close_player);
        mPClosePlay.setOnClickListener(v -> {
                    MusicUtils.closeMiniPlayer();
                    lnt.setVisibility(View.GONE);
                }
        );
        play.setOnClickListener(v -> {
            MusicUtils.playOrPause();
            if (MusicUtils.isPlaying()) {
                Utils.doWavesLottie(visual, true);
                play_cover.setColorFilter(Color.parseColor("#44000000"));
            } else {
                Utils.doWavesLottie(visual, false);
                play_cover.clearColorFilter();
            }
        });
        play.setOnLongClickListener(v -> {
            MusicUtils.next();
            return true;
        });
        ImageButton mOpenPlayer = root.findViewById(R.id.open_player);
        mOpenPlayer.setOnClickListener(v -> PlaceFactory.getPlayerPlace(mAccountId).tryOpenWith(getContext()));
        Title = root.findViewById(R.id.mini_artist);
        Title.setSelected(true);
        mProgress = root.findViewById(R.id.SeekBar01);
        mProgress.setOnSeekBarChangeListener(this);

    }

    private void queueNextRefresh(long delay) {
        Message message = mTimeHandler.obtainMessage(REFRESH_TIME);

        mTimeHandler.removeMessages(REFRESH_TIME);
        mTimeHandler.sendMessageDelayed(message, delay);
    }

    private Transformation TransformCover() {
        return Settings.get().main().isAudio_round_icon() ? new RoundTransformation() : new PolyTransformation();
    }

    @DrawableRes
    private int getAudioCoverSimple() {
        return Settings.get().main().isAudio_round_icon() ? R.drawable.audio_button : R.drawable.audio_button_material;
    }

    private void updatePlaybackControls() {
        if (nonNull(play_cover)) {
            if (MusicUtils.isPlaying()) {
                Utils.doWavesLottie(visual, true);
                play_cover.setColorFilter(Color.parseColor("#44000000"));
            } else {
                Utils.doWavesLottie(visual, false);
                play_cover.clearColorFilter();
            }
        }
    }

    private void recvFullAudioInfo() {
        updateVisibility();
        updateNowPlayingInfo();
        updatePlaybackControls();
        resolveControlViews();

    }

    private void onServiceBindEvent(@MusicUtils.PlayerStatus int status) {
        switch (status) {
            case MusicUtils.PlayerStatus.UPDATE_TRACK_INFO:
                updateVisibility();
                updateNowPlayingInfo();
                updatePlaybackControls();
                resolveControlViews();
                break;
            case MusicUtils.PlayerStatus.UPDATE_PLAY_PAUSE:
                updateVisibility();
                updatePlaybackControls();
                resolveControlViews();
                break;
            case MusicUtils.PlayerStatus.SERVICE_KILLED:
                updateVisibility();
                updatePlaybackControls();
                updateNowPlayingInfo();
                resolveControlViews();
                break;
            case MusicUtils.PlayerStatus.REPEATMODE_CHANGED:
            case MusicUtils.PlayerStatus.SHUFFLEMODE_CHANGED:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNowPlayingInfo() {
        String artist = MusicUtils.getArtistName();
        String trackName = MusicUtils.getTrackName();
        Title.setText(firstNonEmptyString(artist, " ") + " - " + firstNonEmptyString(trackName, " "));
        if (nonNull(play_cover)) {
            Audio audio = MusicUtils.getCurrentAudio();
            if (audio != null && !Utils.isEmpty(audio.getThumb_image_little())) {
                PicassoInstance.with()
                        .load(audio.getThumb_image_little())
                        .placeholder(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), getAudioCoverSimple(), getContext().getTheme())))
                        .transform(TransformCover())
                        .into(play_cover);
            } else {
                PicassoInstance.with().cancelRequest(play_cover);
                play_cover.setImageResource(getAudioCoverSimple());
            }
        }
        //queueNextRefresh(1);
    }

    private void updateVisibility() {
        lnt.setVisibility(MusicUtils.getMiniPlayerVisibility() ? View.VISIBLE : View.GONE);
    }

    private void resolveControlViews() {
        if (mProgress == null) return;

        boolean preparing = MusicUtils.isPreparing();
        boolean initialized = MusicUtils.isInitialized();
        mProgress.setEnabled(!preparing && initialized);
        //mProgress.setIndeterminate(preparing);
    }

    private long refreshCurrentTime() {
        if (!MusicUtils.isInitialized()) {
            return 500;
        }

        try {
            long pos = mPosOverride < 0 ? MusicUtils.position() : mPosOverride;
            long duration = MusicUtils.duration();

            if (pos >= 0 && duration > 0) {
                int progress = (int) (1000 * pos / duration);

                mProgress.setProgress(progress);

                int bufferProgress = (int) ((float) MusicUtils.bufferPercent() * 10F);
                mProgress.setSecondaryProgress(bufferProgress);

                if (mFromTouch) {
                    return 500;
                } else if (!MusicUtils.isPlaying()) {
                    return 500;
                }
            } else {
                mProgress.setProgress(0);
                return 500;
            }

            return 500;
        } catch (Exception ignored) {
        }

        return 500;
    }

    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        if (!fromuser || mService == null) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now;

            refreshCurrentTime();

            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1;
            }
        }

        mPosOverride = MusicUtils.duration() * progress / 1000;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mFromTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPosOverride != -1) {
            MusicUtils.seek(mPosOverride);
            mPosOverride = -1;
        }

        mFromTouch = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        recvFullAudioInfo();
        mTimeHandler = new TimeHandler(this);

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        long next = refreshCurrentTime();
        queueNextRefresh(next);
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerDisposable.dispose();

        mTimeHandler.removeMessages(REFRESH_TIME);
    }

    private static final class TimeHandler extends Handler {

        private final WeakReference<MiniPlayerView> mAudioPlayer;

        /**
         * Constructor of <code>TimeHandler</code>
         */
        TimeHandler(MiniPlayerView player) {
            super(Looper.getMainLooper());
            mAudioPlayer = new WeakReference<>(player);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_TIME) {
                if (mAudioPlayer.get() == null)
                    return;
                long next = mAudioPlayer.get().refreshCurrentTime();
                mAudioPlayer.get().queueNextRefresh(next);
            }
        }
    }
}
