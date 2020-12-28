package dev.ragnarok.fenrir.fragment.search;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.SearchOptionsAdapter;
import dev.ragnarok.fenrir.dialog.SelectChairsDialog;
import dev.ragnarok.fenrir.dialog.SelectCityDialog;
import dev.ragnarok.fenrir.dialog.SelectCountryDialog;
import dev.ragnarok.fenrir.dialog.SelectFacultyDialog;
import dev.ragnarok.fenrir.dialog.SelectSchoolClassesDialog;
import dev.ragnarok.fenrir.dialog.SelectSchoolsDialog;
import dev.ragnarok.fenrir.dialog.SelectUniversityDialog;
import dev.ragnarok.fenrir.fragment.search.options.BaseOption;
import dev.ragnarok.fenrir.fragment.search.options.DatabaseOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleTextOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.InputTextDialog;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FilterEditFragment extends BottomSheetDialogFragment implements SearchOptionsAdapter.OptionClickListener {

    public static final String REQUEST_FILTER_EDIT = "request_filter_edit";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private ArrayList<BaseOption> mData;
    private SearchOptionsAdapter mAdapter;
    private int mAccountId;
    private TextView mEmptyText;

    public static FilterEditFragment newInstance(int accountId, ArrayList<BaseOption> options) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.LIST, options);
        FilterEditFragment fragment = new FilterEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void onDialogResult(@NotNull Bundle result) {
        int key = result.getInt(Extra.KEY);
        Integer id = result.containsKey(Extra.ID) ? result.getInt(Extra.ID) : null;
        String title = result.containsKey(Extra.TITLE) ? result.getString(Extra.TITLE) : null;

        mergeDatabaseOptionValue(key, id == null ? null : new DatabaseOption.Entry(id, title));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        mData = getArguments().getParcelableArrayList(Extra.LIST);
    }

    private void resolveEmptyTextVisibility() {
        if (Objects.nonNull(mEmptyText)) {
            mEmptyText.setVisibility(Utils.isEmpty(mData) ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NotNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View root = View.inflate(requireActivity(), R.layout.sheet_filter_edirt, null);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_options);

        Drawable dr = ResourcesCompat.getDrawable(getResources(), R.drawable.check, requireActivity().getTheme());
        Utils.setColorFilter(dr, CurrentTheme.getColorPrimary(requireActivity()));
        toolbar.setNavigationIcon(dr);
        toolbar.setNavigationOnClickListener(menuItem ->
                onSaveClick());

        mEmptyText = root.findViewById(R.id.empty_text);

        RecyclerView mRecyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new SearchOptionsAdapter(mData);
        mAdapter.setOptionClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        resolveEmptyTextVisibility();

        dialog.setContentView(root);
    }

    private void onSaveClick() {
        Bundle data = new Bundle();
        data.putParcelableArrayList(Extra.LIST, mData);
        getParentFragmentManager().setFragmentResult(REQUEST_FILTER_EDIT, data);
        dismiss();
    }

    @Override
    public void onSpinnerOptionClick(SpinnerOption spinnerOption) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(spinnerOption.title)
                .setItems(spinnerOption.createAvailableNames(requireActivity()), (dialog, which) -> {
                    spinnerOption.value = spinnerOption.available.get(which);
                    mAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    spinnerOption.value = null;
                    mAdapter.notifyDataSetChanged();
                })
                .setPositiveButton(R.string.button_cancel, null)
                .show();
    }

    private void mergeDatabaseOptionValue(int key, DatabaseOption.Entry value) {
        for (BaseOption option : mData) {
            if (option.key == key && option instanceof DatabaseOption) {
                DatabaseOption databaseOption = (DatabaseOption) option;
                databaseOption.value = value;
                resetChildDependensies(databaseOption.childDependencies);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void resetChildDependensies(int... childs) {
        if (childs != null) {
            boolean changed = false;
            for (int key : childs) {
                for (BaseOption option : mData) {
                    if (option.key == key) {
                        option.reset();
                        changed = true;
                    }
                }
            }

            if (changed) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDatabaseOptionClick(DatabaseOption databaseOption) {
        BaseOption dependency = findDependencyByKey(databaseOption.parentDependencyKey);

        switch (databaseOption.type) {
            case DatabaseOption.TYPE_COUNTRY:
                SelectCountryDialog selectCountryDialog = new SelectCountryDialog();

                Bundle args = new Bundle();
                args.putInt(Extra.KEY, databaseOption.key);
                args.putInt(Extra.ACCOUNT_ID, mAccountId);
                selectCountryDialog.setArguments(args);
                getParentFragmentManager().setFragmentResultListener(SelectCountryDialog.REQUEST_CODE_COUNTRY, selectCountryDialog, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                        onDialogResult(result);
                    }
                });
                selectCountryDialog.show(getParentFragmentManager(), "countries");
                break;

            case DatabaseOption.TYPE_CITY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showCitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_UNIVERSITY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showUniversitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_FACULTY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int universityId = ((DatabaseOption) dependency).value.id;
                    showFacultiesDialog(databaseOption, universityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_CHAIR:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int facultyId = ((DatabaseOption) dependency).value.id;
                    showChairsDialog(databaseOption, facultyId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int cityId = ((DatabaseOption) dependency).value.id;
                    showSchoolsDialog(databaseOption, cityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL_CLASS:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showSchoolClassesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onSimpleNumberOptionClick(SimpleNumberOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setAllowEmpty(true)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setValue(option.value == null ? null : String.valueOf(option.value))
                .setCallback(newValue -> {
                    option.value = getIntFromEditable(newValue);
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private Integer getIntFromEditable(String line) {
        if (line == null || TextUtils.getTrimmedLength(line) == 0) {
            return null;
        }

        try {
            return Integer.valueOf(line);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void onSimpleTextOptionClick(SimpleTextOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(option.value)
                .setAllowEmpty(true)
                .setCallback(newValue -> {
                    option.value = newValue;
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    @Override
    public void onSimpleBooleanOptionChanged(SimpleBooleanOption option) {

    }

    @Override
    public void onOptionCleared(BaseOption option) {
        resetChildDependensies(option.childDependencies);
    }

    private void showCitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectCityDialog selectCityDialog = SelectCityDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectCityDialog.REQUEST_CODE_CITY, selectCityDialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        selectCityDialog.show(getParentFragmentManager(), "cities");
    }

    private void showUniversitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectUniversityDialog dialog = SelectUniversityDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectUniversityDialog.REQUEST_CODE_UNIVERSITY, dialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        dialog.show(getParentFragmentManager(), "universities");
    }

    private void showSchoolsDialog(DatabaseOption databaseOption, int cityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolsDialog dialog = SelectSchoolsDialog.newInstance(mAccountId, cityId, args);
        getParentFragmentManager().setFragmentResultListener(SelectSchoolsDialog.REQUEST_CODE_SCHOOL, dialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        dialog.show(getParentFragmentManager(), "schools");
    }

    private void showFacultiesDialog(DatabaseOption databaseOption, int universityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectFacultyDialog dialog = SelectFacultyDialog.newInstance(mAccountId, universityId, args);
        getParentFragmentManager().setFragmentResultListener(SelectFacultyDialog.REQUEST_CODE_FACULTY, dialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        dialog.show(getParentFragmentManager(), "faculties");
    }

    private void showChairsDialog(DatabaseOption databaseOption, int facultyId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectChairsDialog dialog = SelectChairsDialog.newInstance(mAccountId, facultyId, args);
        getParentFragmentManager().setFragmentResultListener(SelectChairsDialog.REQUEST_CODE_CHAIRS, dialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        dialog.show(getParentFragmentManager(), "chairs");
    }

    private void showSchoolClassesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolClassesDialog dialog = SelectSchoolClassesDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectSchoolClassesDialog.REQUEST_CODE_SCHOOL_CLASSES, dialog, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NotNull String requestKey, @NotNull Bundle result) {
                onDialogResult(result);
            }
        });
        dialog.show(getParentFragmentManager(), "school-classes");
    }

    private BaseOption findDependencyByKey(int key) {
        if (key == BaseOption.NO_DEPENDENCY) {
            return null;
        }

        for (BaseOption baseOption : mData) {
            if (baseOption.key == key) {
                return baseOption;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}
