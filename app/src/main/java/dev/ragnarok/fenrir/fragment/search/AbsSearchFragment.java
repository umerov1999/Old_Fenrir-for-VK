package dev.ragnarok.fenrir.fragment.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.fragment.search.options.BaseOption;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.mvp.presenter.search.AbsSearchPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IBaseSearchView;
import dev.ragnarok.fenrir.util.ViewUtils;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

public abstract class AbsSearchFragment<P extends AbsSearchPresenter<V, ?, T, ?>, V extends IBaseSearchView<T>, T, A extends RecyclerView.Adapter<?>>
        extends PlaceSupportMvpFragment<P, V> implements IBaseSearchView<T> {
    public A mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyText;

    private void onSeachOptionsChanged() {
        getPresenter().fireOptionsChanged();
    }

    public View createViewLayout(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = createViewLayout(inflater, container);

        RecyclerView recyclerView = root.findViewById(R.id.list);
        RecyclerView.LayoutManager manager = createLayoutManager();
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = createAdapter(Collections.emptyList());
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmptyText = root.findViewById(R.id.empty);
        mEmptyText.setText(getEmptyText());
        postCreate(root);
        return root;
    }

    @StringRes
    int getEmptyText() {
        return R.string.list_is_empty;
    }

    public void fireTextQueryEdit(String q) {
        getPresenter().fireTextQueryEdit(q);
    }

    @Override
    public void displayData(List<T> data) {
        if (nonNull(mAdapter)) {
            setAdapterData(mAdapter, data);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    public void openSearchFilter() {
        getPresenter().fireOpenFilterClick();
    }

    @Override
    public void displayFilter(int accountId, ArrayList<BaseOption> options) {
        FilterEditFragment fragment = FilterEditFragment.newInstance(accountId, options);
        getParentFragmentManager().setFragmentResultListener(FilterEditFragment.REQUEST_FILTER_EDIT, fragment, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onSeachOptionsChanged();
            }
        });
        fragment.show(getParentFragmentManager(), "filter-edit");
    }

    abstract void setAdapterData(A adapter, List<T> data);

    abstract void postCreate(View root);

    abstract A createAdapter(List<T> data);

    abstract RecyclerView.LayoutManager createLayoutManager();
}
