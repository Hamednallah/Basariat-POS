/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.daos;


import com.basariatpos.db.generated.tables.Applicationsettings;
import com.basariatpos.db.generated.tables.records.ApplicationsettingsRecord;

import java.util.List;
import java.util.Optional;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ApplicationsettingsDao extends DAOImpl<ApplicationsettingsRecord, com.basariatpos.db.generated.tables.pojos.Applicationsettings, String> {

    /**
     * Create a new ApplicationsettingsDao without any configuration
     */
    public ApplicationsettingsDao() {
        super(Applicationsettings.APPLICATIONSETTINGS, com.basariatpos.db.generated.tables.pojos.Applicationsettings.class);
    }

    /**
     * Create a new ApplicationsettingsDao with an attached configuration
     */
    public ApplicationsettingsDao(Configuration configuration) {
        super(Applicationsettings.APPLICATIONSETTINGS, com.basariatpos.db.generated.tables.pojos.Applicationsettings.class, configuration);
    }

    @Override
    public String getId(com.basariatpos.db.generated.tables.pojos.Applicationsettings object) {
        return object.getSettingKey();
    }

    /**
     * Fetch records that have <code>setting_key BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchRangeOfSettingKey(String lowerInclusive, String upperInclusive) {
        return fetchRange(Applicationsettings.APPLICATIONSETTINGS.SETTING_KEY, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>setting_key IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchBySettingKey(String... values) {
        return fetch(Applicationsettings.APPLICATIONSETTINGS.SETTING_KEY, values);
    }

    /**
     * Fetch a unique record that has <code>setting_key = value</code>
     */
    public com.basariatpos.db.generated.tables.pojos.Applicationsettings fetchOneBySettingKey(String value) {
        return fetchOne(Applicationsettings.APPLICATIONSETTINGS.SETTING_KEY, value);
    }

    /**
     * Fetch a unique record that has <code>setting_key = value</code>
     */
    public Optional<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchOptionalBySettingKey(String value) {
        return fetchOptional(Applicationsettings.APPLICATIONSETTINGS.SETTING_KEY, value);
    }

    /**
     * Fetch records that have <code>setting_value BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchRangeOfSettingValue(String lowerInclusive, String upperInclusive) {
        return fetchRange(Applicationsettings.APPLICATIONSETTINGS.SETTING_VALUE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>setting_value IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchBySettingValue(String... values) {
        return fetch(Applicationsettings.APPLICATIONSETTINGS.SETTING_VALUE, values);
    }

    /**
     * Fetch records that have <code>description BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchRangeOfDescription(String lowerInclusive, String upperInclusive) {
        return fetchRange(Applicationsettings.APPLICATIONSETTINGS.DESCRIPTION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>description IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Applicationsettings> fetchByDescription(String... values) {
        return fetch(Applicationsettings.APPLICATIONSETTINGS.DESCRIPTION, values);
    }
}
