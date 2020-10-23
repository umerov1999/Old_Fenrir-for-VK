package dev.ragnarok.fenrir.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpDialogFragment;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.DirectAuthPresenter;
import dev.ragnarok.fenrir.mvp.view.IDirectAuthView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.Objects;

public class DirectAuthDialog extends BaseMvpDialogFragment<DirectAuthPresenter, IDirectAuthView> implements IDirectAuthView {

    public static final String ACTION_LOGIN_COMPLETE = "ACTION_LOGIN_COMPLETE";
    public static final String ACTION_LOGIN_VIA_WEB = "ACTION_LOGIN_VIA_WEB";
    public static final String ACTION_VALIDATE_VIA_WEB = "ACTION_VALIDATE_VIA_WEB";
    private TextInputEditText mLogin;
    private TextInputEditText mPassword;
    private TextInputEditText mCaptcha;
    private TextInputEditText mSmsCode;
    private View mSmsCodeRoot;
    private View mContentRoot;
    private View mLoadingRoot;
    private View mCaptchaRoot;
    private ImageView mCaptchaImage;
    private View mEnterAppCodeRoot;
    private TextInputEditText mAppCode;

    public static DirectAuthDialog newInstance() {
        Bundle args = new Bundle();
        DirectAuthDialog fragment = new DirectAuthDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public DirectAuthDialog targetTo(Fragment fragment, int code) {
        setTargetFragment(fragment, code);
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());

        View view = View.inflate(requireActivity(), R.layout.dialog_direct_auth, null);

        mLogin = view.findViewById(R.id.field_username);
        mLogin.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireLoginEdit(s);
            }
        });

        mPassword = view.findViewById(R.id.field_password);
        mPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().firePasswordEdit(s);
            }
        });

        mEnterAppCodeRoot = view.findViewById(R.id.field_app_code_root);
        mAppCode = view.findViewById(R.id.field_app_code);
        mAppCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireAppCodeEdit(s);
            }
        });

        view.findViewById(R.id.button_send_code_via_sms).setOnClickListener(view1 -> getPresenter().fireButtonSendCodeViaSmsClick());

        mSmsCodeRoot = view.findViewById(R.id.field_sms_code_root);
        mSmsCode = view.findViewById(R.id.field_sms_code);
        mSmsCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireSmsCodeEdit(s);
            }
        });

        mContentRoot = view.findViewById(R.id.content_root);
        mLoadingRoot = view.findViewById(R.id.loading_root);
        mCaptchaRoot = view.findViewById(R.id.captcha_root);
        mCaptcha = view.findViewById(R.id.field_captcha);
        mCaptcha.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireCaptchaEdit(s);
            }
        });
        mCaptchaImage = view.findViewById(R.id.captcha_img);

        builder.setView(view);
        builder.setPositiveButton(R.string.button_login, null);
        if (Constants.IS_HAS_LOGIN_WEB)
            builder.setNeutralButton(R.string.button_login_via_web, (dialogInterface, i) -> getPresenter().fireLoginViaWebClick());
        builder.setTitle(R.string.login_title);
        builder.setIcon(R.drawable.logo_vk);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        fireViewCreated();
        return dialog;
    }

    @NotNull
    @Override
    public IPresenterFactory<DirectAuthPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DirectAuthPresenter(saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Button buttonLogin = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        buttonLogin.setOnClickListener(view -> getPresenter().fireLoginClick());
    }

    @Override
    public void setLoginButtonEnabled(boolean enabled) {
        Button buttonLogin = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);

        if (Objects.nonNull(buttonLogin)) {
            buttonLogin.setEnabled(enabled);
        }
    }

    @Override
    public void setSmsRootVisible(boolean visible) {
        if (Objects.nonNull(mSmsCodeRoot)) {
            mSmsCodeRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setAppCodeRootVisible(boolean visible) {
        if (Objects.nonNull(mEnterAppCodeRoot)) {
            mEnterAppCodeRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void moveFocusToSmsCode() {
        if (Objects.nonNull(mSmsCode)) {
            mSmsCode.requestFocus();
        }
    }

    @Override
    public void moveFocusToAppCode() {
        if (Objects.nonNull(mSmsCode)) {
            mAppCode.requestFocus();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (Objects.nonNull(mLoadingRoot)) {
            mLoadingRoot.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        if (Objects.nonNull(mContentRoot)) {
            mContentRoot.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void setCaptchaRootVisible(boolean visible) {
        if (Objects.nonNull(mCaptchaRoot)) {
            mCaptchaRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayCaptchaImage(String img) {
        if (Objects.nonNull(mCaptchaImage)) {
            PicassoInstance.with()
                    .load(img)
                    .placeholder(R.drawable.background_gray)
                    .into(mCaptchaImage);
        }
    }

    @Override
    public void moveFocusToCaptcha() {
        if (Objects.nonNull(mCaptcha)) {
            mCaptcha.requestFocus();
        }
    }

    @Override
    public void hideKeyboard() {
        try {
            InputMethodManager im = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(mLogin.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mPassword.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mCaptcha.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mSmsCode.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void returnSuccessToParent(int userId, String accessToken, String Login, String Password, String twoFA) {
        returnResultAndDissmiss(new Intent(ACTION_LOGIN_COMPLETE).putExtra(Extra.TOKEN, accessToken).putExtra(Extra.USER_ID, userId).putExtra(Extra.LOGIN, Login).putExtra(Extra.PASSWORD, Password).putExtra(Extra.TWOFA, twoFA));
    }

    @Override
    public void returnSuccessValidation(String url, String Login, String Password, String twoFA) {
        returnResultAndDissmiss(new Intent(ACTION_VALIDATE_VIA_WEB).putExtra(Extra.URL, url).putExtra(Extra.LOGIN, Login).putExtra(Extra.PASSWORD, Password).putExtra(Extra.TWOFA, twoFA));
    }

    private void returnResultAndDissmiss(Intent data) {
        if (Objects.nonNull(getTargetFragment())) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
        dismiss();
    }

    @Override
    public void returnLoginViaWebAction() {
        returnResultAndDissmiss(new Intent(ACTION_LOGIN_VIA_WEB));
    }
}
