package org.smartregister.chw.fp.model;

import androidx.annotation.NonNull;

import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.fp.contract.BaseFpRegisterFragmentContract;
import org.smartregister.chw.fp.util.ConfigHelper;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;

import java.util.HashSet;
import java.util.Set;

public class BaseFpRegisterFragmentModel implements BaseFpRegisterFragmentContract.Model {

    @Override
    public RegisterConfiguration defaultRegisterConfiguration() {
        return ConfigHelper.defaultRegisterConfiguration(FpLibrary.getInstance().context().applicationContext());
    }

    @Override
    public ViewConfiguration getViewConfiguration(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getViewConfiguration(viewConfigurationIdentifier);
    }

    @Override
    public Set<View> getRegisterActiveColumns(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getRegisterActiveColumns(viewConfigurationIdentifier);
    }

    @Override
    public String countSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.selectInitiateMainTableCounts(tableName);
        return countQueryBuilder.mainCondition(mainCondition);
    }

    @NonNull
    @Override
    public String mainSelect(@NonNull String tableName, @NonNull String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(tableName, mainColumns(tableName));
        queryBuilder.customJoin("INNER JOIN " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + " ON  " + tableName + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID + " = " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + FamilyPlanningConstants.DBConstants.FAMILY + " ON  " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID + " = " + FamilyPlanningConstants.DBConstants.FAMILY + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID);

        return queryBuilder.mainCondition(mainCondition);
    }

    protected String[] mainColumns(String tableName) {
        Set<String> columnList = new HashSet<>();
        columnList.add(tableName + "." + FamilyPlanningConstants.DBConstants.LAST_INTERACTED_WITH);
        columnList.add(tableName + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID);
        columnList.add(tableName + "." + FamilyPlanningConstants.DBConstants.FP_METHOD_PROVIDED);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID + " as relationalid");
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.FIRST_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.MIDDLE_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.LAST_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.DOB);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY + "." + FamilyPlanningConstants.DBConstants.VILLAGE_TOWN);

        return columnList.toArray(new String[columnList.size()]);
    }

}
