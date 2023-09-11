package org.smartregister.chw.fp.contract;

import android.content.Context;

public interface BaseFpCallDialogContract {

    interface View {
        void setPendingCallRequest(Dialer dialer);
        Context getCurrentContext();
    }

    interface Dialer {
        void callMe();
    }
}
