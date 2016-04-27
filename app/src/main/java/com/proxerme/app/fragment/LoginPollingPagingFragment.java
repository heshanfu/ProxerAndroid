package com.proxerme.app.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;

import com.proxerme.app.R;
import com.proxerme.app.adapter.PagingAdapter;
import com.proxerme.app.dialog.LoginDialog;
import com.proxerme.app.event.DialogCancelledEvent;
import com.proxerme.app.manager.UserManager;
import com.proxerme.app.util.Utils;
import com.proxerme.library.event.IListEvent;
import com.proxerme.library.event.error.ErrorEvent;
import com.proxerme.library.event.error.LoginErrorEvent;
import com.proxerme.library.event.success.LoginEvent;
import com.proxerme.library.event.success.LogoutEvent;
import com.proxerme.library.interfaces.IdItem;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
public abstract class LoginPollingPagingFragment<T extends IdItem & Parcelable,
        A extends PagingAdapter<T, ?>, E extends IListEvent<T>, EE extends ErrorEvent>
        extends PollingPagingFragment<T, A, E, EE> {

    private boolean loggedIn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateLoggedIn();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateLoggedIn();

        if (!loggedIn) {
            if (getMainApplication().getUserManager().isWorking()) {
                showCurrentLogin();
            } else {
                showLoginError();
            }

            stopLoading();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginEvent event) {
        if (!loggedIn) {
            loggedIn = true;

            doLoad(getFirstPage(), true, true);

            if (getParentActivity() != null) {
                getParentActivity().clearMessage();
            }
        } else {
            doLoad(getFirstPage(), true, true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogout(LogoutEvent event) {
        loggedIn = false;

        cancelRequest();
        clear();
        showLoginError();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginError(LoginErrorEvent event) {
        showLoginError();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDialogCancelled(DialogCancelledEvent event) {
        if (!getMainApplication().getUserManager().isLoggedIn()) {
            showLoginError();
        }
    }

    @Override
    protected boolean canLoad() {
        return loggedIn;
    }

    private void showCurrentLogin() {
        if (getParentActivity() != null) {
            getParentActivity().showMessage(getString(R.string.fragment_login_logging_in),
                    getString(R.string.dialog_cancel), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMainApplication().getUserManager().cancelLogin();

                            showLoginError();
                        }
                    });
        }
    }

    private void showLoginError() {
        if (getParentActivity() != null) {
            getParentActivity().showMessage(getString(R.string.error_not_logged_in),
                    getString(R.string.error_do_login), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utils.areActionsPossible(getParentActivity())) {
                                LoginDialog.show(getParentActivity());
                            }
                        }
                    });
        }
    }

    private void updateLoggedIn() {
        UserManager userManager = getMainApplication().getUserManager();

        loggedIn = userManager.isLoggedIn() && !userManager.isWorking();
    }

}