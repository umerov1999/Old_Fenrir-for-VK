package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;

public class AudioSelectTabsFragment extends BaseFragment {

    public static final int PLAYLISTS = -3;
    public static final int MY_RECOMMENDATIONS = -2;
    public static final int MY_AUDIO = -1;
    public static final int TOP_ALL = 0;
    private int accountId;

    public static Bundle buildArgs(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static AudioSelectTabsFragment newInstance(int accountId) {
        return newInstance(buildArgs(accountId));
    }

    public static AudioSelectTabsFragment newInstance(Bundle args) {
        AudioSelectTabsFragment fragment = new AudioSelectTabsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_audios_tabs, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ViewPager2 viewPager = view.findViewById(R.id.fragment_audios_pager);
        viewPager.setOffscreenPageLimit(1);
        Adapter adapter = new Adapter(this);
        setupViewPager(viewPager, adapter);

        new TabLayoutMediator(view.findViewById(R.id.fragment_audios_tabs), viewPager, (tab, position) -> {
            Integer fid = adapter.mFragments.get(position);
            if (fid == MY_AUDIO)
                tab.setText(getString(R.string.my_saved));
            else if (fid == PLAYLISTS)
                tab.setText(getString(R.string.playlists));
            else if (fid == MY_RECOMMENDATIONS)
                tab.setText(getString(R.string.recommendation));
            else if (fid == TOP_ALL)
                tab.setText(getString(R.string.top));
            else
                tab.setText(VKApiAudio.Genre.getTitleByGenre(requireActivity(), fid));
        }).attach();
    }

    public int getAccountId() {
        return accountId;
    }

    private Fragment CreateAudiosFragment(int option_menu) {
        if (option_menu == PLAYLISTS) {
            AudioPlaylistsFragment fragment = AudioPlaylistsFragment.newInstanceSelect(getAccountId());
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        } else {
            AudiosFragment fragment = AudiosFragment.newInstanceSelect(getAccountId(), option_menu, 0, null);
            fragment.requireArguments().putBoolean(AudiosFragment.EXTRA_IN_TABS_CONTAINER, true);
            return fragment;
        }
    }

    private void setupViewPager(ViewPager2 viewPager, Adapter adapter) {
        adapter.addFragment(MY_AUDIO);
        adapter.addFragment(PLAYLISTS);
        adapter.addFragment(MY_RECOMMENDATIONS);
        if (Settings.get().other().isEnable_show_audio_top()) {
            adapter.addFragment(TOP_ALL);
            adapter.addFragment(VKApiAudio.Genre.ETHNIC);
            adapter.addFragment(VKApiAudio.Genre.INSTRUMENTAL);
            adapter.addFragment(VKApiAudio.Genre.ACOUSTIC_AND_VOCAL);
            adapter.addFragment(VKApiAudio.Genre.ALTERNATIVE);
            adapter.addFragment(VKApiAudio.Genre.CLASSICAL);
            adapter.addFragment(VKApiAudio.Genre.DANCE_AND_HOUSE);
            adapter.addFragment(VKApiAudio.Genre.DRUM_AND_BASS);
            adapter.addFragment(VKApiAudio.Genre.EASY_LISTENING);
            adapter.addFragment(VKApiAudio.Genre.ELECTROPOP_AND_DISCO);
            adapter.addFragment(VKApiAudio.Genre.INDIE_POP);
            adapter.addFragment(VKApiAudio.Genre.METAL);
            adapter.addFragment(VKApiAudio.Genre.OTHER);
            adapter.addFragment(VKApiAudio.Genre.POP);
            adapter.addFragment(VKApiAudio.Genre.REGGAE);
            adapter.addFragment(VKApiAudio.Genre.ROCK);
            adapter.addFragment(VKApiAudio.Genre.TRANCE);
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.select_audio);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_AUDIOS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            AudioSearchCriteria criteria = new AudioSearchCriteria("", false, true);
            PlaceFactory.getSingleTabSearchPlace(getAccountId(), SearchContentType.AUDIOS_SELECT, criteria).tryOpenWith(requireActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_audio_main, menu);
    }

    private class Adapter extends FragmentStateAdapter {
        private final List<Integer> mFragments = new ArrayList<>();

        public Adapter(@NonNull Fragment fragmentActivity) {
            super(fragmentActivity);
        }

        void addFragment(Integer fragment) {
            mFragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return CreateAudiosFragment(mFragments.get(position));
        }

        @Override
        public int getItemCount() {
            return mFragments.size();
        }
    }
}
