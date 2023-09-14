package org.smartregister.chw.fp_sample.interactor;

import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.interactor.BaseFpOtherServicesVisitInteractor;
import org.smartregister.chw.fp.interactor.BaseFpScreeningVisitInteractor;
import org.smartregister.chw.fp_sample.activity.EntryActivity;

public class FpOtherServicesVisitInteractor extends BaseFpOtherServicesVisitInteractor {
    @Override
    public MemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
