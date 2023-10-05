package org.smartregister.chw.fp.interactor;

import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.activity.EntryActivity;

public class FpOtherServicesVisitInteractor extends BaseFpOtherServicesVisitInteractor {
    @Override
    public FpMemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
