package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.domain.ICommunitiesInteractor;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.PostFilter;
import dev.ragnarok.fenrir.model.Token;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.mvp.reflect.OnGuiCreated;
import dev.ragnarok.fenrir.mvp.view.IGroupWallView;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

public class GroupWallPresenter extends AbsWallPresenter<IGroupWallView> {

    private final ISettings.IAccountsSettings settings;
    private final IFaveInteractor faveInteractor;
    private final IOwnersRepository ownersRepository;
    private final ICommunitiesInteractor communitiesInteractor;
    private final IWallsRepository wallsRepository;
    private final List<PostFilter> filters;
    private Community community;
    private CommunityDetails details;

    public GroupWallPresenter(int accountId, int ownerId, @Nullable Community owner, @Nullable Bundle savedInstanceState) {
        super(accountId, ownerId, savedInstanceState);
        community = owner;
        details = new CommunityDetails();

        if (isNull(community)) {
            community = new Community(Math.abs(ownerId));
        }

        ownersRepository = Repository.INSTANCE.getOwners();
        faveInteractor = InteractorFactory.createFaveInteractor();
        communitiesInteractor = InteractorFactory.createCommunitiesInteractor();
        settings = Injection.provideSettings().accounts();
        wallsRepository = Repository.INSTANCE.getWalls();

        filters = new ArrayList<>();
        filters.addAll(createPostFilters());

        syncFiltersWithSelectedMode();
        syncFilterCounters();

        refreshInfo();
    }

    public Community getCommunity() {
        return community;
    }

    @OnGuiCreated
    private void resolveBaseCommunityViews() {
        if (isGuiReady()) {
            getView().displayBaseCommunityData(community, details);
        }
    }

    @OnGuiCreated
    private void resolveMenu() {
        if (isGuiReady()) {
            getView().InvalidateOptionsMenu();
        }
    }

    @OnGuiCreated
    private void resolveCounters() {
        if (isGuiReady()) {
            getView().displayCounters(details.getMembersCount(), details.getTopicsCount(),
                    details.getDocsCount(), details.getPhotosCount(),
                    details.getAudiosCount(), details.getVideosCount(), details.getArticlesCount(), details.getProductsCount(), details.getChatsCount());
        }
    }

    private void refreshInfo() {
        int accountId = getAccountId();
        appendDisposable(ownersRepository.getFullCommunityInfo(accountId, Math.abs(ownerId), IOwnersRepository.MODE_CACHE)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> {
                    onFullInfoReceived(pair.getFirst(), pair.getSecond());
                    requestActualFullInfo();
                }, RxUtils.ignore()));
    }

    private void requestActualFullInfo() {
        int accountId = getAccountId();
        appendDisposable(ownersRepository.getFullCommunityInfo(accountId, Math.abs(ownerId), IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onFullInfoReceived(pair.getFirst(), pair.getSecond()), this::onDetailsGetError));
    }

    private void onFullInfoReceived(Community community, CommunityDetails details) {
        if (nonNull(community)) {
            this.community = community;
        }

        if (nonNull(details)) {
            this.details = details;
        }

        filters.clear();
        filters.addAll(createPostFilters());

        syncFiltersWithSelectedMode();
        syncFilterCounters();

        callView(IGroupWallView::notifyWallFiltersChanged);

        resolveActionButtons();
        resolveCounters();
        resolveBaseCommunityViews();
        resolveMenu();
    }

    private void onDetailsGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private List<PostFilter> createPostFilters() {
        List<PostFilter> filters = new ArrayList<>();
        filters.add(new PostFilter(WallCriteria.MODE_ALL, getString(R.string.all_posts)));
        filters.add(new PostFilter(WallCriteria.MODE_OWNER, getString(R.string.owner_s_posts)));
        filters.add(new PostFilter(WallCriteria.MODE_SUGGEST, getString(R.string.suggests)));

        if (isAdmin()) {
            filters.add(new PostFilter(WallCriteria.MODE_SCHEDULED, getString(R.string.scheduled)));
        }

        return filters;
    }

    private boolean isAdmin() {
        return community.isAdmin();
    }

    private void syncFiltersWithSelectedMode() {
        for (PostFilter filter : filters) {
            filter.setActive(filter.getMode() == getWallFilter());
        }
    }

    private void syncFilterCounters() {
        for (PostFilter filter : filters) {
            switch (filter.getMode()) {
                case WallCriteria.MODE_ALL:
                    filter.setCount(details.getAllWallCount());
                    break;

                case WallCriteria.MODE_OWNER:
                    filter.setCount(details.getOwnerWallCount());
                    break;

                case WallCriteria.MODE_SCHEDULED:
                    filter.setCount(details.getPostponedWallCount());
                    break;

                case WallCriteria.MODE_SUGGEST:
                    filter.setCount(details.getSuggestedWallCount());
                    break;
            }
        }
    }

    @Override
    public void onGuiCreated(@NonNull IGroupWallView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayWallFilters(filters);
    }

    public void firePrimaryButtonClick() {
        if (community.getMemberStatus() == VKApiCommunity.MemberStatus.IS_MEMBER || community.getMemberStatus() == VKApiCommunity.MemberStatus.SENT_REQUEST) {
            leaveCommunity();
        } else {
            joinCommunity();
        }
    }

    public void fireSecondaryButtonClick() {
        if (community.getMemberStatus() == VKApiCommunity.MemberStatus.INVITED) {
            leaveCommunity();
        }
    }

    private void leaveCommunity() {
        int accountid = getAccountId();
        int groupId = Math.abs(ownerId);

        appendDisposable(communitiesInteractor.leave(accountid, groupId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onLeaveResult, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void joinCommunity() {
        int accountid = getAccountId();
        int groupId = Math.abs(ownerId);

        appendDisposable(communitiesInteractor.join(accountid, groupId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onJoinResult, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireHeaderPhotosClick() {
        getView().openPhotoAlbums(getAccountId(), ownerId, community);
    }

    public void fireHeaderAudiosClick() {
        getView().openAudios(getAccountId(), ownerId, community);
    }

    public void fireHeaderArticlesClick() {
        getView().openArticles(getAccountId(), ownerId, community);
    }

    public void fireHeaderProductsClick() {
        getView().openProducts(getAccountId(), ownerId, community);
    }

    public void fireHeaderVideosClick() {
        getView().openVideosLibrary(getAccountId(), ownerId, community);
    }

    public void fireHeaderMembersClick() {
        getView().openCommunityMembers(getAccountId(), Math.abs(ownerId));
    }

    public void fireHeaderTopicsClick() {
        getView().openTopics(getAccountId(), ownerId, community);
    }

    public void fireHeaderDocsClick() {
        getView().openDocuments(getAccountId(), ownerId, community);
    }

    public void fireShowComunityInfoClick() {
        getView().goToShowComunityInfo(getAccountId(), community);
    }

    public void fireShowComunityLinksInfoClick() {
        getView().goToShowComunityLinksInfo(getAccountId(), community);
    }

    public void fireGroupChatsClick() {
        getView().goToGroupChats(getAccountId(), community);
    }

    public void fireHeaderStatusClick() {
        if (nonNull(details) && nonNull(details.getStatusAudio())) {
            getView().playAudioList(getAccountId(), 0, Utils.singletonArrayList(details.getStatusAudio()));
        }
    }

    @OnGuiCreated
    private void resolveActionButtons() {
        if (!isGuiReady()) return;

        if (community.getType() == VKApiCommunity.Type.EVENT) {
            getView().setupPrimaryButton(null);
            getView().setupSecondaryButton(null);
            return;
        }

        @StringRes
        Integer primaryText = null;
        @StringRes
        Integer secondaryText = null;

        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_NOT_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        switch (community.getClosed()) {
                            case VKApiCommunity.Status.CLOSED:
                                primaryText = R.string.community_send_request;
                                break;
                            case VKApiCommunity.Status.OPEN:
                                primaryText = R.string.community_join;
                                break;
                        }

                        break;
                    case VKApiCommunity.Type.PAGE:
                        primaryText = R.string.community_follow;
                        break;
                }

                break;

            case VKApiCommunity.MemberStatus.IS_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        primaryText = R.string.community_leave;
                        break;
                    case VKApiCommunity.Type.PAGE:
                        primaryText = R.string.community_unsubscribe_from_news;
                        break;
                }

                break;

            case VKApiCommunity.MemberStatus.NOT_SURE:
                // TODO: 07.02.2016 ЧЕРТ РАЗБЕРЕТ ЧТО ЭТО.. скорее всего я не уверен, что пойду на встречу
                break;

            case VKApiCommunity.MemberStatus.DECLINED_INVITATION:
                primaryText = R.string.community_send_request;
                break;

            case VKApiCommunity.MemberStatus.SENT_REQUEST:
                primaryText = R.string.cancel_request;
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                primaryText = R.string.community_join;
                secondaryText = R.string.cancel_invitation;
                break;
        }

        getView().setupPrimaryButton(primaryText);
        getView().setupSecondaryButton(secondaryText);
    }

    private void onLeaveResult() {
        Integer resultMessage = null;
        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_MEMBER:
                community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                community.setMember(false);

                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        resultMessage = R.string.community_leave_success;
                        break;
                    case VKApiCommunity.Type.PAGE:
                        resultMessage = R.string.community_unsubscribe_from_news_success;
                        break;
                }
                break;

            case VKApiCommunity.MemberStatus.SENT_REQUEST:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                    community.setMember(false);
                    resultMessage = R.string.request_canceled;
                }
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMember(false);
                    community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                    resultMessage = R.string.invitation_has_been_declined;
                }
                break;
        }

        resolveActionButtons();
        if (nonNull(resultMessage) && isGuiReady()) {
            getView().showSnackbar(resultMessage, true);
        }
    }

    private void onJoinResult() {
        Integer resultMessage = null;

        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_NOT_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        switch (community.getClosed()) {
                            case VKApiCommunity.Status.CLOSED:
                                community.setMember(false);
                                community.setMemberStatus(VKApiCommunity.MemberStatus.SENT_REQUEST);
                                resultMessage = R.string.community_send_request_success;
                                break;

                            case VKApiCommunity.Status.OPEN:
                                community.setMember(true);
                                community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                                resultMessage = R.string.community_join_success;
                                break;
                        }
                        break;

                    case VKApiCommunity.Type.PAGE:
                        community.setMember(true);
                        community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                        resultMessage = R.string.community_follow_success;
                        break;
                }
                break;

            case VKApiCommunity.MemberStatus.DECLINED_INVITATION:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMember(false);
                    community.setMemberStatus(VKApiCommunity.MemberStatus.SENT_REQUEST);
                    resultMessage = R.string.community_send_request_success;
                }
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMember(true);
                    community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                    resultMessage = R.string.community_join_success;
                }
                break;
        }

        resolveActionButtons();

        if (nonNull(resultMessage) && isGuiReady()) {
            getView().showSnackbar(resultMessage, true);
        }
    }

    public void fireFilterEntryClick(PostFilter entry) {
        if (changeWallFilter(entry.getMode())) {
            syncFiltersWithSelectedMode();

            getView().notifyWallFiltersChanged();
        }
    }

    public void fireCommunityControlClick() {
        getView().goToCommunityControl(getAccountId(), community, null);

        /*final int accountId = super.getAccountId();
        final int grouId = Math.abs(ownerId);

        IGroupSettingsInteractor interactor = new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners());
        appendDisposable(interactor.getGroupSettings(accountId, grouId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onSettingsReceived, throwable -> {
                    showError(getView(), getCauseIfRuntime(throwable));
                }));*/
    }

    //private void onSettingsReceived(GroupSettings settings) {
    //    callView(view -> view.goToCommunityControl(getAccountId(), owner, settings));
    //}

    public void fireCommunityMessagesClick() {
        if (Utils.nonEmpty(settings.getAccessToken(ownerId))) {
            openCommunityMessages();
        } else {
            int groupId = Math.abs(ownerId);
            getView().startLoginCommunityActivity(groupId);
        }
    }

    private void openCommunityMessages() {
        int groupId = Math.abs(ownerId);
        int accountId = getAccountId();
        String subtitle = community.getFullName();

        callView(v -> v.openCommunityDialogs(accountId, groupId, subtitle));
    }

    public void fireGroupTokensReceived(ArrayList<Token> tokens) {
        for (Token token : tokens) {
            settings.registerAccountId(token.getOwnerId(), false);
            settings.storeAccessToken(token.getOwnerId(), token.getAccessToken());
        }

        if (tokens.size() == 1) {
            openCommunityMessages();
        }
    }

    public void fireSubscribe() {
        int accountId = getAccountId();
        appendDisposable(wallsRepository.subscribe(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> onExecuteComplete(), this::onExecuteError));
    }

    public void fireUnSubscribe() {
        int accountId = getAccountId();
        appendDisposable(wallsRepository.unsubscribe(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> onExecuteComplete(), this::onExecuteError));
    }

    public void fireAddToBookmarksClick() {
        int accountId = getAccountId();

        appendDisposable(faveInteractor.addPage(accountId, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onExecuteComplete, this::onExecuteError));
    }

    public void fireRemoveFromBookmarks() {
        int accountId = getAccountId();
        appendDisposable(faveInteractor.removePage(accountId, ownerId, false)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onExecuteComplete, this::onExecuteError));
    }

    private void onExecuteError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onExecuteComplete() {
        onRefresh();
        getView().getCustomToast().showToast(R.string.success);
    }

    @Override
    protected void onRefresh() {
        requestActualFullInfo();
    }

    public void fireOptionMenuViewCreated(IGroupWallView.IOptionMenuView view) {
        view.setControlVisible(isAdmin());
        view.setIsFavorite(details.isSetFavorite());
        view.setIsSubscribed(details.isSetSubscribed());
    }

    public void fireChatClick() {
        Peer peer = new Peer(ownerId).setTitle(community.getFullName()).setAvaUrl(community.getMaxSquareAvatar());
        int accountId = getAccountId();
        getView().openChatWith(accountId, accountId, peer);
    }

    @Override
    public void fireAddToNewsClick() {
        appendDisposable(InteractorFactory.createFeedInteractor().saveList(getAccountId(), community.getFullName(), Collections.singleton(community.getOwnerId()))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(i -> getView().showSnackbar(R.string.success, true), t -> showError(getView(), t)));
    }

    @Override
    public void searchStory(boolean ByName) {
        appendDisposable(ownersRepository.searchStory(getAccountId(), ByName ? community.getFullName() : null, ByName ? null : ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    if (!Utils.isEmpty(data)) {
                        stories.clear();
                        stories.addAll(data);
                        getView().updateStory(stories);
                    }
                }, t -> {
                }));
    }

}
