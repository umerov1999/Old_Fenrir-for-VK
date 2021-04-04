package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.ragnarok.fenrir.CheckDonate;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.LoginActivity;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.PostFilter;
import dev.ragnarok.fenrir.model.Token;
import dev.ragnarok.fenrir.module.rlottie.RLottieImageView;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter;
import dev.ragnarok.fenrir.mvp.presenter.GroupWallPresenter;
import dev.ragnarok.fenrir.mvp.view.IGroupWallView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;

public class GroupWallFragment extends AbsWallFragment<IGroupWallView, GroupWallPresenter> implements IGroupWallView {

    private final ActivityResultLauncher<Intent> requestCommunity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<Token> tokens = LoginActivity.extractGroupTokens(result.getData());
                    getPresenter().fireGroupTokensReceived(tokens);
                }
            });
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> getPresenter().fireShowQR(requireActivity()));
    private GroupHeaderHolder mHeaderHolder;

    @Override
    public void displayBaseCommunityData(Community community, CommunityDetails details) {
        if (isNull(mHeaderHolder)) return;

        mHeaderHolder.tvName.setText(community.getFullName());

        if (details.getCover() != null && !Utils.isEmpty(details.getCover().getImages())) {
            int def = 0;
            String url = null;
            for (CommunityDetails.CoverImage i : details.getCover().getImages()) {
                if (i.getWidth() * i.getHeight() > def) {
                    def = i.getWidth() * i.getHeight();
                    url = i.getUrl();
                }
            }
            displayCommunityCover(url);
        } else {
            displayCommunityCover(community.getMaxSquareAvatar());
        }

        String statusText;
        if (nonNull(details.getStatusAudio())) {
            statusText = details.getStatusAudio().getArtistAndTitle();
        } else {
            statusText = details.getStatus();
        }

        mHeaderHolder.tvStatus.setText(statusText);
        mHeaderHolder.tvAudioStatus.setVisibility(nonNull(details.getStatusAudio()) ? View.VISIBLE : View.GONE);

        String screenName = nonEmpty(community.getScreenName()) ? "@" + community.getScreenName() : null;
        mHeaderHolder.tvScreenName.setText(screenName);

        if (!details.isCanMessage())
            mHeaderHolder.fabMessage.setImageResource(R.drawable.close);
        else
            mHeaderHolder.fabMessage.setImageResource(R.drawable.email);

        String photoUrl = community.getMaxSquareAvatar();
        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl).transform(CurrentTheme.createTransformationForAvatar(requireActivity()))
                    .tag(Constants.PICASSO_TAG)
                    .into(mHeaderHolder.ivAvatar);
        }
        mHeaderHolder.ivAvatar.setOnClickListener(v -> {
            Community cmt = Objects.requireNonNull(getPresenter()).getCommunity();
            PlaceFactory.getSingleURLPhotoPlace(cmt.getOriginalAvatar(), cmt.getFullName(), "club" + Math.abs(cmt.getId())).tryOpenWith(requireActivity());
        });
    }

    private void displayCommunityCover(String resource) {
        if (!Settings.get().other().isShow_wall_cover())
            return;
        if (!Utils.isEmpty(resource)) {
            PicassoInstance.with()
                    .load(resource)
                    .transform(new BlurTransformation(6, 1, requireActivity()))
                    .into(mHeaderHolder.vgCover);
        }
    }

    @Override
    public void InvalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu();
    }

    @NotNull
    @Override
    public IPresenterFactory<GroupWallPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            AssertUtils.requireNonNull(wrapper);

            return new GroupWallPresenter(accountId, ownerId, (Community) wrapper.get(), saveInstanceState);
        };
    }

    @Override
    protected int headerLayout() {
        return R.layout.header_group;
    }

    @Override
    protected void onHeaderInflated(View headerRootView) {
        mHeaderHolder = new GroupHeaderHolder(headerRootView);
        setupPaganContent(mHeaderHolder.Runes, mHeaderHolder.paganSymbol);
    }

    @Override
    public void setupPrimaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.primaryActionButton.setText(title);
                mHeaderHolder.primaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.primaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setupSecondaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.secondaryActionButton.setText(title);
                mHeaderHolder.secondaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.secondaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void openTopics(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getTopicsPlace(accoundId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openCommunityMembers(int accoundId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        PlaceFactory.getSingleTabSearchPlace(accoundId, SearchContentType.PEOPLE, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void openDocuments(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getDocumentsPlace(accoundId, ownerId, DocsListPresenter.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayWallFilters(List<PostFilter> filters) {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.setItems(filters);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_community_wall, menu);
        OptionMenuView optionMenuView = new OptionMenuView();
        getPresenter().fireOptionMenuViewCreated(optionMenuView);

        if (!optionMenuView.isSubscribed) {
            menu.add(R.string.notify_wall_added).setOnMenuItemClickListener(item -> {
                getPresenter().fireSubscribe();
                return true;
            });
        } else {
            menu.add(R.string.unnotify_wall_added).setOnMenuItemClickListener(item -> {
                getPresenter().fireUnSubscribe();
                return true;
            });
        }

        if (!optionMenuView.isFavorite) {
            menu.add(R.string.add_to_bookmarks).setOnMenuItemClickListener(item -> {
                getPresenter().fireAddToBookmarksClick();
                return true;
            });
        } else {
            menu.add(R.string.remove_from_bookmarks).setOnMenuItemClickListener(item -> {
                getPresenter().fireRemoveFromBookmarks();
                return true;
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_community_control) {
            getPresenter().fireCommunityControlClick();
            return true;
        }

        if (item.getItemId() == R.id.action_community_messages) {
            getPresenter().fireCommunityMessagesClick();
            return true;
        }

        if (item.getItemId() == R.id.action_show_qr) {
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestWritePermission.launch();
            } else {
                getPresenter().fireShowQR(requireActivity());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyWallFiltersChanged() {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.community);
        ActivityUtils.setToolbarSubtitle(this, null);
    }

    @Override
    public void goToCommunityControl(int accountId, Community community, GroupSettings settings) {
        PlaceFactory.getCommunityControlPlace(accountId, community, settings).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowComunityInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowComunityLinksInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityLinksInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void startLoginCommunityActivity(int groupId) {
        Intent intent = LoginActivity.createIntent(requireActivity(), String.valueOf(Constants.API_ID), "messages,photos,docs,manage", Collections.singletonList(groupId));
        requestCommunity.launch(intent);
    }

    @Override
    public void openCommunityDialogs(int accountId, int groupId, String subtitle) {
        PlaceFactory.getDialogsPlace(accountId, -groupId, subtitle).tryOpenWith(requireActivity());
    }

    @Override
    public void displayCounters(int members, int topics, int docs, int photos, int audio, int video, int articles, int products) {
        if (isNull(mHeaderHolder)) return;
        setupCounter(mHeaderHolder.bTopics, topics);
        setupCounter(mHeaderHolder.bMembers, members);
        setupCounter(mHeaderHolder.bDocuments, docs);
        setupCounter(mHeaderHolder.bPhotos, photos);
        setupCounter(mHeaderHolder.bAudios, audio);
        setupCounter(mHeaderHolder.bVideos, video);
        setupCounter(mHeaderHolder.bArticles, articles);
        setupCounter(mHeaderHolder.bProducts, products);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionMenuView optionMenuView = new OptionMenuView();
        getPresenter().fireOptionMenuViewCreated(optionMenuView);
        menu.findItem(R.id.action_community_control).setVisible(optionMenuView.controlVisible);
    }

    @Override
    public void openProducts(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getMarketAlbumPlace(accountId, ownerId).tryOpenWith(requireActivity());
    }

    private static final class OptionMenuView implements IOptionMenuView {

        boolean controlVisible;

        boolean isFavorite;

        boolean isSubscribed;

        @Override
        public void setControlVisible(boolean visible) {
            controlVisible = visible;
        }

        @Override
        public void setIsFavorite(boolean favorite) {
            isFavorite = favorite;
        }

        @Override
        public void setIsSubscribed(boolean subscribed) {
            isSubscribed = subscribed;
        }
    }

    private class GroupHeaderHolder {
        final ImageView vgCover;
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvStatus;
        final ImageView tvAudioStatus;
        final TextView tvScreenName;

        final TextView bTopics;
        final TextView bArticles;
        final TextView bProducts;
        final TextView bMembers;
        final TextView bDocuments;
        final TextView bPhotos;
        final TextView bAudios;
        final TextView bVideos;
        final MaterialButton primaryActionButton;
        final MaterialButton secondaryActionButton;

        final FloatingActionButton fabMessage;
        final HorizontalOptionsAdapter<PostFilter> mFiltersAdapter;

        final RLottieImageView paganSymbol;
        final View Runes;

        GroupHeaderHolder(@NonNull View root) {
            vgCover = root.findViewById(R.id.cover);
            ivAvatar = root.findViewById(R.id.header_group_avatar);
            tvName = root.findViewById(R.id.header_group_name);
            tvStatus = root.findViewById(R.id.header_group_status);
            tvAudioStatus = root.findViewById(R.id.fragment_group_audio);
            tvScreenName = root.findViewById(R.id.header_group_id);
            bTopics = root.findViewById(R.id.header_group_btopics);
            bMembers = root.findViewById(R.id.header_group_bmembers);
            bDocuments = root.findViewById(R.id.header_group_bdocuments);
            bPhotos = root.findViewById(R.id.header_group_bphotos);
            bAudios = root.findViewById(R.id.header_group_baudios);
            bVideos = root.findViewById(R.id.header_group_bvideos);
            bArticles = root.findViewById(R.id.header_group_barticles);
            bProducts = root.findViewById(R.id.header_group_bproducts);
            primaryActionButton = root.findViewById(R.id.header_group_primary_button);
            secondaryActionButton = root.findViewById(R.id.header_group_secondary_button);
            fabMessage = root.findViewById(R.id.header_group_fab_message);

            paganSymbol = root.findViewById(R.id.pagan_symbol);
            Runes = root.findViewById(R.id.runes_container);

            RecyclerView filterList = root.findViewById(R.id.post_filter_recyclerview);
            filterList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
            mFiltersAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
            mFiltersAdapter.setListener(entry -> getPresenter().fireFilterEntryClick(entry));

            filterList.setAdapter(mFiltersAdapter);

            tvStatus.setOnClickListener(v -> getPresenter().fireHeaderStatusClick());
            fabMessage.setOnClickListener(v -> getPresenter().fireChatClick());
            secondaryActionButton.setOnClickListener(v -> getPresenter().fireSecondaryButtonClick());
            primaryActionButton.setOnClickListener(v -> getPresenter().firePrimaryButtonClick());

            root.findViewById(R.id.header_group_photos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderPhotosClick());
            root.findViewById(R.id.header_group_videos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderVideosClick());
            root.findViewById(R.id.header_group_members_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderMembersClick());
            root.findViewById(R.id.horiz_scroll)
                    .setClipToOutline(true);
            root.findViewById(R.id.header_group_topics_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderTopicsClick());
            root.findViewById(R.id.header_group_documents_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderDocsClick());
            root.findViewById(R.id.header_group_audios_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderAudiosClick());
            root.findViewById(R.id.header_group_articles_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderArticlesClick());
            root.findViewById(R.id.header_group_products_container)
                    .setOnClickListener(v -> {
                        if (CheckDonate.isFullVersion(requireActivity())) {
                            getPresenter().fireHeaderProductsClick();
                        }
                    });
            root.findViewById(R.id.header_group_contacts_container)
                    .setOnClickListener(v -> getPresenter().fireShowComunityInfoClick());
            root.findViewById(R.id.header_group_links_container)
                    .setOnClickListener(v -> getPresenter().fireShowComunityLinksInfoClick());
        }
    }
}
