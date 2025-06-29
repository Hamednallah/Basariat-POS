/*
 * This file is generated by jOOQ.
 */
package com.basariatpos.db.generated.tables.daos;


import com.basariatpos.db.generated.tables.Purchaseorderitems;
import com.basariatpos.db.generated.tables.records.PurchaseorderitemsRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class PurchaseorderitemsDao extends DAOImpl<PurchaseorderitemsRecord, com.basariatpos.db.generated.tables.pojos.Purchaseorderitems, Integer> {

    /**
     * Create a new PurchaseorderitemsDao without any configuration
     */
    public PurchaseorderitemsDao() {
        super(Purchaseorderitems.PURCHASEORDERITEMS, com.basariatpos.db.generated.tables.pojos.Purchaseorderitems.class);
    }

    /**
     * Create a new PurchaseorderitemsDao with an attached configuration
     */
    public PurchaseorderitemsDao(Configuration configuration) {
        super(Purchaseorderitems.PURCHASEORDERITEMS, com.basariatpos.db.generated.tables.pojos.Purchaseorderitems.class, configuration);
    }

    @Override
    public Integer getId(com.basariatpos.db.generated.tables.pojos.Purchaseorderitems object) {
        return object.getPoItemId();
    }

    /**
     * Fetch records that have <code>po_item_id BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfPoItemId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.PO_ITEM_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>po_item_id IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByPoItemId(Integer... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.PO_ITEM_ID, values);
    }

    /**
     * Fetch a unique record that has <code>po_item_id = value</code>
     */
    public com.basariatpos.db.generated.tables.pojos.Purchaseorderitems fetchOneByPoItemId(Integer value) {
        return fetchOne(Purchaseorderitems.PURCHASEORDERITEMS.PO_ITEM_ID, value);
    }

    /**
     * Fetch a unique record that has <code>po_item_id = value</code>
     */
    public Optional<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchOptionalByPoItemId(Integer value) {
        return fetchOptional(Purchaseorderitems.PURCHASEORDERITEMS.PO_ITEM_ID, value);
    }

    /**
     * Fetch records that have <code>purchase_order_id BETWEEN lowerInclusive
     * AND upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfPurchaseOrderId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.PURCHASE_ORDER_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>purchase_order_id IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByPurchaseOrderId(Integer... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.PURCHASE_ORDER_ID, values);
    }

    /**
     * Fetch records that have <code>inventory_item_id BETWEEN lowerInclusive
     * AND upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfInventoryItemId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.INVENTORY_ITEM_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>inventory_item_id IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByInventoryItemId(Integer... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.INVENTORY_ITEM_ID, values);
    }

    /**
     * Fetch records that have <code>quantity_ordered BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfQuantityOrdered(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.QUANTITY_ORDERED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>quantity_ordered IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByQuantityOrdered(Integer... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.QUANTITY_ORDERED, values);
    }

    /**
     * Fetch records that have <code>quantity_received BETWEEN lowerInclusive
     * AND upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfQuantityReceived(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.QUANTITY_RECEIVED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>quantity_received IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByQuantityReceived(Integer... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.QUANTITY_RECEIVED, values);
    }

    /**
     * Fetch records that have <code>purchase_price_per_unit BETWEEN
     * lowerInclusive AND upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfPurchasePricePerUnit(BigDecimal lowerInclusive, BigDecimal upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.PURCHASE_PRICE_PER_UNIT, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>purchase_price_per_unit IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchByPurchasePricePerUnit(BigDecimal... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.PURCHASE_PRICE_PER_UNIT, values);
    }

    /**
     * Fetch records that have <code>subtotal BETWEEN lowerInclusive AND
     * upperInclusive</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchRangeOfSubtotal(BigDecimal lowerInclusive, BigDecimal upperInclusive) {
        return fetchRange(Purchaseorderitems.PURCHASEORDERITEMS.SUBTOTAL, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>subtotal IN (values)</code>
     */
    public List<com.basariatpos.db.generated.tables.pojos.Purchaseorderitems> fetchBySubtotal(BigDecimal... values) {
        return fetch(Purchaseorderitems.PURCHASEORDERITEMS.SUBTOTAL, values);
    }
}
