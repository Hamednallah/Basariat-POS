package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.PatientsRecord;
import com.basariatpos.model.PatientDTO;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Result;
import org.jooq.Record1;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.PATIENTS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PatientRepositoryImplTest {

    @InjectMocks
    private PatientRepositoryImpl patientRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider;
    private MockedStatic<DBManager> mockDBManagerStatic;

    private PatientDTO testPatientDto;
    private PatientsRecord testPatientRecord;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        testPatientDto = new PatientDTO(1, "PAT-001", "John Doe", "0912345678", "Khartoum", true, 1, now, now);

        testPatientRecord = new PatientsRecord();
        testPatientRecord.setPatientId(1);
        testPatientRecord.setSystemPatientId("PAT-001");
        testPatientRecord.setFullName("John Doe");
        testPatientRecord.setPhoneNumber("0912345678");
        testPatientRecord.setAddress("Khartoum");
        testPatientRecord.setWhatsappOptIn(true);
        testPatientRecord.setCreatedByUserId(1);
        testPatientRecord.setCreatedAt(now);
        testPatientRecord.setUpdatedAt(now);
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    private static class TestDataProvider implements MockDataProvider {
        PatientsRecord recordToReturn;
        List<PatientsRecord> recordsListToReturn = new ArrayList<>();
        boolean expectExists = false;
        String lastSQL;
        int nextId = 100; // For simulating auto-increment for patient_id

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                Result<PatientsRecord> result = create.newResult(PATIENTS);
                if (lastSQL.contains("WHERE \"PUBLIC\".\"PATIENTS\".\"PATIENT_ID\" = ?")) {
                    if (recordToReturn != null && recordToReturn.getPatientId().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                } else if (lastSQL.contains("WHERE \"PUBLIC\".\"PATIENTS\".\"SYSTEM_PATIENT_ID\" = ?")) {
                     if (recordToReturn != null && recordToReturn.getSystemPatientId().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                } else if (lastSQL.contains("WHERE \"PUBLIC\".\"PATIENTS\".\"PHONE_NUMBER\" = ?")) {
                     if (recordToReturn != null && recordToReturn.getPhoneNumber().equals(ctx.bindings()[0])) {
                        result.add(recordToReturn);
                    }
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"PATIENTS\"")) { // Covers findAll and searchByName/Phone
                    // For search, this mock is simplified and returns all in recordsListToReturn
                    // A real test might check the WHERE clause for LIKE conditions.
                    result.addAll(recordsListToReturn);
                } else if (lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"PATIENTS\" WHERE \"PUBLIC\".\"PATIENTS\".\"SYSTEM_PATIENT_ID\" = ?)")) {
                    Result<Record1<Boolean>> existsResult = create.newResult(DSL.field("exists", SQLDataType.BOOLEAN));
                    existsResult.add(create.newRecord(DSL.field("exists", SQLDataType.BOOLEAN)).values(expectExists));
                    mock[0] = new MockResult(1, existsResult);
                    return mock; // Return early for exists check
                }
                mock[0] = new MockResult(result.size(), result);
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PATIENTS\"")) {
                PatientsRecord insertedRecord = create.newRecord(PATIENTS);
                insertedRecord.setPatientId(nextId++); // Simulate auto-generated ID
                // Bindings are: SYSTEM_PATIENT_ID, FULL_NAME, PHONE_NUMBER, ADDRESS, WHATSAPP_OPT_IN, CREATED_BY_USER_ID, CREATED_AT, UPDATED_AT
                insertedRecord.setSystemPatientId((String)ctx.bindings()[0]);
                insertedRecord.setFullName((String)ctx.bindings()[1]);
                insertedRecord.setPhoneNumber((String)ctx.bindings()[2]);
                // ... map other fields from bindings as per PatientsRecord field order in insert
                Result<PatientsRecord> result = create.newResult(PATIENTS);
                result.add(insertedRecord);
                mock[0] = new MockResult(1, result);
                this.recordToReturn = insertedRecord; // Make it available for get returning_id if jOOQ uses that
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"PATIENTS\"")) {
                mock[0] = new MockResult(1, create.newResult(PATIENTS)); // Assume 1 row updated
            } else {
                System.err.println("Unhandled SQL in MockDataProvider (Patient): " + lastSQL);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        public void setRecordToReturn(PatientsRecord r) { this.recordToReturn = r; }
        public void setRecordsListToReturn(List<PatientsRecord> l) { this.recordsListToReturn = l; }
        public void setExpectSystemIdExists(boolean e) { this.expectExists = e; }
    }

    @Test
    void findById_exists_returnsDto() {
        mockDataProvider.setRecordToReturn(testPatientRecord);
        Optional<PatientDTO> result = patientRepository.findById(1);
        assertTrue(result.isPresent());
        assertEquals(testPatientDto.getFullName(), result.get().getFullName());
    }

    @Test
    void save_newPatient_insertsAndReturnsDtoWithIds() {
        PatientDTO newPatient = new PatientDTO("Jane Doe", "0987654321", "Omdurman", false);
        // systemPatientId will be generated by the repository method if not set on DTO

        PatientDTO savedPatient = patientRepository.save(newPatient);

        assertNotNull(savedPatient);
        assertTrue(savedPatient.getPatientId() >= 100); // Check if patient_id was assigned
        assertNotNull(savedPatient.getSystemPatientId()); // Check if system_patient_id was generated
        assertFalse(savedPatient.getSystemPatientId().isBlank());
        assertEquals("Jane Doe", savedPatient.getFullName());
        // Verify lastSQL in mockDataProvider for INSERT statement
        assertTrue(mockDataProvider.lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"PATIENTS\""));
    }

    @Test
    void save_existingPatient_updatesAndReturnsDto() {
        mockDataProvider.setRecordToReturn(testPatientRecord); // Prime for the fetch before update
        testPatientDto.setFullName("John Doe Updated");

        PatientDTO updatedPatient = patientRepository.save(testPatientDto);

        assertNotNull(updatedPatient);
        assertEquals(testPatientDto.getPatientId(), updatedPatient.getPatientId());
        assertEquals("John Doe Updated", updatedPatient.getFullName());
        assertTrue(mockDataProvider.lastSQL.startsWith("UPDATE \"PUBLIC\".\"PATIENTS\""));
    }


    @Test
    void systemIdExists_idExists_returnsTrue() {
        mockDataProvider.setExpectSystemIdExists(true);
        boolean exists = patientRepository.systemIdExists("PAT-EXISTING");
        assertTrue(exists);
    }

    @Test
    void systemIdExists_idNotExists_returnsFalse() {
        mockDataProvider.setExpectSystemIdExists(false);
        boolean exists = patientRepository.systemIdExists("PAT-NONEXISTENT");
        assertFalse(exists);
    }

    // Add more tests for other finders (findBySystemPatientId, findByPhoneNumber, searchByName, searchByNameOrPhone, findAll)
    // and edge cases for save (e.g. DTO with systemPatientId already set).
}
