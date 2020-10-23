package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.SelectedProfilesAdapter;
import dev.ragnarok.fenrir.fragment.fave.FaveTabsFragment;
import dev.ragnarok.fenrir.fragment.friends.FriendsTabsFragment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.SelectProfileCriteria;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.MainActivityTransforms;
import dev.ragnarok.fenrir.util.Utils;

public class SelectProfilesActivity extends MainActivity implements SelectedProfilesAdapter.ActionListener, ProfileSelectable {

    private static final String TAG = SelectProfilesActivity.class.getSimpleName();
    private static final String SAVE_SELECTED_OWNERS = "save_selected_owners";
    private SelectProfileCriteria mSelectableCriteria;
    private ArrayList<Owner> mSelectedOwners;
    private RecyclerView mRecyclerView;
    private SelectedProfilesAdapter mProfilesAdapter;

    public static Intent createIntent(Context context, @NonNull Place initialPlace, @NonNull SelectProfileCriteria criteria) {
        return new Intent(context, SelectProfilesActivity.class)
                .setAction(MainActivity.ACTION_OPEN_PLACE)
                .putExtra(Extra.PLACE, initialPlace)
                .putExtra(Extra.CRITERIA, criteria);
    }

    public static void startFriendsSelection(@NonNull Fragment fragment, int requestCode) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        SelectProfileCriteria criteria = new SelectProfileCriteria().setOwnerType(SelectProfileCriteria.OwnerType.ONLY_FRIENDS);

        Intent intent = new Intent(fragment.requireActivity(), SelectProfilesActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putExtra(Extra.CRITERIA, criteria);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void startFaveSelection(@NonNull Fragment fragment, int requestCode) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Place place = PlaceFactory.getBookmarksPlace(aid, FaveTabsFragment.TAB_PAGES);

        SelectProfileCriteria criteria = new SelectProfileCriteria().setOwnerType(SelectProfileCriteria.OwnerType.OWNERS);

        Intent intent = new Intent(fragment.requireActivity(), SelectProfilesActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putExtra(Extra.CRITERIA, criteria);
        fragment.startActivityForResult(intent, requestCode);
    }

    /*public static void start(Activity activity, @Nullable ArrayList<VKApiUser> users, int requestCode){
        int aid = Accounts.getCurrentUid(activity);
        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        Intent intent = new Intent(activity, SelectProfilesActivity.class);
        intent.setAction(SelectProfilesActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putParcelableArrayListExtra(Extra.USERS, users);
        activity.startActivityForResult(intent, requestCode);
    }*/

    @Override
    protected @MainActivityTransforms
    int getMainActivityTransform() {
        return MainActivityTransforms.PROFILES;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mLayoutRes = R.layout.activity_main_with_profiles_selection;
        super.onCreate(savedInstanceState);
        mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;

        mSelectableCriteria = getIntent().getParcelableExtra(Extra.CRITERIA);

        if (savedInstanceState != null) {
            mSelectedOwners = savedInstanceState.getParcelableArrayList(SAVE_SELECTED_OWNERS);
        }

        if (mSelectedOwners == null) {
            mSelectedOwners = new ArrayList<>();
        }

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mProfilesAdapter = new SelectedProfilesAdapter(this, mSelectedOwners);
        mProfilesAdapter.setActionListener(this);

        mRecyclerView = findViewById(R.id.recycleView);
        if (mRecyclerView == null) {
            throw new IllegalStateException("Invalid view");
        }

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mProfilesAdapter);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_SELECTED_OWNERS, mSelectedOwners);
    }

    @Override
    public void onClick(int adapterPosition, Owner owner) {
        mSelectedOwners.remove(mProfilesAdapter.toDataPosition(adapterPosition));
        mProfilesAdapter.notifyItemRemoved(adapterPosition);
        mProfilesAdapter.notifyHeaderChange();
    }

    @Override
    public void onCheckClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.OWNERS, mSelectedOwners);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void select(Owner owner) {
        Logger.d(TAG, "Select, owner: " + owner);

        int index = Utils.indexOfOwner(mSelectedOwners, owner);

        if (index != -1) {
            mSelectedOwners.remove(index);
            mProfilesAdapter.notifyItemRemoved(mProfilesAdapter.toAdapterPosition(index));
        }

        mSelectedOwners.add(0, owner);
        mProfilesAdapter.notifyItemInserted(mProfilesAdapter.toAdapterPosition(0));
        mProfilesAdapter.notifyHeaderChange();
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public SelectProfileCriteria getAcceptableCriteria() {
        return mSelectableCriteria;
    }
}
