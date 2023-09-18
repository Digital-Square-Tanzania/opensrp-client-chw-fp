package org.smartregister.chw.fp.contract;

import android.content.Context;

import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;

import java.util.LinkedHashMap;
import java.util.Map;

public interface BaseFpVisitContract {

    interface View extends VisitView {

        Presenter presenter();

        Form getFormConfig();

        void startForm(BaseFpVisitAction visitAction);

        void startFormActivity(JSONObject jsonForm);

        void startFragment(BaseFpVisitAction visitAction);

        void redrawHeader(FpMemberObject fpMemberObject);

        void redrawVisitUI();

        void displayProgressBar(boolean state);

        Map<String, BaseFpVisitAction> getVisitActions();

        void close();

        void submittedAndClose();

        Presenter getPresenter();

        /**
         * Save the received data into the events table
         * Start aggregation of all events and persist results into the events table
         */
        void submitVisit();

        void initializeActions(LinkedHashMap<String, BaseFpVisitAction> map);

        Context getContext();

        void displayToast(String message);

        Boolean getEditMode();

        void onMemberDetailsReloaded(FpMemberObject fpMemberObject);
    }

    interface VisitView {

        /**
         * Results action when a dialog is opened and returns a payload
         *
         * @param jsonString
         */
        void onDialogOptionUpdated(String jsonString);

        Context getMyContext();
    }

    interface Presenter {

        void startForm(String formName, String memberID, String currentLocationId);

        /**
         * Recall this method to redraw ui after every submission
         *
         * @return
         */
        boolean validateStatus();

        /**
         * Preload header and visit
         */
        void initialize();

        void submitVisit();

        void reloadMemberDetails(String memberID);
    }

    interface Model {

        JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception;

    }

    interface Interactor {

        void reloadMemberDetails(String memberID, InteractorCallBack callBack);

        FpMemberObject getMemberClient(String memberID);

        void saveRegistration(String jsonString, boolean isEditMode, final InteractorCallBack callBack);

        void calculateActions(View view, FpMemberObject fpMemberObject, InteractorCallBack callBack);

        void submitVisit(boolean editMode, String memberID, Map<String, BaseFpVisitAction> map, InteractorCallBack callBack);
    }

    interface InteractorCallBack {

        void onMemberDetailsReloaded(FpMemberObject fpMemberObject);

        void onRegistrationSaved(boolean isEdit);

        void preloadActions(LinkedHashMap<String, BaseFpVisitAction> map);

        void onSubmitted(boolean successful);
    }
}