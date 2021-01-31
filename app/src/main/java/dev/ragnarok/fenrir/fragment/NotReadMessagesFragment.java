package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.MessagesAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.NotReadMessagesPresenter;
import dev.ragnarok.fenrir.mvp.view.INotReadMessagesView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;

public class NotReadMessagesFragment extends PlaceSupportMvpFragment<NotReadMessagesPresenter, INotReadMessagesView>
        implements INotReadMessagesView, MessagesAdapter.OnMessageActionListener {

    private static final String TAG = NotReadMessagesFragment.class.getSimpleName();
    private final ActionModeCallback mActionModeCallback = new ActionModeCallback();
    private RecyclerView mRecyclerView;
    private MessagesAdapter mMessagesAdapter;
    private View mHeaderView;
    private View mFooterView;
    private LoadMoreFooterHelper mHeaderHelper;
    private LoadMoreFooterHelper mFooterHelper;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private ActionMode mActionMode;
    private int unreadCount;

    public static Bundle buildArgs(int accountId, int peerId, int focusMessageId, int incoming, int outgoing, int unreadCount) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.FOCUS_TO, focusMessageId);
        args.putInt(Extra.INCOMING, incoming);
        args.putInt(Extra.OUTGOING, outgoing);
        args.putInt(Extra.COUNT, unreadCount);
        return args;
    }

    public static NotReadMessagesFragment newInstance(Bundle args) {
        NotReadMessagesFragment fragment = new NotReadMessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        unreadCount = requireArguments().getInt(Extra.COUNT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_messages_lookup, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(layoutManager);

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mFooterView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mHeaderHelper = LoadMoreFooterHelper.createFrom(mHeaderView, this::onHeaderLoadMoreClick);
        mFooterHelper = LoadMoreFooterHelper.createFrom(mFooterView, this::onFooterLoadMoreClick);

        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                onFooterLoadMoreClick();
            }

            @Override
            public void onScrollToFirstElement() {
                onHeaderLoadMoreClick();
            }
        };

        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
        return root;
    }

    @Override
    public void showDeleteForAllDialog(ArrayList<Integer> ids) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.messages_delete_for_all_question_message)
                .setNeutralButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_for_all, (dialog, which) -> getPresenter().fireDeleteForAllClick(ids))
                .setNegativeButton(R.string.button_for_me, (dialog, which) -> getPresenter().fireDeleteForMeClick(ids))
                .show();
    }

    private void onFooterLoadMoreClick() {
        getPresenter().fireFooterLoadMoreClick();
    }

    private void onHeaderLoadMoreClick() {
        getPresenter().fireHeaderLoadMoreClick();
    }

    @Override
    public void displayMessages(@NonNull List<Message> messages, @NonNull LastReadId lastReadId) {
        mMessagesAdapter = new MessagesAdapter(requireActivity(), messages, lastReadId, this, false);
        mMessagesAdapter.setOnMessageActionListener(this);
        mMessagesAdapter.addFooter(mFooterView);
        mMessagesAdapter.addHeader(mHeaderView);
        mRecyclerView.setAdapter(mMessagesAdapter);
    }

    @Override
    public void focusTo(int index) {
        mRecyclerView.removeOnScrollListener(mEndlessRecyclerOnScrollListener);
        mRecyclerView.scrollToPosition(index + 1); // +header
        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    @Override
    public void notifyMessagesUpAdded(int startPosition, int count) {
        if (Objects.nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemRangeInserted(startPosition + 1, count); //+header
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        if (Objects.nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemRemoved(0);
            mMessagesAdapter.notifyItemRangeInserted(0, count + 1); //+header
        }
    }

    @Override
    public void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin) {
        mMessagesAdapter.configNowVoiceMessagePlaying(id, progress, paused, amin);
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        mMessagesAdapter.bindVoiceHolderById(holderId, play, paused, progress, amin);
    }

    @Override
    public void disableVoicePlaying() {
        mMessagesAdapter.disableVoiceMessagePlaying();
    }

    @Override
    public void showActionMode(String title, Boolean canEdit, Boolean canPin, Boolean canStar, Boolean doStar) {
        if (Objects.isNull(mActionMode)) {
            mActionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(mActionModeCallback);
        }

        if (Objects.nonNull(mActionMode)) {
            mActionMode.setTitle(title);
            mActionMode.invalidate();
        }
    }

    @Override
    public void finishActionMode() {
        if (Objects.nonNull(mActionMode)) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void notifyDataChanged() {
        if (Objects.nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState) {
        if (Objects.nonNull(mFooterHelper)) {
            mFooterHelper.switchToState(upHeaderState);
        }

        if (Objects.nonNull(mHeaderHelper)) {
            mHeaderHelper.switchToState(downHeaderState);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, new FwdMessages(messages));
    }

    public void fireFinish() {
        getPresenter().fireFinish();
    }

    @Override
    public void doFinish(int incoming, int outgoing, boolean notAnim) {
        Intent intent = new Intent();
        intent.putExtra(Extra.INCOMING, incoming);
        intent.putExtra(Extra.OUTGOING, outgoing);
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
        if (notAnim) {
            requireActivity().overridePendingTransition(0, 0);
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<NotReadMessagesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            Integer focusTo = requireArguments().containsKey(Extra.FOCUS_TO) ? requireArguments().getInt(Extra.FOCUS_TO) : null;
            int incoming = requireArguments().getInt(Extra.INCOMING);
            int outgoing = requireArguments().getInt(Extra.OUTGOING);
            return new NotReadMessagesPresenter(aid, peerId, focusTo, incoming, outgoing, saveInstanceState);
        };
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        if (Objects.nonNull(mActionMode)) {
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onLongAvatarClick(@NonNull Message message, int userId) {
        if (Objects.nonNull(mActionMode)) {
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        getPresenter().fireMessageRestoreClick(message, position);
    }

    @Override
    public void onBotKeyboardClick(@NonNull @NotNull Keyboard.Button button) {

    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        getPresenter().fireMessageLongClick(message);
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public void onMessageDelete(@NonNull Message message) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(message.getId());
        getPresenter().fireDeleteForMeClick(ids);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(R.string.not_read);
            actionBar.setSubtitle(String.valueOf(unreadCount));
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    getPresenter().fireActionModeDeleteClick();
                    break;
                case R.id.copy:
                    getPresenter().fireActionModeCopyClick();
                    break;
                case R.id.forward:
                    getPresenter().fireForwardClick();
                    break;
            }

            mode.finish();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getPresenter().fireActionModeDestroy();
            mActionMode = null;
        }
    }
}
