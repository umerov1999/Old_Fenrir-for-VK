package dev.ragnarok.fenrir.api.impl;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.IFaveApi;
import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.response.FavePageResponse;
import dev.ragnarok.fenrir.api.model.response.FavePostsResponse;
import dev.ragnarok.fenrir.api.services.IFaveService;
import dev.ragnarok.fenrir.db.column.UserColumns;
import io.reactivex.rxjava3.core.Single;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;


class FaveApi extends AbsApi implements IFaveApi {

    FaveApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<FavePageResponse>> getPages(Integer offset, Integer count, String fields, String type) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPages(offset, count, type, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPhotos(offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiVideo>> getVideos(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getVideos(offset, count, "video", 1, UserColumns.API_FIELDS)
                        .map(extractResponseWithErrorHandling()).flatMap(t -> {
                                    List<VkApiAttachments.Entry> temp = listEmptyIfNull(t.items);
                                    List<VKApiVideo> videos = new ArrayList<>();
                                    for (VkApiAttachments.Entry i : temp) {
                                        if (i.attachment instanceof VKApiVideo)
                                            videos.add((VKApiVideo) i.attachment);
                                    }
                                    return Single.just(videos);
                                }
                        ));
    }

    @Override
    public Single<List<VKApiArticle>> getArticles(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getArticles(offset, count, "article", 1, UserColumns.API_FIELDS)
                        .map(extractResponseWithErrorHandling()).flatMap(t -> {
                                    List<VkApiAttachments.Entry> temp = listEmptyIfNull(t.items);
                                    List<VKApiArticle> articles = new ArrayList<>();
                                    for (VkApiAttachments.Entry i : temp) {
                                        if (i.attachment instanceof VKApiArticle)
                                            articles.add((VKApiArticle) i.attachment);
                                    }
                                    return Single.just(articles);
                                }
                        ));
    }

    @Override
    public Single<Items<VKApiArticle>> getOwnerPublishedArticles(Integer owner_id, Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getOwnerPublishedArticles(owner_id, offset, count, "date", 1, UserColumns.API_FIELDS)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<FavePostsResponse> getPosts(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPosts(offset, count, "post", 1, UserColumns.API_FIELDS)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getLinks(offset, count, "link", 1, UserColumns.API_FIELDS)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> addPage(Integer userId, Integer groupId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addPage(userId, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addLink(String link) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addLink(link)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addVideo(Integer owner_id, Integer id, String access_key) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addVideo(owner_id, id, access_key)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addArticle(String url) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addArticle(url)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addPost(Integer owner_id, Integer id, String access_key) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addPost(owner_id, id, access_key)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removePage(Integer userId, Integer groupId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removePage(userId, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeLink(String linkId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeLink(linkId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeArticle(Integer owner_id, Integer article_id) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeArticle(owner_id, article_id)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removePost(Integer owner_id, Integer id) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removePost(owner_id, id)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeVideo(Integer owner_id, Integer id) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeVideo(owner_id, id)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}
