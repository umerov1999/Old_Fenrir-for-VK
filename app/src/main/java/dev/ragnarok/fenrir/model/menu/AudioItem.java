package dev.ragnarok.fenrir.model.menu;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioItem.play_item_audio,
        AudioItem.play_item_after_current_audio,
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
    int play_item_after_current_audio = 2;
    int add_item_audio = 3;
    int save_item_audio = 4;
    int get_recommendation_by_audio = 5;
    int open_album = 6;
    int get_lyrics_menu = 7;
    int copy_url = 8;
    int bitrate_item_audio = 9;
    int search_by_artist = 10;
    int share_button = 11;
    int add_and_download_button = 12;
    int goto_artist = 13;
    int edit_track = 14;
}

