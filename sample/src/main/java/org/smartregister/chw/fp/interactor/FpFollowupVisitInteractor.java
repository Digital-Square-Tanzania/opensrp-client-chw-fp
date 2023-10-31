package org.smartregister.chw.fp.interactor;

import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.activity.EntryActivity;

public class FpFollowupVisitInteractor extends BaseFpFollowupVisitInteractor {
    @Override
    public FpMemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
