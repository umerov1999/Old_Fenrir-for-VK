package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.CheckUpdate;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.AudioRecyclerAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalPlaylistAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudiosPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class AudiosFragment extends BaseMvpFragment<AudiosPresenter, IAudiosView>
        implements IAudiosView, HorizontalPlaylistAdapter.Listener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String ACTION_SELECT = "AudiosFragment.ACTION_SELECT";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioRecyclerAdapter mAudioRecyclerAdapter;
    private final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        public boolean onMove(@NotNull RecyclerView recyclerView,
                              @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mAudioRecyclerAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            getPresenter().playAudio(requireActivity(), mAudioRecyclerAdapter.getItemRawPosition(viewHolder.getBindingAdapterPosition()));
        }
    };
    private boolean inTabsContainer;
    private boolean doAudioLoadTabs;
    private boolean isSelectMode;
    private boolean isSaveMode;
    private View headerPlaylist;
    private HorizontalPlaylistAdapter mPlaylistAdapter;

    public static AudiosFragment newInstance(int accountId, int ownerId, int option_menu_id, int isAlbum, String access_key) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.ID, option_menu_id);
        args.putInt(Extra.ALBUM, isAlbum);
        args.putString(Extra.ACCESS_KEY, access_key);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudiosFragment newInstanceSelect(int accountId, int option_menu_id, int isAlbum, String access_key) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, accountId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.ID, option_menu_id);
        args.putInt(Extra.ALBUM, isAlbum);
        args.putString(Extra.ACCESS_KEY, access_key);
        args.putBoolean(ACTION_SELECT, true);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudiosFragment newInstanceAlbumSelect(int accountId, int ownerId, int option_menu_id, int isAlbum, String access_key) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.ID, option_menu_id);
        args.putInt(Extra.ALBUM, isAlbum);
        args.putString(Extra.ACCESS_KEY, access_key);
        args.putBoolean(ACTION_SELECT, true);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
        isSaveMode = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        if (!inTabsContainer) {
            new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
        }

        FloatingActionButton save_mode = root.findViewById(R.id.save_mode_button);
        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        save_mode.setVisibility(isSelectMode ? View.GONE : (Settings.get().other().isAudio_save_mode_button() ? View.VISIBLE : View.GONE));
        save_mode.setOnClickListener(v -> {
            if (!CheckUpdate.isFullVersion(requireActivity())) {
                return;
            }
            isSaveMode = !isSaveMode;
            Goto.setImageResource(isSaveMode ? R.drawable.check : R.drawable.audio_player);
            mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
            getPresenter().fireUpdateSelectMode();
        });

        if (isSelectMode)
            Goto.setImageResource(R.drawable.check);
        else
            Goto.setImageResource(R.drawable.audio_player);
        if (!isSelectMode) {
            Goto.setOnLongClickListener(v -> {
                Audio curr = MusicUtils.getCurrentAudio();
                if (curr != null) {
                    PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
                } else
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                return false;
            });
        }
        Goto.setOnClickListener(v -> {
            if (isSelectMode) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, getPresenter().getSelected(false));
                requireActivity().setResult(Activity.RESULT_OK, intent);
                requireActivity().finish();
            } else {
                if (isSaveMode) {
                    List<Audio> tracks = getPresenter().getSelected(true);
                    isSaveMode = false;
                    Goto.setImageResource(R.drawable.audio_player);
                    mAudioRecyclerAdapter.toggleSelectMode(isSaveMode);
                    getPresenter().fireUpdateSelectMode();

                    if (!Utils.isEmpty(tracks)) {
                        DownloadWorkUtils.CheckDirectory(Settings.get().other().getMusicDir());
                        int account_id = getPresenter().getAccountId();
                        WorkContinuation object = WorkManager.getInstance(requireActivity()).beginWith(DownloadWorkUtils.makeDownloadRequestAudio(tracks.get(0), account_id));
                        if (tracks.size() > 1) {
                            List<OneTimeWorkRequest> Requests = new ArrayList<>(tracks.size() - 1);
                            boolean is_first = true;
                            for (Audio i : tracks) {
                                if (is_first) {
                                    is_first = false;
                                    continue;
                                }
                                Requests.add(DownloadWorkUtils.makeDownloadRequestAudio(i, account_id));
                            }
                            object = object.then(Requests);
                        }
                        object.enqueue();
                    }
                } else {
                    Audio curr = MusicUtils.getCurrentAudio();
                    if (curr != null) {
                        int index = getPresenter().getAudioPos(curr);
                        if (index >= 0) {
                            if (Settings.get().other().isShow_audio_cover())
                                recyclerView.scrollToPosition(index + mAudioRecyclerAdapter.getHeadersCount());
                            else
                                recyclerView.smoothScrollToPosition(index + mAudioRecyclerAdapter.getHeadersCount());
                        } else
                            CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.audio_not_found);
                    } else
                        CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
                }
            }
        });

        mAudioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList(), getPresenter().isMyAudio(), isSelectMode, 0);


        headerPlaylist = inflater.inflate(R.layout.header_audio_playlist, recyclerView, false);
        RecyclerView headerPlaylistRecyclerView = headerPlaylist.findViewById(R.id.header_audio_playlist);
        headerPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mPlaylistAdapter = new HorizontalPlaylistAdapter(Collections.emptyList());
        mPlaylistAdapter.setListener(this);
        headerPlaylistRecyclerView.setAdapter(mPlaylistAdapter);

        mAudioRecyclerAdapter.setClickListener(new AudioRecyclerAdapter.ClickListener() {
            @Override
            public void onClick(int position, int catalog, Audio audio) {
                getPresenter().playAudio(requireActivity(), position);
            }

            @Override
            public void onEdit(int position, Audio audio) {
                getPresenter().fireEditTrackIn(requireActivity(), audio);
            }

            @Override
            public void onDelete(int position) {
                getPresenter().fireDelete(position);
            }

            @Override
            public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {
                PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity());
            }
        });

        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    @Override
    public void updatePlaylists(List<AudioPlaylist> playlists) {
        if (nonNull(mPlaylistAdapter)) {
            mPlaylistAdapter.setItems(playlists);
            mPlaylistAdapter.notifyDataSetChanged();
            mAudioRecyclerAdapter.addHeader(headerPlaylist);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(R.string.music);
                actionBar.setSubtitle(null);
            }

            if (requireActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_AUDIOS);
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setHideNavigationMenu(false)
                    .setBarsColored(requireActivity(), true)
                    .build()
                    .apply(requireActivity());
        }
        if (!doAudioLoadTabs) {
            doAudioLoadTabs = true;
            getPresenter().LoadAudiosTool();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<AudiosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                requireArguments().getInt(Extra.ID),
                requireArguments().getInt(Extra.ALBUM),
                requireArguments().getBoolean(ACTION_SELECT),
                requireArguments().getString(Extra.ACCESS_KEY),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setData(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableChanged(index);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemBindableRangeInserted(position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void onPlayListClick(AudioPlaylist item, int pos) {
        if (item.getOwnerId() == Settings.get().accounts().getCurrent())
            getPresenter().onDelete(item);
        else
            getPresenter().onAdd(item);
    }
}
