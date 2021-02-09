package dev.ragnarok.fenrir.settings;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.api.model.LocalServerSettings;
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;
import dev.ragnarok.fenrir.model.Lang;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.SwitchableCategory;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.place.Place;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;

public interface ISettings {

    IRecentChats recentChats();

    IDrawerSettings drawerSettings();

    IPushSettings pushSettings();

    ISecuritySettings security();

    IUISettings ui();

    INotificationSettings notifications();

    IMainSettings main();

    IAccountsSettings accounts();

    IOtherSettings other();

    interface IOtherSettings {
        String getFeedSourceIds(int accountId);

        void setFeedSourceIds(int accountId, String sourceIds);

        void storeFeedScrollState(int accountId, String state);

        String restoreFeedScrollState(int accountId);

        String restoreFeedNextFrom(int accountId);

        void storeFeedNextFrom(int accountId, String nextFrom);

        boolean isAudioBroadcastActive();

        void setAudioBroadcastActive(boolean active);

        boolean isShow_audio_cover();

        int getMaxBitmapResolution();

        boolean isNative_parcel();

        boolean isUse_coil();

        String get_Api_Domain();

        String get_Auth_Domain();

        boolean isUse_old_vk_api();

        boolean isDisable_history();

        boolean isShow_wall_cover();

        boolean isDeveloper_mode();

        boolean isForce_cache();

        boolean isKeepLongpoll();

        void setKeepLongpoll(boolean en);

        void setDisableErrorFCM(boolean en);

        boolean isDisabledErrorFCM();

        boolean isSettings_no_push();

        boolean isCommentsDesc();

        boolean toggleCommentsDirection();

        boolean isInfo_reading();

        boolean isAuto_read();

        boolean isNot_update_dialogs();

        boolean isBe_online();

        boolean isShow_donate_anim();

        int getColorChat();

        int getSecondColorChat();

        boolean isCustom_chat_color();

        int getColorMyMessage();

        int getSecondColorMyMessage();

        boolean isCustom_MyMessage();

        boolean isUse_stop_audio();

        boolean isBlur_for_player();

        boolean isShow_mini_player();

        boolean isEnable_show_recent_dialogs();

        boolean isEnable_show_audio_top();

        boolean isUse_internal_downloader();

        boolean isEnable_last_read();

        boolean isNot_read_show();

        String getMusicDir();

        String getPhotoDir();

        String getVideoDir();

        String getDocDir();

        String getStickerDir();

        boolean isPhoto_to_user_dir();

        boolean isDelete_cache_images();

        boolean isClick_next_track();

        boolean isDisabled_encryption();

        boolean isDownload_photo_tap();

        boolean isDisable_sensored_voice();

        boolean isAudio_save_mode_button();

        boolean isShow_mutual_count();

        boolean isNot_friend_show();

        boolean isDo_zoom_photo();

        boolean isChange_upload_size();

        boolean isShow_photos_line();

        boolean isDisable_likes();

        boolean isDo_auto_play_video();

        boolean isHint_stickers();

        void registerDonatesId(List<Integer> Ids);

        @NonNull
        List<Integer> getDonates();

        int getPaganSymbol();

        boolean isRunes_show();

        boolean isShow_pagan_symbol();

        @Lang
        int getLanguage();

        int getEndListAnimation();

        void setSymbolSelectShow(boolean show);

        @NonNull
        LocalServerSettings getLocalServer();

        void setLocalServer(@NonNull LocalServerSettings settings);
    }

    interface IAccountsSettings {
        int INVALID_ID = -1;

        Flowable<Integer> observeChanges();

        Flowable<IAccountsSettings> observeRegistered();

        List<Integer> getRegistered();

        int getCurrent();

        void setCurrent(int accountId);

        void remove(int accountId);

        void registerAccountId(int accountId, boolean setCurrent);

        void storeAccessToken(int accountId, String accessToken);

        void storeLogin(int accountId, String loginCombo);

        void removeDevice(int accountId);

        void storeDevice(int accountId, String deviceName);

        @Nullable
        String getDevice(int accountId);

        String getLogin(int accountId);

        void storeTokenType(int accountId, @Account_Types int type);

        String getAccessToken(int accountId);

        @Account_Types
        int getType(int accountId);

        void removeAccessToken(int accountId);

        void removeType(int accountId);

        void removeLogin(int accountId);
    }

    interface IMainSettings {

        boolean isSendByEnter();

        boolean isMy_message_no_color();

        boolean is_smooth_chat();

        boolean isMessages_menu_down();

        boolean isAmoledTheme();

        boolean isAudio_round_icon();

        boolean isUse_long_click_download();

        boolean isShow_bot_keyboard();

        boolean isPlayer_support_volume();

        boolean isCustomTabEnabled();

        @Nullable
        Integer getUploadImageSize();

        void setUploadImageSize(Integer size);

        int getUploadImageSizePref();

        @PhotoSize
        int getPrefPreviewImageSize();

        void notifyPrefPreviewSizeChanged();

        @PhotoSize
        int getPrefDisplayImageSize(@PhotoSize int byDefault);

        int getStart_newsMode();

        void setPrefDisplayImageSize(@PhotoSize int size);

        boolean isWebview_night_mode();

        boolean isSnow_mode();

        int getPhotoRoundMode();

        int getFontSize();

        boolean isLoad_history_notif();

        boolean isDont_write();

        boolean isOver_ten_attach();

        int cryptVersion();
    }

    interface INotificationSettings {
        int FLAG_SOUND = 1;
        int FLAG_VIBRO = 2;
        int FLAG_LED = 4;
        int FLAG_SHOW_NOTIF = 8;
        int FLAG_HIGH_PRIORITY = 16;

        int getNotifPref(int aid, int peerid);

        void setDefault(int aid, int peerId);

        void setNotifPref(int aid, int peerid, int flag);

        int getOtherNotificationMask();

        boolean isCommentsNotificationsEnabled();

        boolean isFriendRequestAcceptationNotifEnabled();

        boolean isNewFollowerNotifEnabled();

        boolean isWallPublishNotifEnabled();

        boolean isGroupInvitedNotifEnabled();

        boolean isReplyNotifEnabled();

        boolean isNewPostOnOwnWallNotifEnabled();

        boolean isNewPostsNotificationEnabled();

        boolean isLikeNotificationEnable();

        Uri getFeedbackRingtoneUri();

        String getDefNotificationRingtone();

        String getNotificationRingtone();

        void setNotificationRingtoneUri(String path);

        long[] getVibrationLength();

        boolean isQuickReplyImmediately();

        boolean isBirtdayNotifEnabled();
    }

    interface IRecentChats {
        List<RecentChat> get(int acountid);

        void store(int accountid, List<RecentChat> chats);
    }

    interface IDrawerSettings {
        boolean isCategoryEnabled(@SwitchableCategory int category);

        void setCategoriesOrder(@SwitchableCategory int[] order, boolean[] active);

        int[] getCategoriesOrder();

        Observable<Object> observeChanges();
    }

    interface IPushSettings {
        void savePushRegistations(Collection<VkPushRegistration> data);

        List<VkPushRegistration> getRegistrations();
    }

    interface ISecuritySettings {
        boolean isKeyEncryptionPolicyAccepted();

        void setKeyEncryptionPolicyAccepted(boolean accepted);

        boolean isPinValid(@NonNull int[] values);

        void setPin(@Nullable int[] pin);

        boolean isUsePinForEntrance();

        boolean isUsePinForSecurity();

        boolean isEntranceByFingerprintAllowed();

        @KeyLocationPolicy
        int getEncryptionLocationPolicy(int accountId, int peerId);

        void disableMessageEncryption(int accountId, int peerId);

        boolean isMessageEncryptionEnabled(int accountId, int peerId);

        void enableMessageEncryption(int accountId, int peerId, @KeyLocationPolicy int policy);

        void firePinAttemptNow();

        void clearPinHistory();

        List<Long> getPinEnterHistory();

        boolean hasPinHash();

        int getPinHistoryDepth();

        boolean needHideMessagesBodyForNotif();

        boolean AddValueToSet(int value, String arrayName);

        boolean RemoveValueFromSet(int value, String arrayName);

        int getSetSize(String arrayName);

        Set<Integer> loadSet(String arrayName);

        boolean ContainsValuesInSet(int[] values, String arrayName);

        boolean ContainsValueInSet(int value, String arrayName);

        boolean getShowHiddenDialogs();

        void setShowHiddenDialogs(boolean showHiddenDialogs);

        boolean IsShow_hidden_accounts();
    }

    interface IUISettings {
        @StyleRes
        int getMainTheme();

        void setMainTheme(String key);

        void switchNightMode(@NightMode int key);

        String getMainThemeKey();

        @AvatarStyle
        int getAvatarStyle();

        void storeAvatarStyle(@AvatarStyle int style);

        boolean isDarkModeEnabled(Context context);

        int getNightMode();

        Place getDefaultPage(int accountId);

        void notifyPlaceResumed(int type);

        boolean isSystemEmoji();

        boolean isEmojis_full_screen();

        boolean isStickers_by_theme();

        boolean isStickers_by_new();

        int isPhoto_swipe_triggered_pos();

        boolean isShow_profile_in_additional_page();

        @SwipesChatMode
        int getSwipes_chat_mode();

        boolean isDisplay_writing();
    }
}
