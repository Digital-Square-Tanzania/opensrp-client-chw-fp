package org.smartregister.chw.fp.actionhelper;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static org.smartregister.util.JsonFormUtils.VALUE;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpMethodScreeningPastObstetricHistoryActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected FpMemberObject fpMemberObject;

    private Boolean isFormComplete;

    public FpMethodScreeningPastObstetricHistoryActionHelper(Context context, FpMemberObject fpMemberObject) {
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
            JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);

            List<Boolean> expansionPanelsFilled = new ArrayList<Boolean>();
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.has(VALUE) && field.getJSONArray(VALUE).length() > 0) {
                    expansionPanelsFilled.add(true);
                }
            }
            if (expansionPanelsFilled.size() == fields.length()) {
                isFormComplete = true;
            } else if (expansionPanelsFilled.size() > 0 && expansionPanelsFilled.size() < fields.length()) {
                isFormComplete = false;
            } else if (expansionPanelsFilled.size() == 0) {
                isFormComplete = null;
            }

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
        if (isFormComplete == null) return BaseFpVisitAction.Status.PENDING;
        else if (isFormComplete) return BaseFpVisitAction.Status.COMPLETED;
        else return BaseFpVisitAction.Status.PARTIALLY_COMPLETED;
    }
}