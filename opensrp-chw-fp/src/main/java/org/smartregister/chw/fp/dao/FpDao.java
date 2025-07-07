package org.smartregister.chw.fp.dao;

import static org.smartregister.chw.fp.util.FamilyPlanningConstants.DBConstants.FP_METHOD_PROVIDED;
import static org.smartregister.chw.fp.util.FamilyPlanningConstants.DBConstants.FP_NEXT_APPOINTMENT_DATE;
import static org.smartregister.chw.fp.util.FamilyPlanningConstants.DBConstants.FP_REGISTRATION_NUMBER;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.dao.AbstractDao;

import java.util.List;

import timber.log.Timber;

public class FpDao extends AbstractDao {
    public static void closeFpMemberFromRegister(String baseEntityID) {
        String sql = "UPDATE " + FamilyPlanningConstants.TABLES.FP_REGISTER + " set is_closed = 1 where base_entity_id = '" + baseEntityID + "'";
        updateDB(sql);
    }

    public static Visit getLatestVisit(String baseEntityId) {
        String sql = "select visit_id, visit_type, parent_visit_id, visit_date from visits where base_entity_id = '" +
                baseEntityId + "' " +
                "and (" +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_COUNSELING + "' OR " +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_PROVIDE_METHOD + "' OR " +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_FOLLOW_UP_VISIT + "' OR " +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_SCREENING + "' OR " +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_POINT_OF_SERVICE_DELIVERY + "' OR " +
                "visit_type = '" + FamilyPlanningConstants.EVENT_TYPE.FP_OTHER_SERVICES + "'" +
                ") " +
                "ORDER BY visit_date DESC LIMIT 1";
        List<Visit> visit = AbstractDao.readData(sql, getVisitDataMap());
        if (visit.size() == 0) {
            return null;
        }

        return visit.get(0);
    }

    public static Integer getFpWomenCount(String familyBaseEntityId) {
        String sql = "SELECT count(fp.base_entity_id) count " +
                "FROM ec_family_planning fp " +
                "INNER Join ec_family_member fm on fm.base_entity_id = fp.base_entity_id " +
                "WHERE fm.relational_id = '" + familyBaseEntityId + "' COLLATE NOCASE " +
                "AND fp.is_closed = 0 " +
                "AND fp.ecp = 1 ";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.size() == 0)
            return null;

        return res.get(0);
    }

    public static Visit getLatestVisitById(String visitId) {
        String sql = "select visit_id, visit_type, parent_visit_id, visit_date from visits where visit_id = '" +
                visitId + "' ORDER BY visit_date DESC LIMIT 1";
        List<Visit> visit = AbstractDao.readData(sql, getVisitDataMap());
        if (visit.size() == 0) {
            return null;
        }

        return visit.get(0);
    }

    public static Visit getLatestVisit(String baseEntityId, String visitType) {
        String sql = "select visit_id, visit_type, parent_visit_id, visit_date from visits where base_entity_id = '" +
                baseEntityId + "' " +
                "and visit_type = '" + visitType + "' ORDER BY visit_date DESC LIMIT 1";
        List<Visit> visit = AbstractDao.readData(sql, getVisitDataMap());
        if (visit.size() == 0) {
            return null;
        }

        return visit.get(0);
    }

    private static DataMap<Visit> getVisitDataMap() {
        return c -> {
            Visit visit = new Visit();
            visit.setVisitId(getCursorValue(c, "visit_id"));
            visit.setParentVisitID(getCursorValue(c, "parent_visit_id"));
            visit.setVisitType(getCursorValue(c, "visit_type"));
            visit.setDate(getCursorValueAsDate(c, "visit_date"));

            return visit;
        };
    }

    public static boolean isRegisteredForFp(String baseEntityID) {
        String sql = "SELECT count(p.base_entity_id) count FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return false;

        return res.get(0) > 0;
    }

    public static String getClientCategoryAfterScreening(String baseEntityID) {
        String sql = "SELECT client_category_after_screening FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "client_category_after_screening");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static boolean isHivPositive(String baseEntityID) {
        String sql = "SELECT ctc_number FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "ctc_number");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return false;

        return StringUtils.isNotBlank(res.get(0));
    }

    public static boolean hasClientAgreedToFamilyPlanning(String baseEntityID) {
        String sql = "SELECT client_agreed_on_fp_choice FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "client_agreed_on_fp_choice");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return false;

        return res.get(0).equalsIgnoreCase("yes");
    }

    public static String getFollowupOutcome(String baseEntityID) {
        String sql = "SELECT followup_outcome FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "followup_outcome");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static String getSelectedFpMethodAfterCounseling(String baseEntityID) {
        String sql = "SELECT selected_fp_method_after_counseling FROM " + FamilyPlanningConstants.TABLES.FP_REGISTER + " p " + "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "selected_fp_method_after_counseling");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static FpMemberObject getMember(String baseEntityID) {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number ,  pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id left join " + FamilyPlanningConstants.TABLES.FP_REGISTER + " mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver where m.base_entity_id ='" + baseEntityID + "' ";

        DataMap<FpMemberObject> dataMap = cursor -> {
            FpMemberObject fpMemberObject = new FpMemberObject();

            fpMemberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            fpMemberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            fpMemberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            fpMemberObject.setAddress(getCursorValue(cursor, "village_town"));
            fpMemberObject.setGender(getCursorValue(cursor, "gender"));
            fpMemberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            fpMemberObject.setDob(getCursorValue(cursor, "dob"));
            fpMemberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            fpMemberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
            fpMemberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            fpMemberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            fpMemberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            fpMemberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            fpMemberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
            fpMemberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
            fpMemberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " " + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName = (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
            fpMemberObject.setFamilyHeadName(familyHeadName);

            String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " " + getCursorValue(cursor, "pcg_middle_name", "");

            familyPcgName = (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
            fpMemberObject.setPrimaryCareGiverName(familyPcgName);

            fpMemberObject.setFpNextAppointmentDate(getCursorValue(cursor, FP_NEXT_APPOINTMENT_DATE, ""));
            fpMemberObject.setFpMethod(getCursorValue(cursor, FP_METHOD_PROVIDED, ""));
            fpMemberObject.setFpRegistrationNumber(getCursorValue(cursor, FP_REGISTRATION_NUMBER, ""));

            return fpMemberObject;
        };

        List<FpMemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }

    public static Event getEventByFormSubmissionId(String formSubmissionId) {
        String sql = "select json from event where formSubmissionId = '" + formSubmissionId + "'";
        DataMap<Event> dataMap = (c) -> {
            Event event;
            try {
                event = (Event) FpLibrary.getInstance().getEcSyncHelper().convert(new JSONObject(getCursorValue(c, "json")), Event.class);
            } catch (JSONException e) {
                Timber.e(e);
                return null;
            }
            return event;
        };
        return (Event) AbstractDao.readSingleValue(sql, dataMap);
    }
}
