package org.smartregister.chw.fp.listener;


import android.view.View;

import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.fragment.BaseFpCallDialogFragment;
import org.smartregister.chw.fp.util.FpUtil;

import timber.log.Timber;

public class BaseFpCallWidgetDialogListener implements View.OnClickListener {

    private BaseFpCallDialogFragment callDialogFragment;

    public BaseFpCallWidgetDialogListener(BaseFpCallDialogFragment dialogFragment) {
        callDialogFragment = dialogFragment;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.fp_call_close) {
            callDialogFragment.dismiss();
        } else if (i == R.id.fp_call_head_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                FpUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (i == R.id.call_fp_client_phone) {
            try {
                String phoneNumber = (String) v.getTag();
                FpUtil.launchDialer(callDialogFragment.getActivity(), callDialogFragment, phoneNumber);
                callDialogFragment.dismiss();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
