package org.smartregister.chw.fp_sample.activity;

import static org.smartregister.chw.fp.util.FamilyPlanningConstants.ENCOUNTER_TYPE;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.factory.FileSourceFactoryHelper;

import org.json.JSONObject;
import org.smartregister.chw.fp.activity.BaseFpProfileActivity;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.JsonFormUtils;

import timber.log.Timber;


public class FpMemberProfileActivity extends BaseFpProfileActivity {
    public static Visit lastVisit;

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, FpMemberProfileActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void setupViews() {
        initializeFloatingMenu();
        Visit lastVisit = getLastVisit();

        if (isFirstVisit()) {
            if (lastVisit == null) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.record_point_of_service_delivery);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_POINT_OF_SERVICE_DELIVERY)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.provide_fp_counseling);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_COUNSELING)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.fp_screening);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_SCREENING)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.provide_fp_method);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_PROVIDE_METHOD)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.provide_other_services);
            }
        } else {
            if (lastVisit == null || lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_OTHER_SERVICES)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.record_fp_followup_visit);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_FOLLOW_UP_VISIT)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.provide_fp_method);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_PROVIDE_METHOD)) {
                textViewRecordFp.setText(org.smartregister.chw.fp.R.string.provide_other_services);
            }
        }
    }

    @Override
    public void recordFp(FpMemberObject fpMemberObject) {
        FpScreeningActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public Visit getLastVisit() {
        return lastVisit;
    }

    @Override
    public boolean isFirstVisit() {
        return true;
    }

    @Override
    public void startPointOfServiceDeliveryForm() {
        try {
            startForm(FamilyPlanningConstants.FORMS.FP_POINT_OF_SERVICE_DELIVERY);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFpCounselingForm() {
        try {
            startForm(FamilyPlanningConstants.FORMS.FP_COUNSELING);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFpScreeningForm() {
        FpScreeningActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public void startProvideFpMethod() {
        try {
            startForm(FamilyPlanningConstants.FORMS.FP_PROVISION_OF_FP_METHOD);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startProvideOtherServices() {
        FpOtherServicesActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public void startFpFollowupVisit() {

    }

    @Override
    protected FpMemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    private void startForm(String formName) throws Exception {
        JSONObject jsonForm = FileSourceFactoryHelper.getFileSource("").getFormFromFile(getApplicationContext(), formName);

        if (formName.equalsIgnoreCase(FamilyPlanningConstants.FORMS.FP_PROVISION_OF_FP_METHOD) && jsonForm != null) {
            jsonForm.getJSONObject("global").put("fp_method_selected", fpMemberObject.getFpMethod());
            jsonForm.getJSONObject("global").put("sex", fpMemberObject.getGender());
        }

        String currentLocationId = "Tanzania";
        if (jsonForm != null) {
            jsonForm.getJSONObject("metadata").put("encounter_location", currentLocationId);
            Intent intent = new Intent(this, JsonWizardFormActivity.class);
            intent.putExtra("json", jsonForm.toString());

            Form form = new Form();
            form.setWizard(true);
            form.setNextLabel("Next");
            form.setPreviousLabel("Previous");
            form.setSaveLabel("Save");
            form.setHideSaveLabel(true);

            intent.putExtra("form", form);
            startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FamilyPlanningConstants.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(FamilyPlanningConstants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(ENCOUNTER_TYPE);
                if (lastVisit == null)
                    lastVisit = new Visit();
                lastVisit.setVisitType(encounterType);

                if (encounterType.equalsIgnoreCase(FamilyPlanningConstants.EVENT_TYPE.FP_COUNSELING)) {
                    fpMemberObject.setFpMethod(JsonFormUtils.getValue(form, "selected_fp_method_after_counseling"));
                    EntryActivity.getSampleMember().setFpMethod(JsonFormUtils.getValue(form, "selected_fp_method_after_counseling"));
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        delayRefreshSetupViews();
    }

    private void delayRefreshSetupViews() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(this::setupViews, 300);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}