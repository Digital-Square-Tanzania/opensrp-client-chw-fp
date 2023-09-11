package org.smartregister.chw.fp_sample.activity;

import org.smartregister.chw.fp.activity.BaseFpProfileActivity;
import org.smartregister.chw.fp.domain.Visit;

public class BaseTestFpProfileActivity extends BaseFpProfileActivity {
    @Override
    public Visit getLastVisit() {
        return null;
    }

    @Override
    public boolean getIsClientUsingFpMethod() {
        return false;
    }

    @Override
    public boolean isFirstVisit() {
        return false;
    }

    @Override
    public void startPointOfServiceDeliveryForm() {
        //not required
    }

    @Override
    public void startFpCounselingForm() {
        //not required
    }

    @Override
    public void startFpScreeningForm() {
        //not required
    }

    @Override
    public void startProvideFpMethod() {
        //not required
    }

    @Override
    public void startProvideOtherServices() {
        //not required
    }

    @Override
    public void startFpFollowupVisit() {
        //not required
    }
}
