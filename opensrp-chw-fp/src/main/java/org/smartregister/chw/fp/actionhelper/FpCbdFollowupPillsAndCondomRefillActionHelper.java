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
public class FpCbdFollowupPillsAndCondomRefillActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected FpMemberObject fpMemberObject;

    protected String condomRefilled;

    public FpCbdFollowupPillsAndCondomRefillActionHelper(Context context, FpMemberObject fpMemberObject) {
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