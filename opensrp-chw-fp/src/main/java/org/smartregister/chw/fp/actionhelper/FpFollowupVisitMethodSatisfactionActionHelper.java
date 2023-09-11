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
public class FpFollowupVisitMethodSatisfactionActionHelper extends FpVisitActionHelper {
    protected Context context;
    protected MemberObject memberObject;
    protected String clientSatisfiedWithFpMethod;

    public FpFollowupVisitMethodSatisfactionActionHelper(Context context, MemberObject memberObject) {
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
            clientSatisfiedWithFpMethod = JsonFormUtils.getValue(jsonObject, "client_satisfied_with_fp_method");
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

    public BaseFpVisitAction.Builder getBuilder(String title) {
        return new BaseFpVisitAction.Builder(context, title);
    }
}