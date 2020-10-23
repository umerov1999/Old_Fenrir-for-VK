package dev.ragnarok.fenrir.fragment.search;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SearchContentType.PEOPLE, SearchContentType.COMMUNITIES, SearchContentType.NEWS,
        SearchContentType.AUDIOS, SearchContentType.VIDEOS, SearchContentType.MESSAGES, SearchContentType.DOCUMENTS,
        SearchContentType.WALL, SearchContentType.DIALOGS, SearchContentType.AUDIOS_SELECT, SearchContentType.AUDIO_PLAYLISTS})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchContentType {
    int PEOPLE = 0;
    int COMMUNITIES = 1;
    int NEWS = 2;
    int AUDIOS = 3;
    int VIDEOS = 4;
    int MESSAGES = 5;
    int DOCUMENTS = 6;
    int WALL = 7;
    int DIALOGS = 8;
    int AUDIOS_SELECT = 9;
    int AUDIO_PLAYLISTS = 11;
}
