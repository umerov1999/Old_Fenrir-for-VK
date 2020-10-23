package dev.ragnarok.fenrir.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.fragment.AccountsFragment;
import dev.ragnarok.fenrir.fragment.PreferencesFragment;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceProvider;
import dev.ragnarok.fenrir.util.AppPerms;

public class AccountsActivity extends NoMainActivity implements PlaceProvider {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), new AccountsFragment())
                    .addToBackStack("accounts")
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPerms.tryInterceptAppPermission(this, requestCode, permissions, grantResults);
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.PREFERENCES) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), PreferencesFragment.newInstance(place.getArgs()))
                    .addToBackStack("preferences")
                    .commit();
        }
    }

}