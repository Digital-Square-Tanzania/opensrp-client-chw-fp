package org.smartregister.chw.fp.presenter;

import android.content.Context;

import androidx.annotation.Nullable;

import org.smartregister.chw.fp.contract.BaseFpProfileContract;
import org.smartregister.chw.fp.domain.FpMemberObject;

import java.lang.ref.WeakReference;

import timber.log.Timber;


public class BaseFpProfilePresenter implements BaseFpProfileContract.Presenter {
    protected WeakReference<BaseFpProfileContract.View> view;
    protected FpMemberObject fpMemberObject;
    protected BaseFpProfileContract.Interactor interactor;
    protected Context context;

    public BaseFpProfilePresenter(BaseFpProfileContract.View view, BaseFpProfileContract.Interactor interactor, FpMemberObject fpMemberObject) {
        this.view = new WeakReference<>(view);
        this.fpMemberObject = fpMemberObject;
        this.interactor = interactor;
    }

    @Override
    public void fillProfileData(FpMemberObject fpMemberObject) {
        if (fpMemberObject != null && getView() != null) {
            getView().setProfileViewWithData();
        }
    }

    @Override
    public void recordFpButton(@Nullable String visitState) {
        if (getView() == null) {
            return;
        }

        if (("OVERDUE").equals(visitState) || ("DUE").equals(visitState)) {
            if (("OVERDUE").equals(visitState)) {
                getView().setFollowUpButtonOverdue();
            }
        } else {
            getView().hideView();
        }
    }

    @Override
    @Nullable
    public BaseFpProfileContract.View getView() {
        if (view != null && view.get() != null)
            return view.get();

        return null;
    }

    @Override
    public void refreshProfileBottom() {
        interactor.refreshProfileInfo(fpMemberObject, getView());
    }

    @Override
    public void saveForm(String jsonString) {
        try {
            interactor.saveRegistration(jsonString, getView());
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
