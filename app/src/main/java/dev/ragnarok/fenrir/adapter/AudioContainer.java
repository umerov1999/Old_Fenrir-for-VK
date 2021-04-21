package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.player.util.MusicUtils.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.IdPair;
import dev.ragnarok.fenrir.model.menu.AudioItem;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Mp3InfoHelper;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioContainer extends LinearLayout {
    private final Context mContext;
    private final IAudioInteractor mAudioInteractor = InteractorFactory.createAudioInteractor();
    private Disposable mPlayerDisposable = Disposable.disposed();
    private Disposable audioListDisposable = Disposable.disposed();
    private List<Audio> audios = Collections.emptyList();
    private Audio currAudio = MusicUtils.getCurrentAudio();

    public AudioContainer(Context context) {
        super(context);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @DrawableRes
    private int getAudioCoverSimple() {
        return Settings.get().main().isAudio_round_icon() ? R.drawable.audio_button : R.drawable.audio_button_material;
    }

    private Transformation TransformCover() {
        return Settings.get().main().isAudio_round_icon() ? new RoundTransformation() : new PolyTransformation();
    }

    private void updateAudioStatus(AudioHolder holder, Audio audio) {
        if (!audio.equals(currAudio)) {
            holder.visual.setImageResource(isEmpty(audio.getUrl()) ? R.drawable.audio_died : R.drawable.song);
            holder.play_cover.clearColorFilter();
            return;
        }
        switch (MusicUtils.PlayerStatus()) {
            case 1:
                Utils.doWavesLottie(holder.visual, true);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;
            case 2:
                Utils.doWavesLottie(holder.visual, false);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;

        }
    }

    private void deleteTrack(int accountId, Audio audio) {
        audioListDisposable = mAudioInteractor.delete(accountId, audio.getId(), audio.getOwnerId()).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> {
            CustomToast.CreateCustomToast(mContext).showToast(R.string.deleted);
        }, t -> Utils.showErrorInAdapter((Activity) mContext, t));
    }

    private void addTrack(int accountId, Audio audio) {
        audioListDisposable = mAudioInteractor.add(accountId, audio, null).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() ->
                CustomToast.CreateCustomToast(mContext).showToast(R.string.added), t -> Utils.showErrorInAdapter((Activity) mContext, t));
    }

    private void getMp3AndBitrate(int accountId, Audio audio) {
        if (isEmpty(audio.getUrl()) || audio.isHLS()) {
            audioListDisposable = mAudioInteractor.getByIdOld(accountId, Collections.singletonList(new IdPair(audio.getId(), audio.getOwnerId()))).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> getBitrate(t.get(0).getUrl(), t.get(0).getDuration()), e -> getBitrate(audio.getUrl(), audio.getDuration()));
        } else {
            getBitrate(audio.getUrl(), audio.getDuration());
        }
    }

    private void getBitrate(String url, int duration) {
        if (isEmpty(url)) {
            return;
        }
        audioListDisposable = Mp3InfoHelper.getLength(Audio.getMp3FromM3u8(url)).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> CustomToast.CreateCustomToast(mContext).showToast(Mp3InfoHelper.getBitrate(mContext, duration, r)),
                        e -> Utils.showErrorInAdapter((Activity) mContext, e));
    }

    private void get_lyrics(Audio audio) {
        audioListDisposable = mAudioInteractor.getLyrics(Settings.get().accounts().getCurrent(), audio.getLyricsId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> onAudioLyricsReceived(t, audio), t -> Utils.showErrorInAdapter((Activity) mContext, t));
    }

    private void onAudioLyricsReceived(String Text, Audio audio) {
        String title = audio.getArtistAndTitle();

        MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(mContext);
        dlgAlert.setIcon(R.drawable.dir_song);
        dlgAlert.setMessage(Text);
        dlgAlert.setTitle(title != null ? title : mContext.getString(R.string.get_lyrics));
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setNeutralButton(R.string.copy_text, (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("response", Text);
            clipboard.setPrimaryClip(clip);
            CustomToast.CreateCustomToast(mContext).showToast(R.string.copied_to_clipboard);
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void dispose() {
        mPlayerDisposable.dispose();
        audios = Collections.emptyList();
    }

    public void displayAudios(ArrayList<Audio> audios, AttachmentsViewBinder.OnAttachmentsActionCallback mAttachmentsActionCallback) {
        setVisibility(safeIsEmpty(audios) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(audios)) {
            dispose();
            return;
        }
        this.audios = audios;

        int i = audios.size() - getChildCount();
        for (int j = 0; j < i; j++) {
            addView(LayoutInflater.from(mContext).inflate(R.layout.item_audio, this, false));
        }

        for (int g = 0; g < getChildCount(); g++) {
            ViewGroup root = (ViewGroup) getChildAt(g);
            if (g < audios.size()) {
                Audio audio = audios.get(g);

                AudioHolder holder = new AudioHolder(root);

                holder.tvTitle.setText(audio.getArtist());
                holder.tvSubtitle.setText(audio.getTitle());

                if (!audio.isLocal() && !audio.isLocalServer() && Constants.DEFAULT_ACCOUNT_TYPE == Account_Types.VK_ANDROID && !audio.isHLS()) {
                    holder.quality.setVisibility(View.VISIBLE);
                    if (audio.getIsHq()) {
                        holder.quality.setImageResource(R.drawable.high_quality);
                    } else {
                        holder.quality.setImageResource(R.drawable.low_quality);
                    }
                } else {
                    holder.quality.setVisibility(View.GONE);
                }

                updateAudioStatus(holder, audio);
                int finalG = g;

                if (!isEmpty(audio.getThumb_image_little())) {
                    PicassoInstance.with()
                            .load(audio.getThumb_image_little())
                            .placeholder(java.util.Objects.requireNonNull(ResourcesCompat.getDrawable(mContext.getResources(), getAudioCoverSimple(), mContext.getTheme())))
                            .transform(TransformCover())
                            .tag(Constants.PICASSO_TAG)
                            .into(holder.play_cover);
                } else {
                    PicassoInstance.with().cancelRequest(holder.play_cover);
                    holder.play_cover.setImageResource(getAudioCoverSimple());
                }

                holder.ibPlay.setOnLongClickListener(v -> {
                    if (!isEmpty(audio.getThumb_image_very_big())
                            || !isEmpty(audio.getThumb_image_big()) || !isEmpty(audio.getThumb_image_little())) {
                        mAttachmentsActionCallback.onUrlPhotoOpen(firstNonEmptyString(audio.getThumb_image_very_big(),
                                audio.getThumb_image_big(), audio.getThumb_image_little()), audio.getArtist(), audio.getTitle());
                    }
                    return true;
                });

                holder.ibPlay.setOnClickListener(v -> {
                    if (MusicUtils.isNowPlayingOrPreparingOrPaused(audio)) {
                        if (!Settings.get().other().isUse_stop_audio()) {
                            updateAudioStatus(holder, audio);
                            MusicUtils.playOrPause();
                        } else {
                            updateAudioStatus(holder, audio);
                            MusicUtils.stop();
                        }
                    } else {
                        updateAudioStatus(holder, audio);
                        mAttachmentsActionCallback.onAudioPlay(finalG, audios);
                    }
                });
                if (audio.getDuration() <= 0)
                    holder.time.setVisibility(View.INVISIBLE);
                else {
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(AppTextUtils.getDurationString(audio.getDuration()));
                }

                audioListDisposable = Single.create((SingleOnSubscribe<Integer>) emitter -> emitter.onSuccess(DownloadWorkUtils.TrackIsDownloaded(audio)))
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(v -> {
                            if (v == 2) {
                                holder.saved.setImageResource(R.drawable.remote_cloud);
                                Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(mContext));
                            } else {
                                holder.saved.setImageResource(R.drawable.save);
                                Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(mContext));
                            }
                            holder.saved.setVisibility(v != 0 ? View.VISIBLE : View.GONE);
                        }, RxUtils.ignore());
                holder.lyric.setVisibility(audio.getLyricsId() != 0 ? View.VISIBLE : View.GONE);

                holder.my.setVisibility(audio.getOwnerId() == Settings.get().accounts().getCurrent() ? View.VISIBLE : View.GONE);
                holder.Track.setOnLongClickListener(v -> {
                    if (!AppPerms.hasReadWriteStoragePermission(mContext)) {
                        if (mAttachmentsActionCallback != null) {
                            mAttachmentsActionCallback.onRequestWritePermissions();
                        }
                        return false;
                    }
                    holder.saved.setVisibility(View.VISIBLE);
                    holder.saved.setImageResource(R.drawable.save);
                    Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(mContext));
                    int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false, false);
                    if (ret == 0)
                        CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.saved_audio);
                    else if (ret == 1 || ret == 2) {
                        Utils.ThemedSnack(v, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true, false)).show();

                    } else {
                        holder.saved.setVisibility(View.GONE);
                        CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.error_audio);
                    }
                    return true;
                });

                holder.Track.setOnClickListener(view -> {
                    holder.cancelSelectionAnimation();
                    holder.startSomeAnimation();

                    ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();

                    menus.add(new OptionRequest(AudioItem.play_item_audio, mContext.getString(R.string.play), R.drawable.play));
                    if (audio.getOwnerId() != Settings.get().accounts().getCurrent()) {
                        menus.add(new OptionRequest(AudioItem.add_item_audio, mContext.getString(R.string.action_add), R.drawable.list_add));
                        menus.add(new OptionRequest(AudioItem.add_and_download_button, mContext.getString(R.string.add_and_download_button), R.drawable.add_download));
                    } else
                        menus.add(new OptionRequest(AudioItem.add_item_audio, mContext.getString(R.string.delete), R.drawable.ic_outline_delete));
                    menus.add(new OptionRequest(AudioItem.share_button, mContext.getString(R.string.share), R.drawable.ic_outline_share));
                    menus.add(new OptionRequest(AudioItem.save_item_audio, mContext.getString(R.string.save), R.drawable.save));
                    if (audio.getAlbumId() != 0)
                        menus.add(new OptionRequest(AudioItem.open_album, mContext.getString(R.string.open_album), R.drawable.audio_album));
                    menus.add(new OptionRequest(AudioItem.get_recommendation_by_audio, mContext.getString(R.string.get_recommendation_by_audio), R.drawable.music_mic));

                    if (!isEmpty(audio.getMain_artists()))
                        menus.add(new OptionRequest(AudioItem.goto_artist, mContext.getString(R.string.audio_goto_artist), R.drawable.artist_icon));

                    if (audio.getLyricsId() != 0)
                        menus.add(new OptionRequest(AudioItem.get_lyrics_menu, mContext.getString(R.string.get_lyrics_menu), R.drawable.lyric));

                    menus.add(new OptionRequest(AudioItem.bitrate_item_audio, mContext.getString(R.string.get_bitrate), R.drawable.high_quality));
                    menus.add(new OptionRequest(AudioItem.search_by_artist, mContext.getString(R.string.search_by_artist), R.drawable.magnify));
                    menus.add(new OptionRequest(AudioItem.copy_url, mContext.getString(R.string.copy_url), R.drawable.content_copy));


                    menus.header(firstNonEmptyString(audio.getArtist(), " ") + " - " + audio.getTitle(), R.drawable.song, audio.getThumb_image_little());
                    menus.columns(2);
                    menus.show(((FragmentActivity) mContext).getSupportFragmentManager(), "audio_options", option -> {
                        switch (option.getId()) {
                            case AudioItem.play_item_audio:
                                mAttachmentsActionCallback.onAudioPlay(finalG, audios);
                                PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(mContext);
                                break;
                            case AudioItem.share_button:
                                SendAttachmentsActivity.startForSendAttachments(mContext, Settings.get().accounts().getCurrent(), audio);
                                break;
                            case AudioItem.search_by_artist:
                                PlaceFactory.getSingleTabSearchPlace(Settings.get().accounts().getCurrent(), SearchContentType.AUDIOS, new AudioSearchCriteria(audio.getArtist(), true, false)).tryOpenWith(mContext);
                                break;
                            case AudioItem.get_lyrics_menu:
                                get_lyrics(audio);
                                break;
                            case AudioItem.get_recommendation_by_audio:
                                PlaceFactory.SearchByAudioPlace(Settings.get().accounts().getCurrent(), audio.getOwnerId(), audio.getId()).tryOpenWith(mContext);
                                break;
                            case AudioItem.open_album:
                                PlaceFactory.getAudiosInAlbumPlace(Settings.get().accounts().getCurrent(), audio.getAlbum_owner_id(), audio.getAlbumId(), audio.getAlbum_access_key()).tryOpenWith(mContext);
                                break;
                            case AudioItem.copy_url:
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("response", audio.getUrl());
                                clipboard.setPrimaryClip(clip);
                                CustomToast.CreateCustomToast(mContext).showToast(R.string.copied);
                                break;
                            case AudioItem.add_item_audio:
                                boolean myAudio = audio.getOwnerId() == Settings.get().accounts().getCurrent();
                                if (myAudio) {
                                    deleteTrack(Settings.get().accounts().getCurrent(), audio);
                                } else {
                                    addTrack(Settings.get().accounts().getCurrent(), audio);
                                }
                                break;
                            case AudioItem.add_and_download_button:
                                addTrack(Settings.get().accounts().getCurrent(), audio);
                            case AudioItem.save_item_audio:
                                if (!AppPerms.hasReadWriteStoragePermission(mContext)) {
                                    if (mAttachmentsActionCallback != null) {
                                        mAttachmentsActionCallback.onRequestWritePermissions();
                                    }
                                    break;
                                }
                                holder.saved.setVisibility(View.VISIBLE);
                                holder.saved.setImageResource(R.drawable.save);
                                Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(mContext));
                                int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false, false);
                                if (ret == 0)
                                    CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.saved_audio);
                                else if (ret == 1 || ret == 2) {
                                    Utils.ThemedSnack(view, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                            v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true, false)).show();
                                } else {
                                    holder.saved.setVisibility(View.GONE);
                                    CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.error_audio);
                                }
                                break;
                            case AudioItem.bitrate_item_audio:
                                getMp3AndBitrate(Settings.get().accounts().getCurrent(), audio);
                                break;

                            case AudioItem.goto_artist:
                                String[][] artists = Utils.getArrayFromHash(audio.getMain_artists());
                                if (audio.getMain_artists().keySet().size() > 1) {
                                    new MaterialAlertDialogBuilder(mContext)
                                            .setItems(artists[1], (dialog, which) -> PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][which], false).tryOpenWith(mContext)).show();
                                } else {
                                    PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][0], false).tryOpenWith(mContext);
                                }
                                break;
                        }
                    });
                });

                root.setVisibility(View.VISIBLE);
                root.setTag(audio);
            } else {
                root.setVisibility(View.GONE);
                root.setTag(null);
            }
        }
        mPlayerDisposable.dispose();
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    private void onServiceBindEvent(@MusicUtils.PlayerStatus int status) {
        switch (status) {
            case MusicUtils.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicUtils.PlayerStatus.UPDATE_PLAY_PAUSE:
            case MusicUtils.PlayerStatus.SERVICE_KILLED:
                currAudio = MusicUtils.getCurrentAudio();
                if (getChildCount() < audios.size())
                    return;
                for (int g = 0; g < audios.size(); g++) {
                    ViewGroup root = (ViewGroup) getChildAt(g);
                    AudioHolder holder = new AudioHolder(root);
                    updateAudioStatus(holder, audios.get(g));
                }
                break;
            case MusicUtils.PlayerStatus.REPEATMODE_CHANGED:
            case MusicUtils.PlayerStatus.SHUFFLEMODE_CHANGED:
            case MusicUtils.PlayerStatus.UPDATE_PLAY_LIST:
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        currAudio = MusicUtils.getCurrentAudio();
        if (!isEmpty(audios)) {
            mPlayerDisposable = observeServiceBinding()
                    .compose(RxUtils.applyObservableIOToMainSchedulers())
                    .subscribe(this::onServiceBindEvent);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerDisposable.dispose();
        audioListDisposable.dispose();
    }

    private class AudioHolder {
        final TextView tvTitle;
        final TextView tvSubtitle;
        final View ibPlay;
        final ImageView play_cover;
        final TextView time;
        final ImageView saved;
        final ImageView lyric;
        final ImageView my;
        final ImageView quality;
        final View Track;
        final MaterialCardView selectionView;
        final MaterialCardView isSelectedView;
        final Animator.AnimatorListener animationAdapter;
        final RLottieImageView visual;
        ObjectAnimator animator;

        AudioHolder(View root) {
            tvTitle = root.findViewById(R.id.dialog_title);
            tvSubtitle = root.findViewById(R.id.dialog_message);
            ibPlay = root.findViewById(R.id.item_audio_play);
            play_cover = root.findViewById(R.id.item_audio_play_cover);
            time = root.findViewById(R.id.item_audio_time);
            saved = root.findViewById(R.id.saved);
            lyric = root.findViewById(R.id.lyric);
            Track = root.findViewById(R.id.track_option);
            my = root.findViewById(R.id.my);
            selectionView = root.findViewById(R.id.item_audio_selection);
            isSelectedView = root.findViewById(R.id.item_audio_select_add);
            isSelectedView.setVisibility(View.GONE);
            quality = root.findViewById(R.id.quality);
            visual = root.findViewById(R.id.item_audio_visual);
            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.GONE);
                }
            };
        }

        void startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(mContext));
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void cancelSelectionAnimation() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            selectionView.setVisibility(View.INVISIBLE);
        }
    }
}
