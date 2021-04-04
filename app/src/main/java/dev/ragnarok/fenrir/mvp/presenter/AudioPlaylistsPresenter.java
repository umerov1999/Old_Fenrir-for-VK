package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioPlaylistsView;
import dev.ragnarok.fenrir.util.FindAt;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioPlaylistsPresenter extends AccountDependencyPresenter<IAudioPlaylistsView> {

    private static final int SEARCH_COUNT = 20;
    private static final int GET_COUNT = 50;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<AudioPlaylist> addon;
    private final List<AudioPlaylist> playlists;
    private final IAudioInteractor fInteractor;
    private final int owner_id;
    private Disposable actualDataDisposable = Disposable.disposed();
    private AudioPlaylist pending_to_add;
    private int Foffset;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private FindAt search_at;
    private boolean doAudioLoadTabs;

    public AudioPlaylistsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        playlists = new ArrayList<>();
        addon = new ArrayList<>();
        fInteractor = InteractorFactory.createAudioInteractor();
        search_at = new FindAt();
    }

    public int getOwner_id() {
        return owner_id;
    }

    @Override
    public void onGuiCreated(@NonNull IAudioPlaylistsView view) {
        super.onGuiCreated(view);
        view.displayData(playlists);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        appendDisposable(fInteractor.getPlaylists(accountId, owner_id, offset, GET_COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<AudioPlaylist> data) {
        Foffset = offset + GET_COUNT;
        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;

        if (offset == 0) {
            playlists.clear();
            playlists.addAll(addon);
            playlists.addAll(data);
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        } else {
            int startSize = playlists.size();
            playlists.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doAudioLoadTabs) {
            return;
        } else {
            doAudioLoadTabs = true;
        }
        if (getAccountId() == owner_id) {
            appendDisposable(fInteractor.getDualPlaylists(getAccountId(), owner_id, -21, -22)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(pl -> {
                        addon.clear();
                        addon.addAll(pl);
                        loadActualData(0);
                    }, i -> loadActualData(0)));
        } else {
            loadActualData(0);
        }
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(actualDataLoading);
        }
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && nonEmpty(playlists) && actualDataReceived && !actualDataLoading) {
            if (search_at.isSearchMode()) {
                search(false);
            } else {
                loadActualData(Foffset);
            }
            return false;
        }
        return true;
    }

    private void doSearch(int accountId) {
        actualDataLoading = true;
        resolveRefreshingView();
        appendDisposable(fInteractor.search_owner_playlist(accountId, search_at.getQuery(), owner_id, SEARCH_COUNT, search_at.getOffset(), 0)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(playlist -> onSearched(playlist.getFirst(), playlist.getSecond()), this::onActualDataGetError));
    }

    private void onSearched(FindAt search_at, List<AudioPlaylist> playlist) {
        actualDataLoading = false;
        actualDataReceived = true;
        endOfContent = search_at.isEnded();

        if (this.search_at.getOffset() == 0) {
            playlists.clear();
            playlists.addAll(playlist);
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        } else {
            if (nonEmpty(playlist)) {
                int startSize = playlists.size();
                playlists.addAll(playlist);
                callView(view -> view.notifyDataAdded(startSize, playlist.size()));
            }
        }
        this.search_at = search_at;
        resolveRefreshingView();
    }

    private void search(boolean sleep_search) {
        if (actualDataLoading) return;
        int accountId = getAccountId();

        if (!sleep_search) {
            doSearch(accountId);
            return;
        }

        actualDataDisposable.dispose();
        actualDataDisposable = (Single.just(new Object())
                .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> doSearch(accountId), this::onActualDataGetError));
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();
        if (!search_at.do_compare(query)) {
            actualDataLoading = false;
            if (Utils.isEmpty(query)) {
                actualDataDisposable.dispose();
                fireRefresh(false);
            } else {
                fireRefresh(true);
            }
        }
    }

    public void onDelete(int index, AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(fInteractor.deletePlaylist(accountId, album.getId(), album.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    playlists.remove(index);
                    callView(v -> v.notifyItemRemoved(index));
                    getView().getCustomToast().showToast(R.string.success);
                }, throwable -> showError(getView(), throwable)));
    }

    public void onEdit(Context context, int index, AudioPlaylist album) {
        View root = View.inflate(context, R.layout.entry_playlist_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(album.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_description)).setText(album.getDescription());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.edit)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(fInteractor.editPlaylist(getAccountId(), album.getOwnerId(), album.getId(),
                        ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString()).compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(v -> fireRefresh(false), t -> showError(getView(), getCauseIfRuntime(t)))))
                .setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    private void doInsertPlaylist(AudioPlaylist playlist) {
        int offset = addon.size();
        playlists.add(offset, playlist);
        callView(v -> v.notifyDataAdded(offset, 1));
    }

    public void fireCreatePlaylist(Context context) {
        View root = View.inflate(context, R.layout.entry_playlist_info, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.create_playlist)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> appendDisposable(fInteractor.createPlaylist(getAccountId(), owner_id,
                        ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_description)).getText().toString()).compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::doInsertPlaylist, t -> showError(getView(), getCauseIfRuntime(t)))))
                .setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(fInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getCustomToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    public void fireAudiosSelected(List<Audio> audios) {
        List<AccessIdPair> targets = new ArrayList<>(audios.size());
        for (Audio i : audios) {
            targets.add(new AccessIdPair(i.getId(), i.getOwnerId(), i.getAccessKey()));
        }
        int accountId = getAccountId();
        appendDisposable(fInteractor.addToPlaylist(accountId, pending_to_add.getOwnerId(), pending_to_add.getId(), targets)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getCustomToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
        pending_to_add = null;
    }

    public void onPlaceToPending(AudioPlaylist album) {
        pending_to_add = album;
        callView(v -> v.doAddAudios(getAccountId()));
    }

    public void fireRefresh(boolean sleep_search) {
        if (actualDataLoading) {
            return;
        }

        if (search_at.isSearchMode()) {
            search_at.reset();
            search(sleep_search);
        } else {
            loadActualData(0);
        }
    }
}
