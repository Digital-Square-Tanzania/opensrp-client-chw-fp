package org.smartregister.chw.fp.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.fp.util.FamilyPlanningConstants;


public class FpFollowupMemberProfileActivity extends FpMemberProfileActivity {
    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, FpFollowupMemberProfileActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }


    @Override
    public void startFpFollowupVisit() {
        FpFollowupVisitProvisionOfServicesActivity.startMe(this, fpMemberObject.getBaseEntityId(), false);
    }

    @Override
    public boolean isFirstVisit() {
        return false;
    }

}