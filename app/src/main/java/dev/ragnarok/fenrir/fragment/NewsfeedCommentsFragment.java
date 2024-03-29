package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isLandscape;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.NewsfeedCommentsAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.NewsfeedComment;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.NewsfeedCommentsPresenter;
import dev.ragnarok.fenrir.mvp.view.INewsfeedCommentsView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class NewsfeedCommentsFragment extends PlaceSupportMvpFragment<NewsfeedCommentsPresenter, INewsfeedCommentsView>
        implements INewsfeedCommentsView, NewsfeedCommentsAdapter.ActionListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsfeedCommentsAdapter mAdapter;

    public static NewsfeedCommentsFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsfeedCommentsFragment fragment = new NewsfeedCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_newsfeed_comments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(NewsfeedCommentsPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(requireActivity())) {
            manager = new StaggeredGridLayoutManager(isLandscape(requireActivity()) ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity());
        }

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(NewsfeedCommentsPresenter::fireScrollToEnd);
            }
        });

        mAdapter = new NewsfeedCommentsAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.setActionListener(this);
        mAdapter.setOwnerClickListener(this);

        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<NewsfeedCommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            return new NewsfeedCommentsPresenter(accountId, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.NEWSFEED_COMMENTS);

        ActivityUtils.setToolbarTitle(this, R.string.drawer_newsfeed_comments);
        ActivityUtils.setToolbarSubtitle(this, null);

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_NEWSFEED_COMMENTS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayData(List<NewsfeedComment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void onPostBodyClick(NewsfeedComment comment) {
        callPresenter(p -> p.firePostClick((Post) comment.getModel()));
    }

    @Override
    public void onCommentBodyClick(NewsfeedComment comment) {
        callPresenter(p -> p.fireCommentBodyClick(comment));
    }
}
