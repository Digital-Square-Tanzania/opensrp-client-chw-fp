package org.smartregister.chw.fp.util;

public interface FamilyPlanningConstants {

    int REQUEST_CODE_GET_JSON = 2244;
    String ENCOUNTER_TYPE = "encounter_type";
    String STEP_ONE = "step1";
    String STEP_TWO = "step2";

    interface JSON_FORM_EXTRA {
        String JSON = "json";
        String ENCOUNTER_TYPE = "encounter_type";

        String DELETE_EVENT_ID = "deleted_event_id";

        String DELETE_FORM_SUBMISSION_ID = "deleted_form_submission_id";
    }

    interface EVENT_TYPE {
        String FP_REGISTRATION = "FP Registration";

        String UPDATE_FP_REGISTRATION = "Update FP Registration";

        String FP_OTHER_SERVICES= "FP Other Services";

        String FP_FOLLOW_UP_VISIT = "FP Follow-up Visit";

        String FP_SCREENING = "FP Screening";

        String FP_PROVIDE_METHOD = "Provide FP method";

        String FP_POINT_OF_SERVICE_DELIVERY = "Point of Service Delivery";

        String FP_COUNSELING = "Provide FP Counseling";

        String DELETE_EVENT = "Delete Event";
    }

    interface FORMS {
        String FP_METHOD_SCREENING_MEDICAL_HISTORY = "fp_method_screening_medical_history";

        String FP_METHOD_SCREENING_OBSTETRIC_HISTORY = "fp_method_screening_obstetric_history";

        String FP_METHOD_SCREENING_PAST_OBSTETRIC_HISTORY = "fp_method_screening_past_obstetric_history";

        String FP_METHOD_SCREENING_GYNECOLOGICAL_HISTORY = "fp_method_screening_gynecological_history";

        String FP_METHOD_SCREENING_PHYSICAL_EXAMINATION = "fp_method_screening_physical_examination";

        String FP_METHOD_SCREENING_VAGINAL_EXAMINATION = "fp_method_screening_vaginal_examination";

        String FP_METHOD_SCREENING_MEDICAL_ELIGIBILITY_CRITERIA = "fp_method_screening_medical_eligibility_criteria";

        String FP_OTHER_SERVICES_OFFERED = "fp_other_services_offered";

        String FP_OTHER_SERVICES_OFFERED_HTS = "fp_other_services_offered_hts";

        String FP_OTHER_SERVICES_OFFERED_CERVICAL_CANCER = "fp_other_services_offered_cervical_cancer";

        String FP_COUNSELING = "fp_counseling";

        String FP_POINT_OF_SERVICE_DELIVERY = "fp_point_of_service_delivery";

        String FP_PROVISION_OF_FP_METHOD = "fp_provision_of_method";

        String FP_FOLLOWUP_VISIT_METHOD_SATISFACTION = "fp_followup_visit_method_satisfaction";

        String FP_FOLLOWUP_VISIT_VITALS = "fp_followup_visit_vitals";

        String FP_FOLLOWUP_VISIT_METHOD_CONTINUATION = "fp_followup_visit_method_continuation";

    }

    interface TABLES {
        String FP_REGISTER = "ec_family_planning";

        String FP_FOLLOW_UP = "ec_family_planning_follow_up_visit";
    }

    interface ACTIVITY_PAYLOAD {
        String BASE_ENTITY_ID = "BASE_ENTITY_ID";
        String FAMILY_BASE_ENTITY_ID = "FAMILY_BASE_ENTITY_ID";
        String ACTION = "ACTION";
        String FP_FORM_NAME = "FP_FORM_NAME";
        String EDIT_MODE = "editMode";
        String MEMBER_OBJECT = "FpMemberObject";

    }

    interface ACTIVITY_PAYLOAD_TYPE {
        String REGISTRATION = "REGISTRATION";
        String FOLLOW_UP_VISIT = "FOLLOW_UP_VISIT";
    }

    interface CONFIGURATION {
        String FP_REGISTRATION_CONFIGURATION = "fp_registration";
    }

    interface DBConstants {
        String FAMILY_MEMBER = "ec_family_member";

        String FAMILY = "ec_family";

        String FIRST_NAME = "first_name";

        String MIDDLE_NAME = "middle_name";

        String LAST_NAME = "last_name";

        String BASE_ENTITY_ID = "base_entity_id";

        String UNIQUE_ID = "unique_id";

        String GENDER = "gender";

        String DOB = "dob";

        String AGE = "age";

        String LAST_INTERACTED_WITH = "last_interacted_with";

        String VILLAGE_TOWN = "village_town";

        String DATE_REMOVED = "date_removed";

        String RELATIONALID = "relationalid";

        String FAMILY_HEAD = "family_head";

        String PRIMARY_CARE_GIVER = "primary_caregiver";

        String RELATIONAL_ID = "relational_id";

        String DETAILS = "details";

        String FP_METHOD_ACCEPTED = "fp_method_accepted";

        String FP_NEXT_APPOINTMENT_DATE = "next_appointment_date";

        String FP_POP = "POP";

        String FP_COC = "COC";

        String FP_FEMALE_CONDOM = "Female condom";

        String FP_MALE_CONDOM = "Male condom";

        String FP_INJECTABLE = "Injectable";

        String FP_IUCD = "IUCD";

        String FP_FEMALE_STERLIZATION = "Female sterilization";

        String FP_MALE_STERLIZATION = "Male sterilization";

        String FP_IMPLANON_NXT = "Implanon - NXT";
    }

}