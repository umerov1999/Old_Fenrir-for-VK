package dev.ragnarok.fenrir.fragment;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso3.Callback;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dev.ragnarok.fenrir.App;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.view.CircleCounterButton;
import dev.ragnarok.fenrir.view.TouchImageView;
import dev.ragnarok.fenrir.view.pager.GoBackCallback;
import dev.ragnarok.fenrir.view.pager.WeakGoBackAnimationAdapter;
import dev.ragnarok.fenrir.view.pager.WeakPicassoLoadCallback;
import dev.ragnarok.fenrir.view.verticalswipe.BelowFractionalClamp;
import dev.ragnarok.fenrir.view.verticalswipe.PropertySideEffect;
import dev.ragnarok.fenrir.view.verticalswipe.SensitivityClamp;
import dev.ragnarok.fenrir.view.verticalswipe.SettleOnTopAction;
import dev.ragnarok.fenrir.view.verticalswipe.VerticalSwipeBehavior;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;


public class SinglePhotoFragment extends BaseFragment
        implements GoBackCallback, BackPressCallback {

    private static final int REQUEST_WRITE_PERMISSION = 160;
    private final WeakGoBackAnimationAdapter mGoBackAnimationAdapter = new WeakGoBackAnimationAdapter(this);
    private String url;
    private String prefix;
    private String photo_prefix;

    public static SinglePhotoFragment newInstance(Bundle args) {
        SinglePhotoFragment fragment = new SinglePhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(String url, String download_prefix, String photo_prefix) {
        Bundle args = new Bundle();
        args.putString(Extra.URL, url);
        args.putString(Extra.STATUS, download_prefix);
        args.putString(Extra.KEY, photo_prefix);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = requireArguments().getString(Extra.URL);
        prefix = DownloadWorkUtils.makeLegalFilename(requireArguments().getString(Extra.STATUS), null);
        photo_prefix = DownloadWorkUtils.makeLegalFilename(requireArguments().getString(Extra.KEY), null);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_single_url_photo, container, false);

        CircleCounterButton mDownload = root.findViewById(R.id.button_download);
        mDownload.setVisibility(url.contains("content://") || url.contains("file://") ? View.GONE : View.VISIBLE);

        PhotoViewHolder ret = new PhotoViewHolder(root);
        ret.bindTo(url);

        VerticalSwipeBehavior<TouchImageView> ui = VerticalSwipeBehavior.Companion.from(ret.photo);
        ui.setSettle(new SettleOnTopAction());
        ui.setSideEffect(new PropertySideEffect(View.ALPHA, View.SCALE_X, View.SCALE_Y));
        BelowFractionalClamp clampDelegate = new BelowFractionalClamp(3f, 3f);
        ui.setClamp(new SensitivityClamp(0.5f, clampDelegate, 0.5f));
        ui.setListener(new VerticalSwipeBehavior.SwipeListener() {
            @Override
            public void onReleased() {
            }

            @Override
            public void onCaptured() {
            }

            @Override
            public void onPreSettled(int diff) {
            }

            @Override
            public void onPostSettled(boolean isSuccess) {
                if (isSuccess) {
                    goBack();
                }
            }
        });

        ret.photo.setOnLongClickListener(v -> {
            doSaveOnDrive(true);
            return true;
        });
        ret.photo.setOnTouchListener((view, event) -> {
            if (event.getPointerCount() >= 2 || view.canScrollHorizontally(1) && view.canScrollHorizontally(-1)) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        ui.setCanSwipe(false);
                        container.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        ui.setCanSwipe(true);
                        container.requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return true;
        });
        mDownload.setOnClickListener(v -> doSaveOnDrive(true));

        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            doSaveOnDrive(false);
        }
    }

    private void doSaveOnDrive(boolean Request) {
        if (Request) {
            if (!AppPerms.hasWriteStoragePermision(App.getInstance())) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        }
        File dir = new File(Settings.get().other().getPhotoDir());
        if (!dir.isDirectory()) {
            boolean created = dir.mkdirs();
            if (!created) {
                CustomToast.CreateCustomToast(requireActivity()).showToastError("Can't create directory " + dir);
                return;
            }
        } else
            dir.setLastModified(Calendar.getInstance().getTime().getTime());

        if (prefix != null && Settings.get().other().isPhoto_to_user_dir()) {
            File dir_final = new File(dir.getAbsolutePath() + "/" + prefix);
            if (!dir_final.isDirectory()) {
                boolean created = dir_final.mkdirs();
                if (!created) {
                    CustomToast.CreateCustomToast(requireActivity()).showToastError("Can't create directory " + dir);
                    return;
                }
            } else
                dir_final.setLastModified(Calendar.getInstance().getTime().getTime());
            dir = dir_final;
        }
        DateFormat DOWNLOAD_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        DownloadWorkUtils.doDownloadPhoto(requireActivity(), url, dir.getAbsolutePath(), prefix + "." + photo_prefix + ".profile." + DOWNLOAD_DATE_FORMAT.format(new Date()));
    }

    @Override
    public void goBack() {
        if (isAdded() && canGoBack()) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean canGoBack() {
        return requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 1;
    }

    @Override
    public boolean onBackPressed() {
        ObjectAnimator objectAnimatorPosition = ObjectAnimator.ofFloat(getView(), "translationY", -600);
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(getView(), View.ALPHA, 1, 0);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorPosition, objectAnimatorAlpha);
        animatorSet.setDuration(200);
        animatorSet.addListener(mGoBackAnimationAdapter);
        animatorSet.start();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(false, false)
                .build()
                .apply(requireActivity());
    }

    private class PhotoViewHolder implements Callback {
        private final WeakPicassoLoadCallback mPicassoLoadCallback;
        public TouchImageView photo;
        public ProgressBar progress;
        public final FloatingActionButton reload;
        private boolean mLoadingNow;

        public PhotoViewHolder(View view) {
            photo = view.findViewById(R.id.image_view);
            progress = view.findViewById(R.id.progress_bar);
            photo = view.findViewById(idOfImageView());
            photo.setMaxZoom(8f);
            photo.setDoubleTapScale(2f);
            photo.setDoubleTapMaxZoom(4f);
            progress = view.findViewById(idOfProgressBar());
            reload = view.findViewById(R.id.goto_button);
            mPicassoLoadCallback = new WeakPicassoLoadCallback(this);

        }

        public void bindTo(@NonNull String url) {
            reload.setOnClickListener(v -> {
                reload.setVisibility(View.INVISIBLE);
                if (nonEmpty(url)) {
                    loadImage(url);
                } else
                    PicassoInstance.with().cancelRequest(photo);
            });

            if (nonEmpty(url)) {
                loadImage(url);
            } else {
                PicassoInstance.with().cancelRequest(photo);
                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.empty_url);
            }

        }

        private void resolveProgressVisibility() {
            progress.setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
        }

        protected void loadImage(@NonNull String url) {
            mLoadingNow = true;

            resolveProgressVisibility();

            PicassoInstance.with()
                    .load(url)
                    .into(photo, mPicassoLoadCallback);
        }

        @IdRes
        protected int idOfImageView() {
            return R.id.image_view;
        }

        @IdRes
        protected int idOfProgressBar() {
            return R.id.progress_bar;
        }

        @Override
        public void onSuccess() {
            mLoadingNow = false;
            resolveProgressVisibility();
            reload.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(Throwable e) {
            mLoadingNow = false;
            resolveProgressVisibility();
            reload.setVisibility(View.VISIBLE);
        }
    }
}
