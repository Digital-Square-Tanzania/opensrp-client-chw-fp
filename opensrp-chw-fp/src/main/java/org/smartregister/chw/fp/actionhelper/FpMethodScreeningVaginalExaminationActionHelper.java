package org.smartregister.chw.fp.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.JsonFormUtils;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpMethodScreeningVaginalExaminationActionHelper extends FpVisitActionHelper {
    protected Context context;

    protected FpMemberObject fpMemberObject;

    protected String wasVaginalExaminationPerfomed;

    public FpMethodScreeningVaginalExaminationActionHelper(Context context, FpMemberObject fpMemberObject) {
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
            wasVaginalExaminationPerfomed = JsonFormUtils.getValue(jsonObject, "was_vaginal_examination_performed");
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
        if (StringUtils.isNotBlank(wasVaginalExaminationPerfomed)) {
            return BaseFpVisitAction.Status.COMPLETED;
        } else {
            return BaseFpVisitAction.Status.PENDING;
        }
    }
}