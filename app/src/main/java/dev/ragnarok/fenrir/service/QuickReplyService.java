package dev.ragnarok.fenrir.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.RemoteInput;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.SaveMessageBuilder;
import dev.ragnarok.fenrir.util.RxUtils;

public class QuickReplyService extends IntentService {

    public static final String ACTION_ADD_MESSAGE = "SendService.ACTION_ADD_MESSAGE";
    public static final String ACTION_MARK_AS_READ = "SendService.ACTION_MARK_AS_READ";

    public QuickReplyService() {
        super(QuickReplyService.class.getName());
    }

    public static Intent intentForAddMessage(Context context, int accountId, int peerId, Message msg) {
        Intent intent = new Intent(context, QuickReplyService.class);
        intent.setAction(ACTION_ADD_MESSAGE);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE, msg);
        return intent;
    }

    public static Intent intentForReadMessage(Context context, int accountId, int peerId, int msgId) {
        Intent intent = new Intent(context, QuickReplyService.class);
        intent.setAction(ACTION_MARK_AS_READ);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE_ID, msgId);
        return intent;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && ACTION_ADD_MESSAGE.equals(intent.getAction()) && intent.getExtras() != null) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            Bundle msg = RemoteInput.getResultsFromIntent(intent);

            if (msg != null) {
                CharSequence body = msg.getCharSequence(Extra.BODY);
                addMessage(accountId, peerId, body == null ? null : body.toString());
            }
        } else if (intent != null && ACTION_MARK_AS_READ.equals(intent.getAction()) && intent.getExtras() != null) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            int msgId = intent.getExtras().getInt(Extra.MESSAGE_ID);
            Repository.INSTANCE.getMessages().markAsRead(accountId, peerId, msgId).blockingSubscribe(RxUtils.dummy(), RxUtils.ignore());
        }
    }

    private void addMessage(int accountId, int peerId, String body) {
        IMessagesRepository messagesInteractor = Repository.INSTANCE.getMessages();
        SaveMessageBuilder builder = new SaveMessageBuilder(accountId, peerId).setBody(body);
        messagesInteractor.put(builder).blockingSubscribe();
        Repository.INSTANCE.getMessages().runSendingQueue();
    }
}
