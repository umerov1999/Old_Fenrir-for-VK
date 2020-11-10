package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.adapter.CommentsAdapter;
import dev.ragnarok.fenrir.adapter.OwnersListAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommentsPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommentsView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.CommentsInputViewController;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelperComment;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;
import dev.ragnarok.fenrir.view.emoji.EmojiconsPopup;
import dev.ragnarok.fenrir.view.emoji.StickersKeyWordsAdapter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

public class CommentsFragment extends PlaceSupportMvpFragment<CommentsPresenter, ICommentsView> implements
        ICommentsView, EmojiconsPopup.OnStickerClickedListener, CommentsInputViewController.OnInputActionCallback,
        CommentsAdapter.OnCommentActionListener, EmojiconTextView.OnHashTagClickListener, BackPressCallback {

    private static final String EXTRA_AT_COMMENT_OBJECT = "at_comment_object";

    private static final String EXTRA_AT_COMMENT_THREAD = "at_comment_thread";

    private static final int REQUEST_CODE_ATTACHMENTS = 17;
    private static final int REQUEST_EDIT = 18;
    private CommentsInputViewController mInputController;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ViewGroup mReplyView;
    private TextView mReplyText;
    private LoadMoreFooterHelperComment upHelper;
    private LoadMoreFooterHelperComment downhelper;
    private CommentsAdapter mAdapter;
    private ProgressBar mCenterProgressBar;
    private View mEmptyView;
    private ImageView mAuthorAvatar;
    private AlertDialog mDeepLookingProgressDialog;
    private RecyclerView stickersKeywordsView;
    private StickersKeyWordsAdapter stickersAdapter;
    private boolean mCanSendCommentAsAdmin;
    private boolean mTopicPollAvailable;
    private boolean mGotoSourceAvailable;
    @StringRes
    private Integer mGotoSourceText;

    public static CommentsFragment newInstance(@NonNull Place place) {
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(place.getArgs());
        return fragment;
    }

    public static Bundle buildArgs(int accountId, Commented commented, Integer focusToComment, Integer CommentThread) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putParcelable(Extra.COMMENTED, commented);
        if (focusToComment != null) {
            bundle.putInt(EXTRA_AT_COMMENT_OBJECT, focusToComment);
        }
        if (CommentThread != null)
            bundle.putInt(EXTRA_AT_COMMENT_THREAD, CommentThread);

        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        stickersKeywordsView = root.findViewById(R.id.stickers);
        stickersAdapter = new StickersKeyWordsAdapter(requireActivity(), Collections.emptyList());
        stickersAdapter.setStickerClickedListener(stickerId -> {
            getPresenter().fireStickerClick(stickerId);
            getPresenter().resetDraftMessage();
        });
        stickersKeywordsView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        stickersKeywordsView.setAdapter(stickersAdapter);
        stickersKeywordsView.setVisibility(View.GONE);

        mAuthorAvatar = root.findViewById(R.id.author_avatar);

        mInputController = new CommentsInputViewController(requireActivity(), root, this);
        mInputController.setOnSickerClickListener(this);
        mInputController.setSendOnEnter(Settings.get().main().isSendByEnter());

        mLinearLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mReplyView = root.findViewById(R.id.fragment_comments_reply_container);
        mReplyText = root.findViewById(R.id.fragment_comments_reply_user);

        root.findViewById(R.id.fragment_comments_delete_reply).setOnClickListener(v -> getPresenter().fireReplyCancelClick());

        View loadUpView = inflater.inflate(R.layout.footer_load_more_comment, mRecyclerView, false);
        upHelper = LoadMoreFooterHelperComment.createFrom(loadUpView, () -> getPresenter().fireUpLoadMoreClick());
        upHelper.setEndOfListText(" ");

        View loadDownView = inflater.inflate(R.layout.footer_load_more_comment, mRecyclerView, false);
        downhelper = LoadMoreFooterHelperComment.createFrom(loadDownView, () -> getPresenter().fireDownLoadMoreClick());
        downhelper.setEndOfListTextRes(R.string.place_for_your_comment);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToTop();
            }
        });

        mAdapter = new CommentsAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.addHeader(loadDownView);
        mAdapter.addFooter(loadUpView);
        mAdapter.setListener(this);
        mAdapter.setOnHashTagClickListener(this);

        mRecyclerView.setAdapter(mAdapter);

        mCenterProgressBar = root.findViewById(R.id.progress_bar);
        mEmptyView = root.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public boolean onSendLongClick() {
        if (mCanSendCommentAsAdmin) {
            getPresenter().fireSendLongClick();
            return true;
        }

        return false;
    }

    @NotNull
    @Override
    public IPresenterFactory<CommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            Commented commented = requireArguments().getParcelable(Extra.COMMENTED);

            Integer focusTo = null;
            Integer ThreadComment = null;
            if (requireArguments().containsKey(EXTRA_AT_COMMENT_OBJECT)) {
                focusTo = requireArguments().getInt(EXTRA_AT_COMMENT_OBJECT);
                requireArguments().remove(EXTRA_AT_COMMENT_OBJECT);
            }

            if (requireArguments().containsKey(EXTRA_AT_COMMENT_THREAD)) {
                ThreadComment = requireArguments().getInt(EXTRA_AT_COMMENT_THREAD);
                requireArguments().remove(EXTRA_AT_COMMENT_THREAD);
            }

            return new CommentsPresenter(accountId, commented, focusTo, requireActivity(), ThreadComment, saveInstanceState);
        };
    }

    @Override
    public void displayData(List<Comment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(data);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupLoadUpHeader(int state) {
        if (nonNull(upHelper)) {
            upHelper.switchToState(state);
        }
    }

    @Override
    public void setupLoadDownFooter(int state) {
        if (nonNull(downhelper)) {
            downhelper.switchToState(state);
        }
    }

    @Override
    public void notifyDataAddedToTop(int count) {
        if (nonNull(mAdapter)) {
            int startSize = mAdapter.getRealItemCount();
            mAdapter.notifyItemRangeInserted(startSize + mAdapter.getHeadersCount(), count);
        }
    }

    @Override
    public void notifyDataAddedToBottom(int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(0);
            mAdapter.notifyItemRangeInserted(0, count + 1);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void moveFocusTo(int index, boolean smooth) {
        if (isNull(mAdapter)) {
            return;
        }

        int adapterPosition = index + mAdapter.getHeadersCount();
        if (smooth) {
            if (nonNull(mRecyclerView)) {
                mRecyclerView.smoothScrollToPosition(adapterPosition);
            }
        } else {
            if (nonNull(mLinearLayoutManager)) {
                mLinearLayoutManager.scrollToPosition(adapterPosition);
            }
        }
    }

    @Override
    public void displayBody(String body) {
        if (nonNull(mInputController)) {
            mInputController.setTextQuietly(body);
            getPresenter().fireTextEdited(body);
        }
    }

    @Override
    public void displayAttachmentsCount(int count) {
        if (nonNull(mInputController)) {
            mInputController.setAttachmentsCount(count);
        }
    }

    @Override
    public void setButtonSendAvailable(boolean available) {
        if (nonNull(mInputController)) {
            mInputController.setCanSendNormalMessage(available);
        }
    }

    @Override
    public void openAttachmentsManager(int accountId, Integer draftCommentId, int sourceOwnerId, String draftCommentBody) {
        PlaceFactory.getCommentCreatePlace(accountId, draftCommentId, sourceOwnerId, draftCommentBody)
                .targetTo(this, REQUEST_CODE_ATTACHMENTS)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void setupReplyViews(String replyTo) {
        if (nonNull(mReplyView)) {
            mReplyView.setVisibility(nonNull(replyTo) ? View.VISIBLE : View.GONE);
        }

        if (nonNull(replyTo) && nonNull(mReplyText)) {
            mReplyText.setText(replyTo);
        }
    }

    @Override
    public void replaceBodySelectionTextTo(String replyText) {
        if (nonNull(mInputController)) {
            TextInputEditText edit = mInputController.getInputField();

            int selectionStart = edit.getSelectionStart();
            int selectionEnd = edit.getSelectionEnd();
            edit.getText().replace(selectionStart, selectionEnd, replyText);
        }
    }

    @Override
    public void goToCommentEdit(int accountId, Comment comment, Integer commemtId) {
        PlaceFactory.getEditCommentPlace(accountId, comment, commemtId)
                .targetTo(this, REQUEST_EDIT)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void goToWallPost(int accountId, int postId, int postOwnerId) {
        PlaceFactory.getPostPreviewPlace(accountId, postId, postOwnerId).tryOpenWith(requireActivity());
    }

    @Override
    public void goToVideoPreview(int accountId, int videoId, int videoOwnerId) {
        PlaceFactory.getVideoPreviewPlace(accountId, videoOwnerId, videoId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void banUser(int accountId, int groupId, User user) {
        PlaceFactory.getCommunityAddBanPlace(accountId, groupId, Utils.singletonArrayList(user)).tryOpenWith(requireActivity());
    }

    @Override
    public void displayAuthorAvatar(String url) {
        if (nonNull(mAuthorAvatar)) {
            if (nonEmpty(url)) {
                mAuthorAvatar.setVisibility(View.VISIBLE);

                PicassoInstance.with()
                        .load(url)
                        .transform(new RoundTransformation())
                        .into(mAuthorAvatar);
            } else {
                mAuthorAvatar.setVisibility(View.GONE);
                PicassoInstance.with()
                        .cancelRequest(mAuthorAvatar);
            }
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (nonNull(mLinearLayoutManager) && nonNull(mAdapter)) {
            mLinearLayoutManager.scrollToPosition(position + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void showCommentSentToast() {
        showToast(R.string.toast_comment_sent, true);
    }

    @Override
    public void showAuthorSelectDialog(List<Owner> owners) {
        ArrayList<Owner> data = new ArrayList<>(owners);
        OwnersListAdapter adapter = new OwnersListAdapter(requireActivity(), data);
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_comment_author)
                .setAdapter(adapter, (dialog, which) -> getPresenter().fireAuthorSelected(data.get(which)))
                .setNegativeButton(R.string.button_cancel, null)
                .show();

    }

    @Override
    public void setupOptionMenu(boolean topicPollAvailable, boolean gotoSourceAvailable, Integer gotoSourceText) {
        mTopicPollAvailable = topicPollAvailable;
        mGotoSourceAvailable = gotoSourceAvailable;
        mGotoSourceText = gotoSourceText;

        try {
            requireActivity().invalidateOptionsMenu();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setEpmtyTextVisible(boolean visible) {
        if (nonNull(mEmptyView)) {
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setCenterProgressVisible(boolean visible) {
        if (nonNull(mCenterProgressBar)) {
            mCenterProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayDeepLookingCommentProgress() {
        mDeepLookingProgressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setCancelable(true).setCancelListener(dialog -> getPresenter().fireDeepLookingCancelledByUser()).build();
        mDeepLookingProgressDialog.show();
    }

    @Override
    public void dismissDeepLookingCommentProgress() {
        if (nonNull(mDeepLookingProgressDialog)) {
            mDeepLookingProgressDialog.dismiss();
        }
    }

    @Override
    public void setCanSendSelectAuthor(boolean can) {
        mCanSendCommentAsAdmin = can;
    }

    @Override
    public void updateStickers(List<Sticker> items) {
        if (Utils.isEmpty(items)) {
            stickersKeywordsView.setVisibility(View.GONE);
        } else {
            stickersKeywordsView.setVisibility(View.VISIBLE);
        }
        stickersAdapter.setData(items);
    }

    @Override
    public void onStickerClick(Sticker sticker) {
        getPresenter().fireStickerClick(sticker);
    }

    @Override
    public void onInputTextChanged(String s) {
        getPresenter().fireInputTextChanged(s);
        getPresenter().fireTextEdited(s);
    }

    @Override
    public void onSendClicked() {
        getPresenter().fireSendClick();
    }

    @Override
    public void onAttachClick() {
        getPresenter().fireAttachClick();
    }

    @Override
    public void onReplyToOwnerClick(int ownerId, int commentId) {
        getPresenter().fireReplyToOwnerClick(commentId);
    }

    @Override
    public void onRestoreComment(int commentId) {
        getPresenter().fireCommentRestoreClick(commentId);
    }

    @Override
    public void onAvatarClick(int ownerId) {
        onOpenOwner(ownerId);
    }

    @Override
    public void onCommentLikeClick(Comment comment, boolean add) {
        getPresenter().fireCommentLikeClick(comment, add);
    }

    @Override
    public void populateCommentContextMenu(ContextMenu menu, Comment comment) {
        ContextView contextView = new ContextView();
        getPresenter().fireCommentContextViewCreated(contextView, comment);

        menu.setHeaderTitle(comment.getFullAuthorName());

        menu.add(R.string.reply).setOnMenuItemClickListener(item -> {
            getPresenter().fireReplyToCommentClick(comment);
            return true;
        });

        menu.add(R.string.report).setOnMenuItemClickListener(item -> {
            getPresenter().fireReport(comment);
            return true;
        });

        if (contextView.canDelete) {
            menu.add(R.string.delete)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireCommentDeleteClick(comment);
                        return true;
                    });
        }

        if (contextView.canEdit) {
            menu.add(R.string.edit)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireCommentEditClick(comment);
                        return true;
                    });
        }

        if (contextView.canBan) {
            menu.add(R.string.ban_author)
                    .setOnMenuItemClickListener(item -> {
                        getPresenter().fireBanClick(comment);
                        return true;
                    });
        }

        menu.add(R.string.like)
                .setVisible(!comment.isUserLikes())
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireCommentLikeClick(comment, true);
                    return true;
                });

        menu.add(R.string.dislike)
                .setVisible(comment.isUserLikes())
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireCommentLikeClick(comment, false);
                    return true;
                });

        menu.add(R.string.who_likes)
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireWhoLikesClick(comment);
                    return true;
                });

        menu.add(R.string.send_to_friend)
                .setOnMenuItemClickListener(item -> {
                    getPresenter().fireReplyToChat(comment);
                    return true;
                });
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        getPresenter().fireHashtagClick(hashTag);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ATTACHMENTS) {
            String body = data.getStringExtra(Extra.BODY);
            postPrenseterReceive(presenter -> presenter.fireEditBodyResult(body));
        }

        if (requestCode == REQUEST_EDIT && resultCode == Activity.RESULT_OK) {
            Comment comment = data.getParcelableExtra(Extra.COMMENT);

            if (nonNull(comment)) {
                postPrenseterReceive(presenter -> presenter.fireCommentEditResult(comment));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.comments_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.open_poll).setVisible(mTopicPollAvailable);

        MenuItem gotoSource = menu.findItem(R.id.to_commented);
        gotoSource.setVisible(mGotoSourceAvailable);

        if (mGotoSourceAvailable) {
            gotoSource.setTitle(mGotoSourceText);
        }

        boolean desc = Settings.get().other().isCommentsDesc();
        menu.findItem(R.id.direction).setIcon(getDirectionIcon(desc));
    }

    @DrawableRes
    private int getDirectionIcon(boolean desc) {
        return desc ? R.drawable.double_up : R.drawable.double_down;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getPresenter().fireRefreshClick();
                return true;
            case R.id.open_poll:
                getPresenter().fireTopicPollClick();
                return true;
            case R.id.to_commented:
                getPresenter().fireGotoSourceClick();
                return true;
            case R.id.direction:
                boolean decs = Settings.get().other().toggleCommentsDirection();
                item.setIcon(getDirectionIcon(decs));
                getPresenter().fireDirectionChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        return mInputController.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInputController.destroyView();
        mInputController = null;
    }

    private static final class ContextView implements ICommentContextView {

        boolean canEdit;

        boolean canDelete;

        boolean canBan;

        @Override
        public void setCanEdit(boolean can) {
            canEdit = can;
        }

        @Override
        public void setCanDelete(boolean can) {
            canDelete = can;
        }

        @Override
        public void setCanBan(boolean can) {
            canBan = can;
        }
    }
}
