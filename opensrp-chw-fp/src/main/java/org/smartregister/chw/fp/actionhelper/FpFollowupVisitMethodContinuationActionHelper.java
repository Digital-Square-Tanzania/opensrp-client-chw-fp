package org.smartregister.chw.fp.actionhelper;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static com.vijay.jsonwizard.constants.JsonFormConstants.VALUE;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.JsonFormUtils;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpFollowupVisitMethodContinuationActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected FpMemberObject fpMemberObject;

    protected String methodExpired;

    public FpFollowupVisitMethodContinuationActionHelper(Context context, FpMemberObject fpMemberObject) {
        this.context = context;
        this.fpMemberObject = fpMemberObject;
    }

    /**
     * set preprocessed status to be inert
     *
     * @return null
     */
    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            methodExpired = JsonFormUtils.getValue(jsonObject, "method_expired");
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
        if (StringUtils.isNotBlank(methodExpired)) {
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

            JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
            JSONObject followupOutcome = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "followup_outcome");
            if (!StringUtils.isBlank(clientWantsToSwitchOrStop)) {
                if (followupOutcome != null) {
                    followupOutcome.put(VALUE, clientWantsToSwitchOrStop);
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
}