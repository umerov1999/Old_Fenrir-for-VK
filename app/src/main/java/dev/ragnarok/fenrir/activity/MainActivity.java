package dev.ragnarok.fenrir.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.stream.JsonReader;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.CheckDonate;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.dialog.ResolveDomainDialog;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.impl.CountersInteractor;
import dev.ragnarok.fenrir.fragment.AbsWallFragment;
import dev.ragnarok.fenrir.fragment.AdditionalNavigationFragment;
import dev.ragnarok.fenrir.fragment.AnswerVKOfficialFragment;
import dev.ragnarok.fenrir.fragment.AudioCatalogFragment;
import dev.ragnarok.fenrir.fragment.AudioPlayerFragment;
import dev.ragnarok.fenrir.fragment.AudiosFragment;
import dev.ragnarok.fenrir.fragment.AudiosInCatalogFragment;
import dev.ragnarok.fenrir.fragment.AudiosTabsFragment;
import dev.ragnarok.fenrir.fragment.BrowserFragment;
import dev.ragnarok.fenrir.fragment.ChatFragment;
import dev.ragnarok.fenrir.fragment.ChatUsersFragment;
import dev.ragnarok.fenrir.fragment.CommentsFragment;
import dev.ragnarok.fenrir.fragment.CommunitiesFragment;
import dev.ragnarok.fenrir.fragment.CommunityBanEditFragment;
import dev.ragnarok.fenrir.fragment.CommunityControlFragment;
import dev.ragnarok.fenrir.fragment.CommunityInfoContactsFragment;
import dev.ragnarok.fenrir.fragment.CommunityInfoLinksFragment;
import dev.ragnarok.fenrir.fragment.CommunityManagerEditFragment;
import dev.ragnarok.fenrir.fragment.CreatePhotoAlbumFragment;
import dev.ragnarok.fenrir.fragment.CreatePollFragment;
import dev.ragnarok.fenrir.fragment.DialogsFragment;
import dev.ragnarok.fenrir.fragment.DocPreviewFragment;
import dev.ragnarok.fenrir.fragment.DocsFragment;
import dev.ragnarok.fenrir.fragment.DrawerEditFragment;
import dev.ragnarok.fenrir.fragment.FeedFragment;
import dev.ragnarok.fenrir.fragment.FeedbackFragment;
import dev.ragnarok.fenrir.fragment.FwdsFragment;
import dev.ragnarok.fenrir.fragment.GifPagerFragment;
import dev.ragnarok.fenrir.fragment.GiftsFragment;
import dev.ragnarok.fenrir.fragment.ImportantMessagesFragment;
import dev.ragnarok.fenrir.fragment.LikesFragment;
import dev.ragnarok.fenrir.fragment.LinksInCatalogFragment;
import dev.ragnarok.fenrir.fragment.LogsFragement;
import dev.ragnarok.fenrir.fragment.MarketViewFragment;
import dev.ragnarok.fenrir.fragment.MessagesLookFragment;
import dev.ragnarok.fenrir.fragment.NewsfeedCommentsFragment;
import dev.ragnarok.fenrir.fragment.NewsfeedMentionsFragment;
import dev.ragnarok.fenrir.fragment.NotificationPreferencesFragment;
import dev.ragnarok.fenrir.fragment.OwnerArticlesFragment;
import dev.ragnarok.fenrir.fragment.PhotoAllCommentFragment;
import dev.ragnarok.fenrir.fragment.PhotoPagerFragment;
import dev.ragnarok.fenrir.fragment.PlaylistsInCatalogFragment;
import dev.ragnarok.fenrir.fragment.PollFragment;
import dev.ragnarok.fenrir.fragment.PreferencesFragment;
import dev.ragnarok.fenrir.fragment.ProductAlbumsFragment;
import dev.ragnarok.fenrir.fragment.ProductsFragment;
import dev.ragnarok.fenrir.fragment.RequestExecuteFragment;
import dev.ragnarok.fenrir.fragment.SecurityPreferencesFragment;
import dev.ragnarok.fenrir.fragment.ShortedLinksFragment;
import dev.ragnarok.fenrir.fragment.SinglePhotoFragment;
import dev.ragnarok.fenrir.fragment.StoryPagerFragment;
import dev.ragnarok.fenrir.fragment.ThemeFragment;
import dev.ragnarok.fenrir.fragment.TopicsFragment;
import dev.ragnarok.fenrir.fragment.UserBannedFragment;
import dev.ragnarok.fenrir.fragment.UserDetailsFragment;
import dev.ragnarok.fenrir.fragment.VKPhotoAlbumsFragment;
import dev.ragnarok.fenrir.fragment.VKPhotosFragment;
import dev.ragnarok.fenrir.fragment.VideoPreviewFragment;
import dev.ragnarok.fenrir.fragment.VideosFragment;
import dev.ragnarok.fenrir.fragment.VideosInCatalogFragment;
import dev.ragnarok.fenrir.fragment.VideosTabsFragment;
import dev.ragnarok.fenrir.fragment.WallPostFragment;
import dev.ragnarok.fenrir.fragment.attachments.CommentCreateFragment;
import dev.ragnarok.fenrir.fragment.attachments.CommentEditFragment;
import dev.ragnarok.fenrir.fragment.attachments.PostCreateFragment;
import dev.ragnarok.fenrir.fragment.attachments.PostEditFragment;
import dev.ragnarok.fenrir.fragment.attachments.RepostFragment;
import dev.ragnarok.fenrir.fragment.conversation.ConversationFragmentFactory;
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment;
import dev.ragnarok.fenrir.fragment.friends.FriendsTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SearchTabsFragment;
import dev.ragnarok.fenrir.fragment.search.SingleTabSearchFragment;
import dev.ragnarok.fenrir.fragment.wallattachments.WallAttachmentsFragmentFactory;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.SectionCounters;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.drawer.AbsMenuItem;
import dev.ragnarok.fenrir.model.drawer.RecentChat;
import dev.ragnarok.fenrir.model.drawer.SectionMenuItem;
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosListView;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceProvider;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.push.IPushRegistrationResolver;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.SwipesChatMode;
import dev.ragnarok.fenrir.util.Accounts;
import dev.ragnarok.fenrir.util.Action;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.MainActivityTransforms;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.zoomhelper.ZoomHelper;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class MainActivity extends AppCompatActivity implements AdditionalNavigationFragment.NavigationDrawerCallbacks,
        OnSectionResumeCallback, AppStyleable, PlaceProvider, ServiceConnection, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String ACTION_CHAT_FROM_SHORTCUT = "dev.ragnarok.fenrir.ACTION_CHAT_FROM_SHORTCUT";
    public static final String ACTION_OPEN_PLACE = "dev.ragnarok.fenrir.activity.MainActivity.openPlace";
    public static final String ACTION_OPEN_AUDIO_PLAYER = "dev.ragnarok.fenrir.activity.MainActivity.openAudioPlayer";
    public static final String ACTION_SEND_ATTACHMENTS = "dev.ragnarok.fenrir.ACTION_SEND_ATTACHMENTS";
    public static final String ACTION_SWITH_ACCOUNT = "dev.ragnarok.fenrir.ACTION_SWITH_ACCOUNT";
    public static final String ACTION_OPEN_WALL = "dev.ragnarok.fenrir.ACTION_OPEN_WALL";

    public static final String EXTRA_NO_REQUIRE_PIN = "no_require_pin";

    /**
     * Extra with type {@link dev.ragnarok.fenrir.model.ModelsBundle} only
     */
    public static final String EXTRA_INPUT_ATTACHMENTS = "input_attachments";
    protected static final int DOUBLE_BACK_PRESSED_TIMEOUT = 2000;
    private static final String TAG = "MainActivity_LOG";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final List<Action<MainActivity>> postResumeActions = new ArrayList<>(0);
    private final ActivityResultLauncher<Intent> requestEnterPin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    finish();
                }
            });
    protected int mAccountId;
    private final ActivityResultLauncher<Intent> requestQRScan = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult scanner = IntentIntegrator.parseActivityResult(result);
                if (!Utils.isEmpty(scanner.getContents())) {
                    MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(this);
                    dlgAlert.setIcon(R.drawable.qr_code);
                    dlgAlert.setMessage(scanner.getContents());
                    dlgAlert.setTitle(getString(R.string.scan_qr));
                    dlgAlert.setPositiveButton(R.string.open, (dialog, which) -> LinkHelper.openUrl(this, mAccountId, scanner.getContents()));
                    dlgAlert.setNeutralButton(R.string.copy_text, (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("response", scanner.getContents());
                        clipboard.setPrimaryClip(clip);
                        CustomToast.CreateCustomToast(this).showToast(R.string.copied_to_clipboard);
                    });
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            });
    private final ActivityResultLauncher<Intent> requestLogin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                mAccountId = Settings.get()
                        .accounts()
                        .getCurrent();

                if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
                    supportFinishAfterTransition();
                }
            });
    protected int mLayoutRes = Settings.get().main().isSnow_mode() ? R.layout.activity_main_with_snow : R.layout.activity_main;
    protected long mLastBackPressedTime;
    /**
     * Атрибуты секции, которая на данный момент находится на главном контейнере экрана
     */
    private AbsMenuItem mCurrentFrontSection;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigation;
    private ViewGroup mBottomNavigationContainer;
    private FragmentContainerView mViewFragment;
    private final FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = () -> {
        resolveToolbarNavigationIcon();
        keyboardHide();
    };
    private MusicUtils.ServiceToken mAudioPlayServiceToken;
    private boolean mDestroyed;
    /**
     * First - DrawerItem, second - Clear back stack before adding
     */
    private Pair<AbsMenuItem, Boolean> mTargetPage;
    private boolean resumed;
    private boolean isZoomPhoto;

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    protected @MainActivityTransforms
    int getMainActivityTransform() {
        return MainActivityTransforms.MAIN;
    }

    private void postResume(Action<MainActivity> action) {
        if (resumed) {
            action.call(this);
        } else {
            postResumeActions.add(action);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isZoomPhoto) {
            return super.dispatchTouchEvent(ev);
        }
        return ZoomHelper.Companion.getInstance().dispatchTouchEvent(ev, this) || super.dispatchTouchEvent(ev);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Settings.get().ui().getMainTheme());
        getDelegate().applyDayNight();
        Utils.prepareDensity(this);

        super.onCreate(savedInstanceState);
        mDestroyed = false;
        isZoomPhoto = Settings.get().other().isDo_zoom_photo();

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onCurrentAccountChange));

        mCompositeDisposable.add(Stores.getInstance()
                .dialogs()
                .observeUnreadDialogsCount()
                .filter(pair -> pair.getFirst() == mAccountId)
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(pair -> updateMessagesBagde(pair.getSecond())));

        bindToAudioPlayService();

        setContentView(mLayoutRes);

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        setStatusbarColored(true, Settings.get().ui().isDarkModeEnabled(this));

        mBottomNavigation = findViewById(R.id.bottom_navigation_menu);
        mBottomNavigation.setOnNavigationItemSelectedListener(this);

        mBottomNavigationContainer = findViewById(R.id.bottom_navigation_menu_container);
        mViewFragment = findViewById(R.id.fragment);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        resolveToolbarNavigationIcon();

        updateMessagesBagde(Stores.getInstance()
                .dialogs()
                .getUnreadDialogsCount(mAccountId));

        if (isNull(savedInstanceState)) {
            boolean intentWasHandled = handleIntent(getIntent());

            if (!intentWasHandled) {
                Place place = Settings.get().ui().getDefaultPage(mAccountId);
                place.tryOpenWith(this);
            }
            checkFCMRegistration();

            CheckDonate.UpdateDonateList(this);

            if (!isAuthValid()) {
                startAccountsActivity();
            } else {
                if (getMainActivityTransform() == MainActivityTransforms.MAIN) {

                    mCompositeDisposable.add(InteractorFactory.createAudioInteractor().PlaceToAudioCache(this)
                            .compose(RxUtils.applyCompletableIOToMainSchedulers())
                            .subscribe(RxUtils.dummy(), t -> {/*TODO*/}));

                    mCompositeDisposable.add(InteractorFactory.createStickersInteractor().PlaceToStickerCache(this)
                            .compose(RxUtils.applyCompletableIOToMainSchedulers())
                            .subscribe(RxUtils.dummy(), t -> {/*TODO*/}));

                    CheckMusicInPC();

                    if (Settings.get().other().isDelete_cache_images()) {
                        PreferencesFragment.CleanImageCache(this, false);
                    }
                }

                UpdateNotificationCount(mAccountId);
                boolean needPin = Settings.get().security().isUsePinForEntrance()
                        && !getIntent().getBooleanExtra(EXTRA_NO_REQUIRE_PIN, false);
                if (needPin) {
                    startEnterPinActivity();
                }
            }
        }
    }

    public void UpdateNotificationCount(int account) {
        mCompositeDisposable.add(new CountersInteractor(Injection.provideNetworkInterfaces()).getApiCounters(account)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::updateNotificationsBagde, t -> removeNotificationsBagde()));
    }

    private void CheckMusicInPC() {
        if (!AppPerms.hasReadWriteStoragePermision(this))
            return;
        File audios = new File(Settings.get().other().getMusicDir(), "audio_downloads.json");
        if (!audios.exists())
            return;
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(audios), StandardCharsets.UTF_8));
            reader.beginArray();
            while (reader.hasNext()) {
                MusicUtils.RemoteAudios.add(reader.nextString());
            }
        } catch (Throwable ignore) {
            CustomToast.CreateCustomToast(this).showToastError(R.string.remote_audio_error);
        }
    }

    @Override
    protected void onPause() {
        resumed = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        for (Action<MainActivity> action : postResumeActions) {
            action.call(this);
        }
        postResumeActions.clear();
    }

    private void startEnterPinActivity() {
        Intent intent = new Intent(this, EnterPinActivity.getClass(this));
        requestEnterPin.launch(intent);
    }

    private void checkFCMRegistration() {
        if (!checkPlayServices(this)) {
            if (!Settings.get().other().isDisabledErrorFCM()) {
                Utils.ThemedSnack(mViewFragment, getString(R.string.this_device_does_not_support_fcm), BaseTransientBottomBar.LENGTH_LONG)
                        .setAnchorView(mBottomNavigationContainer).setAction(R.string.button_access, v -> Settings.get().other().setDisableErrorFCM(true)).show();
            }
            return;
        }

        IPushRegistrationResolver resolver = Injection.providePushRegistrationResolver();

        mCompositeDisposable.add(resolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore()));

        //RequestHelper.checkPushRegistration(this);
    }

    private void bindToAudioPlayService() {
        if (!isActivityDestroyed() && mAudioPlayServiceToken == null) {
            mAudioPlayServiceToken = MusicUtils.bindToServiceWithoutStart(this, this);
        }
    }

    private void resolveToolbarNavigationIcon() {
        if (isNull(mToolbar)) return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            Drawable tr = AppCompatResources.getDrawable(this, R.drawable.arrow_left);
            Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(this));
            mToolbar.setNavigationIcon(tr);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            if (!isFragmentWithoutNavigation()) {
                Drawable tr = AppCompatResources.getDrawable(this, R.drawable.client_round);
                Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(this));
                mToolbar.setNavigationIcon(tr);
                mToolbar.setNavigationOnClickListener(v -> {

                    ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
                    menus.add(new OptionRequest(R.id.button_ok, getString(R.string.set_offline), R.drawable.offline));
                    menus.add(new OptionRequest(R.id.button_cancel, getString(R.string.open_clipboard_url), R.drawable.web));
                    menus.add(new OptionRequest(R.id.button_camera, getString(R.string.scan_qr), R.drawable.qr_code));
                    menus.show(getSupportFragmentManager(), "left_options", option -> {
                        switch (option.getId()) {
                            case R.id.button_ok:
                                mCompositeDisposable.add(InteractorFactory.createAccountInteractor().setOffline(Settings.get().accounts().getCurrent())
                                        .compose(RxUtils.applySingleIOToMainSchedulers())
                                        .subscribe(this::OnSetOffline, t -> OnSetOffline(false)));
                                break;
                            case R.id.button_cancel:
                                ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                if (clipBoard != null && clipBoard.getPrimaryClip() != null && clipBoard.getPrimaryClip().getItemCount() > 0 && clipBoard.getPrimaryClip().getItemAt(0).getText() != null) {
                                    String temp = clipBoard.getPrimaryClip().getItemAt(0).getText().toString();
                                    LinkHelper.openUrl(this, mAccountId, temp);
                                }
                                break;
                            case R.id.button_camera:
                                IntentIntegrator integrator = new IntentIntegrator(this);
                                integrator.setCameraId(0);
                                integrator.setBeepEnabled(true);
                                integrator.setBarcodeImageEnabled(false);
                                requestQRScan.launch(integrator.createScanIntent());
                                break;
                        }
                    });
                });
            } else {
                Drawable tr = AppCompatResources.getDrawable(this, R.drawable.arrow_left);
                Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(this));
                mToolbar.setNavigationIcon(tr);
                if (getMainActivityTransform() != MainActivityTransforms.SWIPEBLE) {
                    mToolbar.setNavigationOnClickListener(v -> openNavigationPage(AdditionalNavigationFragment.SECTION_ITEM_FEED));
                } else {
                    mToolbar.setNavigationOnClickListener(v -> onBackPressed());
                }
            }
        }
    }

    private void OnSetOffline(boolean succ) {
        if (succ)
            CustomToast.CreateCustomToast(this).showToast(R.string.succ_offline);
        else
            CustomToast.CreateCustomToast(this).showToastError(R.string.err_offline);
    }

    private void onCurrentAccountChange(int newAccountId) {
        mAccountId = newAccountId;
        Accounts.showAccountSwitchedToast(this);
        UpdateNotificationCount(newAccountId);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(TAG, "onNewIntent, intent: " + intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        if (ACTION_OPEN_WALL.equals(intent.getAction())) {
            int owner_id = intent.getExtras().getInt(Extra.OWNER_ID);
            PlaceFactory.getOwnerWallPlace(mAccountId, owner_id, null).tryOpenWith(this);
            return true;
        }

        if (ACTION_SWITH_ACCOUNT.equals(intent.getAction())) {
            int newAccountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            Settings.get()
                    .accounts()
                    .setCurrent(newAccountId);

            mAccountId = newAccountId;
            intent.setAction(ACTION_MAIN);
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        Logger.d(TAG, "handleIntent, extras: " + extras + ", action: " + action);

        if (extras != null && ActivityUtils.checkInputExist(this)) {
            mCurrentFrontSection = AdditionalNavigationFragment.SECTION_ITEM_DIALOGS;
            openNavigationPage(mCurrentFrontSection);
            return true;
        }

        if (ACTION_SEND_ATTACHMENTS.equals(action)) {
            mCurrentFrontSection = AdditionalNavigationFragment.SECTION_ITEM_DIALOGS;
            openNavigationPage(mCurrentFrontSection);
            return true;
        }

        if (ACTION_OPEN_PLACE.equals(action)) {
            Place place = intent.getParcelableExtra(Extra.PLACE);
            openPlace(place);
            if (place.type == Place.CHAT) {
                return Settings.get().ui().getSwipes_chat_mode() != SwipesChatMode.SLIDR || Settings.get().ui().getSwipes_chat_mode() == SwipesChatMode.DISABLED;
            }
            return true;
        }

        if (ACTION_OPEN_AUDIO_PLAYER.equals(action)) {
            openPlace(PlaceFactory.getPlayerPlace(mAccountId));
            return false;
        }

        if (ACTION_CHAT_FROM_SHORTCUT.equals(action)) {
            int aid = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int prefsAid = Settings.get()
                    .accounts()
                    .getCurrent();

            if (prefsAid != aid) {
                Settings.get()
                        .accounts()
                        .setCurrent(aid);
            }

            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            String title = intent.getStringExtra(Extra.TITLE);
            String imgUrl = intent.getStringExtra(Extra.IMAGE);

            Peer peer = new Peer(peerId).setTitle(title).setAvaUrl(imgUrl);
            PlaceFactory.getChatPlace(aid, aid, peer).tryOpenWith(this);
            return Settings.get().ui().getSwipes_chat_mode() != SwipesChatMode.SLIDR || Settings.get().ui().getSwipes_chat_mode() == SwipesChatMode.DISABLED;
        }

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            LinkHelper.openUrl(this, mAccountId, String.valueOf(data));
            return true;
        }

        return false;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (nonNull(mToolbar)) {
            mToolbar.setNavigationOnClickListener(null);
            mToolbar.setOnMenuItemClickListener(null);
        }

        super.setSupportActionBar(toolbar);

        mToolbar = toolbar;
        resolveToolbarNavigationIcon();
    }

    private void openChat(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        if (Settings.get().other().isEnable_show_recent_dialogs()) {
            RecentChat recentChat = new RecentChat(accountId, peer.getId(), peer.getTitle(), peer.getAvaUrl());
            getNavigationFragment().appendRecentChat(recentChat);
            getNavigationFragment().refreshNavigationItems();
            getNavigationFragment().selectPage(recentChat);
        }
        if (Settings.get().ui().getSwipes_chat_mode() == SwipesChatMode.DISABLED) {
            ChatFragment chatFragment = ChatFragment.Companion.newInstance(accountId, messagesOwnerId, peer);
            attachToFront(chatFragment);
        } else {
            if (Settings.get().ui().getSwipes_chat_mode() == SwipesChatMode.SLIDR && getMainActivityTransform() == MainActivityTransforms.MAIN) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.setAction(ChatActivity.ACTION_OPEN_PLACE);
                intent.putExtra(Extra.PLACE, PlaceFactory.getChatPlace(accountId, messagesOwnerId, peer));
                startActivity(intent);
            } else if (Settings.get().ui().getSwipes_chat_mode() == SwipesChatMode.SLIDR && getMainActivityTransform() != MainActivityTransforms.MAIN) {
                ChatFragment chatFragment = ChatFragment.Companion.newInstance(accountId, messagesOwnerId, peer);
                attachToFront(chatFragment);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void openRecentChat(RecentChat chat) {
        int accountId = mAccountId;
        int messagesOwnerId = mAccountId;
        openChat(accountId, messagesOwnerId, new Peer(chat.getPeerId()).setAvaUrl(chat.getIconUrl()).setTitle(chat.getTitle()));
    }

    private void openTargetPage() {
        if (mTargetPage == null) {
            return;
        }

        AbsMenuItem item = mTargetPage.getFirst();
        boolean clearBackStack = mTargetPage.getSecond();

        if (item.equals(mCurrentFrontSection)) {
            return;
        }

        if (item.getType() == AbsMenuItem.TYPE_ICON) {
            openNavigationPage(item, clearBackStack);
        }

        if (item.getType() == AbsMenuItem.TYPE_RECENT_CHAT) {
            openRecentChat((RecentChat) item);
        }

        mTargetPage = null;
    }

    private AdditionalNavigationFragment getNavigationFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (AdditionalNavigationFragment) fm.findFragmentById(R.id.additional_navigation_menu);
    }

    private void openNavigationPage(@NonNull AbsMenuItem item) {
        openNavigationPage(item, true);
    }

    private void startAccountsActivity() {
        Intent intent = new Intent(this, AccountsActivity.class);
        requestLogin.launch(intent);
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        /*if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }*/

        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // TODO: 13.12.2017 Exception java.lang.IllegalStateException:Can not perform this action after onSaveInstanceState
        Logger.d(TAG, "Back stack was cleared");
    }

    private void openNavigationPage(@NonNull AbsMenuItem item, boolean clearBackStack) {
        if (item.getType() == AbsMenuItem.TYPE_RECENT_CHAT) {
            openRecentChat((RecentChat) item);
            return;
        }

        SectionMenuItem sectionDrawerItem = (SectionMenuItem) item;
        if (sectionDrawerItem.getSection() == AdditionalNavigationFragment.PAGE_ACCOUNTS) {
            startAccountsActivity();
            return;
        }

        mCurrentFrontSection = item;
        getNavigationFragment().selectPage(item);

        if (clearBackStack) {
            clearBackStack();
        }

        int aid = mAccountId;

//        PlaceFactory.getDialogsPlace(aid, aid, null)

        switch (sectionDrawerItem.getSection()) {
            case AdditionalNavigationFragment.PAGE_DIALOGS:
                openPlace(PlaceFactory.getDialogsPlace(aid, aid, null));
                break;
            case AdditionalNavigationFragment.PAGE_FRIENDS:
                openPlace(PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null));
                break;
            case AdditionalNavigationFragment.PAGE_GROUPS:
                openPlace(PlaceFactory.getCommunitiesPlace(aid, aid));
                break;
            case AdditionalNavigationFragment.PAGE_PREFERENSES:
                openPlace(PlaceFactory.getPreferencesPlace(aid));
                break;
            case AdditionalNavigationFragment.PAGE_MUSIC:
                openPlace(PlaceFactory.getAudiosPlace(aid, aid));
                break;
            case AdditionalNavigationFragment.PAGE_DOCUMENTS:
                openPlace(PlaceFactory.getDocumentsPlace(aid, aid, DocsListPresenter.ACTION_SHOW));
                break;
            case AdditionalNavigationFragment.PAGE_FEED:
                openPlace(PlaceFactory.getFeedPlace(aid));
                break;
            case AdditionalNavigationFragment.PAGE_NOTIFICATION:
                openPlace(PlaceFactory.getNotificationsPlace(aid));
                break;
            case AdditionalNavigationFragment.PAGE_PHOTOS:
                openPlace(PlaceFactory.getVKPhotoAlbumsPlace(aid, aid, IVkPhotosView.ACTION_SHOW_PHOTOS, null));
                break;
            case AdditionalNavigationFragment.PAGE_VIDEOS:
                openPlace(PlaceFactory.getVideosPlace(aid, aid, IVideosListView.ACTION_SHOW));
                break;
            case AdditionalNavigationFragment.PAGE_BOOKMARKS:
                openPlace(PlaceFactory.getBookmarksPlace(aid, FaveTabsFragment.TAB_PAGES));
                break;
            case AdditionalNavigationFragment.PAGE_SEARCH:
                openPlace(PlaceFactory.getSearchPlace(aid, SearchTabsFragment.TAB_PEOPLE));
                break;
            case AdditionalNavigationFragment.PAGE_NEWSFEED_COMMENTS:
                openPlace(PlaceFactory.getNewsfeedCommentsPlace(aid));
                break;
            default:
                throw new IllegalArgumentException("Unknown place!!! " + item);
        }
    }

    @Override
    public void onSheetItemSelected(AbsMenuItem item, boolean longClick) {
        if (mCurrentFrontSection != null && mCurrentFrontSection.equals(item)) {
            return;
        }

        mTargetPage = Pair.Companion.create(item, !longClick);
        //после закрытия бокового меню откроется данная страница
    }

    @Override
    public void onSheetClosed() {
        postResume(MainActivity::openTargetPage);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        mDestroyed = true;

        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);

        //if(!bNoDestroyServiceAudio)
        unbindFromAudioPlayService();
        super.onDestroy();
    }

    private void unbindFromAudioPlayService() {
        if (mAudioPlayServiceToken != null) {
            MusicUtils.unbindFromService(mAudioPlayServiceToken);
            mAudioPlayServiceToken = null;
        }
    }

    private boolean isAuthValid() {
        return mAccountId != ISettings.IAccountsSettings.INVALID_ID;
    }

    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        SwipeTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
     */

    public void keyboardHide() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception ignored) {

        }
    }

    private Fragment getFrontFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onBackPressed() {
        if (getNavigationFragment().isSheetOpen()) {
            getNavigationFragment().closeSheet();
            return;
        }

        Fragment front = getFrontFragment();
        if (front instanceof BackPressCallback) {
            if (!(((BackPressCallback) front).onBackPressed())) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (getMainActivityTransform() != MainActivityTransforms.SWIPEBLE) {
                if (isFragmentWithoutNavigation()) {
                    openNavigationPage(AdditionalNavigationFragment.SECTION_ITEM_FEED);
                    return;
                }
                if (isChatFragment()) {
                    openNavigationPage(AdditionalNavigationFragment.SECTION_ITEM_DIALOGS);
                    return;
                }
            }
            if (mLastBackPressedTime < 0
                    || mLastBackPressedTime + DOUBLE_BACK_PRESSED_TIMEOUT > System.currentTimeMillis()) {
                supportFinishAfterTransition();
                return;
            }

            mLastBackPressedTime = System.currentTimeMillis();
            Snackbar.make(mViewFragment, getString(R.string.click_back_to_exit), BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(mBottomNavigationContainer).show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isChatFragment() {
        return getFrontFragment() instanceof ChatFragment;
    }

    private boolean isFragmentWithoutNavigation() {
        return getFrontFragment() instanceof CommentsFragment ||
                getFrontFragment() instanceof PostCreateFragment ||
                getFrontFragment() instanceof GifPagerFragment;
    }

    @Override
    public boolean onNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    /* Убрать выделение в боковом меню */
    private void resetNavigationSelection() {
        mCurrentFrontSection = null;
        getNavigationFragment().selectPage(null);
    }

    @Override
    public void onSectionResume(SectionMenuItem sectionDrawerItem) {
        getNavigationFragment().selectPage(sectionDrawerItem);

        switch (sectionDrawerItem.getSection()) {
            case AdditionalNavigationFragment.PAGE_FEED:
                mBottomNavigation.getMenu().getItem(0).setChecked(true);
                break;
            case AdditionalNavigationFragment.PAGE_SEARCH:
                mBottomNavigation.getMenu().getItem(1).setChecked(true);
                break;
            case AdditionalNavigationFragment.PAGE_DIALOGS:
                mBottomNavigation.getMenu().getItem(2).setChecked(true);
                break;
            case AdditionalNavigationFragment.PAGE_NOTIFICATION:
                mBottomNavigation.getMenu().getItem(3).setChecked(true);
                break;
            default:
                mBottomNavigation.getMenu().getItem(4).setChecked(true);
                break;
        }

        mCurrentFrontSection = sectionDrawerItem;
    }

    @Override
    public void onChatResume(int accountId, int peerId, String title, String imgUrl) {
        if (Settings.get().other().isEnable_show_recent_dialogs()) {
            RecentChat recentChat = new RecentChat(accountId, peerId, title, imgUrl);
            getNavigationFragment().appendRecentChat(recentChat);
            getNavigationFragment().refreshNavigationItems();
            getNavigationFragment().selectPage(recentChat);
            mCurrentFrontSection = recentChat;
        }
    }

    @Override
    public void onClearSelection() {
        resetNavigationSelection();
        mCurrentFrontSection = null;
    }

    private void attachToFront(Fragment fragment) {
        attachToFront(fragment, true);
    }

    private void attachToFront(Fragment fragment, boolean animate) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (animate)
            fragmentTransaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);

        fragmentTransaction
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void setStatusbarColored(boolean colored, boolean invertIcons) {
        int statusbarNonColored = CurrentTheme.getStatusBarNonColored(this);
        int statusbarColored = CurrentTheme.getStatusBarColor(this);

        if (Utils.hasLollipop()) {
            Window w = getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(colored ? statusbarColored : statusbarNonColored);
            int navigationColor = colored ? CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
            w.setNavigationBarColor(navigationColor);
        }

        if (Utils.hasMarshmallow()) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        if (Utils.hasOreo()) {
            Window w = getWindow();
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                w.setNavigationBarColor(Color.WHITE);
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                @ColorInt
                int navigationColor = colored ?
                        CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
                w.setNavigationBarColor(navigationColor);
            }
        }
    }

    @Override
    public void hideMenu(boolean hide) {
        if (hide) {
            getNavigationFragment().closeSheet();
            getNavigationFragment().blockSheet();
            mBottomNavigationContainer.setVisibility(View.GONE);
        } else {
            mBottomNavigationContainer.setVisibility(View.VISIBLE);
            getNavigationFragment().unblockSheet();
        }
    }

    @Override
    public void openMenu(boolean open) {
//        if (open) {
//            getNavigationFragment().openSheet();
//        } else {
//            getNavigationFragment().closeSheet();
//        }
    }

    @Override
    public void openPlace(Place place) {
        Bundle args = place.getArgs();
        switch (place.type) {
            case Place.VIDEO_PREVIEW:
                attachToFront(VideoPreviewFragment.newInstance(args));
                break;

            case Place.STORY_PLAYER:
                attachToFront(StoryPagerFragment.newInstance(args));
                break;

            case Place.FRIENDS_AND_FOLLOWERS:
                attachToFront(FriendsTabsFragment.newInstance(args));
                break;

            case Place.WIKI_PAGE:
            case Place.EXTERNAL_LINK:
                attachToFront(BrowserFragment.newInstance(args));
                break;

            case Place.DOC_PREVIEW:
                Document document = args.getParcelable(Extra.DOC);
                if (document != null && document.hasValidGifVideoLink()) {
                    int aid = args.getInt(Extra.ACCOUNT_ID);
                    ArrayList<Document> documents = new ArrayList<>(Collections.singletonList(document));

                    Bundle argsForGifs = GifPagerFragment.buildArgs(aid, documents, 0);
                    attachToFront(GifPagerFragment.newInstance(argsForGifs));
                } else {
                    attachToFront(DocPreviewFragment.newInstance(args));
                }
                break;

            case Place.WALL_POST:
                attachToFront(WallPostFragment.newInstance(args));
                break;

            case Place.COMMENTS:
                attachToFront(CommentsFragment.newInstance(place));
                break;

            case Place.WALL:
                attachToFront(AbsWallFragment.newInstance(args));
                break;

            case Place.CONVERSATION_ATTACHMENTS:
                attachToFront(ConversationFragmentFactory.newInstance(args));
                break;

            case Place.PLAYER:
                Fragment player = getSupportFragmentManager().findFragmentByTag("audio_player");
                if (player instanceof AudioPlayerFragment)
                    ((AudioPlayerFragment) player).dismiss();
                AudioPlayerFragment.newInstance(args).show(getSupportFragmentManager(), "audio_player");
                break;

            case Place.CHAT:
                Peer peer = args.getParcelable(Extra.PEER);
                AssertUtils.requireNonNull(peer);
                openChat(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID), peer);
                break;

            case Place.SEARCH:
                attachToFront(SearchTabsFragment.newInstance(args));
                break;

            case Place.BUILD_NEW_POST:
                PostCreateFragment postCreateFragment = PostCreateFragment.newInstance(args);
                attachToFront(postCreateFragment);
                break;

            case Place.EDIT_COMMENT: {
                Comment comment = args.getParcelable(Extra.COMMENT);
                int accountId = args.getInt(Extra.ACCOUNT_ID);
                Integer commemtId = args.getInt(Extra.COMMENT_ID);
                CommentEditFragment commentEditFragment = CommentEditFragment.newInstance(accountId, comment, commemtId);
                place.applyFragmentListener(commentEditFragment, getSupportFragmentManager());
                attachToFront(commentEditFragment);
                break;
            }

            case Place.EDIT_POST:
                PostEditFragment postEditFragment = PostEditFragment.newInstance(args);
                attachToFront(postEditFragment);
                break;

            case Place.REPOST:
                RepostFragment repostFragment = RepostFragment.newInstance(args);
                attachToFront(repostFragment);
                break;

            case Place.DIALOGS:
                attachToFront(DialogsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getString(Extra.SUBTITLE)
                ));
                break;

            case Place.FORWARD_MESSAGES:
                attachToFront(FwdsFragment.newInstance(args));
                break;

            case Place.TOPICS:
                attachToFront(TopicsFragment.newInstance(args));
                break;

            case Place.CHAT_MEMBERS:
                attachToFront(ChatUsersFragment.newInstance(args));
                break;

            case Place.COMMUNITIES:
                CommunitiesFragment communitiesFragment = CommunitiesFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.USER_ID)
                );

                attachToFront(communitiesFragment);
                break;

            case Place.AUDIOS:
                attachToFront(AudiosTabsFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID)));
                break;

            case Place.MENTIONS:
                attachToFront(NewsfeedMentionsFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID)));
                break;

            case Place.AUDIOS_IN_ALBUM:
                attachToFront(AudiosFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID), args.getInt(Extra.ID), 1, args.getString(Extra.ACCESS_KEY)));
                break;

            case Place.SEARCH_BY_AUDIO:
                attachToFront(AudiosFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID), args.getInt(Extra.ID), 2, null));
                break;

            case Place.VIDEO_ALBUM:
                attachToFront(VideosFragment.newInstance(args));
                break;

            case Place.VIDEOS:
                attachToFront(VideosTabsFragment.newInstance(args));
                break;

            case Place.VK_PHOTO_ALBUMS:
                attachToFront(VKPhotoAlbumsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getString(Extra.ACTION),
                        args.getParcelable(Extra.OWNER), false
                ));
                break;

            case Place.VK_PHOTO_ALBUM:
                attachToFront(VKPhotosFragment.newInstance(args));
                break;

            case Place.VK_PHOTO_ALBUM_GALLERY:
            case Place.FAVE_PHOTOS_GALLERY:
            case Place.SIMPLE_PHOTO_GALLERY:
            case Place.VK_PHOTO_TMP_SOURCE:
                attachToFront(PhotoPagerFragment.newInstance(place.type, args));
                break;

            case Place.POLL:
                attachToFront(PollFragment.newInstance(args));
                break;

            case Place.BOOKMARKS:
                attachToFront(FaveTabsFragment.newInstance(args));
                break;

            case Place.DOCS:
                attachToFront(DocsFragment.newInstance(args));
                break;

            case Place.FEED:
                attachToFront(FeedFragment.newInstance(args));
                break;

            case Place.NOTIFICATIONS:
                if (Settings.get().accounts().getType(mAccountId) == Account_Types.VK_ANDROID || Settings.get().accounts().getType(mAccountId) == Account_Types.VK_ANDROID_HIDDEN) {
                    attachToFront(AnswerVKOfficialFragment.newInstance(Settings.get().accounts().getCurrent()));
                    break;
                }
                attachToFront(FeedbackFragment.newInstance(args));
                break;

            case Place.PREFERENCES:
                attachToFront(PreferencesFragment.newInstance(args));
                break;

            case Place.RESOLVE_DOMAIN:
                ResolveDomainDialog domainDialog = ResolveDomainDialog.newInstance(args);
                domainDialog.show(getSupportFragmentManager(), "resolve-domain");
                break;

            case Place.VK_INTERNAL_PLAYER:
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtras(args);
                startActivity(intent);
                break;

            case Place.NOTIFICATION_SETTINGS:
                attachToFront(new NotificationPreferencesFragment());
                break;

            case Place.LIKES_AND_COPIES:
                attachToFront(LikesFragment.newInstance(args));
                break;

            case Place.CREATE_PHOTO_ALBUM:
            case Place.EDIT_PHOTO_ALBUM:
                CreatePhotoAlbumFragment createPhotoAlbumFragment = CreatePhotoAlbumFragment.newInstance(args);
                attachToFront(createPhotoAlbumFragment);
                break;

            case Place.MESSAGE_LOOKUP:
                attachToFront(MessagesLookFragment.newInstance(args));
                break;

            case Place.GIF_PAGER:
                attachToFront(GifPagerFragment.newInstance(args));
                break;

            case Place.SECURITY:
                attachToFront(new SecurityPreferencesFragment());
                break;

            case Place.CREATE_POLL:
                CreatePollFragment createPollFragment = CreatePollFragment.newInstance(args);
                place.applyFragmentListener(createPollFragment, getSupportFragmentManager());
                attachToFront(createPollFragment);
                break;

            case Place.COMMENT_CREATE:
                openCommentCreatePlace(place);
                break;

            case Place.LOGS:
                attachToFront(LogsFragement.newInstance());
                break;

            case Place.SINGLE_SEARCH:
                SingleTabSearchFragment singleTabSearchFragment = SingleTabSearchFragment.newInstance(args);
                attachToFront(singleTabSearchFragment);
                break;

            case Place.NEWSFEED_COMMENTS:
                NewsfeedCommentsFragment newsfeedCommentsFragment = NewsfeedCommentsFragment.newInstance(args.getInt(Extra.ACCOUNT_ID));
                attachToFront(newsfeedCommentsFragment);
                break;

            case Place.COMMUNITY_CONTROL:
                CommunityControlFragment communityControlFragment = CommunityControlFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getParcelable(Extra.OWNER),
                        args.getParcelable(Extra.SETTINGS)
                );
                attachToFront(communityControlFragment);
                break;

            case Place.COMMUNITY_INFO:
                CommunityInfoContactsFragment communityInfoFragment = CommunityInfoContactsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getParcelable(Extra.OWNER)
                );
                attachToFront(communityInfoFragment);
                break;

            case Place.COMMUNITY_INFO_LINKS:
                CommunityInfoLinksFragment communityLinksFragment = CommunityInfoLinksFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getParcelable(Extra.OWNER)
                );
                attachToFront(communityLinksFragment);
                break;

            case Place.SETTINGS_THEME:
                ThemeFragment themes = ThemeFragment.newInstance();
                attachToFront(themes);
                if (getNavigationFragment().isSheetOpen()) {
                    getNavigationFragment().closeSheet();
                    return;
                }
                break;

            case Place.COMMUNITY_BAN_EDIT:
                CommunityBanEditFragment communityBanEditFragment = CommunityBanEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        (Banned) args.getParcelable(Extra.BANNED)
                );
                attachToFront(communityBanEditFragment);
                break;

            case Place.COMMUNITY_ADD_BAN:
                attachToFront(CommunityBanEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        args.getParcelableArrayList(Extra.USERS)
                ));
                break;

            case Place.COMMUNITY_MANAGER_ADD:
                attachToFront(CommunityManagerEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        args.getParcelableArrayList(Extra.USERS)
                ));
                break;

            case Place.COMMUNITY_MANAGER_EDIT:
                attachToFront(CommunityManagerEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        (Manager) args.getParcelable(Extra.MANAGER)
                ));
                break;

            case Place.REQUEST_EXECUTOR:
                attachToFront(RequestExecuteFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.USER_BLACKLIST:
                attachToFront(UserBannedFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.DRAWER_EDIT:
                attachToFront(DrawerEditFragment.newInstance());
                break;

            case Place.SINGLE_PHOTO:
                attachToFront(SinglePhotoFragment.newInstance(args));
                break;

            case Place.ARTIST:
                attachToFront(AudioCatalogFragment.newInstance(args));
                break;

            case Place.CATALOG_BLOCK_AUDIOS:
                attachToFront(AudiosInCatalogFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getString(Extra.ID), args.getString(Extra.TITLE)));
                break;

            case Place.CATALOG_BLOCK_PLAYLISTS:
                attachToFront(PlaylistsInCatalogFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getString(Extra.ID), args.getString(Extra.TITLE)));
                break;

            case Place.CATALOG_BLOCK_VIDEOS:
                attachToFront(VideosInCatalogFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getString(Extra.ID), args.getString(Extra.TITLE)));
                break;

            case Place.CATALOG_BLOCK_LINKS:
                attachToFront(LinksInCatalogFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getString(Extra.ID), args.getString(Extra.TITLE)));
                break;

            case Place.SHORT_LINKS:
                attachToFront(ShortedLinksFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.IMPORTANT_MESSAGES:
                attachToFront(ImportantMessagesFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.OWNER_ARTICLES:
                attachToFront(OwnerArticlesFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID)));
                break;

            case Place.USER_DETAILS:
                int accountId = args.getInt(Extra.ACCOUNT_ID);
                User user = args.getParcelable(Extra.USER);
                UserDetails details = args.getParcelable("details");
                attachToFront(UserDetailsFragment.newInstance(accountId, user, details));
                break;

            case Place.WALL_ATTACHMENTS:
                Fragment wall_attachments = WallAttachmentsFragmentFactory.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID), args.getString(Extra.TYPE));
                if (wall_attachments == null) {
                    throw new IllegalArgumentException("wall_attachments cant bee null");
                }
                attachToFront(wall_attachments);
                break;

            case Place.MARKET_ALBUMS:
                attachToFront(ProductAlbumsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID)
                ));
                break;
            case Place.MARKETS:
                attachToFront(ProductsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getInt(Extra.ALBUM_ID)
                ));
                break;

            case Place.PHOTO_ALL_COMMENT:
                attachToFront(PhotoAllCommentFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID)
                ));
                break;

            case Place.GIFTS:
                attachToFront(GiftsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID)
                ));
                break;

            case Place.MARKET_VIEW:
                attachToFront(MarketViewFragment.newInstance(args));
                break;
            default:
                throw new IllegalArgumentException("Main activity can't open this place, type: " + place.type);
        }
    }

    private void openCommentCreatePlace(Place place) {
        CommentCreateFragment fragment = CommentCreateFragment.newInstance(
                place.getArgs().getInt(Extra.ACCOUNT_ID),
                place.getArgs().getInt(Extra.COMMENT_ID),
                place.getArgs().getInt(Extra.OWNER_ID),
                place.getArgs().getString(Extra.BODY)
        );

        place.applyFragmentListener(fragment, getSupportFragmentManager());
        attachToFront(fragment);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (name.getClassName().equals(MusicPlaybackService.class.getName())) {
            Logger.d(TAG, "Connected to MusicPlaybackService");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (isActivityDestroyed()) return;

        if (name.getClassName().equals(MusicPlaybackService.class.getName())) {
            Logger.d(TAG, "Disconnected from MusicPlaybackService");
            mAudioPlayServiceToken = null;
            bindToAudioPlayService();
        }
    }

    private boolean isActivityDestroyed() {
        return mDestroyed;
    }

    private void openPageAndCloseSheet(AbsMenuItem item) {
        if (getNavigationFragment().isSheetOpen()) {
            getNavigationFragment().closeSheet();
            onSheetItemSelected(item, false);
        } else {
            openNavigationPage(item);
        }
    }

    private void updateMessagesBagde(Integer count) {
        if (mBottomNavigation != null) {
            if (count > 0) {
                BadgeDrawable badgeDrawable = mBottomNavigation.getOrCreateBadge(R.id.menu_messages);
                badgeDrawable.setBackgroundColor(CurrentTheme.getColorPrimary(this));
                badgeDrawable.setBadgeTextColor(CurrentTheme.getColorOnPrimary(this));
                badgeDrawable.setNumber(count);
            } else {
                mBottomNavigation.removeBadge(R.id.menu_messages);
            }
        }
    }

    private void updateNotificationsBagde(SectionCounters counters) {
        if (mBottomNavigation != null) {
            if (counters.getNotifications() > 0) {
                BadgeDrawable badgeDrawable = mBottomNavigation.getOrCreateBadge(R.id.menu_feedback);
                badgeDrawable.setBackgroundColor(CurrentTheme.getColorPrimary(this));
                badgeDrawable.setBadgeTextColor(CurrentTheme.getColorOnPrimary(this));
                badgeDrawable.setNumber(counters.getNotifications());
            } else {
                mBottomNavigation.removeBadge(R.id.menu_feedback);
            }
            if (counters.getMessages() > 0) {
                BadgeDrawable badgeDrawable = mBottomNavigation.getOrCreateBadge(R.id.menu_messages);
                badgeDrawable.setBackgroundColor(CurrentTheme.getColorPrimary(this));
                badgeDrawable.setBadgeTextColor(CurrentTheme.getColorOnPrimary(this));
                badgeDrawable.setNumber(counters.getMessages());
            } else {
                mBottomNavigation.removeBadge(R.id.menu_messages);
            }
        }
    }

    private void removeNotificationsBagde() {
        if (mBottomNavigation != null) {
            mBottomNavigation.removeBadge(R.id.menu_feedback);
            mBottomNavigation.removeBadge(R.id.menu_messages);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_feed:
                openPageAndCloseSheet(AdditionalNavigationFragment.SECTION_ITEM_FEED);
                return true;
            case R.id.menu_search:
                openPageAndCloseSheet(AdditionalNavigationFragment.SECTION_ITEM_SEARCH);
                return true;
            case R.id.menu_messages:
                openPageAndCloseSheet(AdditionalNavigationFragment.SECTION_ITEM_DIALOGS);
                return true;
            case R.id.menu_feedback:
                openPageAndCloseSheet(AdditionalNavigationFragment.SECTION_ITEM_FEEDBACK);
                return true;
            case R.id.menu_other:
                if (getNavigationFragment().isSheetOpen()) {
                    getNavigationFragment().closeSheet();
                } else {
                    getNavigationFragment().openSheet();
                }
                return true;
        }
        return false;
    }
}
