package dev.ragnarok.fenrir.api.model.longpoll;

public class WriteTextInDialogUpdate extends AbsLongpollEvent {

    public int user_id;
    public int chat_id;
    public int flags;

    public WriteTextInDialogUpdate() {
        super(ACTION_USER_WRITE_TEXT_IN_DIALOG);
    }

    public int getUserId() {
        return user_id;
    }
}