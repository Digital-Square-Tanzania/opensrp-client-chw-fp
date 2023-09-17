package org.smartregister.chw.fp.interactor;


import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.actionhelper.FpFollowupVisitMethodSatisfactionActionHelper;
import org.smartregister.chw.fp.actionhelper.FpFollowupVisitRecordPointOfServiceDeliveryActionHelper;
import org.smartregister.chw.fp.actionhelper.FpFollowupVisitVitalsActionHelper;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class BaseFpFollowupVisitInteractor implements BaseFpVisitContract.Interactor {

    private final FpLibrary fpLibrary;
    private final LinkedHashMap<String, BaseFpVisitAction> actionList;
    protected AppExecutors appExecutors;
    private ECSyncHelper syncHelper;
    private Context mContext;
    private Map<String, List<VisitDetail>> details = null;

    @VisibleForTesting
    public BaseFpFollowupVisitInteractor(AppExecutors appExecutors, FpLibrary FpLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.fpLibrary = FpLibrary;
        this.syncHelper = syncHelper;
        this.actionList = new LinkedHashMap<>();
    }

    public BaseFpFollowupVisitInteractor() {
        this(new AppExecutors(), FpLibrary.getInstance(), FpLibrary.getInstance().getEcSyncHelper());
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
        if (view.getEditMode()) {
            Visit lastVisit = fpLibrary.visitRepository().getLatestVisit(fpMemberObject.getBaseEntityId(), FamilyPlanningConstants.EVENT_TYPE.FP_FOLLOW_UP_VISIT);

            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(fpLibrary.visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            try {
                evaluateRecordPointOfServicesDelivery(fpMemberObject, details);
                evaluateSatisfactionOfFpMethod(fpMemberObject, details, callBack);
                evaluateVitals(fpMemberObject, details);
            } catch (BaseFpVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected void evaluateRecordPointOfServicesDelivery(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpFollowupVisitRecordPointOfServiceDeliveryActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_point_of_service_delivery);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withProcessingMode(BaseFpVisitAction.ProcessingMode.SEPARATE).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_POINT_OF_SERVICE_DELIVERY).build();
        actionList.put(actionName, action);
    }

    protected void evaluateSatisfactionOfFpMethod(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details, final BaseFpVisitContract.InteractorCallBack callBack) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpFollowupVisitMethodSatisfactionActionHelper(mContext, fpMemberObject, details, actionList, callBack);
        String actionName = mContext.getString(R.string.fp_method_satisfaction);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_FOLLOWUP_VISIT_METHOD_SATISFACTION).build();
        actionList.put(actionName, action);
    }

    protected void evaluateVitals(FpMemberObject fpMemberObject, Map<String, List<VisitDetail>> details) throws BaseFpVisitAction.ValidationException {
        FpVisitActionHelper actionHelper = new FpFollowupVisitVitalsActionHelper(mContext, fpMemberObject);
        String actionName = mContext.getString(R.string.fp_vitals);
        BaseFpVisitAction action = getBuilder(actionName).withOptional(false).withDetails(details).withHelper(actionHelper).withFormName(FamilyPlanningConstants.FORMS.FP_FOLLOWUP_VISIT_VITALS).build();
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
        return FamilyPlanningConstants.EVENT_TYPE.FP_FOLLOW_UP_VISIT;
    }

    protected String getTableName() {
        return FamilyPlanningConstants.TABLES.FP_REGISTER;
    }

}