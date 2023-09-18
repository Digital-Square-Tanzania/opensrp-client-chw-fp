package org.smartregister.chw.fp.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.VisitDetail;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpCbdFollowupPillsAndCondomRefillActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected FpMemberObject fpMemberObject;

    protected String condomRefilled;

    private String jsonPayload;

    public FpCbdFollowupPillsAndCondomRefillActionHelper(Context context, FpMemberObject fpMemberObject) {
        this.context = context;
        this.fpMemberObject = fpMemberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            jsonObject.getJSONObject("global").put("fp_method_selected", fpMemberObject.getFpMethod());
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
            condomRefilled = JsonFormUtils.getValue(jsonObject, "condom_refilled");
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
        if (StringUtils.isNotBlank(condomRefilled)) {
            return BaseFpVisitAction.Status.COMPLETED;
        } else {
            return BaseFpVisitAction.Status.PENDING;
        }
    }

}