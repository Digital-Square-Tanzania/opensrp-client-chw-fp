package org.smartregister.chw.fp.actionhelper;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static org.smartregister.AllConstants.OPTIONS;

import android.content.Context;
import android.os.Build;

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
public class FpOtherServicesOfferedActionHelper extends FpVisitActionHelper {
    final BaseFpVisitContract.InteractorCallBack callBack;

    protected Context context;

    protected FpMemberObject fpMemberObject;

    protected String otherServicesOffered;

    protected Map<String, List<VisitDetail>> details;

    protected LinkedHashMap<String, BaseFpVisitAction> actionList;

    private String jsonPayload;

    public FpOtherServicesOfferedActionHelper(Context context, FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details, LinkedHashMap<String, BaseFpVisitAction> actionList, BaseFpVisitContract.InteractorCallBack callBack) {
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
            if (fpMemberObject.getGender().equalsIgnoreCase("male")) {
                JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject otherServicesOffered = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "other_services_offered");
                if (otherServicesOffered != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        otherServicesOffered.getJSONArray(OPTIONS).remove(1);
                    }
                }
            }
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
            otherServicesOffered = JsonFormUtils.getValue(jsonObject, "other_services_offered");

            if (otherServicesOffered.contains("hts")) {
                FpVisitActionHelper htsActionHelper = new FpOtherServicesOfferedHtsActionHelper(context, fpMemberObject);
                String actionName = context.getString(R.string.fp_other_services_hts);
                BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(htsActionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_OTHER_SERVICES_OFFERED_HTS).build();
                actionList.put(actionName, action);
            } else {
                actionList.remove(context.getString(R.string.fp_other_services_hts));
            }

            if (otherServicesOffered.contains("cervical_cancer")) {
                FpVisitActionHelper cervicalCancerActionHelper = new FpOtherServicesOfferedCervicalCancerActionHelper(context, fpMemberObject);
                String cervicalCancerActionName = context.getString(R.string.fp_other_services_cervical_cancer);
                BaseFpVisitAction cervicalCancerAction = getBuilder(cervicalCancerActionName).withOptional(false).withDetails(details).withHelper(cervicalCancerActionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_OTHER_SERVICES_OFFERED_CERVICAL_CANCER).build();
                actionList.put(cervicalCancerActionName, cervicalCancerAction);
            } else {
                actionList.remove(context.getString(R.string.fp_other_services_cervical_cancer));
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