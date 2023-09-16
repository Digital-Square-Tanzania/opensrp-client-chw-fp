package org.smartregister.chw.fp_sample.interactor;

import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.interactor.BaseFpScreeningVisitInteractor;
import org.smartregister.chw.fp_sample.activity.EntryActivity;

public class FpScreeningVisitInteractor extends BaseFpScreeningVisitInteractor {
    @Override
    public FpMemberObject getMemberClient(String memberID) {
        return EntryActivity.getSampleMember();
    }
}
