package dev.ragnarok.fenrir.api.model.longpoll;

public class UserIsOfflineUpdate extends AbsLongpollEvent {

    public int user_id;
    public int flags;

    public UserIsOfflineUpdate() {
        super(ACTION_USER_IS_OFFLINE);
    }

    public int getUserId() {
        return user_id;
    }

    public int getFlags() {
        return flags;
    }
}