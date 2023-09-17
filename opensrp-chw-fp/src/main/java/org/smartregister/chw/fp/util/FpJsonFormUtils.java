package org.smartregister.chw.fp.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;

import java.util.Date;

import timber.log.Timber;

public class FpJsonFormUtils extends org.smartregister.util.JsonFormUtils {
    public static final String METADATA = "metadata";

    public static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = malariaFormFields(jsonForm);

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = Triple.of(jsonForm != null && fields != null, jsonForm, fields);
        return registrationFormParams;
    }

    public static JSONArray malariaFormFields(JSONObject jsonForm) {
        try {
            JSONArray fieldsOne = fields(jsonForm, FamilyPlanningConstants.STEP_ONE);
            JSONArray fieldsTwo = fields(jsonForm, FamilyPlanningConstants.STEP_TWO);
            if (fieldsTwo != null) {
                for (int i = 0; i < fieldsTwo.length(); i++) {
                    fieldsOne.put(fieldsTwo.get(i));
                }
            }
            return fieldsOne;

        } catch (JSONException e) {
            Timber.tag(TAG).e(e);
        }
        return null;
    }

    public static JSONArray fields(JSONObject jsonForm, String step) {
        try {

            JSONObject step1 = jsonForm.has(step) ? jsonForm.getJSONObject(step) : null;
            if (step1 == null) {
                return null;
            }

            return step1.has(FIELDS) ? step1.getJSONArray(FIELDS) : null;

        } catch (JSONException e) {
            Timber.tag(TAG).e(e);
        }
        return null;
    }

    public static Event processJsonForm(AllSharedPreferences allSharedPreferences, String
            jsonString) {

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

        if (!registrationFormParams.getLeft()) {
            return null;
        }

        JSONObject jsonForm = registrationFormParams.getMiddle();
        JSONArray fields = registrationFormParams.getRight();
        String entityId = getString(jsonForm, ENTITY_ID);
        String encounter_type = jsonForm.optString(FamilyPlanningConstants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

        if (FamilyPlanningConstants.EVENT_TYPE.FP_REGISTRATION.equals(encounter_type)) {
            encounter_type = FamilyPlanningConstants.TABLES.FP_REGISTER;
        } else if (FamilyPlanningConstants.EVENT_TYPE.FP_FOLLOW_UP_VISIT.equals(encounter_type)) {
            encounter_type = FamilyPlanningConstants.TABLES.FP_FOLLOW_UP;
        }
        return org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag(allSharedPreferences), entityId, getString(jsonForm, FamilyPlanningConstants.ENCOUNTER_TYPE), encounter_type);
    }

    protected static FormTag formTag(AllSharedPreferences allSharedPreferences) {
        FormTag formTag = new FormTag();
        formTag.providerId = allSharedPreferences.fetchRegisteredANM();
        formTag.appVersion = FpLibrary.getInstance().getApplicationVersion();
        formTag.databaseVersion = FpLibrary.getInstance().getDatabaseVersion();
        return formTag;
    }

    public static void tagEvent(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));
        event.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        event.setEventDate(new Date());

        event.setClientApplicationVersion(FpLibrary.getInstance().getApplicationVersion());
        event.setClientDatabaseVersion(FpLibrary.getInstance().getDatabaseVersion());
    }

    private static String locationId(AllSharedPreferences allSharedPreferences) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        String userLocationId = allSharedPreferences.fetchUserLocalityId(providerId);
        if (StringUtils.isBlank(userLocationId)) {
            userLocationId = allSharedPreferences.fetchDefaultLocalityId(providerId);
        }

        return userLocationId;
    }

    public static void getRegistrationForm(JSONObject jsonObject, String entityId, String
            currentLocationId) throws JSONException {
        jsonObject.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);
        jsonObject.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, entityId);
        jsonObject.put(DBConstants.KEY.RELATIONAL_ID, entityId);
    }

    public static JSONObject getFormAsJson(String formName) throws Exception {
        return FormUtils.getInstance(FpLibrary.getInstance().context().applicationContext()).getFormJson(formName);
    }

}
