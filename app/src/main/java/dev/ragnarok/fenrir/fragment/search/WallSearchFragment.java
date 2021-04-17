package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.WallAdapter;
import dev.ragnarok.fenrir.fragment.search.criteria.WallSearchCriteria;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.search.WallSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IWallSearchView;
import dev.ragnarok.fenrir.util.Utils;

public class WallSearchFragment extends AbsSearchFragment<WallSearchPresenter, IWallSearchView, Post, WallAdapter>
        implements IWallSearchView, WallAdapter.ClickListener {

    public static WallSearchFragment newInstance(int accountId, WallSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        WallSearchFragment fragment = new WallSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<WallSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            WallSearchCriteria c = getArguments().getParcelable(Extra.CRITERIA);
            return new WallSearchPresenter(accountId, c, saveInstanceState);
        };
    }

    @Override
    void setAdapterData(WallAdapter adapter, List<Post> data) {
        adapter.setItems(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    WallAdapter createAdapter(List<Post> data) {
        return new WallAdapter(requireActivity(), data, this, this);
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        RecyclerView.LayoutManager manager;

        if (Utils.is600dp(requireActivity())) {
            boolean land = Utils.isLandscape(requireActivity());
            manager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        }

        return manager;
    }

    @Override
    public void onOwnerClick(int ownerId) {
        getPresenter().fireOwnerClick(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        getPresenter().fireShareClick(post);
    }

    @Override
    public void onPostClick(Post post) {
        getPresenter().firePostClick(post);
    }

    @Override
    public void onRestoreClick(Post post) {

    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireShowLikesClick(post);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireShowCopiesClick(post);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }
}
