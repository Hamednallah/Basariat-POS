/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables;


import com.basariatpos.db.generated.Keys;
import com.basariatpos.db.generated.Public;
import com.basariatpos.db.generated.tables.Appointments.AppointmentsPath;
import com.basariatpos.db.generated.tables.Auditlog.AuditlogPath;
import com.basariatpos.db.generated.tables.Expenses.ExpensesPath;
import com.basariatpos.db.generated.tables.Opticaldiagnostics.OpticaldiagnosticsPath;
import com.basariatpos.db.generated.tables.Patients.PatientsPath;
import com.basariatpos.db.generated.tables.Payments.PaymentsPath;
import com.basariatpos.db.generated.tables.Purchaseorders.PurchaseordersPath;
import com.basariatpos.db.generated.tables.Salesorders.SalesordersPath;
import com.basariatpos.db.generated.tables.Shiftpauselog.ShiftpauselogPath;
import com.basariatpos.db.generated.tables.Shifts.ShiftsPath;
import com.basariatpos.db.generated.tables.Userpermissions.UserpermissionsPath;
import com.basariatpos.db.generated.tables.records.UsersRecord;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jooq.Check;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Users extends TableImpl<UsersRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.users</code>
     */
    public static final Users USERS = new Users();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UsersRecord> getRecordType() {
        return UsersRecord.class;
    }

    /**
     * The column <code>public.users.user_id</code>.
     */
    public final TableField<UsersRecord, Integer> USER_ID = createField(DSL.name("user_id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.users.username</code>.
     */
    public final TableField<UsersRecord, String> USERNAME = createField(DSL.name("username"), SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>public.users.password_hash</code>.
     */
    public final TableField<UsersRecord, String> PASSWORD_HASH = createField(DSL.name("password_hash"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.users.full_name</code>.
     */
    public final TableField<UsersRecord, String> FULL_NAME = createField(DSL.name("full_name"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.users.role</code>.
     */
    public final TableField<UsersRecord, String> ROLE = createField(DSL.name("role"), SQLDataType.VARCHAR(50).nullable(false), this, "");

    /**
     * The column <code>public.users.is_active</code>.
     */
    public final TableField<UsersRecord, Boolean> IS_ACTIVE = createField(DSL.name("is_active"), SQLDataType.BOOLEAN.defaultValue(DSL.field(DSL.raw("true"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.users.created_at</code>.
     */
    public final TableField<UsersRecord, OffsetDateTime> CREATED_AT = createField(DSL.name("created_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "");

    /**
     * The column <code>public.users.updated_at</code>.
     */
    public final TableField<UsersRecord, OffsetDateTime> UPDATED_AT = createField(DSL.name("updated_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6).defaultValue(DSL.field(DSL.raw("CURRENT_TIMESTAMP"), SQLDataType.TIMESTAMPWITHTIMEZONE)), this, "");

    private Users(Name alias, Table<UsersRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Users(Name alias, Table<UsersRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(String alias) {
        this(DSL.name(alias), USERS);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(Name alias) {
        this(alias, USERS);
    }

    /**
     * Create a <code>public.users</code> table reference
     */
    public Users() {
        this(DSL.name("users"), null);
    }

    public <O extends Record> Users(Table<O> path, ForeignKey<O, UsersRecord> childPath, InverseForeignKey<O, UsersRecord> parentPath) {
        super(path, childPath, parentPath, USERS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class UsersPath extends Users implements Path<UsersRecord> {
        public <O extends Record> UsersPath(Table<O> path, ForeignKey<O, UsersRecord> childPath, InverseForeignKey<O, UsersRecord> parentPath) {
            super(path, childPath, parentPath);
        }
        private UsersPath(Name alias, Table<UsersRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public UsersPath as(String alias) {
            return new UsersPath(DSL.name(alias), this);
        }

        @Override
        public UsersPath as(Name alias) {
            return new UsersPath(alias, this);
        }

        @Override
        public UsersPath as(Table<?> alias) {
            return new UsersPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<UsersRecord, Integer> getIdentity() {
        return (Identity<UsersRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<UsersRecord> getPrimaryKey() {
        return Keys.USERS_PKEY;
    }

    @Override
    public List<UniqueKey<UsersRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.USERS_USERNAME_KEY);
    }

    private transient AppointmentsPath _appointments;

    /**
     * Get the implicit to-many join path to the
     * <code>public.appointments</code> table
     */
    public AppointmentsPath appointments() {
        if (_appointments == null)
            _appointments = new AppointmentsPath(this, null, Keys.APPOINTMENTS__APPOINTMENTS_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _appointments;
    }

    private transient AuditlogPath _auditlog;

    /**
     * Get the implicit to-many join path to the <code>public.auditlog</code>
     * table
     */
    public AuditlogPath auditlog() {
        if (_auditlog == null)
            _auditlog = new AuditlogPath(this, null, Keys.AUDITLOG__AUDITLOG_USER_ID_FKEY.getInverseKey());

        return _auditlog;
    }

    private transient ExpensesPath _expenses;

    /**
     * Get the implicit to-many join path to the <code>public.expenses</code>
     * table
     */
    public ExpensesPath expenses() {
        if (_expenses == null)
            _expenses = new ExpensesPath(this, null, Keys.EXPENSES__EXPENSES_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _expenses;
    }

    private transient OpticaldiagnosticsPath _opticaldiagnostics;

    /**
     * Get the implicit to-many join path to the
     * <code>public.opticaldiagnostics</code> table
     */
    public OpticaldiagnosticsPath opticaldiagnostics() {
        if (_opticaldiagnostics == null)
            _opticaldiagnostics = new OpticaldiagnosticsPath(this, null, Keys.OPTICALDIAGNOSTICS__OPTICALDIAGNOSTICS_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _opticaldiagnostics;
    }

    private transient PatientsPath _patients;

    /**
     * Get the implicit to-many join path to the <code>public.patients</code>
     * table
     */
    public PatientsPath patients() {
        if (_patients == null)
            _patients = new PatientsPath(this, null, Keys.PATIENTS__PATIENTS_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _patients;
    }

    private transient PaymentsPath _payments;

    /**
     * Get the implicit to-many join path to the <code>public.payments</code>
     * table
     */
    public PaymentsPath payments() {
        if (_payments == null)
            _payments = new PaymentsPath(this, null, Keys.PAYMENTS__PAYMENTS_RECEIVED_BY_USER_ID_FKEY.getInverseKey());

        return _payments;
    }

    private transient PurchaseordersPath _purchaseorders;

    /**
     * Get the implicit to-many join path to the
     * <code>public.purchaseorders</code> table
     */
    public PurchaseordersPath purchaseorders() {
        if (_purchaseorders == null)
            _purchaseorders = new PurchaseordersPath(this, null, Keys.PURCHASEORDERS__PURCHASEORDERS_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _purchaseorders;
    }

    private transient SalesordersPath _salesorders;

    /**
     * Get the implicit to-many join path to the <code>public.salesorders</code>
     * table
     */
    public SalesordersPath salesorders() {
        if (_salesorders == null)
            _salesorders = new SalesordersPath(this, null, Keys.SALESORDERS__SALESORDERS_CREATED_BY_USER_ID_FKEY.getInverseKey());

        return _salesorders;
    }

    private transient ShiftpauselogPath _shiftpauselog;

    /**
     * Get the implicit to-many join path to the
     * <code>public.shiftpauselog</code> table
     */
    public ShiftpauselogPath shiftpauselog() {
        if (_shiftpauselog == null)
            _shiftpauselog = new ShiftpauselogPath(this, null, Keys.SHIFTPAUSELOG__SHIFTPAUSELOG_PAUSED_BY_USER_ID_FKEY.getInverseKey());

        return _shiftpauselog;
    }

    private transient ShiftsPath _shifts;

    /**
     * Get the implicit to-many join path to the <code>public.shifts</code>
     * table
     */
    public ShiftsPath shifts() {
        if (_shifts == null)
            _shifts = new ShiftsPath(this, null, Keys.SHIFTS__SHIFTS_STARTED_BY_USER_ID_FKEY.getInverseKey());

        return _shifts;
    }

    private transient UserpermissionsPath _userpermissions;

    /**
     * Get the implicit to-many join path to the
     * <code>public.userpermissions</code> table
     */
    public UserpermissionsPath userpermissions() {
        if (_userpermissions == null)
            _userpermissions = new UserpermissionsPath(this, null, Keys.USERPERMISSIONS__USERPERMISSIONS_USER_ID_FKEY.getInverseKey());

        return _userpermissions;
    }

    @Override
    public List<Check<UsersRecord>> getChecks() {
        return Arrays.asList(
            Internal.createCheck(this, DSL.name("users_role_check"), "(((role)::text = ANY ((ARRAY['Admin'::character varying, 'Cashier'::character varying])::text[])))", true)
        );
    }

    @Override
    public Users as(String alias) {
        return new Users(DSL.name(alias), this);
    }

    @Override
    public Users as(Name alias) {
        return new Users(alias, this);
    }

    @Override
    public Users as(Table<?> alias) {
        return new Users(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(String name) {
        return new Users(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(Name name) {
        return new Users(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(Table<?> name) {
        return new Users(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Condition condition) {
        return new Users(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Users where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Users whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
