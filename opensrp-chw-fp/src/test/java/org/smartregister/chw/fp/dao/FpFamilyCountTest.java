package org.smartregister.chw.fp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.dao.AbstractDao;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = 28, manifest = Config.NONE)
public class FpFamilyCountTest extends FpDao {
    private Repository previousRepository;
    private SQLiteDatabase data;
    private final List<Cursor> cursors = new ArrayList<>();
    private String lastCountQuery;
    private String[] lastCountArguments;

    @Before
    public void setUp() throws Exception {
        previousRepository = ReflectionHelpers.getStaticField(AbstractDao.class, "repository");
        data = SQLiteDatabase.create(null);
        data.execSQL("CREATE TABLE ec_family_member (base_entity_id TEXT PRIMARY KEY, "
                + "relational_id TEXT, gender TEXT)");
        Repository repository = mock(Repository.class);
        // The same DAO contract supports both legacy and current SQLCipher dependencies.
        Class<?> databaseType = Repository.class.getMethod("getReadableDatabase").getReturnType();
        Object cipher = mock(databaseType, call -> {
            if ("rawQuery".equals(call.getMethod().getName())) {
                String query = call.getArgument(0);
                String[] arguments = call.getArgument(1);
                if (query.startsWith("SELECT count(")) {
                    lastCountQuery = query;
                    lastCountArguments = arguments;
                }
                Cursor cursor = data.rawQuery(query, arguments);
                cursors.add(cursor);
                return cursor;
            }
            return Answers.RETURNS_DEFAULTS.answer(call);
        });
        doReturn(cipher).when(repository).getReadableDatabase();
        setRepository(repository);
    }

    @After
    public void tearDown() {
        for (Cursor cursor : cursors) {
            if (!cursor.isClosed()) cursor.close();
        }
        data.close();
        setRepository(previousRepository);
    }

    @Test
    public void modernRegisterWithoutLegacyColumnCountsOnlyActiveHouseholdMembers() {
        createModernRegister();
        modernMember("active", "family", "Female", 0, "pop");
        modernMember("closed", "family", "Female", 1, "pop");
        modernMember("other-family", "another-family", "Female", 0, "pop");
        insertRegister("missing-member", 0, null, "pop");

        assertEquals(Integer.valueOf(1), FpDao.getFpWomenCount("family"));
        assertFalse(lastCountQuery.contains("ecp"));
        assertCursorsClosed();
    }

    @Test
    public void modernCountPreservesMembershipAcrossMethodsAndGenders() {
        createModernRegister();
        modernMember("female", "family", "Female", 0, "pop");
        modernMember("male", "family", "Male", 0, "condom");
        modernMember("not-provided", "family", "Female", 0, "method_not_provided");
        modernMember("no-method", "family", null, 0, null);

        assertEquals(Integer.valueOf(4), FpDao.getFpWomenCount("family"));
        assertFalse(lastCountQuery.contains("gender"));
        assertFalse(lastCountQuery.contains("provided_fp_method"));
    }

    @Test
    public void legacyRegisterRetainsNonEmergencyFlagAndHouseholdRestrictions() {
        data.execSQL("CREATE TABLE ec_family_planning "
                + "(base_entity_id TEXT, is_closed INTEGER, ecp TEXT)");
        legacyMember("regular-female", "family", "Female", 0, "1");
        legacyMember("regular-male", "family", "Male", 0, "1");
        legacyMember("emergency", "family", "Female", 0, "0");
        legacyMember("unknown", "family", "Female", 0, null);
        legacyMember("closed", "family", "Female", 1, "1");
        legacyMember("other-family", "another-family", "Female", 0, "1");

        assertEquals(Integer.valueOf(2), FpDao.getFpWomenCount("family"));
        assertTrue(lastCountQuery.contains("fp.ecp = 1"));
        assertCursorsClosed();
    }

    @Test
    public void legacyFlagStillAppliesWhenMethodColumnAlsoExists() {
        data.execSQL("CREATE TABLE ec_family_planning (base_entity_id TEXT, "
                + "is_closed INTEGER, ecp TEXT, provided_fp_method TEXT)");
        insertMember("regular", "family", "Male");
        insertMember("emergency", "family", "Female");
        insertRegister("regular", 0, "1", "condom");
        insertRegister("emergency", 0, "0", "ecp");

        assertEquals(Integer.valueOf(1), FpDao.getFpWomenCount("family"));
    }

    @Test
    public void householdIdsAreBoundAndComparedWithoutCaseSensitivity() {
        createModernRegister();
        modernMember("quoted-family-member", "house'hold", "Female", 0, "pop");
        modernMember("other-member", "other-family", "Male", 0, "condom");

        assertEquals(Integer.valueOf(1), FpDao.getFpWomenCount("HOUSE'HOLD"));
        assertEquals("HOUSE'HOLD", lastCountArguments[0]);
        assertTrue(lastCountQuery.contains("= ? COLLATE NOCASE"));
        assertFalse(lastCountQuery.contains("HOUSE'HOLD"));
        assertEquals(Integer.valueOf(0), FpDao.getFpWomenCount("' OR 1 = 1 --"));
    }

    @Test
    public void emptyOrUnmatchedHouseholdReturnsKnownZero() {
        createModernRegister();
        assertEquals(Integer.valueOf(0), FpDao.getFpWomenCount("family"));
        modernMember("other-member", "other-family", "Female", 0, "pop");
        assertEquals(Integer.valueOf(0), FpDao.getFpWomenCount("family"));
    }

    @Test
    public void columnInspectionDoesNotCacheAnOldSchema() {
        createModernRegister();
        modernMember("regular", "family", "Female", 0, "pop");
        modernMember("emergency", "family", "Female", 0, "ecp");
        assertEquals(Integer.valueOf(2), FpDao.getFpWomenCount("family"));

        data.execSQL("ALTER TABLE ec_family_planning ADD COLUMN ecp INTEGER");
        data.execSQL("UPDATE ec_family_planning SET ecp = CASE "
                + "WHEN base_entity_id = 'regular' THEN 1 ELSE 0 END");

        assertEquals(Integer.valueOf(1), FpDao.getFpWomenCount("family"));
        assertCursorsClosed();
    }

    @Test
    public void missingRegisterRemainsAnUnknownCountInsteadOfAFalseZero() {
        assertNull(FpDao.getFpWomenCount("family"));
        assertCursorsClosed();
    }

    private void createModernRegister() {
        data.execSQL("CREATE TABLE ec_family_planning "
                + "(base_entity_id TEXT, is_closed INTEGER, provided_fp_method TEXT)");
    }

    private void modernMember(String id, String family, String gender, int closed, String method) {
        insertMember(id, family, gender);
        insertRegister(id, closed, null, method);
    }

    private void legacyMember(String id, String family, String gender, int closed, String flag) {
        insertMember(id, family, gender);
        insertRegister(id, closed, flag, null);
    }

    private void insertMember(String id, String family, String gender) {
        ContentValues values = new ContentValues();
        values.put("base_entity_id", id);
        values.put("relational_id", family);
        values.put("gender", gender);
        data.insertOrThrow("ec_family_member", null, values);
    }

    private void insertRegister(String id, int closed, String flag, String method) {
        ContentValues values = new ContentValues();
        values.put("base_entity_id", id);
        values.put("is_closed", closed);
        if (lastColumnExists("ecp")) values.put("ecp", flag);
        if (lastColumnExists("provided_fp_method")) values.put("provided_fp_method", method);
        data.insertOrThrow("ec_family_planning", null, values);
    }

    private boolean lastColumnExists(String name) {
        try (Cursor cursor = data.rawQuery("PRAGMA table_info(ec_family_planning)", null)) {
            while (cursor.moveToNext()) {
                if (name.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) return true;
            }
            return false;
        }
    }

    private void assertCursorsClosed() {
        for (Cursor cursor : cursors) assertTrue(cursor.isClosed());
    }
}
