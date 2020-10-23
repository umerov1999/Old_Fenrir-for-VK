package dev.ragnarok.fenrir.model.menu;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioItem.play_item_audio,
        AudioItem.add_item_audio,
        AudioItem.save_item_audio,
        AudioItem.get_recommendation_by_audio,
        AudioItem.open_album,
        AudioItem.get_lyrics_menu,
        AudioItem.copy_url,
        AudioItem.bitrate_item_audio,
        AudioItem.search_by_artist,
        AudioItem.share_button,
        AudioItem.add_and_download_button,
        AudioItem.goto_artist,
        AudioItem.edit_track})
@Retention(RetentionPolicy.SOURCE)
public @interface AudioItem {
    int play_item_audio = 1;
    int add_item_audio = 2;
    int save_item_audio = 3;
    int get_recommendation_by_audio = 4;
    int open_album = 5;
    int get_lyrics_menu = 6;
    int copy_url = 7;
    int bitrate_item_audio = 8;
    int search_by_artist = 9;
    int share_button = 10;
    int add_and_download_button = 11;
    int goto_artist = 12;
    int edit_track = 13;
}

