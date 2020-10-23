package dev.ragnarok.fenrir.api.model.longpoll;

public class UserIsOnlineUpdate extends AbsLongpollEvent {

    public int user_id;
    public int extra;

    public UserIsOnlineUpdate() {
        super(ACTION_USER_IS_ONLINE);
    }

    public int getUserId() {
        return user_id;
    }
}