package dev.ragnarok.fenrir.fragment.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.fragment.AbsOwnersListFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.FollowersPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;

public class FollowersFragment extends AbsOwnersListFragment<FollowersPresenter, ISimpleOwnersView> {
    public static FollowersFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.USER_ID, userId);
        FollowersFragment followersFragment = new FollowersFragment();
        followersFragment.setArguments(args);
        return followersFragment;
    }

    @NonNull
    @Override
    public IPresenterFactory<FollowersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FollowersPresenter(getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }
}
