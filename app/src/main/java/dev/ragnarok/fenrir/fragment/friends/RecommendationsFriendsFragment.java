package dev.ragnarok.fenrir.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.RecommendationsFriendsPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;

public class RecommendationsFriendsFragment extends AbsOwnersListFragment<RecommendationsFriendsPresenter, ISimpleOwnersView> {

    private boolean isRequested;

    public static RecommendationsFriendsFragment newInstance(int accountId, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.USER_ID, userId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        RecommendationsFriendsFragment friendsFragment = new RecommendationsFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRequested) {
            isRequested = true;
            getPresenter().doLoad();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<RecommendationsFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RecommendationsFriendsPresenter(
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }
}
