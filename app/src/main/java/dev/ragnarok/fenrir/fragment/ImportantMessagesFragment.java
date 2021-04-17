package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.adapter.MessagesAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.ImportantMessagesPresenter;
import dev.ragnarok.fenrir.mvp.view.IImportantMessagesView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.ViewUtils;

public class ImportantMessagesFragment extends PlaceSupportMvpFragment<ImportantMessagesPresenter, IImportantMessagesView>
        implements MessagesAdapter.OnMessageActionListener, IImportantMessagesView, AttachmentsViewBinder.VoiceActionListener {

    private final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            getPresenter().fireRemoveImportant(viewHolder.getBindingAdapterPosition());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    };
    private MessagesAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static ImportantMessagesFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        ImportantMessagesFragment mFragment = new ImportantMessagesFragment();
        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_important_msgs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);

        mAdapter = new MessagesAdapter(requireActivity(), Collections.emptyList(), this, true);
        mAdapter.setOnMessageActionListener(this);
        mAdapter.setVoiceActionListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setSubtitle(null);
            actionBar.setTitle(R.string.important);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        onOpenOwner(userId);
    }

    @Override
    public void onLongAvatarClick(@NonNull Message message, int userId) {
        onOpenOwner(userId);
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
    }

    @Override
    public void onBotKeyboardClick(@NonNull Keyboard.Button button) {
    }

    @Override
    public void goToMessagesLookup(int accountId, int peerId, int messageId) {
        PlaceFactory.getMessagesLookupPlace(accountId, peerId, messageId, null).tryOpenWith(requireActivity());
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        getPresenter().fireForwardClick();
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessagesLookup(message);
    }

    @Override
    public void onMessageDelete(@NonNull Message message) {

    }

    @Override
    public void displayMessages(@NonNull List<Message> messages, @NonNull LastReadId lastReadId) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(messages, lastReadId);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, new FwdMessages(messages));
    }

    @Override
    public void notifyMessagesUpAdded(int position, int count) {
        // not supported
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        // not supported
    }

    @Override
    public void configNowVoiceMessagePlaying(int voiceId, float progress, boolean paused, boolean amin) {
        if (nonNull(mAdapter)) {
            mAdapter.configNowVoiceMessagePlaying(voiceId, progress, paused, amin);
        }
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        if (nonNull(mAdapter)) {
            mAdapter.bindVoiceHolderById(holderId, play, paused, progress, amin);
        }
    }

    @Override
    public void disableVoicePlaying() {
        if (nonNull(mAdapter)) {
            mAdapter.disableVoiceMessagePlaying();
        }
    }

    @Override
    public void showActionMode(String title, Boolean canEdit, Boolean canPin, Boolean canStar, Boolean doStar) {
        // not supported
    }

    @Override
    public void finishActionMode() {
        // not supported
    }

    @NonNull
    @Override
    public IPresenterFactory<ImportantMessagesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            return new ImportantMessagesPresenter(accountId, saveInstanceState);
        };
    }

    @Override
    public void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId) {
        getPresenter().fireVoiceHolderCreated(voiceMessageId, voiceHolderId);
    }

    @Override
    public void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage) {
        getPresenter().fireVoicePlayButtonClick(voiceHolderId, voiceMessageId, voiceMessage);
    }

    @Override
    public void onTranscript(String voiceMessageId, int messageId) {
        getPresenter().fireTranscript(voiceMessageId, messageId);
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }
}
