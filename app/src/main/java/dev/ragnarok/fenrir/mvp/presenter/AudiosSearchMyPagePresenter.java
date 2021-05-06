package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosSearchMyPageView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.FindAtWithContent;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

public class AudiosSearchMyPagePresenter extends AccountDependencyPresenter<IAudiosSearchMyPageView> {

    private static final int SEARCH_COUNT = 200;
    private static final int GET_COUNT = 50;
    private static final int SEARCH_VIEW_COUNT = 20;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<Audio> audios;
    private final IAudioInteractor audioInteractor;
    private final int ownerId;
    private final FindAudio searcher;
    private Disposable sleepDataDisposable = Disposable.disposed();
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private boolean doAudioLoadTabs;

    public AudiosSearchMyPagePresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.ownerId = ownerId;
        audios = new ArrayList<>();
        audioInteractor = InteractorFactory.createAudioInteractor();
        searcher = new FindAudio(getCompositeDisposable());
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosSearchMyPageView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        appendDisposable(audioInteractor.get(getAccountId(), 0, ownerId, offset, GET_COUNT, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<Audio> data) {
        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;

        if (offset == 0) {
            audios.clear();
            audios.addAll(data);
            callView(IAudiosSearchMyPageView::notifyListChanged);
        } else {
            int startSize = audios.size();
            audios.addAll(data);
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
        loadActualData(0);
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayLoading(actualDataLoading);
        }
    }

    @Override
    public void onDestroyed() {
        sleepDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (nonEmpty(audios) && actualDataReceived && !actualDataLoading) {
            if (searcher.isSearchMode()) {
                searcher.do_search();
            } else if (!endOfContent) {
                loadActualData(audios.size());
            }
            return false;
        }
        return true;
    }

    private void sleep_search(String q) {
        if (actualDataLoading) return;

        sleepDataDisposable.dispose();
        if (Utils.isEmpty(q)) {
            if (searcher.cancel()) {
                fireRefresh();
            }
        } else {
            sleepDataDisposable = (Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(videos -> searcher.do_search(q), this::onActualDataGetError));
        }
    }

    public void fireSearchRequestChanged(String q) {
        sleep_search(q == null ? null : q.trim());
    }

    public int getAudioPos(Audio audio) {
        if (!Utils.isEmpty(audios) && audio != null) {
            int pos = 0;
            for (Audio i : audios) {
                if (i.getId() == audio.getId() && i.getOwnerId() == audio.getOwnerId()) {
                    i.setAnimationNow(true);
                    int finalPos = pos;
                    callView(v -> v.notifyItemChanged(finalPos));
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, new ArrayList<>(audios), position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public ArrayList<Audio> getSelected(boolean noDownloaded) {
        ArrayList<Audio> ret = new ArrayList<>();
        for (Audio i : audios) {
            if (i.isSelected()) {
                if (noDownloaded) {
                    if (DownloadWorkUtils.TrackIsDownloaded(i) == 0 && !Utils.isEmpty(i.getUrl()) && !i.getUrl().contains("file://") && !i.getUrl().contains("content://")) {
                        ret.add(i);
                    }
                } else {
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public void fireSelectAll() {
        for (Audio i : audios) {
            i.setIsSelected(true);
        }
        callView(IAudiosSearchMyPageView::notifyListChanged);
    }

    public void fireUpdateSelectMode() {
        for (Audio i : audios) {
            if (i.isSelected()) {
                i.setIsSelected(false);
            }
        }
        callView(IAudiosSearchMyPageView::notifyListChanged);
    }

    public boolean isMyAudio() {
        return ownerId == getAccountId();
    }

    public void fireDelete(int position) {
        fireRefresh();
    }

    public void fireRefresh() {
        if (actualDataLoading) {
            return;
        }

        if (searcher.isSearchMode()) {
            searcher.reset();
        } else {
            loadActualData(0);
        }
    }

    private class FindAudio extends FindAtWithContent<Audio> {
        public FindAudio(CompositeDisposable disposable) {
            super(disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT);
        }

        @Override
        protected Single<List<Audio>> search(int offset, int count) {
            return audioInteractor.get(getAccountId(), 0, ownerId, offset, count, null);
        }

        @Override
        protected void onError(@NonNull Throwable e) {
            onActualDataGetError(e);
        }

        @Override
        protected void onResult(@NonNull List<Audio> data) {
            actualDataReceived = true;
            int startSize = audios.size();
            audios.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        @Override
        protected void updateLoading(boolean loading) {
            actualDataLoading = loading;
            resolveRefreshingView();
        }

        @Override
        protected void clean() {
            audios.clear();
            callView(IAudiosSearchMyPageView::notifyListChanged);
        }

        private boolean checkArtists(@Nullable Map<String, String> data, @NonNull String q) {
            if (Utils.isEmpty(data)) {
                return false;
            }
            for (String i : data.values()) {
                if (i.toLowerCase().contains(q.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected boolean compare(@NonNull Audio data, @NonNull String q) {
            return (Utils.safeCheck(data.getTitle(), () -> data.getTitle().toLowerCase().contains(q.toLowerCase()))
                    || Utils.safeCheck(data.getArtist(), () -> data.getArtist().toLowerCase().contains(q.toLowerCase()))
                    || checkArtists(data.getMain_artists(), q));
        }
    }
}
