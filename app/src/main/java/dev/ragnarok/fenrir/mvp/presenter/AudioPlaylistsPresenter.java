package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioPlaylistsView;
import dev.ragnarok.fenrir.util.FindAt;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

public class AudioPlaylistsPresenter extends AccountDependencyPresenter<IAudioPlaylistsView> {

    private static final int SEARCH_COUNT = 20;
    private static final int GET_COUNT = 50;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<AudioPlaylist> addon;
    private final List<AudioPlaylist> pages;
    private final IAudioInteractor fInteractor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int Foffset;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private FindAt search_at;

    public AudioPlaylistsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        pages = new ArrayList<>();
        addon = new ArrayList<>();
        fInteractor = InteractorFactory.createAudioInteractor();
        search_at = new FindAt();
    }

    public void LoadAudiosTool() {
        if (getAccountId() == owner_id) {
            actualDataDisposable.add(fInteractor.getDualPlaylists(getAccountId(), owner_id, -21, -22)
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

    public int getOwner_id() {
        return owner_id;
    }

    @Override
    public void onGuiCreated(@NonNull IAudioPlaylistsView view) {
        super.onGuiCreated(view);
        view.displayData(pages);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getPlaylists(accountId, owner_id, offset, GET_COUNT)
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
            pages.clear();
            pages.addAll(addon);
            pages.addAll(data);
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        } else {
            int startSize = pages.size();
            pages.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
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
        if (!endOfContent && nonEmpty(pages) && actualDataReceived && !actualDataLoading) {
            loadActualData(Foffset);
            return false;
        }
        return true;
    }

    private void doSearch(int accountId) {
        actualDataLoading = true;
        resolveRefreshingView();
        actualDataDisposable.add(fInteractor.search_owner_playlist(accountId, search_at.getQuery(), owner_id, SEARCH_COUNT, search_at.getOffset(), 0)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(playlist -> onSearched(playlist.getFirst(), playlist.getSecond()), this::onActualDataGetError));
    }

    private void onSearched(FindAt search_at, List<AudioPlaylist> playlist) {
        actualDataLoading = false;
        actualDataReceived = true;
        endOfContent = search_at.isEnded();

        if (this.search_at.getOffset() == 0) {
            pages.clear();
            pages.addAll(playlist);
            callView(IAudioPlaylistsView::notifyDataSetChanged);
        } else {
            if (nonEmpty(playlist)) {
                int startSize = pages.size();
                pages.addAll(playlist);
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

        actualDataDisposable.add(Single.just(new Object())
                .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> doSearch(accountId), this::onActualDataGetError));
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();
        if (!search_at.do_compare(query)) {
            actualDataLoading = false;
            if (Utils.isEmpty(query)) {
                actualDataDisposable.clear();
                fireRefresh(false);
            } else {
                fireRefresh(true);
            }
        }
    }

    public void onDelete(int index, AudioPlaylist album) {
        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.deletePlaylist(accountId, album.getId(), album.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    pages.remove(index);
                    callView(IAudioPlaylistsView::notifyDataSetChanged);
                    getView().getCustomToast().showToast(R.string.success);
                }, throwable -> showError(getView(), throwable)));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getCustomToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    public void fireRefresh(boolean sleep_search) {

        actualDataDisposable.clear();
        actualDataLoading = false;

        if (search_at.isSearchMode()) {
            search_at.reset();
            search(sleep_search);
        } else {
            loadActualData(0);
        }
    }
}
