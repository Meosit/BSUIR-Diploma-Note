package org.monium.android.currency.service

import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageException
import org.monium.android.currency.Currency
import org.monium.android.wallet.Wallet

/**
 * @author Mikhail Snitavets
 */
interface CurrencyService {

    /**
     * @param ordering defines specific [Ordering] for the entity list
     * by default it's [Ordering.AS_ADDED]
     * @return all currencies in specified order
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAll(ordering: Ordering = Ordering.AS_ADDED): List<Currency>

    /**
     * Retrieves all [Currency] entities which are used in
     * all user-defined [Wallet] records.
     *
     * @param ordering defines specific [Ordering] for the entity list,
     * by default it's [Ordering.AS_ADDED]
     * @return all preferred currencies with specified order.
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAllPreferred(ordering: Ordering = Ordering.AS_ADDED): List<Currency>
}