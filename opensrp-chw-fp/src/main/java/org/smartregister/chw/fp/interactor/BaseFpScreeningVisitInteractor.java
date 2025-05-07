package org.smartregister.chw.fp.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningGynecologicalHistoryActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningMedicalEligibilityActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningMedicalHistoryActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningObstetricHistoryActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningPastObstetricHistoryActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningPhysicalExaminationActionHelper;
import org.smartregister.chw.fp.actionhelper.FpMethodScreeningVaginalExaminationActionHelper;
import org.smartregister.chw.fp.actionhelper.FpVisitActionHelper;
import org.smartregister.chw.fp.contract.BaseFpVisitContract;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.domain.VisitDetail;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.repository.VisitRepository;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.JsonFormUtils;
import org.smartregister.chw.fp.util.NCUtils;
import org.smartregister.chw.fp.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class BaseFpScreeningVisitInteractor implements BaseFpVisitContract.Interactor {

    private final FpLibrary fpLibrary;
    private final LinkedHashMap<String, BaseFpVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;
    private Map<String, List<VisitDetail>> details = null;

    @VisibleForTesting
    public BaseFpScreeningVisitInteractor(AppExecutors appExecutors, FpLibrary FpLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.fpLibrary = FpLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseFpScreeningVisitInteractor() {
        this(new AppExecutors(), FpLibrary.getInstance(), FpLibrary.getInstance().getEcSyncHelper());
    }

    // Helper method to compare day, month, and year components
    private static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH);
        int day1 = cal1.get(Calendar.DAY_OF_MONTH);

        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);

        return (year1 == year2) && (month1 == month2) && (day1 == day2);
    }

    @Override
    public void reloadMemberDetails(String memberID, BaseFpVisitContract.InteractorCallBack callBack) {
        FpMemberObject fpMemberObject = getMemberClient(memberID);
        if (fpMemberObject != null) {
            final Runnable runnable = () -> {
                appExecutors.mainThread().execute(() -> callBack.onMemberDetailsReloaded(fpMemberObject));
            };
            appExecutors.diskIO().execute(runnable);
        }
    }

    /**
     * Override this method and return actual member object for the provided user
     *
     * @param memberID unique identifier for the user
     * @return FpMemberObject wrapper for the user's data
     */
    @Override
    public FpMemberObject getMemberClient(String memberID) {
        return FpDao.getMember(memberID);
    }

    @Override
    public void saveRegistration(String jsonString, boolean isEditMode, BaseFpVisitContract.InteractorCallBack callBack) {
        Timber.v("saveRegistration");
    }

    @Override
    public void calculateActions(final BaseFpVisitContract.View view, FpMemberObject fpMemberObject, final BaseFpVisitContract.InteractorCallBack callBack) {
        mContext = view.getContext();
        Date lastVisitDate;
        Visit lastVisit = fpLibrary.visitRepository().getLatestVisit(fpMemberObject.getBaseEntityId(), FamilyPlanningConstants.EVENT_TYPE.FP_SCREENING);
        if (view.getEditMode()) {
            if (lastVisit != null) {
                lastVisitDate = lastVisit.getDate();
                details = VisitUtils.getVisitGroups(fpLibrary.visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            } else {
                lastVisitDate = null;
            }
        } else {
            if (lastVisit != null)
                lastVisitDate = lastVisit.getDate();
            else
                lastVisitDate = null;
        }

        final Runnable runnable = () -> {
            try {

                Date currentDate = new Date();
                // Compare the day, month, and year components

                if (view.getEditMode()) {
                    evaluateMedicalHistory(fpMemberObject, details);
                } else if (lastVisitDate == null || !isSameDay(lastVisitDate, currentDate)) {
                    evaluateMedicalHistory(fpMemberObject, details);
                }

                if (fpMemberObject.getGender().equalsIgnoreCase("female")) {
                    if (view.getEditMode()) {
                        evaluateObstetricHistory(fpMemberObject, details);
                        evaluateGynecologicalHistory(fpMemberObject, details);
                        evaluatePhysicalExamination(fpMemberObject, details);
                        evaluateVaginalExamination(fpMemberObject, details);
                    } else if (lastVisitDate == null || !isSameDay(lastVisitDate, currentDate)) {
                        evaluateObstetricHistory(fpMemberObject, details);
                        evaluateGynecologicalHistory(fpMemberObject, details);
                        evaluatePhysicalExamination(fpMemberObject, details);
                        evaluateVaginalExamination(fpMemberObject, details);
                    }
                }
                evaluateMedicalEligibilityCriteria(fpMemberObject, details);


            } catch (BaseFpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void evaluateMedicalHistory(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningMedicalHistoryActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_medical_history);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_MEDICAL_HISTORY).build();
        actionList.put(actionName, action);
    }

    protected void evaluateObstetricHistory(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningObstetricHistoryActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_obstetric_history);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_OBSTETRIC_HISTORY).build();
        actionList.put(actionName, action);
    }

    protected void evaluatePastObstetricHistory(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningPastObstetricHistoryActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_past_obstetric_history);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_PAST_OBSTETRIC_HISTORY).build();
        actionList.put(actionName, action);
    }

    protected void evaluateGynecologicalHistory(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningGynecologicalHistoryActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_gynecological_history);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_GYNECOLOGICAL_HISTORY).build();
        actionList.put(actionName, action);
    }

    protected void evaluatePhysicalExamination(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningPhysicalExaminationActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_physical_examination);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_PHYSICAL_EXAMINATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateVaginalExamination(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningVaginalExaminationActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_vaginal_examination);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_VAGINAL_EXAMINATION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateMedicalEligibilityCriteria(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpMethodScreeningMedicalEligibilityActionHelper(mContext, fpMemberObject);

        String actionName = mContext.getString(R.string.fp_medical_eligibility_criteria);

        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_METHOD_SCREENING_MEDICAL_ELIGIBILITY_CRITERIA).build();

        actionList.put(actionName, action);
    }

    public BaseFpVisitAction.Builder getBuilder(String title) {
        return new BaseFpVisitAction.Builder(mContext, title);
    }

    @Override
    public void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseFpVisitAction> map, final BaseFpVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            boolean result = true;
            try {
                submitVisit(editMode, memberID, map, "");
            } catch (Exception e) {
                Timber.e(e);
                result = false;
            }

            final boolean finalResult = result;
            appExecutors.mainThread().execute(() -> callBack.onSubmitted(finalResult));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseFpVisitAction> map, String parentEventType) throws Exception {
        // create a map of the different types

        Map<String, BaseFpVisitAction> externalVisits = new HashMap<>();
        Map<String, String> combinedJsons = new HashMap<>();
        String payloadType = null;
        String payloadDetails = null;

        // aggregate forms to be processed
        for (Map.Entry<String, BaseFpVisitAction> entry : map.entrySet()) {
            String json = entry.getValue().getJsonPayload();
            if (StringUtils.isNotBlank(json)) {
                // do not process events that are meant to be in detached mode
                // in a similar manner to the the aggregated events

                BaseFpVisitAction action = entry.getValue();
                BaseFpVisitAction.ProcessingMode mode = action.getProcessingMode();

                if (mode == BaseFpVisitAction.ProcessingMode.SEPARATE && StringUtils.isBlank(parentEventType)) {
                    externalVisits.put(entry.getKey(), entry.getValue());
                } else {
                    if (action.getActionStatus() != BaseFpVisitAction.Status.PENDING)
                        combinedJsons.put(entry.getKey(), json);
                }

                payloadType = action.getPayloadType().name();
                payloadDetails = action.getPayloadDetails();
            }
        }

        String type = StringUtils.isBlank(parentEventType) ? getEncounterType() : getEncounterType();

        // persist to database
        Visit visit = saveVisit(editMode, memberID, type, combinedJsons, parentEventType);
        if (visit != null) {
            saveVisitDetails(visit, payloadType, payloadDetails);
            processExternalVisits(visit, externalVisits, memberID);
        }

        if (fpLibrary.isSubmitOnSave()) {
            List<Visit> visits = new ArrayList<>(1);
            visits.add(visit);
            VisitUtils.processVisits(visits, fpLibrary.visitRepository(), fpLibrary.visitDetailsRepository());

            Context context = FpLibrary.getInstance().context().applicationContext();

        }
    }

    /**
     * recursively persist visits to the db
     *
     * @param visit
     * @param externalVisits
     * @param memberID
     * @throws Exception
     */
    protected void processExternalVisits(Visit visit, Map<String, BaseFpVisitAction> externalVisits, String memberID) throws Exception {
        if (visit != null && !externalVisits.isEmpty()) {
            for (Map.Entry<String, BaseFpVisitAction> entry : externalVisits.entrySet()) {
                Map<String, BaseFpVisitAction> subEvent = new HashMap<>();
                subEvent.put(entry.getKey(), entry.getValue());

                String subMemberID = entry.getValue().getBaseEntityID();
                if (StringUtils.isBlank(subMemberID)) subMemberID = memberID;

                submitVisit(false, subMemberID, subEvent, visit.getVisitType());
            }
        }
    }

    protected @Nullable Visit saveVisit(boolean editMode, String memberID, String encounterType, final Map<String, String> jsonString, String parentEventType) throws Exception {

        AllSharedPreferences allSharedPreferences = FpLibrary.getInstance().context().allSharedPreferences();

        String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        Event baseEvent = JsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());

        // only tag the first event with the date
        if (StringUtils.isBlank(parentEventType)) {
            prepareEvent(baseEvent);
        } else {
            prepareSubEvent(baseEvent);
        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(JsonFormUtils.generateRandomUUIDString());
            JsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

            String visitID = (editMode) ? visitRepository().getLatestVisit(memberID, getEncounterType()).getVisitId() : JsonFormUtils.generateRandomUUIDString();

            // reset database
            if (editMode) {
                Visit visit = visitRepository().getVisitByVisitId(visitID);
                if (visit != null) baseEvent.setEventDate(visit.getDate());

                VisitUtils.deleteProcessedVisit(visitID, memberID);
                deleteOldVisit(visitID);
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visit.setParentVisitID(getParentVisitEventID(visit, parentEventType));

            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }

    protected String getParentVisitEventID(Visit visit, String parentEventType) {
        return visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
    }

    @VisibleForTesting
    public VisitRepository visitRepository() {
        return FpLibrary.getInstance().visitRepository();
    }

    protected void deleteOldVisit(String visitID) {
        visitRepository().deleteVisit(visitID);
        FpLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(visitID);

        List<Visit> childVisits = visitRepository().getChildEvents(visitID);
        for (Visit v : childVisits) {
            visitRepository().deleteVisit(v.getVisitId());
            FpLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(v.getVisitId());
        }
    }

    protected void saveVisitDetails(Visit visit, String payloadType, String payloadDetails) {
        if (visit.getVisitDetails() == null) return;

        for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
            if (entry.getValue() != null) {
                for (VisitDetail d : entry.getValue()) {
                    d.setPreProcessedJson(payloadDetails);
                    d.setPreProcessedType(payloadType);
                    FpLibrary.getInstance().visitDetailsRepository().addVisitDetails(d);
                }
            }
        }
    }

    /**
     * Injects implementation specific changes to the event
     *
     * @param baseEvent
     */
    protected void prepareEvent(Event baseEvent) {
        if (baseEvent != null) {
            // add fp date obs and last
            List<Object> list = new ArrayList<>();
            list.add(new Date());
            baseEvent.addObs(new Obs("concept", "text", "fp_visit_date", "", list, new ArrayList<>(), null, "fp_visit_date"));
        }
    }

    /**
     * injects additional meta data to the event
     *
     * @param baseEvent
     */
    protected void prepareSubEvent(Event baseEvent) {
        Timber.v("You can add information to sub events");
    }

    protected String getEncounterType() {
        return FamilyPlanningConstants.EVENT_TYPE.FP_SCREENING;
    }

    protected String getTableName() {
        return FamilyPlanningConstants.TABLES.FP_REGISTER;
    }

}