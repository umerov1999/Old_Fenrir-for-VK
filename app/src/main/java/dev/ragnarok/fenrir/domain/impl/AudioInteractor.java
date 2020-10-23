package dev.ragnarok.fenrir.domain.impl;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.CatalogBlock;
import dev.ragnarok.fenrir.model.IdPair;
import dev.ragnarok.fenrir.player.util.MusicUtils;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.FindAt;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

public class AudioInteractor implements IAudioInteractor {

    private final INetworker networker;

    public AudioInteractor(INetworker networker) {
        this.networker = networker;
    }

    protected static String join(Collection<IdPair> audios, String delimiter) {
        if (isNull(audios)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (IdPair pair : audios) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(pair.ownerId).append("_").append(pair.id);
        }

        return sb.toString();
    }

    @Override
    public Completable add(int accountId, Audio orig, Integer groupId, Integer albumId) {
        return networker.vkDefault(accountId)
                .audio()
                .add(orig.getId(), orig.getOwnerId(), groupId, albumId)
                .ignoreElement();
    }

    @Override
    public Completable delete(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .delete(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable edit(int accountId, int ownerId, int audioId, String artist, String title, String text) {
        return networker.vkDefault(accountId)
                .audio()
                .edit(ownerId, audioId, artist, title, text)
                .ignoreElement();
    }

    @Override
    public Completable restore(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .restore(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, Collection<Integer> targetIds) {
        return networker.vkDefault(accountId)
                .audio()
                .setBroadcast(new dev.ragnarok.fenrir.api.model.IdPair(audioId, audioOwnerId), targetIds)
                .ignoreElement();
    }

    @Override
    public Single<List<Audio>> get(int accountId, Integer album_id, int ownerId, int offset, int count, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .get(album_id, ownerId, offset, count, accessKey)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getById(int accountId, List<IdPair> audios) {
        return networker.vkDefault(accountId)
                .audio()
                .getById(join(audios, ","))
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getByIdOld(int accountId, List<IdPair> audios) {
        return networker.vkDefault(accountId)
                .audio()
                .getByIdOld(join(audios, ","))
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<String> getLyrics(int accountId, int lyrics_id) {
        return networker.vkDefault(accountId)
                .audio().getLyrics(lyrics_id).map(out -> out.text);
    }

    @Override
    public Single<List<Audio>> getPopular(int accountId, int foreign, int genre, int count) {

        return networker.vkDefault(accountId)
                .audio()
                .getPopular(foreign, genre, count)
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getRecommendations(int accountId, int audioOwnerId, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getRecommendations(audioOwnerId, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getRecommendationsByAudio(int accountId, String audio, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getRecommendationsByAudio(audio, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<AudioPlaylist>> getPlaylists(int accountId, int owner_id, int offset, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylists(owner_id, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioPlaylist> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<AudioCatalog>> getCatalog(int accountId, String artist_id) {
        return networker.vkDefault(accountId)
                .audio()
                .getCatalog(artist_id)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioCatalog> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<AudioPlaylist> followPlaylist(int accountId, int playlist_id, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .followPlaylist(playlist_id, ownerId, accessKey)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<AudioPlaylist> getPlaylistById(int accountId, int playlist_id, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylistById(playlist_id, ownerId, accessKey)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<List<AudioPlaylist>> getDualPlaylists(int accountId, int owner_id, int first_playlist, int second_playlist) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylistById(first_playlist, owner_id, null)
                .flatMap(out -> networker.vkDefault(accountId)
                        .audio()
                        .getPlaylistById(second_playlist, owner_id, null)
                        .flatMap(out2 -> {
                            List<AudioPlaylist> ret = new ArrayList<>(2);
                            ret.add(Dto2Model.transform(out));
                            ret.add(Dto2Model.transform(out2));
                            return Single.just(ret);
                        }));
    }

    @Override
    public Single<Integer> deletePlaylist(int accountId, int playlist_id, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .deletePlaylist(playlist_id, ownerId)
                .map(resultId -> resultId);
    }

    @Override
    public Single<List<Audio>> search(int accountId, AudioSearchCriteria criteria, int offset, int count) {
        Boolean isMyAudio = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_ADDED);
        Boolean isbyArtist = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_BY_ARTIST);
        Boolean isautocmp = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_AUTOCOMPLETE);
        Boolean islyrics = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_WITH_LYRICS);
        SpinnerOption sortOption = criteria.findOptionByKey(AudioSearchCriteria.KEY_SORT);
        Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        return networker.vkDefault(accountId)
                .audio()
                .search(criteria.getQuery(), isautocmp, islyrics, isbyArtist, sort, isMyAudio, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<AudioPlaylist>> searchPlaylists(int accountId, AudioPlaylistSearchCriteria criteria, int offset, int count) {

        return networker.vkDefault(accountId)
                .audio()
                .searchPlaylists(criteria.getQuery(), offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioPlaylist> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<CatalogBlock> getCatalogBlockById(int accountId, String block_id, String start_from) {
        return networker.vkDefault(accountId)
                .audio()
                .getCatalogBlockById(block_id, start_from)
                .map(Dto2Model::transform);
    }

    @Override
    public Completable PlaceToAudioCache(Context context) {
        if (!AppPerms.hasReadWriteStoragePermision(context))
            return Completable.complete();
        return Completable.create(t -> {
            File temp = new File(Settings.get().other().getMusicDir());
            if (!temp.exists()) {
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                t.onComplete();
                return;
            }
            MusicUtils.CachedAudios.clear();
            for (File u : file_list) {
                if (u.isFile())
                    MusicUtils.CachedAudios.add(u.getName());
            }
        });
    }

    @Override
    public Single<Pair<FindAt, List<AudioPlaylist>>> search_owner_playlist(int accountId, String q, int ownerId, int count, int offset, int loaded) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylists(ownerId, offset, count)
                .flatMap(items -> {
                    List<VKApiAudioPlaylist> dtos = listEmptyIfNull(items.getItems());
                    List<AudioPlaylist> playlists = new ArrayList<>(dtos.size());

                    for (VKApiAudioPlaylist dto : dtos) {
                        if (Utils.safeCheck(dto.title, () -> dto.title.toLowerCase().contains(q.toLowerCase()))
                                || Utils.safeCheck(dto.artist_name, () -> dto.artist_name.toLowerCase().contains(q.toLowerCase()))
                                || Utils.safeCheck(dto.description, () -> dto.description.toLowerCase().contains(q.toLowerCase()))) {
                            playlists.add(Dto2Model.transform(dto));
                        }
                    }
                    int ld = loaded + playlists.size();

                    if (ld >= count || Utils.isEmpty(dtos)) {
                        return Single.just(new Pair<>(new FindAt(q, offset + count, Utils.isEmpty(dtos)), playlists));
                    }

                    return search_owner_playlist(accountId, q, ownerId, count, offset + count, ld).flatMap(t -> {
                        playlists.addAll(t.getSecond());
                        return Single.just(new Pair<>(t.getFirst(), playlists));

                    });
                });
    }

    @Override
    public Single<List<Audio>> loadLocalAudios(int accountId, Context context) {
        if (!AppPerms.hasReadWriteStoragePermision(context)) {
            AppPerms.requestReadWriteStoragePermission((Activity) context);
            return Single.just(new ArrayList<>());
        }
        File dir = new File(Settings.get().other().getMusicDir());
        if (dir.listFiles() == null || java.util.Objects.requireNonNull(dir.listFiles()).length <= 0)
            return Single.just(new ArrayList<>());
        ArrayList<File> files = new ArrayList<>();
        for (File file : java.util.Objects.requireNonNull(dir.listFiles())) {
            if (!file.isDirectory() && file.getName().contains(".mp3")) {
                files.add(file);
            }
        }
        if (files.isEmpty())
            return Single.just(new ArrayList<>());
        Collections.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        ArrayList<Audio> audios = new ArrayList<>(files.size());
        for (File file : files) {

            Audio rt = new Audio().setId(file.getAbsolutePath().hashCode()).setUrl("file://" + file.getAbsolutePath()).setIsLocal(true).setOwnerId(accountId);
            String TrackName = file.getName().replace(".mp3", "");
            String Artist = "";
            String[] arr = TrackName.split(" - ");
            if (arr.length > 1) {
                Artist = arr[0];
                TrackName = TrackName.replace(Artist + " - ", "");
            }
            rt.setArtist(Artist);
            rt.setTitle(TrackName);

            audios.add(rt);
        }
        return Single.just(audios);
    }
}