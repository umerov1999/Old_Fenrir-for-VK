package dev.ragnarok.fenrir.fragment.wallattachments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.WallAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.wallattachments.WallPostQueryAttachmentsPresenter;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallPostQueryAttachmentsView;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MySearchView;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

public class WallPostQueryAttachmentsFragment extends PlaceSupportMvpFragment<WallPostQueryAttachmentsPresenter, IWallPostQueryAttachmentsView>
        implements IWallPostQueryAttachmentsView, WallAdapter.ClickListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WallAdapter mAdapter;
    private FloatingActionButton mLoadMore;

    public static WallPostQueryAttachmentsFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        WallPostQueryAttachmentsFragment fragment = new WallPostQueryAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wall_attachments_query, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.empty);
        mLoadMore = root.findViewById(R.id.goto_button);

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getPresenter().fireSearchRequestChanged(query, false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getPresenter().fireSearchRequestChanged(newText, true);
                return false;
            }
        });

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });
        mLoadMore.setOnClickListener(v -> getPresenter().fireScrollToEnd());

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new WallAdapter(requireActivity(), Collections.emptyList(), this, this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayData(List<Post> posts) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(posts);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<WallPostQueryAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new WallPostQueryAttachmentsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void setToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onSetLoadingStatus(int isLoad) {
        switch (isLoad) {
            case 1:
                mLoadMore.setImageResource(R.drawable.audio_died);
                break;
            case 2:
                mLoadMore.setImageResource(R.drawable.view);
                break;
            default:
                mLoadMore.setImageResource(R.drawable.ic_arrow_down);
                break;
        }
    }

    @Override
    public void onAvatarClick(int ownerId) {
        onOwnerClick(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        getPresenter().fireShareClick(post);
    }

    @Override
    public void onPostClick(Post post) {
        getPresenter().firePostBodyClick(post);
    }

    @Override
    public void onRestoreClick(Post post) {
        getPresenter().firePostRestoreClick(post);
    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireLikeLongClick(post);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireShareLongClick(post);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }

    @Override
    public void openPostEditor(int accountId, Post post) {
        PlaceUtil.goToPostEditor(requireActivity(), accountId, post);
    }
}
