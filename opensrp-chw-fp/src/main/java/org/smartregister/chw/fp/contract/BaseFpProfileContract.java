package org.smartregister.chw.fp.contract;

import android.content.Context;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;

public interface BaseFpProfileContract {
    interface View extends InteractorCallBack {
        Context getContext();

        void setProfileViewWithData();

        void openMedicalHistory();

        void recordFp(FpMemberObject fpMemberObject);

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

        void setFollowUpButtonOverdue();

        void setFollowUpButtonDue();

        void hideFollowUpVisitButton();

        void showFollowUpVisitButton();

    }

    interface Presenter {

        void fillProfileData(@Nullable FpMemberObject fpMemberObject);

        void saveForm(String jsonString);

        @Nullable
        View getView();

        void refreshProfileBottom();

        void recordFpButton(String visitState);
    }

    interface Interactor {

        void refreshProfileInfo(FpMemberObject fpMemberObject, InteractorCallBack callback);

        void saveRegistration(String jsonString, final BaseFpProfileContract.InteractorCallBack callBack);
    }


    interface InteractorCallBack {
        void refreshMedicalHistory(boolean hasHistory);
    }
}