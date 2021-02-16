package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.EndlessData;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveUsersView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;


public class FavePagesPresenter extends AccountDependencyPresenter<IFaveUsersView> {

    private final List<FavePage> pages;

    private final List<FavePage> search_pages;

    private final IFaveInteractor faveInteractor;
    private final boolean isUser;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataReceived;
    private boolean endOfContent;
    private String q;
    private boolean cacheLoadingNow;
    private boolean actualDataLoading;
    private boolean doLoadTabs;

    public FavePagesPresenter(int accountId, boolean isUser, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        pages = new ArrayList<>();
        search_pages = new ArrayList<>();
        faveInteractor = InteractorFactory.createFaveInteractor();
        this.isUser = isUser;

        loadAllCachedData();
    }

    private boolean isSearchNow() {
        return nonEmpty(q);
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();

        if (Objects.safeEquals(q, this.q)) {
            return;
        }
        this.q = query;
        search_pages.clear();
        for (FavePage i : pages) {
            if (i.getOwner() == null || Utils.isEmpty(i.getOwner().getFullName())) {
                continue;
            }
            if (i.getOwner().getFullName().toLowerCase().contains(q.toLowerCase())) {
                search_pages.add(i);
            }
        }

        if (isSearchNow())
            callView(v -> v.displayData(search_pages));
        else
            callView(v -> v.displayData(pages));
    }

    @Override
    public void onGuiCreated(@NonNull IFaveUsersView view) {
        super.onGuiCreated(view);
        if (isSearchNow()) {
            view.displayData(search_pages);
        } else {
            view.displayData(pages);
        }
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(faveInteractor.getPages(accountId, 500, offset, isUser)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));


    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, EndlessData<FavePage> data) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDataLoading = false;
        endOfContent = !data.hasNext();
        actualDataReceived = true;

        if (offset == 0) {
            pages.clear();
            pages.addAll(data.get());
            callView(IFaveUsersView::notifyDataSetChanged);
        } else {
            int startSize = pages.size();
            pages.addAll(data.get());
            callView(view -> view.notifyDataAdded(startSize, data.get().size()));
        }

        resolveRefreshingView();
        fireScrollToEnd();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doLoadTabs) {
            return;
        } else {
            doLoadTabs = true;
        }
        loadActualData(0);
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(actualDataLoading);
        }
    }

    private void loadAllCachedData() {
        cacheLoadingNow = true;
        int accountId = getAccountId();

        cacheDisposable.add(faveInteractor.getCachedPages(accountId, isUser)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCachedGetError));
    }

    private void onCachedGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onCachedDataReceived(List<FavePage> data) {
        cacheLoadingNow = false;

        pages.clear();
        pages.addAll(data);
        callView(IFaveUsersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && nonEmpty(pages) && actualDataReceived && !cacheLoadingNow && !actualDataLoading && !isSearchNow()) {
            loadActualData(pages.size());
            return false;
        }
        return true;
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }

    public void fireOwnerClick(Owner owner) {
        getView().openOwnerWall(getAccountId(), owner);
    }

    private void onUserRemoved(int accountId, int ownerId) {
        if (getAccountId() != accountId) {
            return;
        }

        int index = findIndexById(pages, Math.abs(ownerId));

        if (index != -1) {
            pages.remove(index);
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireOwnerDelete(Owner owner) {
        int accountId = getAccountId();
        appendDisposable(faveInteractor.removePage(accountId, owner.getOwnerId(), isUser)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUserRemoved(accountId, owner.getOwnerId()), t -> showError(getView(), getCauseIfRuntime(t))));
    }
}
