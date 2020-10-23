package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.IRelationshipInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;


public class FollowersPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final int userId;
    private final IRelationshipInteractor relationshipInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean actualDataLoading;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean cacheLoadingNow;

    public FollowersPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        relationshipInteractor = InteractorFactory.createRelationshipInteractor();
    }

    public void doLoad() {
        loadAllCacheData();
        requestActualData(0);
    }

    private void requestActualData(int offset) {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getFollowers(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onActualDataReceived(offset, users), this::onActualDataGetError));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayRefreshing(actualDataLoading);
        }
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<User> users) {
        actualDataLoading = false;

        cacheDisposable.clear();

        actualDataReceived = true;
        endOfContent = users.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(users);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int startSzie = data.size();
            data.addAll(users);
            callView(view -> view.notifyDataAdded(startSzie, users.size()));
        }

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !cacheLoadingNow && !actualDataLoading && actualDataReceived) {
            requestActualData(data.size());
        }
    }

    @Override
    void onUserRefreshed() {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDataDisposable.clear();
        requestActualData(0);
    }

    private void loadAllCacheData() {
        cacheLoadingNow = true;

        int accountId = getAccountId();
        cacheDisposable.add(relationshipInteractor.getCachedFollowers(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheDataGetError));
    }

    private void onCacheDataGetError(Throwable t) {
        cacheLoadingNow = false;
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onCachedDataReceived(List<User> users) {
        cacheLoadingNow = false;

        data.addAll(users);
        callView(ISimpleOwnersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}