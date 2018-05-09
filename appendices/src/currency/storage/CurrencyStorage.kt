package org.monium.android.currency.storage

import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageException
import org.monium.android.currency.Currency
import org.monium.android.monthlyincome.MonthlyIncome

/**
 * @author Mikhail Snitavets
 */
interface CurrencyStorage {

    /**
     * Finds a single entity by it's [id]
     *
     * @return entity with specified [id] or `null` if that entity doesn't exist
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findById(id: Long): Currency?

    /**
     * @param ordering defines specific [Ordering] for the entity list
     * by default it's [Ordering.AS_ADDED]
     * @return all currencies in specified order
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAll(ordering: Ordering = Ordering.AS_ADDED): List<Currency>

    /**
     * Retrieves all [Currency] entities which are used in
     * all user-defined [MonthlyIncome] records.
     *
     * @param ordering defines specific [Ordering] for the entity list,
     * by default it's [Ordering.AS_ADDED]
     * @return all preferred currencies with specified order.
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAllPreferred(ordering: Ordering = Ordering.AS_ADDED): List<Currency>
}