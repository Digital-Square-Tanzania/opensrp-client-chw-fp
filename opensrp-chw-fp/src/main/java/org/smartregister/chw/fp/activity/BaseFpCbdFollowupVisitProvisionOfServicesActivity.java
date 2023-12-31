package org.smartregister.chw.fp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.adapter.BaseFpVisitAdapter;
import org.smartregister.chw.fp.contract.BaseFpVisitContract;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.interactor.BaseFpCbdFollowupVisitInteractor;
import org.smartregister.chw.fp.model.BaseFpVisitAction;
import org.smartregister.chw.fp.presenter.BaseFpVisitPresenter;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.view.activity.SecuredActivity;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;

public class BaseFpCbdFollowupVisitProvisionOfServicesActivity extends SecuredActivity implements BaseFpVisitContract.View, View.OnClickListener {

    private static final String TAG = BaseFpCbdFollowupVisitProvisionOfServicesActivity.class.getCanonicalName();
    protected Map<String, BaseFpVisitAction> actionList = new LinkedHashMap<>();
    protected BaseFpVisitContract.Presenter presenter;
    protected FpMemberObject fpMemberObject;
    protected String baseEntityID;
    protected Boolean isEditMode = false;
    protected RecyclerView.Adapter mAdapter;
    protected ProgressBar progressBar;
    protected TextView tvSubmit;
    protected TextView tvTitle;
    protected String current_action;
    protected String confirmCloseTitle;
    protected String confirmCloseMessage;

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, BaseFpCbdFollowupVisitProvisionOfServicesActivity.class);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        activity.startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fp_visit);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isEditMode = getIntent().getBooleanExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.EDIT_MODE, false);
            baseEntityID = getIntent().getStringExtra(FamilyPlanningConstants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
            fpMemberObject = getMemberObject(baseEntityID);
        }

        confirmCloseTitle = getString(R.string.confirm_form_close);
        confirmCloseMessage = getString(R.string.confirm_form_close_explanation);
        setUpView();
        displayProgressBar(true);
        registerPresenter();
        if (presenter != null) {
            if (StringUtils.isNotBlank(baseEntityID)) {
                presenter.reloadMemberDetails(baseEntityID);
            } else {
                presenter.initialize();
            }
        }
    }

    protected FpMemberObject getMemberObject(String baseEntityId) {
        return FpDao.getMember(baseEntityId);
    }

    public void setUpView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        findViewById(R.id.close).setOnClickListener(this);
        tvSubmit = findViewById(R.id.customFontTextViewSubmit);
        tvSubmit.setOnClickListener(this);
        tvTitle = findViewById(R.id.customFontTextViewName);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new BaseFpVisitAdapter(this, this, (LinkedHashMap) actionList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        redrawVisitUI();
    }

    protected void registerPresenter() {
        presenter = new BaseFpVisitPresenter(fpMemberObject, this, new BaseFpCbdFollowupVisitInteractor());
    }

    @Override
    public void initializeActions(LinkedHashMap<String, BaseFpVisitAction> map) {
        actionList.clear();

        //Rearranging the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.fp_health_education))) {
            BaseFpVisitAction healthEducationAction = map.get(getString(R.string.fp_health_education));
            if (healthEducationAction != null) {
                actionList.put(getString(R.string.fp_health_education), healthEducationAction);
            }
        }

        if (map.containsKey(getString(R.string.fp_method_satisfaction))) {
            BaseFpVisitAction fpMethodSatisfactionAction = map.get(getString(R.string.fp_method_satisfaction));
            if (fpMethodSatisfactionAction != null) {
                actionList.put(getString(R.string.fp_method_satisfaction), fpMethodSatisfactionAction);
            }
        }

        if (map.containsKey(getString(R.string.fp_method_refill))) {
            BaseFpVisitAction fpMethodRefillAction = map.get(getString(R.string.fp_method_refill));
            if (fpMethodRefillAction != null) {
                actionList.put(getString(R.string.fp_method_refill), fpMethodRefillAction);
            }
        }

        if (map.containsKey(getString(R.string.fp_next_appointment_date))) {
            BaseFpVisitAction fpNextAppointmentDateAction = map.get(getString(R.string.fp_next_appointment_date));
            if (fpNextAppointmentDateAction != null) {
                actionList.put(getString(R.string.fp_next_appointment_date), fpNextAppointmentDateAction);
            }
        }
        //====================End of Necessary evil ====================================


        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Boolean getEditMode() {
        return isEditMode;
    }

    @Override
    public void onMemberDetailsReloaded(FpMemberObject fpMemberObject) {
        this.fpMemberObject = fpMemberObject;
        presenter.initialize();
        redrawHeader(fpMemberObject);
    }

    @Override
    protected void onCreation() {
        Timber.v("Empty onCreation");
    }

    @Override
    protected void onResumption() {
        Timber.v("Empty onResumption");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            displayExitDialog(() -> close());
        } else if (v.getId() == R.id.customFontTextViewSubmit) {
            submitVisit();
        }
    }

    @Override
    public BaseFpVisitContract.Presenter presenter() {
        return presenter;
    }

    @Override
    public Form getFormConfig() {
        return null;
    }

    @Override
    public void startForm(BaseFpVisitAction visitAction) {
        current_action = visitAction.getTitle();

        if (StringUtils.isNotBlank(visitAction.getJsonPayload())) {
            try {
                JSONObject jsonObject = new JSONObject(visitAction.getJsonPayload());
                startFormActivity(jsonObject);
            } catch (Exception e) {
                Timber.e(e);
                String locationId = FpLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(visitAction.getFormName(), fpMemberObject.getBaseEntityId(), locationId);
            }
        } else {
            String locationId = FpLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            presenter().startForm(visitAction.getFormName(), fpMemberObject.getBaseEntityId(), locationId);
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, JsonFormActivity.class);
        intent.putExtra(FamilyPlanningConstants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, FamilyPlanningConstants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void startFragment(BaseFpVisitAction visitAction) {
        current_action = visitAction.getTitle();

        if (visitAction.getDestinationFragment() != null)
            visitAction.getDestinationFragment().show(getSupportFragmentManager(), current_action);

    }

    @Override
    public void redrawHeader(FpMemberObject fpMemberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", fpMemberObject.getFullName(), String.valueOf(fpMemberObject.getAge()), getString(R.string.fp_visit)));
    }

    @Override
    public void redrawVisitUI() {
        boolean valid = actionList.size() > 0;
        for (Map.Entry<String, BaseFpVisitAction> entry : actionList.entrySet()) {
            BaseFpVisitAction action = entry.getValue();
            if ((!action.isOptional() && (action.getActionStatus() == BaseFpVisitAction.Status.PENDING && action.isValid())) || !action.isEnabled()) {
                valid = false;
                break;
            }
        }

        int res_color = valid ? R.color.white : R.color.light_grey;
        tvSubmit.setTextColor(getResources().getColor(res_color));
        tvSubmit.setOnClickListener(valid ? this : null); // update listener to null

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayProgressBar(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }


    @Override
    public Map<String, BaseFpVisitAction> getVisitActions() {
        return actionList;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void submittedAndClose() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        close();
    }

    @Override
    public BaseFpVisitContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void submitVisit() {
        getPresenter().submitVisit();
    }

    @Override
    public void onDialogOptionUpdated(String jsonString) {
        BaseFpVisitAction baseFpVisitAction = actionList.get(current_action);
        if (baseFpVisitAction != null) {
            baseFpVisitAction.setJsonPayload(jsonString);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    public Context getMyContext() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FamilyPlanningConstants.REQUEST_CODE_GET_JSON) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String jsonString = data.getStringExtra(FamilyPlanningConstants.JSON_FORM_EXTRA.JSON);
                    BaseFpVisitAction fpVisitAction = actionList.get(current_action);
                    if (fpVisitAction != null) {
                        fpVisitAction.setJsonPayload(jsonString);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                BaseFpVisitAction fpVisitAction = actionList.get(current_action);
                if (fpVisitAction != null) fpVisitAction.evaluateStatus();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // update the adapter after every payload
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    public void onBackPressed() {
        displayExitDialog(BaseFpCbdFollowupVisitProvisionOfServicesActivity.this::finish);
    }

    protected void displayExitDialog(final Runnable onConfirm) {
        AlertDialog dialog = new AlertDialog.Builder(this, com.vijay.jsonwizard.R.style.AppThemeAlertDialog).setTitle(confirmCloseTitle).setMessage(confirmCloseMessage).setNegativeButton(com.vijay.jsonwizard.R.string.yes, (dialog1, which) -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
        }).setPositiveButton(com.vijay.jsonwizard.R.string.no, (dialog2, which) -> Timber.d("No button on dialog in %s", JsonFormActivity.class.getCanonicalName())).create();

        dialog.show();
    }

}