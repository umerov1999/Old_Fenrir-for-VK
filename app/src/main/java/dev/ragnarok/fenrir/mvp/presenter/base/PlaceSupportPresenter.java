package dev.ragnarok.fenrir.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.util.RxUtils;

public abstract class PlaceSupportPresenter<V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends AccountDependencyPresenter<V> {

    public PlaceSupportPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
    }

    public void fireLinkClick(@NonNull Link link) {
        getView().openLink(getAccountId(), link);
    }

    public void fireUrlClick(@NonNull String url) {
        getView().openUrl(getAccountId(), url);
    }

    public void fireWikiPageClick(@NonNull WikiPage page) {
        getView().openWikiPage(getAccountId(), page);
    }

    public void fireStoryClick(@NonNull Story story) {
        getView().openStory(getAccountId(), story);
    }

    public void firePhotoClick(@NonNull ArrayList<Photo> photos, int index, boolean refresh) {
        getView().openSimplePhotoGallery(getAccountId(), photos, index, refresh);
    }

    public void firePostClick(@NonNull Post post) {
        getView().openPost(getAccountId(), post);
    }

    public void fireDocClick(@NonNull Document document) {
        getView().openDocPreview(getAccountId(), document);
    }

    public void fireOwnerClick(int ownerId) {
        getView().openOwnerWall(getAccountId(), ownerId);
    }

    public void fireGoToMessagesLookup(@NonNull Message message) {
        getView().goToMessagesLookupFWD(getAccountId(), message.getPeerId(), message.getId());
    }

    public void fireForwardMessagesClick(@NonNull ArrayList<Message> messages) {
        getView().openForwardMessages(getAccountId(), messages);
    }

    public void fireAudioPlayClick(int position, @NonNull ArrayList<Audio> apiAudio) {
        getView().playAudioList(getAccountId(), position, apiAudio);
    }

    public void fireVideoClick(@NonNull Video apiVideo) {
        getView().openVideo(getAccountId(), apiVideo);
    }

    public void fireAudioPlaylistClick(@NotNull AudioPlaylist playlist) {
        getView().openAudioPlaylist(getAccountId(), playlist);
    }

    public void fireWallReplyOpen(@NotNull WallReply reply) {
        getView().goWallReplyOpen(getAccountId(), reply);
    }

    public void firePollClick(@NonNull Poll poll) {
        getView().openPoll(getAccountId(), poll);
    }

    public void fireHashtagClick(String hashTag) {
        getView().openSearch(getAccountId(), SearchContentType.NEWS, new NewsFeedCriteria(hashTag));
    }

    public void fireShareClick(Post post) {
        getView().repostPost(getAccountId(), post);
    }

    public void fireCommentsClick(Post post) {
        getView().openComments(getAccountId(), Commented.from(post), null);
    }

    public void firePhotoAlbumClick(@NotNull PhotoAlbum album) {
        getView().openPhotoAlbum(getAccountId(), album);
    }

    public void fireMarketAlbumClick(@NonNull MarketAlbum market_album) {
        getView().toMarketAlbumOpen(getAccountId(), market_album);
    }

    public void fireMarketClick(@NonNull Market market) {
        getView().toMarketOpen(getAccountId(), market);
    }

    public void fireFaveArticleClick(@NotNull Article article) {
        if (!article.getIsFavorite()) {
            appendDisposable(InteractorFactory.createFaveInteractor().addArticle(getAccountId(), article.getURL())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(RxUtils.dummy(), t -> {
                    }));
        } else {
            appendDisposable(InteractorFactory.createFaveInteractor().removeArticle(getAccountId(), article.getOwnerId(), article.getId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(i -> {
                    }, t -> {
                    }));
        }
    }

    public final void fireCopiesLikesClick(String type, int ownerId, int itemId, String filter) {
        if (ILikesInteractor.FILTER_LIKES.equals(filter)) {
            getView().goToLikes(getAccountId(), type, ownerId, itemId);
        } else if (ILikesInteractor.FILTER_COPIES.equals(filter)) {
            getView().goToReposts(getAccountId(), type, ownerId, itemId);
        }
    }
}
