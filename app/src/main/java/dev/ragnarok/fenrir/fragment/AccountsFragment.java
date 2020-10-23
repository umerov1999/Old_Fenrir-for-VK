package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dev.ragnarok.fenrir.Account_Types;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.EnterPinActivity;
import dev.ragnarok.fenrir.activity.LoginActivity;
import dev.ragnarok.fenrir.activity.ProxyManagerActivity;
import dev.ragnarok.fenrir.adapter.AccountAdapter;
import dev.ragnarok.fenrir.api.Auth;
import dev.ragnarok.fenrir.db.DBHelper;
import dev.ragnarok.fenrir.dialog.DirectAuthDialog;
import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.longpoll.LongpollInstance;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.ShortcutUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AccountsFragment extends BaseFragment implements View.OnClickListener, AccountAdapter.Callback {

    private static final int REQUEST_PIN_FOR_SECURITY = 120;
    private static final String SAVE_DATA = "save_data";
    private static final int REQUEST_LOGIN = 107;
    private static final int REQEUST_DIRECT_LOGIN = 108;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextView empty;
    private RecyclerView mRecyclerView;
    private AccountAdapter mAdapter;
    private final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        public boolean onMove(@NotNull RecyclerView recyclerView,
                              @NotNull RecyclerView.ViewHolder viewHolder, @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NotNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
            Account account = mAdapter.getByPosition(viewHolder.getBindingAdapterPosition());
            boolean idCurrent = account.getId() == Settings.get()
                    .accounts()
                    .getCurrent();
            if (!idCurrent) {
                setAsActive(account);
            }
        }
    };
    private ArrayList<Account> mData;
    private IOwnersRepository mOwnersInteractor;
    private IAccountsInteractor accountsInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mOwnersInteractor = Repository.INSTANCE.getOwners();
        accountsInteractor = InteractorFactory.createAccountInteractor();

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(SAVE_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_accounts, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        empty = root.findViewById(R.id.empty);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);

        root.findViewById(R.id.fab).setOnClickListener(this);
        root.findViewById(R.id.kate_acc).setOnClickListener(this);
        return root;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new AccountAdapter(requireActivity(), mData, this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            load();
        }

        resolveEmptyText();
    }

    private void resolveEmptyText() {
        if (!isAdded() || empty == null) return;
        empty.setVisibility(Utils.safeIsEmpty(mData) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, mData);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    private void load() {
        mCompositeDisposable.add(accountsInteractor
                .getAll()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(appAccounts -> {
                    mData.clear();
                    mData.addAll(appAccounts);

                    if (Objects.nonNull(mAdapter)) {
                        mAdapter.notifyDataSetChanged();
                    }

                    resolveEmptyText();
                    if (isAdded() && Utils.safeIsEmpty(mData)) {
                        requireActivity().invalidateOptionsMenu();
                        startDirectLogin();
                    }
                }));
    }

    private void startExportAccounts() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = null;
        properties.show_hidden_files = true;
        FilePickerDialog dialog = new FilePickerDialog(requireActivity(), properties, Settings.get().ui().getMainTheme());
        dialog.setTitle(R.string.export_accounts);
        dialog.setDialogSelectionListener(files -> {
            File file = new File(files[0], "fenrir_accounts_backup.json");

            appendDisposable(mOwnersInteractor.findBaseOwnersDataAsBundle(Settings.get().accounts().getCurrent(), Settings.get().accounts().getRegistered(), IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(userInfo -> SaveAccounts(file, userInfo), throwable -> SaveAccounts(file, null)));
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PIN_FOR_SECURITY:
                if (resultCode == Activity.RESULT_OK) {
                    startExportAccounts();
                }
                break;
            case REQUEST_LOGIN:
                if (resultCode == Activity.RESULT_OK) {
                    int uid = data.getExtras().getInt(Extra.USER_ID);
                    String token = data.getStringExtra(Extra.TOKEN);
                    String Login = data.getStringExtra(Extra.LOGIN);
                    String Password = data.getStringExtra(Extra.PASSWORD);
                    String TwoFA = data.getStringExtra(Extra.TWOFA);
                    processNewAccount(uid, token, Constants.DEFAULT_ACCOUNT_TYPE, Login != null ? Login : "", Password != null ? Password : "", TwoFA != null ? TwoFA : "none", true, true);
                }
                break;

            case REQEUST_DIRECT_LOGIN:
                if (resultCode == Activity.RESULT_OK) {
                    if (DirectAuthDialog.ACTION_LOGIN_VIA_WEB.equals(data.getAction())) {
                        startLoginViaWeb();
                    } else if (DirectAuthDialog.ACTION_VALIDATE_VIA_WEB.equals(data.getAction())) {
                        String url = data.getStringExtra(Extra.URL);
                        String Login = data.getStringExtra(Extra.LOGIN);
                        String Password = data.getStringExtra(Extra.PASSWORD);
                        String TwoFA = data.getStringExtra(Extra.TWOFA);
                        startValidateViaWeb(url, Login, Password, TwoFA);
                    } else if (DirectAuthDialog.ACTION_LOGIN_COMPLETE.equals(data.getAction())) {
                        int uid = data.getExtras().getInt(Extra.USER_ID);
                        String token = data.getStringExtra(Extra.TOKEN);
                        String Login = data.getStringExtra(Extra.LOGIN);
                        String Password = data.getStringExtra(Extra.PASSWORD);
                        String TwoFA = data.getStringExtra(Extra.TWOFA);
                        processNewAccount(uid, token, Constants.DEFAULT_ACCOUNT_TYPE, Login, Password, TwoFA, true, true);
                    }
                }
                break;
        }
    }

    private int indexOf(int uid) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId() == uid) {
                return i;
            }
        }

        return -1;
    }

    private void merge(Account account) {
        int index = indexOf(account.getId());

        if (index != -1) {
            mData.set(index, account);
        } else {
            mData.add(account);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        resolveEmptyText();
    }

    private void processNewAccount(int uid, String token, @Account_Types int type, String Login, String Password, String TwoFA, boolean IsSend, boolean isCurrent) {
        //Accounts account = new Accounts(token, uid);

        // важно!! Если мы получили новый токен, то необходимо удалить запись
        // о регистрации push-уведомлений
        //PushSettings.unregisterFor(getContext(), account);

        Settings.get()
                .accounts()
                .storeAccessToken(uid, token);

        Settings.get()
                .accounts().storeTokenType(uid, type);

        Settings.get()
                .accounts()
                .registerAccountId(uid, isCurrent);

        merge(new Account(uid, null));

        mCompositeDisposable.add(mOwnersInteractor.getBaseOwnerInfo(uid, uid, IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owner -> merge(new Account(uid, owner)), t -> {/*ignored*/}));
    }

    private void startLoginViaWeb() {
        Intent intent = LoginActivity.createIntent(requireActivity(), String.valueOf(Constants.API_ID), Auth.getScope());
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void startValidateViaWeb(String url, String Login, String Password, String TwoFa) {
        Intent intent = LoginActivity.createIntent(requireActivity(), url, Login, Password, TwoFa);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void startDirectLogin() {
        DirectAuthDialog.newInstance()
                .targetTo(this, REQEUST_DIRECT_LOGIN)
                .show(getParentFragmentManager(), "direct-login");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startDirectLogin();
                break;
            case R.id.kate_acc:
                onKate();
                break;
        }
    }

    private void onKate() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.import_from_kate)
                .setMessage(R.string.import_from_kate_summary)
                .setPositiveButton(R.string.button_yes, (dialogInterface, i) -> doImportKateAccounts())
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void doImportKateAccounts() {
        if (!AppPerms.hasReadStoragePermision(getActivity())) {
            AppPerms.requestReadExternalStoragePermission(getActivity());
            return;
        }
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = new String[]{"xml"};
        properties.show_hidden_files = true;
        FilePickerDialog dialog = new FilePickerDialog(requireActivity(), properties, Settings.get().ui().getMainTheme());
        dialog.setTitle(R.string.import_accounts);
        dialog.setDialogSelectionListener(files -> {
            try {
                FileInputStream dataFromServerStream = new FileInputStream(files[0]);
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = dBuilder.parse(new InputSource(new InputStreamReader(dataFromServerStream, StandardCharsets.UTF_8)));
                NodeList elements = doc.getElementsByTagName("map").item(0).getChildNodes();
                int count = 0;
                for (int i = 0; i < elements.getLength(); i++) {
                    NamedNodeMap attributes = elements.item(i).getAttributes();
                    if (attributes == null || attributes.getNamedItem("name") == null)
                        continue;
                    String name = attributes.getNamedItem("name").getNodeValue();
                    if (name.equals("accounts")) {
                        JSONArray jsonRoot = new JSONArray(elements.item(i).getTextContent());
                        List<Integer> accounts = Settings.get().accounts().getRegistered();
                        for (int s = 0; s < jsonRoot.length(); s++) {
                            JSONObject mJsonObject = jsonRoot.getJSONObject(s);
                            count++;
                            if (accounts != null && accounts.size() > 0) {
                                if (accounts.contains(mJsonObject.getInt("mid")))
                                    continue;
                            }
                            processNewAccount(mJsonObject.getInt("mid"), mJsonObject.getString("access_token"), Account_Types.KATE, "", "", "kate_app", true, false);
                        }
                        break;
                    }

                }
                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.kate_worked, count);
            } catch (Exception e) {
                CustomToast.CreateCustomToast(requireActivity()).showToastError("Import Error [" + e.getClass().getName() + "] : " + e.getMessage());
            }
        });
        dialog.show();
    }

    private void delete(Account account) {
        Settings.get()
                .accounts()
                .removeAccessToken(account.getId());

        Settings.get()
                .accounts()
                .removeType(account.getId());

        Settings.get()
                .accounts()
                .remove(account.getId());

        DBHelper.removeDatabaseFor(requireActivity(), account.getId());

        LongpollInstance.get().forceDestroy(account.getId());

        mData.remove(account);
        mAdapter.notifyDataSetChanged();
        resolveEmptyText();
    }

    private void setAsActive(Account account) {
        Settings.get()
                .accounts()
                .setCurrent(account.getId());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Account account) {
        boolean idCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        if (account.getId() > 0) {
            menus.add(new OptionRequest(0, getString(R.string.delete), R.drawable.ic_outline_delete));
            menus.add(new OptionRequest(1, getString(R.string.add_to_home_screen), R.drawable.plus));
            if (!idCurrent) {
                menus.add(new OptionRequest(2, getString(R.string.set_as_active), R.drawable.account_circle));
            }
        } else
            menus.add(new OptionRequest(0, getString(R.string.delete), R.drawable.ic_outline_delete));
        menus.header(account.getDisplayName(), R.drawable.account_circle, account.getOwner() != null ? account.getOwner().getMaxSquareAvatar() : null);
        menus.show(getChildFragmentManager(), "account_options", option -> {
            switch (option.getId()) {
                case 0:
                    delete(account);
                    break;
                case 1:
                    createShortcut(account);
                    break;
                case 2:
                    setAsActive(account);
                    break;
            }
        });
    }

    private void SaveAccounts(File file, IOwnersBundle Users) {
        FileOutputStream out = null;
        try {
            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (int i : Settings.get().accounts().getRegistered()) {
                JsonObject temp = new JsonObject();

                Owner owner = Users.getById(i);
                temp.addProperty("user_name", owner.getFullName());
                temp.addProperty("user_id", i);
                temp.addProperty("type", Settings.get().accounts().getType(i));
                temp.addProperty("domain", owner.getDomain());
                temp.addProperty("access_token", Settings.get().accounts().getAccessToken(i));
                temp.addProperty("avatar", owner.getMaxSquareAvatar());
                arr.add(temp);
            }
            root.add("fenrir_accounts", arr);
            byte[] bytes = root.toString().getBytes(StandardCharsets.UTF_8);
            out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();
            Injection.provideApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.saved_to_param_file_name, file.getAbsolutePath());
        } catch (Exception e) {
            CustomToast.CreateCustomToast(requireActivity()).showToastError(e.getLocalizedMessage());
        } finally {
            Utils.safelyClose(out);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_proxy) {
            startProxySettings();
            return true;
        }

        if (item.getItemId() == R.id.action_preferences) {
            PlaceFactory.getPreferencesPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
            return true;
        }

        if (item.getItemId() == R.id.entry_account) {
            View root = View.inflate(requireActivity(), R.layout.entry_account, null);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.entry_account)
                    .setCancelable(true)
                    .setView(root)
                    .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                        try {
                            int id = Integer.parseInt(((TextInputEditText) root.findViewById(R.id.edit_user_id)).getText().toString().trim());
                            String access_token = ((TextInputEditText) root.findViewById(R.id.edit_access_token)).getText().toString().trim();
                            int selected = ((Spinner) root.findViewById(R.id.access_token_type)).getSelectedItemPosition();
                            int[] types = {Account_Types.VK_ANDROID, Account_Types.KATE, Account_Types.VK_ANDROID_HIDDEN, Account_Types.KATE_HIDDEN};
                            if (!Utils.isEmpty(access_token) && id != 0 && selected >= 0 && selected < 3) {
                                processNewAccount(id, access_token, types[selected], "", "", "fenrir_app", true, false);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, null);
            builder.create().show();
            return true;
        }

        if (item.getItemId() == R.id.export_accounts) {
            if (!AppPerms.hasReadWriteStoragePermision(getActivity())) {
                AppPerms.requestReadWriteStoragePermission(getActivity());
                return true;
            }
            if (Settings.get().accounts() == null || Settings.get().accounts().getRegistered() == null || Settings.get().accounts().getRegistered().size() <= 0)
                return true;
            if (Settings.get().security().isUsePinForSecurity()) {
                startActivityForResult(new Intent(requireActivity(), EnterPinActivity.class), REQUEST_PIN_FOR_SECURITY);
            } else
                startExportAccounts();
            return true;
        }

        if (item.getItemId() == R.id.import_accounts) {
            if (!AppPerms.hasReadStoragePermision(getActivity())) {
                AppPerms.requestReadExternalStoragePermission(getActivity());
                return true;
            }
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.root = Environment.getExternalStorageDirectory();
            properties.error_dir = Environment.getExternalStorageDirectory();
            properties.offset = Environment.getExternalStorageDirectory();
            properties.extensions = new String[]{"json"};
            properties.show_hidden_files = true;
            FilePickerDialog dialog = new FilePickerDialog(requireActivity(), properties, Settings.get().ui().getMainTheme());
            dialog.setTitle(R.string.import_accounts);
            dialog.setDialogSelectionListener(files -> {
                try {
                    StringBuilder jbld = new StringBuilder();
                    File file = new File(files[0]);
                    if (file.exists()) {
                        FileInputStream dataFromServerStream = new FileInputStream(file);
                        BufferedReader d = new BufferedReader(new InputStreamReader(dataFromServerStream, StandardCharsets.UTF_8));
                        while (d.ready())
                            jbld.append(d.readLine());
                        d.close();
                        JsonArray reader = JsonParser.parseString(jbld.toString()).getAsJsonObject().getAsJsonArray("fenrir_accounts");
                        for (JsonElement i : reader) {
                            JsonObject elem = i.getAsJsonObject();
                            int id = elem.get("user_id").getAsInt();
                            if (Settings.get().accounts().getRegistered().contains(id))
                                continue;
                            String token = elem.get("access_token").getAsString();
                            int Type = elem.get("type").getAsInt();
                            processNewAccount(id, token, Type, "", "", "fenrir_app", true, false);
                        }
                    }
                    CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.accounts_restored, file.getAbsolutePath());
                } catch (Exception e) {
                    CustomToast.CreateCustomToast(requireActivity()).showToastError(e.getLocalizedMessage());
                }
            });
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startProxySettings() {
        startActivity(new Intent(requireActivity(), ProxyManagerActivity.class));
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_accounts, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.export_accounts).setVisible(mData.size() > 0);
    }

    private void createShortcut(Account account) {
        if (account.getId() < 0) {
            return; // this is comminity
        }

        User user = (User) account.getOwner();

        Context app = requireContext().getApplicationContext();

        appendDisposable(Completable.create(emitter -> {
            String avaUrl = user == null ? null : user.getMaxSquareAvatar();
            ShortcutUtils.createAccountShurtcut(app, account.getId(), account.getDisplayName(), avaUrl);
            emitter.onComplete();
        }).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> {
                },
                t -> Snackbar.make(requireView(), t.getLocalizedMessage(), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000")).setAnchorView(mRecyclerView).show()));
    }
}
