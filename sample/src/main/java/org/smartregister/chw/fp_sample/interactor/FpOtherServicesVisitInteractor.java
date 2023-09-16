package org.smartregister.chw.fp_sample.interactor;

import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.interactor.BaseFpOtherServicesVisitInteractor;
import org.smartregister.chw.fp_sample.activity.EntryActivity;

public class FpOtherServicesVisitInteractor extends BaseFpOtherServicesVisitInteractor {
    @Override
    public FpMemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
