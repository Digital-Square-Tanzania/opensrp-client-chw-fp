package org.smartregister.chw.fp.actionhelper;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.contract.BaseFpVisitContract;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.VisitDetail;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.JsonFormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpCbdFollowupMethodSatisfactionActionHelper extends FpVisitActionHelper {
    final BaseFpVisitContract.InteractorCallBack callBack;
    protected Context context;
    protected FpMemberObject fpMemberObject;

    protected Map<String, List<VisitDetail>> details;

    protected LinkedHashMap<String, BaseFpVisitAction> actionList;

    protected String clientSatisfiedWithFpMethod;
    protected String client_want_to_continue_same_method;

    private String jsonPayload;

    public FpCbdFollowupMethodSatisfactionActionHelper(Context context, FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details, LinkedHashMap<String, BaseFpVisitAction> actionList, final BaseFpVisitContract.InteractorCallBack callBack) {
        this.context = context;
        this.fpMemberObject = fpMemberObject;
        this.details = details;
        this.actionList = actionList;
        this.callBack = callBack;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            jsonObject.getJSONObject("global").put("fp_method", fpMemberObject.getFpMethod());
            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            clientSatisfiedWithFpMethod = JsonFormUtils.getValue(jsonObject, "client_satisfied_with_fp_method");
            client_want_to_continue_same_method = JsonFormUtils.getValue(jsonObject, "client_want_to_continue_same_method");

            if (client_want_to_continue_same_method.contains("yes") &&
                    (fpMemberObject.getFpMethod().equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_COC) ||
                            fpMemberObject.getFpMethod().equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_POP) ||
                            fpMemberObject.getFpMethod().equalsIgnoreCase(FamilyPlanningConstants.DBConstants.FP_CONDOM)
                    )
            ) {
                FpVisitActionHelper actionHelper = new FpCbdFollowupPillsAndCondomRefillActionHelper(context, fpMemberObject);
                String actionName = context.getString(R.string.fp_method_refill);
                BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_CBD_FOLLOWUP_REFILL_CONDOMS_AND_PILLS).build();
                actionList.put(actionName, action);
            } else {
                actionList.remove(context.getString(R.string.fp_method_refill));
            }


            //Calling the callback method to preload the actions in the actions list.
            new AppExecutors().mainThread().execute(() -> callBack.preloadActions(actionList));

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseFpVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(clientSatisfiedWithFpMethod)) {
            return BaseFpVisitAction.Status.COMPLETED;
        } else {
            return BaseFpVisitAction.Status.PENDING;
        }
    }

    @Override
    public String postProcess(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);

            String clientWantsToSwitchOrStop = JsonFormUtils.getValue(jsonObject, "client_want_to_switch_stop");
            String clientWantToSwitchStopContinue = JsonFormUtils.getValue(jsonObject, "client_want_to_switch_stop_continue");
            String outcome = clientWantToSwitchStopContinue != null ? clientWantToSwitchStopContinue : clientWantsToSwitchOrStop;
            JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
            JSONObject followupOutcome = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "followup_outcome");
            if (!StringUtils.isBlank(clientWantsToSwitchOrStop)) {
                if (followupOutcome != null) {
                    followupOutcome.put(VALUE, outcome);
                }
            } else {
                if (followupOutcome != null) {
                    followupOutcome.put(VALUE, "continuing_with_fp_method");
                }
            }
            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }


        return super.postProcess(jsonPayload);
    }


    public BaseFpVisitAction.Builder getBuilder(String title) {
        return new BaseFpVisitAction.Builder(context, title);
    }
}