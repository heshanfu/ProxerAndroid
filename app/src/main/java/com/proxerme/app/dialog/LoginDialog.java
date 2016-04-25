package com.proxerme.app.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.proxerme.app.R;
import com.proxerme.app.event.CancelledEvent;
import com.proxerme.app.util.ErrorHandler;
import com.proxerme.app.util.EventBusBuffer;
import com.proxerme.library.entity.LoginUser;
import com.proxerme.library.event.error.LoginErrorEvent;
import com.proxerme.library.event.success.LoginEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A dialog, which shows a login mask to the user. It also handles the login and shows a ProgressBar
 * to the user.
 *
 * @author Ruben Gees
 */
public class LoginDialog extends MainDialog {

    private static final String STATE_LOADING = "login_loading";

    ViewGroup root;

    @BindView(R.id.dialog_login_username_container)
    TextInputLayout usernameInputContainer;
    @BindView(R.id.dialog_login_password_container)
    TextInputLayout passwordInputContainer;

    @BindView(R.id.dialog_login_username)
    EditText usernameInput;
    @BindView(R.id.dialog_login_password)
    EditText passwordInput;
    @BindView(R.id.dialog_login_remember)
    CheckBox remember;

    @BindView(R.id.dialog_login_input_container)
    ViewGroup inputContainer;

    @BindView(R.id.dialog_login_progress)
    ProgressBar progress;

    private boolean loading;

    private Unbinder unbinder;

    private EventBusBuffer eventBusBuffer = new EventBusBuffer() {
        @Subscribe
        public void onLogin(LoginEvent event) {
            addToQueue(event);
        }

        @Subscribe
        public void onLoginError(LoginErrorEvent event) {
            addToQueue(event);
        }
    };

    public static void show(@NonNull AppCompatActivity activity) {
        new LoginDialog().show(activity.getSupportFragmentManager(), "login_dialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext()).autoDismiss(false)
                .title(R.string.dialog_login_title).positiveText(R.string.dialog_login_go)
                .negativeText(R.string.dialog_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        login();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        materialDialog.cancel();
                    }
                }).customView(initViews(), true);

        return builder.build();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            loading = savedInstanceState.getBoolean(STATE_LOADING);
        }

        handleVisibility();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        root = null;

        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        getMainApplication().getUserManager().cancelLogin();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        EventBus.getDefault().post(new CancelledEvent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_LOADING, loading);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginEvent event) {
        loading = false;

        dismiss();
    }

    @Subscribe(priority = 1, threadMode = ThreadMode.MAIN)
    public void onLoginError(LoginErrorEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);

        loading = false;

        handleVisibility();

        //noinspection ThrowableResultOfMethodCallIgnored
        Toast.makeText(getContext(),
                ErrorHandler.getMessageForErrorCode(getContext(),
                        event.getItem()), Toast.LENGTH_LONG).show();
    }

    private View initViews() {
        root = (ViewGroup) View.inflate(getContext(), R.layout.dialog_login, null);

        unbinder = ButterKnife.bind(this, root);

        LoginUser user = getMainApplication().getUserManager().getUser();

        if (user != null) {
            usernameInput.setText(user.getUsername());
            passwordInput.setText(user.getPassword());
        }

        passwordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login();

                    return true;
                }
                return false;
            }
        });

        usernameInput.addTextChangedListener(new OnTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetError(usernameInputContainer);
            }
        });

        passwordInput.addTextChangedListener(new OnTextListener() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetError(passwordInputContainer);
            }
        });

        return root;
    }

    private void login() {
        if (!loading) {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (checkInput(username, password)) {
                loading = true;
                handleVisibility();

                getMainApplication().getUserManager().login(new LoginUser(username, password),
                        remember.isChecked());
            }
        }
    }

    private void handleVisibility() {
        if (inputContainer != null && progress != null) {
            if (loading) {
                inputContainer.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            } else {
                inputContainer.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean checkInput(@NonNull String username, @NonNull String password) {
        boolean inputCorrect = true;

        if (TextUtils.isEmpty(username)) {
            inputCorrect = false;

            setError(usernameInputContainer,
                    getContext().getString(R.string.dialog_login_error_no_username));
        }

        if (TextUtils.isEmpty(password)) {
            inputCorrect = false;

            setError(passwordInputContainer,
                    getContext().getString(R.string.dialog_login_error_no_password));
        }
        return inputCorrect;
    }

    private void setError(@NonNull TextInputLayout container, @NonNull String error) {
        container.setError(error);
        container.setErrorEnabled(true);
    }

    private void resetError(@NonNull TextInputLayout container) {
        container.setError(null);
        container.setErrorEnabled(false);
    }

    @Override
    protected EventBusBuffer getEventBusBuffer() {
        return eventBusBuffer;
    }

    private static abstract class OnTextListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
