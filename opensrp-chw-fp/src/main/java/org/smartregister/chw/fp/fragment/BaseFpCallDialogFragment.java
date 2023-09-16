package org.smartregister.chw.fp.fragment;

import static android.view.View.GONE;
import static org.smartregister.util.Utils.getName;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.contract.BaseFpCallDialogContract;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.fp.listener.BaseFpCallWidgetDialogListener;

public class BaseFpCallDialogFragment extends DialogFragment implements BaseFpCallDialogContract.View {

    public static final String DIALOG_TAG = "BaseMalariaCallDialogFragment_DIALOG_TAG";
    private static FpMemberObject MEMBER_OBJECT;
    private View.OnClickListener listener = null;

    public static BaseFpCallDialogFragment launchDialog(Activity activity, FpMemberObject MO) {
        BaseFpCallDialogFragment dialogFragment = BaseFpCallDialogFragment.newInstance();
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        Fragment prev = activity.getFragmentManager().findFragmentByTag(DIALOG_TAG);
        MEMBER_OBJECT = MO;
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, DIALOG_TAG);

        return dialogFragment;
    }

    public static BaseFpCallDialogFragment newInstance() {
        return new BaseFpCallDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ChwTheme_Dialog_FullWidth);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fp_member_call_widget_dialog_fragment, container, false);
        setUpPosition();
        if (listener == null) {
            listener = new BaseFpCallWidgetDialogListener(this);
        }

        initUI(dialogView);
        return dialogView;
    }

    private void setCallTitle(ViewGroup rootView, int viewId, final String message) {
        TextView callTitle = rootView.findViewById(viewId);
        if (MEMBER_OBJECT.getBaseEntityId().equals(MEMBER_OBJECT.getFamilyHead())) {
            callTitle.setText(String.format("%s %s", message, getResources().getString(R.string.call_family_head)));
        } else if (MEMBER_OBJECT.getBaseEntityId().equals(MEMBER_OBJECT.getPrimaryCareGiver())) {
            callTitle.setText(String.format("%s %s", message, getResources().getString(R.string.call_primary_caregiver)));
        } else {
            callTitle.setText(String.format("%s %s", message, getResources().getString(R.string.call_fp_client)));
        }
    }

    private void initUI(ViewGroup rootView) {
        if (StringUtils.isNotBlank(MEMBER_OBJECT.getPhoneNumber())) {
            setCallTitle(rootView, R.id.call_title, getResources().getString(R.string.call));
            if (StringUtils.isNotBlank(MEMBER_OBJECT.getFamilyHead())) {
                TextView familyHeadName = rootView.findViewById(R.id.fp_call_head_name);
                familyHeadName.setText(MEMBER_OBJECT.getFamilyHeadName());
                TextView clientCallHeadPhone = rootView.findViewById(R.id.fp_call_head_phone);
                clientCallHeadPhone.setTag(MEMBER_OBJECT.getPhoneNumber());
                clientCallHeadPhone.setText(getName(getCurrentContext().getString(R.string.call), MEMBER_OBJECT.getFamilyHeadPhoneNumber()));
                clientCallHeadPhone.setOnClickListener(listener);

            } else {
                rootView.findViewById(R.id.fp_layout_family_head).setVisibility(GONE);
            }

            if (!MEMBER_OBJECT.getBaseEntityId().equals(MEMBER_OBJECT.getFamilyHead())) {
                //just a member
                TextView malariaClientNameTextView = rootView.findViewById(R.id.call_fp_client_name);
                malariaClientNameTextView.setText(String.format("%s %s %s", MEMBER_OBJECT.getFirstName(), MEMBER_OBJECT.getMiddleName(), MEMBER_OBJECT.getLastName()));

                setCallTitle(rootView, R.id.call_fp_client_title, "");
                TextView callMalariaClientPhone = rootView.findViewById(R.id.call_fp_client_phone);
                callMalariaClientPhone.setTag(MEMBER_OBJECT.getPhoneNumber());
                callMalariaClientPhone.setText(getName(getCurrentContext().getString(R.string.call), MEMBER_OBJECT.getPhoneNumber()));
                callMalariaClientPhone.setOnClickListener(listener);
            } else {
                rootView.findViewById(R.id.layout_fp_client).setVisibility(GONE);
            }
        }

        rootView.findViewById(R.id.fp_call_close).setOnClickListener(listener);
    }

    private void setUpPosition() {
        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        p.y = 20;
        getDialog().getWindow().setAttributes(p);
    }

    @Override
    public Context getCurrentContext() {
        return getActivity();
    }

    @Override
    public void setPendingCallRequest(BaseFpCallDialogContract.Dialer dialer) {
//        Implement pending call request
//        BaseAncWomanCallDialogContract.Dialer mDialer = dialer;
    }
}
