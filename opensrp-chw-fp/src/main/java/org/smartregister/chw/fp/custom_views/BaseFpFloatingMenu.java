package org.smartregister.chw.fp.custom_views;

import android.app.Activity;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.chw.fp.R;
import org.smartregister.chw.fp.domain.MemberObject;
import org.smartregister.chw.fp.fragment.BaseFpCallDialogFragment;

public class BaseFpFloatingMenu extends LinearLayout implements View.OnClickListener {
    private MemberObject MEMBER_OBJECT;

    public BaseFpFloatingMenu(Context context, MemberObject MEMBER_OBJECT) {
        super(context);
        initUi();
        this.MEMBER_OBJECT = MEMBER_OBJECT;
    }

    protected void initUi() {
        inflate(getContext(), R.layout.view_fp_floating_menu, this);
        FloatingActionButton fab = findViewById(R.id.sbc_fab);
        if (fab != null)
            fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sbc_fab) {
            Activity activity = (Activity) getContext();
            BaseFpCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }  else if (view.getId() == R.id.refer_to_facility_layout) {
            Activity activity = (Activity) getContext();
            BaseFpCallDialogFragment.launchDialog(activity, MEMBER_OBJECT);
        }
    }
}