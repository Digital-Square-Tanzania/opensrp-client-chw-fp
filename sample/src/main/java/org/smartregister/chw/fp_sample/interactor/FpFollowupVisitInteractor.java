package org.smartregister.chw.fp_sample.interactor;

import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.interactor.BaseFpFollowupVisitInteractor;
import org.smartregister.chw.fp_sample.activity.EntryActivity;

public class FpFollowupVisitInteractor extends BaseFpFollowupVisitInteractor {
    @Override
    public MemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
