package org.smartregister.chw.fp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.contract.FpProfileContract;
import org.smartregister.chw.fp.custom_views.BaseFpFloatingMenu;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.domain.Visit;
import org.smartregister.chw.fp.interactor.BaseFpProfileInteractor;
import org.smartregister.chw.fp.presenter.BaseFpProfilePresenter;
import org.smartregister.chw.fp.util.Constants;
import org.smartregister.chw.fp.util.FpUtil;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public abstract class BaseFpProfileActivity extends BaseProfileActivity implements FpProfileContract.View, FpProfileContract.InteractorCallBack {
    protected MemberObject memberObject;
    protected FpProfileContract.Presenter profilePresenter;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;
    protected TextView textViewRecordFp;
    protected View view_most_due_overdue_row;
    protected RelativeLayout rlLastVisit;
    protected RelativeLayout visitStatus;
    protected ImageView imageViewCross;
    protected TextView textViewUndo;
    protected TextView textViewVisitDone;
    protected RelativeLayout visitDone;
    protected LinearLayout recordVisits;
    protected TextView textViewVisitDoneEdit;
    protected BaseFpFloatingMenu baseFpFloatingMenu;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private ProgressBar progressBar;

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, BaseFpProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_fp_profile);
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        toolbar.setNavigationOnClickListener(v -> BaseFpProfileActivity.this.finish());
        appBarLayout = this.findViewById(R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setOutlineProvider(null);
        }

        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        view_most_due_overdue_row = findViewById(R.id.view_most_due_overdue_row);
        imageViewCross = findViewById(R.id.tick_image);
        rlLastVisit = findViewById(R.id.rlLastVisit);
        textViewVisitDone = findViewById(R.id.textview_visit_done);
        visitStatus = findViewById(R.id.record_visit_not_done_bar);
        visitDone = findViewById(R.id.visit_done_bar);
        recordVisits = findViewById(R.id.record_visits);
        progressBar = findViewById(R.id.progress_bar);
        textViewVisitDoneEdit = findViewById(R.id.textview_edit);
        textViewRecordFp = findViewById(R.id.textview_record_fp);
        textViewUndo = findViewById(R.id.textview_undo);
        imageView = findViewById(R.id.imageview_profile);
        textViewVisitDoneEdit.setOnClickListener(this);
        rlLastVisit.setOnClickListener(this);
        textViewRecordFp.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);

        imageRenderHelper = new ImageRenderHelper(this);
        memberObject = getMemberObject(baseEntityId);
        initializePresenter();
        profilePresenter.fillProfileData(memberObject);
        setupViews();
    }

    protected MemberObject getMemberObject(String baseEntityId) {
        return FpDao.getMember(baseEntityId);
    }

    @Override
    protected void setupViews() {
        initializeFloatingMenu();
        Visit lastVisit = getLastVisit();

        if (isFirstVisit()) {
            if (lastVisit == null) {
                textViewRecordFp.setText(R.string.record_point_of_service_delivery);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_POINT_OF_SERVICE_DELIVERY)) {
                textViewRecordFp.setText(R.string.provide_fp_counseling);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_COUNSELING)) {
                textViewRecordFp.setText(R.string.fp_screening);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_SCREENING)) {
                textViewRecordFp.setText(R.string.provide_fp_method);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_PROVIDE_METHOD)) {
                textViewRecordFp.setText(R.string.provide_other_services);
            }
        } else {
            if (lastVisit == null || lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_OTHER_SERVICES)) {
                textViewRecordFp.setText(R.string.record_fp_followup_visit);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_FOLLOW_UP_VISIT)) {
                textViewRecordFp.setText(R.string.provide_fp_method);
            } else if (lastVisit.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.FP_PROVIDE_METHOD)) {
                textViewRecordFp.setText(R.string.provide_other_services);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.title_layout) {
            onBackPressed();
        } else if (id == R.id.rlLastVisit) {
            this.openMedicalHistory();
        } else if (id == R.id.textview_record_fp) {
            if (isFirstVisit()) {
                if (((TextView) view).getText().equals(this.getString(R.string.record_point_of_service_delivery))) {
                    startPointOfServiceDeliveryForm();
                } else if (((TextView) view).getText().equals(this.getString(R.string.provide_fp_counseling))) {
                    startFpCounselingForm();
                } else if (((TextView) view).getText().equals(this.getString(R.string.fp_screening))) {
                    startFpScreeningForm();
                } else if (((TextView) view).getText().equals(this.getString(R.string.provide_fp_method))) {
                    startProvideFpMethod();
                } else if (((TextView) view).getText().equals(this.getString(R.string.provide_other_services))) {
                    startProvideOtherServices();
                }
            } else {
                if (((TextView) view).getText().equals(this.getString(R.string.record_fp_followup_visit))) {
                    startFpFollowupVisit();
                } else if (((TextView) view).getText().equals(this.getString(R.string.provide_fp_method))) {
                    startProvideFpMethod();
                } else if (((TextView) view).getText().equals(this.getString(R.string.provide_other_services))) {
                    startProvideOtherServices();
                }
            }
//            this.recordFp(memberObject);
        }
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        profilePresenter = new BaseFpProfilePresenter(this, new BaseFpProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    public void initializeFloatingMenu() {
        if (StringUtils.isNotBlank(memberObject.getPhoneNumber())) {
            baseFpFloatingMenu = new BaseFpFloatingMenu(this, memberObject);
            baseFpFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(baseFpFloatingMenu, linearLayoutParams);
        }
    }

    @Override
    public void hideView() {
        textViewRecordFp.setVisibility(View.GONE);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        int age = new Period(new DateTime(memberObject.getAge()), new DateTime()).getYears();
        textViewName.setText(String.format("%s %s %s, %d", memberObject.getFirstName(), memberObject.getMiddleName(), memberObject.getLastName(), age));
        textViewGender.setText(FpUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());

        if (StringUtils.isNotBlank(memberObject.getFamilyHead()) && memberObject.getFamilyHead().equals(memberObject.getBaseEntityId())) {
            findViewById(R.id.family_sbc_head).setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNotBlank(memberObject.getPrimaryCareGiver()) && memberObject.getPrimaryCareGiver().equals(memberObject.getBaseEntityId())) {
            findViewById(R.id.primary_sbc_caregiver).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setOverDueColor() {
        textViewRecordFp.setBackground(getResources().getDrawable(R.drawable.record_btn_selector_overdue));
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        //fetch profile data
    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        showProgressBar(false);
        rlLastVisit.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
    }


    @Override
    public void openMedicalHistory() {
        //implement
    }

    @Override
    public void recordFp(MemberObject memberObject) {
        //implement
    }

    @Nullable
    private String formatTime(Date dateTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return formatter.format(dateTime);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            profilePresenter.saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
        }
    }
}
