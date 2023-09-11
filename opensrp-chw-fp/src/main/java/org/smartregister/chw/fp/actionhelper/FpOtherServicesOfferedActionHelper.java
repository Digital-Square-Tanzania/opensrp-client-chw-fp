package org.smartregister.chw.fp.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.contract.BaseFpVisitContract;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.domain.VisitDetail;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.fp.util.Constants;
import org.smartregister.chw.fp.util.JsonFormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * FP Activity Action Helper
 */
public class FpOtherServicesOfferedActionHelper extends FpVisitActionHelper {
    final BaseFpVisitContract.InteractorCallBack callBack;

    protected Context context;

    protected MemberObject memberObject;

    protected String otherServicesOffered;

    protected Map<String, List<VisitDetail>> details;

    protected LinkedHashMap<String, BaseFpVisitAction> actionList;

    public FpOtherServicesOfferedActionHelper(Context context, MemberObject memberObject, Map<String, List<VisitDetail>> details, LinkedHashMap<String, BaseFpVisitAction> actionList, BaseFpVisitContract.InteractorCallBack callBack) {
        this.context = context;
        this.memberObject = memberObject;
        this.details = details;
        this.actionList = actionList;
        this.callBack = callBack;
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
            otherServicesOffered = JsonFormUtils.getValue(jsonObject, "other_services_offered");

            if (otherServicesOffered.contains("hts")) {
                FpVisitActionHelper htsActionHelper = new FpOtherServicesOfferedHtsActionHelper(context, memberObject);
                String actionName = context.getString(R.string.fp_other_services_hts);
                BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(htsActionHelper).withFormName(Constants.FORMS.FP_OTHER_SERVICES_OFFERED_HTS).build();
                actionList.put(actionName, action);
            }

            if (otherServicesOffered.contains("cervical_cancer")) {
                FpVisitActionHelper cervicalCancerActionHelper = new FpOtherServicesOfferedCervicalCancerActionHelper(context, memberObject);
                String cervicalCancerActionName = context.getString(R.string.fp_other_services_cervical_cancer);
                BaseFpVisitAction cervicalCancerAction = getBuilder(cervicalCancerActionName).withOptional(false).withDetails(details).withHelper(cervicalCancerActionHelper).withFormName(Constants.FORMS.FP_OTHER_SERVICES_OFFERED_CERVICAL_CANCER).build();
                actionList.put(cervicalCancerActionName, cervicalCancerAction);
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
        if (StringUtils.isNotBlank(otherServicesOffered)) {
            return BaseFpVisitAction.Status.COMPLETED;
        } else {
            return BaseFpVisitAction.Status.PENDING;
        }
    }

    public BaseFpVisitAction.Builder getBuilder(String title) {
        return new BaseFpVisitAction.Builder(context, title);
    }
}