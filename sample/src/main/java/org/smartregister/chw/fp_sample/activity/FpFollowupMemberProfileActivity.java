package org.smartregister.chw.fp_sample.activity;

import static org.smartregister.chw.fp.util.Constants.ENCOUNTER_TYPE;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.factory.FileSourceFactoryHelper;

import org.json.JSONObject;
import org.smartregister.chw.fp.activity.BaseFpProfileActivity;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.Constants;
import org.smartregister.chw.fp.util.JsonFormUtils;

import timber.log.Timber;


public class FpFollowupMemberProfileActivity extends BaseFpProfileActivity {
    public static Visit lastVisit;

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, FpFollowupMemberProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        memberObject.setFpMethod("jadelle");
    }

    @Override
    public void recordFp(MemberObject memberObject) {
        FpScreeningActivity.startMe(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    public Visit getLastVisit() {
        return lastVisit;
    }

    @Override
    public boolean getIsClientUsingFpMethod() {
        return true;
    }

    @Override
    public boolean isFirstVisit() {
        return false;
    }

    @Override
    public void startPointOfServiceDeliveryForm() {
        try {
            startForm(Constants.FORMS.FP_POINT_OF_SERVICE_DELIVERY);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFpCounselingForm() {
        try {
            startForm(Constants.FORMS.FP_COUNSELING);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startFpScreeningForm() {
        FpScreeningActivity.startMe(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    public void startProvideFpMethod() {
        try {
            startForm(Constants.FORMS.FP_PROVISION_OF_FP_METHOD);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startProvideOtherServices() {
        FpOtherServicesActivity.startMe(this, memberObject.getBaseEntityId(), false);
    }

    @Override
    public void startFpFollowupVisit() {

    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    private void startForm(String formName) throws Exception {
        JSONObject jsonForm = FileSourceFactoryHelper.getFileSource("").getFormFromFile(getApplicationContext(), formName);

        if (formName.equalsIgnoreCase(Constants.FORMS.FP_PROVISION_OF_FP_METHOD) && jsonForm != null) {
            jsonForm.getJSONObject("global").put("fp_method_selected", memberObject.getFpMethod());
            jsonForm.getJSONObject("global").put("sex", memberObject.getGender());
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
            startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(ENCOUNTER_TYPE);
                if (lastVisit == null)
                    lastVisit = new Visit();
                lastVisit.setVisitType(encounterType);

                if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.FP_COUNSELING)) {
                    memberObject.setFpMethod(JsonFormUtils.getValue(form, "selected_fp_method_after_counseling"));
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