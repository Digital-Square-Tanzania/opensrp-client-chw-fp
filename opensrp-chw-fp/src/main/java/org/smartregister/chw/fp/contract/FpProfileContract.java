package org.smartregister.chw.fp.contract;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.domain.Visit;

public interface FpProfileContract {
    interface View extends InteractorCallBack {

        void setProfileViewWithData();

        void setOverDueColor();

        void openMedicalHistory();

        void recordFp(MemberObject memberObject);

        void showProgressBar(boolean status);

        void hideView();

        Visit getLastVisit();

        boolean getIsClientUsingFpMethod();

        boolean isFirstVisit();

        void startPointOfServiceDeliveryForm();

        void startFpCounselingForm();

        void startFpScreeningForm();

        void startProvideFpMethod();

        void startProvideOtherServices();

        void startFpFollowupVisit();

    }

    interface Presenter {

        void fillProfileData(@Nullable MemberObject memberObject);

        void saveForm(String jsonString);

        @Nullable
        View getView();

        void refreshProfileBottom();

        void recordFpButton(String visitState);
    }

    interface Interactor {

        void refreshProfileInfo(MemberObject memberObject, InteractorCallBack callback);

        void saveRegistration(String jsonString, final FpProfileContract.InteractorCallBack callBack);
    }


    interface InteractorCallBack {
        void refreshMedicalHistory(boolean hasHistory);
    }
}