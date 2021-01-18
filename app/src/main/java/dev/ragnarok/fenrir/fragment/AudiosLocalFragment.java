package dev.ragnarok.fenrir.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AudioLocalRecyclerAdapter;
import dev.ragnarok.fenrir.adapter.DocsUploadAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudiosLocalPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosLocalView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class AudiosLocalFragment extends BaseMvpFragment<AudiosLocalPresenter, IAudiosLocalView>
        implements MySearchView.OnQueryTextListener, DocsUploadAdapter.ActionListener, AudioLocalRecyclerAdapter.ClickListener, IAudiosLocalView {

    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> {
                if (isPresenterPrepared()) {
                    getPresenter().LoadAudiosTool();
                }
            });
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioLocalRecyclerAdapter mAudioRecyclerAdapter;
    private boolean doAudioLoadTabs;
    private DocsUploadAdapter mUploadAdapter;
    private View mUploadRoot;

    public static AudiosLocalFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudiosLocalFragment fragment = new AudiosLocalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_local_music, container, false);

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setRightButtonVisibility(false);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setQuery("", true);

        RecyclerView uploadRecyclerView = root.findViewById(R.id.uploads_recycler_view);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mUploadAdapter = new DocsUploadAdapter(Collections.emptyList(), this);
        uploadRecyclerView.setAdapter(mUploadAdapter);
        mUploadRoot = root.findViewById(R.id.uploads_root);

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

        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        Goto.setImageResource(R.drawable.audio_player);

        Goto.setOnLongClickListener(v -> {
            Audio curr = MusicUtils.getCurrentAudio();
            if (curr != null) {
                PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
            } else
                CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
            return false;
        });
        Goto.setOnClickListener(v -> {
            Audio curr = MusicUtils.getCurrentAudio();
            if (curr != null) {
                int index = getPresenter().getAudioPos(curr);
                if (index >= 0) {
                    if (Settings.get().other().isShow_audio_cover())
                        recyclerView.scrollToPosition(index);
                    else
                        recyclerView.smoothScrollToPosition(index);
                } else
                    CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.audio_not_found);
            } else
                CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.null_audio);
        });
        mAudioRecyclerAdapter = new AudioLocalRecyclerAdapter(requireActivity(), Collections.emptyList());
        mAudioRecyclerAdapter.setClickListener(this);
        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!doAudioLoadTabs) {
            doAudioLoadTabs = true;
            if (!AppPerms.hasReadStoragePermission(getActivity())) {
                requestReadPermission.launch();
            } else {
                getPresenter().LoadAudiosTool();
            }
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<AudiosLocalPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosLocalPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setItems(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void setUploadDataVisible(boolean visible) {
        if (nonNull(mUploadRoot)) {
            mUploadRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayUploads(List<Upload> data) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.setData(data);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void notifyUploadDataChanged() {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyUploadItemsAdded(int position, int count) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyUploadItemChanged(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void notifyUploadItemRemoved(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyUploadProgressChanged(int position, int progress, boolean smoothly) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.changeUploadProgress(position, progress, smoothly);
        }
    }

    @Override
    public void onRemoveClick(Upload upload) {
        getPresenter().fireRemoveClick(upload);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getPresenter().fireQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getPresenter().fireQuery(newText);
        return false;
    }

    @Override
    public void onClick(int position, Audio audio) {
        getPresenter().playAudio(requireActivity(), position);
    }

    @Override
    public void onDelete(int position) {
        getPresenter().fireDelete(position);
    }

    @Override
    public void onUpload(int position, Audio audio) {
        getPresenter().fireFileForUploadSelected(audio.getUrl());
    }
}
