package com.basariatpos.repository;

import com.basariatpos.config.DBManager;
import com.basariatpos.db.generated.tables.records.UserpermissionsRecord;
import com.basariatpos.db.generated.tables.records.UsersRecord;
import com.basariatpos.model.UserDTO;
import org.jooq.*;
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
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.basariatpos.db.generated.Tables.USERS;
import static com.basariatpos.db.generated.Tables.USERPERMISSIONS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @InjectMocks
    private UserRepositoryImpl userRepository;

    private DSLContext dslContext;
    private TestDataProvider mockDataProvider; // Renamed for clarity
    private MockedStatic<DBManager> mockDBManagerStatic;

    private UserDTO testUserDto;
    private UsersRecord testUserRecord;

    @BeforeEach
    void setUp() {
        mockDataProvider = new TestDataProvider();
        Connection connection = new MockConnection(mockDataProvider);
        dslContext = DSL.using(connection, SQLDialect.POSTGRES);

        mockDBManagerStatic = Mockito.mockStatic(DBManager.class);
        mockDBManagerStatic.when(DBManager::getDSLContext).thenReturn(dslContext);

        testUserDto = new UserDTO(1, "testuser", "Test User", "Admin", true, new ArrayList<>(), "hashedpassword");
        testUserRecord = new UsersRecord();
        testUserRecord.setUserId(1);
        testUserRecord.setUsername("testuser");
        testUserRecord.setFullName("Test User");
        testUserRecord.setRole("Admin");
        testUserRecord.setPasswordHash("hashedpassword");
        testUserRecord.setIsActive(true);
        testUserRecord.setCreatedAt(OffsetDateTime.now());
        testUserRecord.setUpdatedAt(OffsetDateTime.now());
    }

    @AfterEach
    void tearDown() {
        mockDBManagerStatic.close();
    }

    // --- TestDataProvider Inner Class ---
    // (This class simulates DB responses for jOOQ queries)
    private static class TestDataProvider implements MockDataProvider {
        UsersRecord userToReturn;
        List<UsersRecord> usersListToReturn = new ArrayList<>();
        List<UserpermissionsRecord> permissionsListToReturn = new ArrayList<>();
        boolean expectUserExists = false;
        boolean expectPermissionExists = false;
        int nextUserId = 1; // For simulating auto-increment
        String lastSQL; // For debugging or simple verification

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {
            lastSQL = ctx.sql().toUpperCase();
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            MockResult[] mock = new MockResult[1];

            if (lastSQL.startsWith("SELECT")) {
                if (lastSQL.contains("FROM \"PUBLIC\".\"USERS\"") && lastSQL.contains("WHERE \"PUBLIC\".\"USERS\".\"USERNAME\" = ?")) {
                    Result<UsersRecord> result = create.newResult(USERS);
                    if (userToReturn != null && userToReturn.getUsername().equals(ctx.bindings()[0])) {
                        result.add(userToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("FROM \"PUBLIC\".\"USERS\"") && lastSQL.contains("WHERE \"PUBLIC\".\"USERS\".\"USER_ID\" = ?")) {
                     Result<UsersRecord> result = create.newResult(USERS);
                    if (userToReturn != null && userToReturn.getUserId().equals(ctx.bindings()[0])) {
                        result.add(userToReturn);
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.equals("SELECT \"PUBLIC\".\"USERS\".\"USER_ID\", \"PUBLIC\".\"USERS\".\"USERNAME\", \"PUBLIC\".\"USERS\".\"PASSWORD_HASH\", \"PUBLIC\".\"USERS\".\"FULL_NAME\", \"PUBLIC\".\"USERS\".\"ROLE\", \"PUBLIC\".\"USERS\".\"IS_ACTIVE\", \"PUBLIC\".\"USERS\".\"CREATED_AT\", \"PUBLIC\".\"USERS\".\"UPDATED_AT\" FROM \"PUBLIC\".\"USERS\"")) {
                    Result<UsersRecord> result = create.newResult(USERS);
                    result.addAll(usersListToReturn);
                     mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("SELECT \"PUBLIC\".\"USERPERMISSIONS\".\"PERMISSION_NAME\" FROM \"PUBLIC\".\"USERPERMISSIONS\" WHERE (\"PUBLIC\".\"USERPERMISSIONS\".\"USER_ID\" = ? AND \"PUBLIC\".\"USERPERMISSIONS\".\"IS_GRANTED\" = TRUE)")) {
                    Result<Record1<String>> result = create.newResult(USERPERMISSIONS.PERMISSION_NAME);
                    for(UserpermissionsRecord pRec : permissionsListToReturn){
                        if(pRec.getUserId().equals(ctx.bindings()[0]) && pRec.getIsGranted()){
                            Record1<String> r = create.newRecord(USERPERMISSIONS.PERMISSION_NAME);
                            r.setValue(USERPERMISSIONS.PERMISSION_NAME, pRec.getPermissionName());
                            result.add(r);
                        }
                    }
                    mock[0] = new MockResult(result.size(), result);
                } else if (lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"USERPERMISSIONS\" WHERE (\"PUBLIC\".\"USERPERMISSIONS\".\"USER_ID\" = ? AND \"PUBLIC\".\"USERPERMISSIONS\".\"PERMISSION_NAME\" = ? AND \"PUBLIC\".\"USERPERMISSIONS\".\"IS_GRANTED\" = TRUE))")) {
                    Result<Record1<Boolean>> result = create.newResult(DSL.field("exists", SQLDataType.BOOLEAN));
                    result.add(create.newRecord(DSL.field("exists", SQLDataType.BOOLEAN)).values(expectPermissionExists));
                    mock[0] = new MockResult(1, result);
                } else {
                     mock[0] = new MockResult(0, create.newResult(USERS)); // Default empty
                }
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"USERS\"")) {
                userToReturn = create.newRecord(USERS); // Simulate insert
                userToReturn.setUserId(nextUserId++);
                userToReturn.setUsername((String)ctx.bindings()[0]);
                userToReturn.setPasswordHash((String)ctx.bindings()[1]);
                userToReturn.setFullName((String)ctx.bindings()[2]);
                userToReturn.setRole((String)ctx.bindings()[3]);
                userToReturn.setIsActive((Boolean)ctx.bindings()[4]);
                // Timestamps would be set by DB or NOW()
                mock[0] = new MockResult(1, create.newResult(USERS)); // 1 row inserted
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"USERS\"")) {
                 mock[0] = new MockResult(1, create.newResult(USERS)); // Assume 1 row updated
            } else if (lastSQL.startsWith("INSERT INTO \"PUBLIC\".\"USERPERMISSIONS\"")) {
                mock[0] = new MockResult(1, create.newResult(USERPERMISSIONS));
            } else if (lastSQL.startsWith("UPDATE \"PUBLIC\".\"USERPERMISSIONS\"")) {
                 mock[0] = new MockResult(1, create.newResult(USERPERMISSIONS));
            }
            // Add more conditions for other SQL statements (UPDATE, DELETE, other SELECTs)
            if(mock[0] == null) { // Fallback for unhandled SQL
                System.err.println("Unhandled SQL in MockDataProvider: " + sql);
                mock[0] = new MockResult(0, create.newResult());
            }
            return mock;
        }
        // Helper methods to prime the provider for specific tests
        public void setExpectedUser(UsersRecord user) { this.userToReturn = user; }
        public void setExpectedUsersList(List<UsersRecord> users) { this.usersListToReturn = users; }
        public void setExpectedPermissionsList(List<UserpermissionsRecord> permissions) { this.permissionsListToReturn = permissions; }
        public void setExpectPermissionExists(boolean exists) { this.expectPermissionExists = exists; }
    }

    // --- Test Cases ---
    @Test
    void findByUsername_userExists_returnsUserDTO() {
        mockDataProvider.setExpectedUser(testUserRecord);
        Optional<UserDTO> result = userRepository.findByUsername("testuser");
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("hashedpassword", result.get().getPasswordHash()); // Verify hash is included
    }

    @Test
    void findByUsername_userNotExists_returnsEmpty() {
        mockDataProvider.setExpectedUser(null);
        Optional<UserDTO> result = userRepository.findByUsername("unknownuser");
        assertFalse(result.isPresent());
    }

    @Test
    void save_newUser_returnsUserDTOWithId() {
        UserDTO newUser = new UserDTO(0, "newbie", "New User", "Cashier"); // ID 0 or ignored
        newUser.setActive(true);

        // MockDataProvider will simulate the insert and assign nextUserId
        UserDTO savedUser = userRepository.save(newUser, "newhash");

        assertNotNull(savedUser);
        assertTrue(savedUser.getUserId() >= 1); // Check if an ID was assigned
        assertEquals("newbie", savedUser.getUsername());
        // Verify lastSQL in mockDataProvider if needed, or internal state of provider
    }

    @Test
    void findUserPermissions_userHasPermissions_returnsPermissionNames() {
        UserpermissionsRecord p1 = new UserpermissionsRecord(1, "perm1", true, OffsetDateTime.now());
        UserpermissionsRecord p2 = new UserpermissionsRecord(1, "perm2", true, OffsetDateTime.now());
        UserpermissionsRecord p3_notGranted = new UserpermissionsRecord(1, "perm3", false, OffsetDateTime.now());
        UserpermissionsRecord p4_otherUser = new UserpermissionsRecord(2, "perm4", true, OffsetDateTime.now());

        mockDataProvider.setExpectedPermissionsList(List.of(p1, p2, p3_notGranted, p4_otherUser));

        List<String> permissions = userRepository.findUserPermissions(1);

        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("perm1"));
        assertTrue(permissions.contains("perm2"));
        assertFalse(permissions.contains("perm3"));
        assertFalse(permissions.contains("perm4"));
    }

    @Test
    void hasPermission_userHasPermission_returnsTrue() {
        mockDataProvider.setExpectPermissionExists(true);
        boolean result = userRepository.hasPermission(1, "orders.create");
        assertTrue(result);
        // Verify the SQL executed by the mock provider (example of how you might check)
        assertTrue(mockDataProvider.lastSQL.contains("SELECT EXISTS(SELECT 1 FROM \"PUBLIC\".\"USERPERMISSIONS\" WHERE (\"PUBLIC\".\"USERPERMISSIONS\".\"USER_ID\" = ? AND \"PUBLIC\".\"USERPERMISSIONS\".\"PERMISSION_NAME\" = ? AND \"PUBLIC\".\"USERPERMISSIONS\".\"IS_GRANTED\" = TRUE))"));
    }

    @Test
    void hasPermission_userDoesNotHavePermission_returnsFalse() {
        mockDataProvider.setExpectPermissionExists(false);
        boolean result = userRepository.hasPermission(1, "admin.access");
        assertFalse(result);
    }

    // Add more tests for:
    // findById, update, updateUserPassword, findAll, setUserActiveStatus, grantPermission, revokePermission
    // Ensure to set up mockDataProvider appropriately for each case.
}
