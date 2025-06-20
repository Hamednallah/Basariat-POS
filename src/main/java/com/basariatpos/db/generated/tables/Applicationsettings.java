/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables;


import com.basariatpos.db.generated.Keys;
import com.basariatpos.db.generated.Public;
import com.basariatpos.db.generated.tables.records.ApplicationsettingsRecord;

import java.util.Collection;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Applicationsettings extends TableImpl<ApplicationsettingsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.applicationsettings</code>
     */
    public static final Applicationsettings APPLICATIONSETTINGS = new Applicationsettings();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ApplicationsettingsRecord> getRecordType() {
        return ApplicationsettingsRecord.class;
    }

    /**
     * The column <code>public.applicationsettings.setting_key</code>.
     */
    public final TableField<ApplicationsettingsRecord, String> SETTING_KEY = createField(DSL.name("setting_key"), SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>public.applicationsettings.setting_value</code>.
     */
    public final TableField<ApplicationsettingsRecord, String> SETTING_VALUE = createField(DSL.name("setting_value"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.applicationsettings.description</code>.
     */
    public final TableField<ApplicationsettingsRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "");

    private Applicationsettings(Name alias, Table<ApplicationsettingsRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Applicationsettings(Name alias, Table<ApplicationsettingsRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>public.applicationsettings</code> table reference
     */
    public Applicationsettings(String alias) {
        this(DSL.name(alias), APPLICATIONSETTINGS);
    }

    /**
     * Create an aliased <code>public.applicationsettings</code> table reference
     */
    public Applicationsettings(Name alias) {
        this(alias, APPLICATIONSETTINGS);
    }

    /**
     * Create a <code>public.applicationsettings</code> table reference
     */
    public Applicationsettings() {
        this(DSL.name("applicationsettings"), null);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<ApplicationsettingsRecord> getPrimaryKey() {
        return Keys.APPLICATIONSETTINGS_PKEY;
    }

    @Override
    public Applicationsettings as(String alias) {
        return new Applicationsettings(DSL.name(alias), this);
    }

    @Override
    public Applicationsettings as(Name alias) {
        return new Applicationsettings(alias, this);
    }

    @Override
    public Applicationsettings as(Table<?> alias) {
        return new Applicationsettings(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Applicationsettings rename(String name) {
        return new Applicationsettings(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Applicationsettings rename(Name name) {
        return new Applicationsettings(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Applicationsettings rename(Table<?> name) {
        return new Applicationsettings(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings where(Condition condition) {
        return new Applicationsettings(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Applicationsettings where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Applicationsettings where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Applicationsettings where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @PlainSQL
    public Applicationsettings where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    public Applicationsettings whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}
