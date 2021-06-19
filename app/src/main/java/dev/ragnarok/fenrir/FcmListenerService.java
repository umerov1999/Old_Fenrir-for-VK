package dev.ragnarok.fenrir;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.push.IPushRegistrationResolver;
import dev.ragnarok.fenrir.push.PushType;
import dev.ragnarok.fenrir.push.message.BirthdayFCMMessage;
import dev.ragnarok.fenrir.push.message.CommentFCMMessage;
import dev.ragnarok.fenrir.push.message.FCMMessage;
import dev.ragnarok.fenrir.push.message.FriendAcceptedFCMMessage;
import dev.ragnarok.fenrir.push.message.FriendFCMMessage;
import dev.ragnarok.fenrir.push.message.GroupInviteFCMMessage;
import dev.ragnarok.fenrir.push.message.LikeFCMMessage;
import dev.ragnarok.fenrir.push.message.NewPostPushMessage;
import dev.ragnarok.fenrir.push.message.ReplyFCMMessage;
import dev.ragnarok.fenrir.push.message.WallPostFCMMessage;
import dev.ragnarok.fenrir.push.message.WallPublishFCMMessage;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.RxUtils;

public class FcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "FcmListenerService";

    @SuppressLint("CheckResult")
    @WorkerThread
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Injection.providePushRegistrationResolver()
                .resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore());
    }

    @Override
    @WorkerThread
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Context context = getApplicationContext();
        Logger.d(TAG, message.getData().size() > 0 ? "Data-notification" : "Notification-notification");

        String pushType = message.getData().get("type");

        Logger.d(TAG, "onMessage, from: " + message.getFrom() + ", pushType: " + pushType + ", data: " + message.getData());
        if (isEmpty(pushType) || Settings.get().other().isSettings_no_push()) {
            return;
        }

        StringBuilder bundleDump = new StringBuilder();
        for (Map.Entry<String, String> entry : message.getData().entrySet()) {
            try {
                String line = "key: " + entry.getKey() + ", value: " + entry.getValue() + ", class: " + (entry.getValue() == null ? "null" : entry.getValue().getClass());
                Logger.d(TAG, line);
                bundleDump.append("\n").append(line);
            } catch (Exception ignored) {
            }
        }

        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        if (!registrationResolver.canReceivePushNotification()) {
            Logger.d(TAG, "Invalid push registration on VK");
            return;
        }

        /*
        if(Settings.get().other().isDebug_mode() && !pushType.equals(PushType.MSG) && !pushType.equals("chat") && !pushType.equals("erase") && !pushType.equals(PushType.VALIDATE_DEVICE)) {
            PersistentLogger.logThrowable("Push issues", new Exception("Found Push event, key: " + pushType + ", dump: " + bundleDump));
        }
         */

        try {
            switch (pushType) {
                case PushType.VALIDATE_DEVICE:
                case PushType.MSG:
                case "chat":
                    FCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.POST:
                    WallPostFCMMessage.fromRemoteMessage(message).nofify(context, accountId);
                    break;
                case PushType.COMMENT:
                    CommentFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.FRIEND:
                    FriendFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.NEW_POST:
                    new NewPostPushMessage(accountId, message).notifyIfNeed(context);
                    break;
                case PushType.LIKE:
                    new LikeFCMMessage(accountId, message).notifyIfNeed(context);
                    break;

                case PushType.REPLY:
                    ReplyFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.WALL_PUBLISH:
                    WallPublishFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.FRIEND_ACCEPTED:
                    FriendAcceptedFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.GROUP_INVITE:
                    GroupInviteFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.BIRTHDAY:
                    BirthdayFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.SHOW_MESSAGE:
                    NotificationHelper.showSimpleNotification(context, message.getData().get("body"), message.getData().get("title"), null);
                    break;
                default:
                    //PersistentLogger.logThrowable("Push issues", new Exception("Unespected Push event, collapse_key: " + pushType + ", dump: " + bundleDump));
                    break;
            }
        } catch (Exception e) {
            PersistentLogger.logThrowable("Push issues", e);
            e.printStackTrace();
        }
    }
}