package org.monium.android.transaction.storage

import org.monium.android.common.DateRange
import org.monium.android.common.storage.StorageException
import org.monium.android.transaction.Transaction

/**
 * @author Mikhail Snitavets.
 */
interface TransactionStorage {

    /**
     * Finds a single entity by it's [id]. Entity is fetched eagerly (subentities are fetched as well).
     *
     * @return entity with specified [id] or `null` if that entity doesn't exist
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findById(id: Long): Transaction?

    /**
     * Finds all transactions within specified [range]. Transactions sorted by
     * [Transaction.occurredAt] descending.
     * Entities are fetched eagerly (subentities are fetched as well).
     *
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAll(range: DateRange = DateRange.lastYear()): List<Transaction>

    /**
     * Update all transactions where [Transaction.category] with [oldId] to a [newId]
     * @param oldId old category id of transactions
     * @param newId new category for transactions
     * @return count of updated transactions
     */
    fun batchUpdateCategoryId(oldId: Long?, newId: Long?): Int

    /**
     * Deletes all transactions which related to the wallet with specified [id]
     * @return count of deleted transactions
     */
    fun batchDeleteByWalletId(id: Long): Int

    /**
     * Creates [Transaction]. [Transaction.id] is ignored.
     *
     * @return created entity with filled auto-generated entries
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun create(transaction: Transaction): Transaction


    /**
     * Updates existing [Transaction] based on it's ID
     *
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun update(transaction: Transaction)

    /**
     * Removes [Transaction] with specified ID
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun remove(id: Long)

}
