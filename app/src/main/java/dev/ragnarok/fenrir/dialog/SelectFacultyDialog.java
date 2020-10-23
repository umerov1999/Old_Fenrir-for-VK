package dev.ragnarok.fenrir.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.vkdatabase.FacultiesAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.database.Faculty;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectFacultyDialog extends AccountDependencyDialogFragment implements FacultiesAdapter.Listener {

    private static final int COUNT_PER_REQUEST = 1000;
    private int mAccountId;
    private int universityId;
    private IDatabaseInteractor mDatabaseInteractor;
    private ArrayList<Faculty> mData;
    private RecyclerView mRecyclerView;
    private FacultiesAdapter mAdapter;

    public static SelectFacultyDialog newInstance(int aid, int universityId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.UNIVERSITY_ID, universityId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectFacultyDialog selectCityDialog = new SelectFacultyDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
        universityId = getArguments().getInt(Extra.UNIVERSITY_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View root = inflater.inflate(R.layout.dialog_simple_recycler_view, container, false);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

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

        mAdapter = new FacultiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }
    }

    private void request(int offset) {
        appendDisposable(mDatabaseInteractor.getFaculties(mAccountId, universityId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(faculties -> onDataReceived(offset, faculties), t -> {/* TODO: 04.10.2017*/ }));
    }

    private void onDataReceived(int offset, List<Faculty> faculties) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(faculties);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Faculty faculty) {
        Intent intent = new Intent();
        intent.putExtra(Extra.FACULTY, faculty);
        intent.putExtra(Extra.ID, faculty.getId());
        intent.putExtra(Extra.TITLE, faculty.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}