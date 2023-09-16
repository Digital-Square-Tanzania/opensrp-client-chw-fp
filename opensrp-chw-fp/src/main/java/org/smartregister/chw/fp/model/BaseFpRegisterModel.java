package org.smartregister.chw.fp.model;

import org.json.JSONObject;
import org.smartregister.chw.fp.contract.BaseFpRegisterContract;
import org.smartregister.chw.fp.util.FpJsonFormUtils;

public class BaseFpRegisterModel implements BaseFpRegisterContract.Model {

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = FpJsonFormUtils.getFormAsJson(formName);
        FpJsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }

}
