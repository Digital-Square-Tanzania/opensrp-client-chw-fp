package org.smartregister.chw.fp.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.contract.BaseFpProfileContract;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.FpUtil;

public class BaseFpProfileInteractor implements BaseFpProfileContract.Interactor {
    protected AppExecutors appExecutors;

    @VisibleForTesting
    BaseFpProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseFpProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void refreshProfileInfo(FpMemberObject fpMemberObject, BaseFpProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> {
            callback.refreshMedicalHistory(getVisit(FamilyPlanningConstants.EVENT_TYPE.FP_SCREENING, fpMemberObject) != null);
        });
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final String jsonString, final BaseFpProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            try {
                FpUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        appExecutors.diskIO().execute(runnable);
    }

    private Visit getVisit(String eventType, FpMemberObject fpMemberObject) {
        try {
            return FpLibrary.getInstance().visitRepository().getLatestVisit(fpMemberObject.getBaseEntityId(), eventType);
        } catch (Exception e) {
            return null;
        }
    }
}
