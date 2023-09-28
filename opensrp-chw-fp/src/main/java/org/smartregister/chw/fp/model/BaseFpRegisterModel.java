package org.smartregister.chw.fp.model;

import org.json.JSONObject;
import org.smartregister.chw.fp.contract.BaseFpRegisterContract;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.FpJsonFormUtils;

import timber.log.Timber;

public class BaseFpRegisterModel implements BaseFpRegisterContract.Model {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = FpJsonFormUtils.getFormAsJson(formName);

        if (formName.equalsIgnoreCase(FamilyPlanningConstants.FORMS.FP_PROVISION_OF_FP_METHOD)) {
            try {
                FpMemberObject fpMemberObject = FpDao.getMember(entityId);
                jsonObject.getJSONObject("global").put("fp_method_selected", FpDao.getSelectedFpMethodAfterCounseling(fpMemberObject.getBaseEntityId()));
                jsonObject.getJSONObject("global").put("sex", fpMemberObject.getGender());
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (formName.equalsIgnoreCase(FamilyPlanningConstants.FORMS.FP_COUNSELING) || formName.equalsIgnoreCase(FamilyPlanningConstants.FORMS.FP_ENROLLMENT)) {
            try {
                FpMemberObject fpMemberObject = FpDao.getMember(entityId);
                jsonObject.getJSONObject("global").put("sex", fpMemberObject.getGender());
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        FpJsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

}
