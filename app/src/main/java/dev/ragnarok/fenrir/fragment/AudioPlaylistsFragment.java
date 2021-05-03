package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.AudioSelectActivity;
import dev.ragnarok.fenrir.adapter.AudioPlaylistsAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudioPlaylistsPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioPlaylistsView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class AudioPlaylistsFragment extends BaseMvpFragment<AudioPlaylistsPresenter, IAudioPlaylistsView> implements IAudioPlaylistsView, AudioPlaylistsAdapter.ClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String ACTION_SELECT = "AudioPlaylistsFragment.ACTION_SELECT";
    private final ActivityResultLauncher<Intent> requestAudioSelect = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<Audio> audios = result.getData().getParcelableArrayListExtra("attachments");
                    AssertUtils.requireNonNull(audios);
                    getPresenter().fireAudiosSelected(audios);
                }
            });
    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioPlaylistsAdapter mAdapter;
    private boolean inTabsContainer;
    private boolean isSelectMode;

    public static AudioPlaylistsFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        AudioPlaylistsFragment fragment = new AudioPlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioPlaylistsFragment newInstanceSelect(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, accountId);
        args.putBoolean(ACTION_SELECT, true);
        AudioPlaylistsFragment fragment = new AudioPlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio_playlist, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        mEmpty = root.findViewById(R.id.fragment_audio_playlist_empty_text);
        FloatingActionButton mAdd = root.findViewById(R.id.add_button);

        if (mAdd != null) {
            if (getPresenter().getAccountId() != getPresenter().getOwner_id())
                mAdd.setVisibility(View.GONE);
            else {
                mAdd.setVisibility(View.VISIBLE);
                mAdd.setOnClickListener(v -> getPresenter().fireCreatePlaylist(requireActivity()));
            }
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getPresenter().fireSearchRequestChanged(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getPresenter().fireSearchRequestChanged(newText);
                return false;
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh(false));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new AudioPlaylistsAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(R.string.playlists);
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
    }

    @Override
    public void displayData(List<AudioPlaylist> playlists) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(playlists);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(position);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<AudioPlaylistsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioPlaylistsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void onAlbumClick(int index, AudioPlaylist album) {
        if (isSelectMode) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, new ArrayList<>(Collections.singleton(album)));
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        } else {
            PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
        }
    }

    @Override
    public void onOpenClick(int index, AudioPlaylist album) {
        PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onDelete(int index, AudioPlaylist album) {
        getPresenter().onDelete(index, album);
    }

    @Override
    public void onEdit(int index, AudioPlaylist album) {
        getPresenter().onEdit(requireActivity(), index, album);
    }

    @Override
    public void onAddAudios(int index, AudioPlaylist album) {
        getPresenter().onPlaceToPending(album);
    }

    @Override
    public void doAddAudios(int accountId) {
        requestAudioSelect.launch(AudioSelectActivity.createIntent(requireActivity(), accountId));
    }

    @Override
    public void onAdd(int index, AudioPlaylist album) {
        getPresenter().onAdd(album);
    }
}
