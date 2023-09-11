package org.smartregister.chw.fp.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.fp.contract.FpRegisterContract;
import org.smartregister.chw.fp.util.FpUtil;
import org.smartregister.chw.fp.util.AppExecutors;

public class BaseFpRegisterInteractor implements FpRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    BaseFpRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public BaseFpRegisterInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void saveRegistration(final String jsonString, final FpRegisterContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            try {
                FpUtil.saveFormEvent(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.onRegistrationSaved());
        };
        appExecutors.diskIO().execute(runnable);
    }
}
