package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.MessagesLookPresenter;
import dev.ragnarok.fenrir.mvp.view.IMessagesLookView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;

public class MessagesLookFragment extends PlaceSupportMvpFragment<MessagesLookPresenter, IMessagesLookView>
        implements IMessagesLookView, MessagesAdapter.OnMessageActionListener, BackPressCallback {

    private static final String TAG = MessagesLookFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RelativeLayout mActionRoot;
    private MessagesAdapter mMessagesAdapter;
    private View mHeaderView;
    private View mFooterView;
    private LoadMoreFooterHelper mHeaderHelper;
    private LoadMoreFooterHelper mFooterHelper;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private ActionModeHolder mActionView;

    public static Bundle buildArgs(int accountId, int peerId, int focusMessageId, @Nullable Message message) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.FOCUS_TO, focusMessageId);
        if (nonNull(message)) {
            args.putParcelable(Extra.MESSAGE, message);
        }
        return args;
    }

    public static MessagesLookFragment newInstance(Bundle args) {
        MessagesLookFragment fragment = new MessagesLookFragment();
        fragment.setArguments(args);
        return fragment;
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
        mActionRoot = root.findViewById(R.id.action_mode);

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
        mMessagesAdapter = new MessagesAdapter(requireActivity(), messages, lastReadId, this, true);
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
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemRangeInserted(startPosition + 1, count); //+header
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        if (nonNull(mMessagesAdapter)) {
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
        if (mActionRoot == null) {
            return;
        }
        if (mActionRoot.getChildCount() == 0) {
            mActionView = new ActionModeHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.view_action_mode, mActionRoot, false));
            mActionRoot.addView(mActionView.rootView);
        }
        if (Settings.get().main().isMessages_menu_down()) {
            ((RelativeLayout.LayoutParams) mActionView.rootView.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            ((RelativeLayout.LayoutParams) mActionView.rootView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            ((RelativeLayout.LayoutParams) mActionView.rootView.getLayoutParams()).removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            ((RelativeLayout.LayoutParams) mActionView.rootView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }

        mActionView.show();
        mActionView.titleView.setText(title);
        mActionView.buttonEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        mActionView.buttonPin.setVisibility(canPin ? View.VISIBLE : View.GONE);
        mActionView.buttonStar.setVisibility(canStar ? View.VISIBLE : View.GONE);
        mActionView.buttonStar.setImageResource(doStar ? R.drawable.star_add : R.drawable.star_none);
    }

    @Override
    public void finishActionMode() {
        if (nonNull(mActionView)) {
            mActionView.hide();
        }
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState) {
        if (nonNull(mFooterHelper)) {
            mFooterHelper.switchToState(upHeaderState);
        }

        if (nonNull(mHeaderHelper)) {
            mHeaderHelper.switchToState(downHeaderState);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, new FwdMessages(messages));
    }

    @NotNull
    @Override
    public IPresenterFactory<MessagesLookPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            int focusTo = requireArguments().getInt(Extra.FOCUS_TO);
            Message message = requireArguments().containsKey(Extra.MESSAGE) ? requireArguments().getParcelable(Extra.MESSAGE) : null;
            return new MessagesLookPresenter(aid, peerId, focusTo, message, saveInstanceState);
        };
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        if (nonNull(mActionView) && mActionView.isVisible()) {
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onLongAvatarClick(@NonNull Message message, int userId) {
        if (nonNull(mActionView) && mActionView.isVisible()) {
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
    public boolean onBackPressed() {
        if (nonNull(mActionView) && mActionView.isVisible()) {
            mActionView.hide();
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.viewing_messages);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    class ActionModeHolder implements View.OnClickListener {
        public View buttonClose;
        public View rootView;
        public View buttonEdit;
        public View buttonForward;
        public View buttonCopy;
        public View buttonDelete;
        public View buttonPin;
        public ImageView buttonStar;
        public TextView titleView;

        public ActionModeHolder(View rootView) {
            this.rootView = rootView;
            buttonClose = rootView.findViewById(R.id.buttonClose);
            buttonEdit = rootView.findViewById(R.id.buttonEdit);
            buttonForward = rootView.findViewById(R.id.buttonForward);
            buttonCopy = rootView.findViewById(R.id.buttonCopy);
            buttonDelete = rootView.findViewById(R.id.buttonDelete);
            buttonPin = rootView.findViewById(R.id.buttonPin);
            buttonStar = rootView.findViewById(R.id.buttonStar);
            titleView = rootView.findViewById(R.id.actionModeTitle);

            buttonClose.setOnClickListener(this);
            buttonEdit.setOnClickListener(this);
            buttonForward.setOnClickListener(this);
            buttonCopy.setOnClickListener(this);
            buttonDelete.setOnClickListener(this);
            buttonPin.setOnClickListener(this);
            buttonStar.setOnClickListener(this);
        }

        public void show() {
            rootView.setVisibility(View.VISIBLE);
        }

        public boolean isVisible() {
            return rootView.getVisibility() == View.VISIBLE;
        }

        public void hide() {
            rootView.setVisibility(View.GONE);
            getPresenter().fireActionModeDestroy();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonClose:
                    hide();
                    break;
                case R.id.buttonForward:
                    getPresenter().fireForwardClick();
                    hide();
                    break;
                case R.id.buttonCopy:
                    getPresenter().fireActionModeCopyClick();
                    hide();
                    break;
                case R.id.buttonDelete:
                    getPresenter().fireActionModeDeleteClick();
                    hide();
                    break;
            }
        }
    }
}
