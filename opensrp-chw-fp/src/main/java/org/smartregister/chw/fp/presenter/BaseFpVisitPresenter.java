package org.smartregister.chw.fp.presenter;

import org.json.JSONObject;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.contract.BaseFpVisitContract;
import org.smartregister.chw.fp.util.JsonFormUtils;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import timber.log.Timber;

public class BaseFpVisitPresenter implements BaseFpVisitContract.Presenter, BaseFpVisitContract.InteractorCallBack {

    protected WeakReference<BaseFpVisitContract.View> view;
    protected BaseFpVisitContract.Interactor interactor;
    protected FpMemberObject fpMemberObject;

    public BaseFpVisitPresenter(FpMemberObject fpMemberObject, BaseFpVisitContract.View view, BaseFpVisitContract.Interactor interactor) {
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
        this.fpMemberObject = fpMemberObject;
    }

    @Override
    public void startForm(String formName, String memberID, String currentLocationId) {
        try {
            if (view.get() != null) {
                JSONObject jsonObject = FormUtils.getInstance(view.get().getContext()).getFormJson(formName);
                JsonFormUtils.getRegistrationForm(jsonObject, memberID, currentLocationId);
                view.get().startFormActivity(jsonObject);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public boolean validateStatus() {
        return false;
    }

    @Override
    public void initialize() {
        view.get().displayProgressBar(true);
        view.get().redrawHeader(fpMemberObject);
        interactor.calculateActions(view.get(), fpMemberObject, this);
    }

    @Override
    public void submitVisit() {
        if (view.get() != null) {
            view.get().displayProgressBar(true);
            interactor.submitVisit(view.get().getEditMode(), fpMemberObject.getBaseEntityId(), view.get().getPmtctHomeVisitActions(), this);
        }
    }

    @Override
    public void reloadMemberDetails(String memberID) {
        view.get().displayProgressBar(true);
        interactor.reloadMemberDetails(memberID, this);
    }

    @Override
    public void onMemberDetailsReloaded(FpMemberObject fpMemberObject) {
        if (view.get() != null) {
            this.fpMemberObject = fpMemberObject;
            view.get().displayProgressBar(false);
            view.get().onMemberDetailsReloaded(fpMemberObject);
        }
    }

    @Override
    public void onRegistrationSaved(boolean isEdit) {
        Timber.v("onRegistrationSaved");
    }

    @Override
    public void preloadActions(LinkedHashMap<String, BaseFpVisitAction> map) {
        if (view.get() != null)
            view.get().initializeActions(map);
    }

    @Override
    public void onSubmitted(boolean successful) {
        if (view.get() != null) {
            view.get().displayProgressBar(false);
            if (successful) {
                view.get().submittedAndClose();
            } else {
                view.get().displayToast(view.get().getContext().getString(R.string.error_unable_save_home_visit));
            }
        }
    }
}
