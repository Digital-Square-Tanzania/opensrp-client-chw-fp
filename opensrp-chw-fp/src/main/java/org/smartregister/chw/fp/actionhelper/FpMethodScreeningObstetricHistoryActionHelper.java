package org.smartregister.chw.fp.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.JsonFormUtils;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpMethodScreeningObstetricHistoryActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected MemberObject memberObject;

    protected String numberOfPregnancies;

    protected String numberOfMiscarriages;

    protected String numberStillBirths;

    protected String numberLiveBirths;

    protected String numberChildrenAlive;

    protected String dateLastDelivery;

    protected String isClientBreastfeeding;

    public FpMethodScreeningObstetricHistoryActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
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
            numberOfPregnancies = JsonFormUtils.getValue(jsonObject, "number_of_pregnancies");
            numberOfMiscarriages = JsonFormUtils.getValue(jsonObject, "number_of_miscarriages");
            numberStillBirths = JsonFormUtils.getValue(jsonObject, "number_still_births");
            numberLiveBirths = JsonFormUtils.getValue(jsonObject, "number_live_births");
            numberChildrenAlive = JsonFormUtils.getValue(jsonObject, "number_children_alive");
            dateLastDelivery = JsonFormUtils.getValue(jsonObject, "date_last_delivery");
            isClientBreastfeeding = JsonFormUtils.getValue(jsonObject, "is_client_breastfeeding");
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
        if (StringUtils.isNotBlank(numberOfPregnancies)) {
            return BaseFpVisitAction.Status.COMPLETED;
        } else {
            return BaseFpVisitAction.Status.PENDING;
        }
    }
}