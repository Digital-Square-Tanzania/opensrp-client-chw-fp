package org.smartregister.chw.fp.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.contract.FpProfileContract;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.fp.util.Constants;
import org.smartregister.chw.fp.util.FpUtil;

public class BaseFpProfileInteractor implements FpProfileContract.Interactor {
    protected AppExecutors appExecutors;

    @VisibleForTesting
    BaseFpProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseFpProfileInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void refreshProfileInfo(MemberObject memberObject, FpProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> {
            callback.refreshMedicalHistory(getVisit(Constants.EVENT_TYPE.FP_FOLLOW_UP_VISIT, memberObject) != null);
        });
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final String jsonString, final FpProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            try {
                FpUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        appExecutors.diskIO().execute(runnable);
    }

    private Visit getVisit(String eventType, MemberObject memberObject) {
        try {
            return FpLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), eventType);
        } catch (Exception e) {
            return null;
        }
    }
}
