package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.ApplicationsettingsRecord;
import com.basariatpos.model.ApplicationSettingDTO;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.APPLICATIONSSETTINGS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSettingsRepositoryImplTest {

    @InjectMocks
    private ApplicationSettingsRepositoryImpl settingsRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private ApplicationSettingDTO testSettingDto;
    private ApplicationsettingsRecord testSettingRecord;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        testSettingDto = new ApplicationSettingDTO("app.theme", "dark", "Default application theme");
        testSettingRecord = new ApplicationsettingsRecord();
        testSettingRecord.setSettingKey("app.theme");
        testSettingRecord.setSettingValue("dark");
        testSettingRecord.setDescription("Default application theme");
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        ApplicationsettingsRecord recordToReturn;
        List<ApplicationsettingsRecord> recordsListToReturn = new ArrayList<>();
        String lastSQL;

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                Result<ApplicationsettingsRecord> result = create.newResult(APPLICATIONSSETTINGS);
                if (lastSQL.contains("WHERE \"PUBLIC\".\"APPLICATIONSSETTINGS\".\"SETTING_KEY\" = ?")) {
                    if (recordToReturn != null && recordToReturn.getSettingKey().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"APPLICATIONSSETTINGS\"")) { // findAll
                    result.addAll(recordsListToReturn);
                }
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"APPLICATIONSSETTINGS\"")) {
                // This covers the ON CONFLICT ... DO UPDATE part as well for mock purposes
                // A real DB would handle the upsert. Mock just assumes success.
                mock[0] = new MockResult(1, create.newResult(APPLICATIONSSETTINGS));
            } else {
                System.err.println("Unhandled SQL in MockDataProvider (AppSettings): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void setRecordToReturn(ApplicationsettingsRecord r) { this.recordToReturn = r; }
        public void setRecordsListToReturn(List<ApplicationsettingsRecord> l) { this.recordsListToReturn = l; }
    }

    @Test
    void findByKey_exists_returnsDto() {
        mockDataProvider.setRecordToReturn(testSettingRecord);
        Optional<ApplicationSettingDTO> result = settingsRepository.findByKey("app.theme");
        assertTrue(result.isPresent());
        assertEquals(testSettingDto.getSettingValue(), result.get().getSettingValue());
    }

    @Test
    void findByKey_notExists_returnsEmpty() {
        mockDataProvider.setRecordToReturn(null);
        Optional<ApplicationSettingDTO> result = settingsRepository.findByKey("nonexistent.key");
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_returnsAllSettings() {
        ApplicationsettingsRecord anotherRecord = new ApplicationsettingsRecord("app.version", "1.0", "App version");
        mockDataProvider.setRecordsListToReturn(List.of(testSettingRecord, anotherRecord));

        List<ApplicationSettingDTO> results = settingsRepository.findAll();

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getSettingKey().equals("app.theme")));
        assertTrue(results.stream().anyMatch(s -> s.getSettingKey().equals("app.version")));
    }

    @Test
    void save_newSetting_performsUpsert() {
        ApplicationSettingDTO newSetting = new ApplicationSettingDTO("new.setting", "new_value", "A new setting");

        ApplicationSettingDTO result = settingsRepository.save(newSetting);

        assertNotNull(result);
        assertEquals("new.setting", result.getSettingKey());
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"APPLICATIONSSETTINGS\""));
        assertTrue(mockDataProvider.lastSQL.contains("ON CONFLICT (\"SETTING_KEY\") DO UPDATE"));
    }

    @Test
    void save_existingSetting_performsUpsert() {
        testSettingDto.setSettingValue("light"); // Change value for update

        ApplicationSettingDTO result = settingsRepository.save(testSettingDto);

        assertNotNull(result);
        assertEquals("app.theme", result.getSettingKey());
        assertEquals("light", result.getSettingValue());
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"APPLICATIONSSETTINGS\""));
        assertTrue(mockDataProvider.lastSQL.contains("ON CONFLICT (\"SETTING_KEY\") DO UPDATE"));
        // More detailed check could involve verifying the SET part of the SQL if MockDataProvider exposed bindings for UPDATE on conflict
    }
}
