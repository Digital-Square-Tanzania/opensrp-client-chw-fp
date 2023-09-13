package org.smartregister.chw.fp_sample.activity;

import static org.smartregister.chw.fp.util.Constants.ENCOUNTER_TYPE;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.fp.activity.BaseFpScreeningActivity;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.presenter.BaseFpVisitPresenter;
import org.smartregister.chw.fp.util.Constants;
import org.smartregister.chw.fp_sample.R;
import org.smartregister.chw.fp_sample.interactor.FpScreeningVisitInteractor;

import timber.log.Timber;

public class FpScreeningActivity extends BaseFpScreeningActivity {
    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, FpScreeningActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    protected void registerPresenter() {
        presenter = new BaseFpVisitPresenter(memberObject, this, new FpScreeningVisitInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, SampleJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void submittedAndClose() {
        Intent returnIntent = new Intent();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(actionList.get(getString(R.string.fp_medical_eligibility_criteria)).getJsonPayload());
            jsonObject.put(ENCOUNTER_TYPE, Constants.EVENT_TYPE.FP_SCREENING);
        } catch (JSONException e) {
            Timber.e(e);
        }
        returnIntent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonObject.toString());
        setResult(Activity.RESULT_OK, returnIntent);
        close();
    }
}
