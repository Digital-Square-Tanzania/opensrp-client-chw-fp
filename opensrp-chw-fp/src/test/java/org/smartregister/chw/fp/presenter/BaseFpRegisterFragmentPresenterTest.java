package org.smartregister.chw.fp.presenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.fp.contract.BaseFpRegisterFragmentContract;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.fp.util.DBConstants;
import org.smartregister.configurableviews.model.View;

import java.util.Set;
import java.util.TreeSet;

public class BaseFpRegisterFragmentPresenterTest {
    @Mock
    protected BaseFpRegisterFragmentContract.View view;

    @Mock
    protected BaseFpRegisterFragmentContract.Model model;

    private BaseFpRegisterFragmentPresenter baseFpRegisterFragmentPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        baseFpRegisterFragmentPresenter = new BaseFpRegisterFragmentPresenter(view, model, "");
    }

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseFpRegisterFragmentPresenter);
    }

    @Test
    public void getMainCondition() {
        Assert.assertEquals("ec_family_planning.is_closed is 0", baseFpRegisterFragmentPresenter.getMainCondition());
    }

    @Test
    public void getDueFilterCondition() {
        Assert.assertEquals(" (cast( julianday(STRFTIME('%Y-%m-%d', datetime('now'))) -  julianday(IFNULL(SUBSTR(malaria_test_date,7,4)|| '-' || SUBSTR(malaria_test_date,4,2) || '-' || SUBSTR(malaria_test_date,1,2),'')) as integer) between 7 and 14) ", baseFpRegisterFragmentPresenter.getDueFilterCondition());
    }

    @Test
    public void getDefaultSortQuery() {
        Assert.assertEquals(FamilyPlanningConstants.TABLES.FP_REGISTER + "." + DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ", baseFpRegisterFragmentPresenter.getDefaultSortQuery());
    }

    @Test
    public void getMainTable() {
        Assert.assertEquals(FamilyPlanningConstants.TABLES.FP_REGISTER, baseFpRegisterFragmentPresenter.getMainTable());
    }

    @Test
    public void initializeQueries() {
        Set<View> visibleColumns = new TreeSet<>();
        baseFpRegisterFragmentPresenter.initializeQueries(null);
        Mockito.doNothing().when(view).initializeQueryParams("ec_family_planning", null, null);
        Mockito.verify(view).initializeQueryParams("ec_family_planning", null, null);
        Mockito.verify(view).initializeAdapter(visibleColumns);
        Mockito.verify(view).countExecute();
        Mockito.verify(view).filterandSortInInitializeQueries();
    }

}