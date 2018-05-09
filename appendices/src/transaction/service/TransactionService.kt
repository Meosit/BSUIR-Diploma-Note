package org.monium.android.transaction.service

import org.monium.android.common.DateRange
import org.monium.android.common.storage.StorageException
import org.monium.android.transaction.Transaction
import org.monium.android.wallet.Wallet

/**
 * @author Mikhail Snitavets
 */
interface TransactionService {

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
     * Creates [Transaction]. [Transaction.id] is ignored.
     *
     * **Note:**
     * * For nested entities ([Transaction.destWallet], [Transaction.srcWallet], [Transaction.category]) only id value taken into account
     * * Any operations with the transactions also affects the related [Wallet.balance] values
     * @param trustedSource indicates that [transaction] that passed already contains all
     * filled nested entities and there is no need for additional querying of them.
     * @return created entity with filled auto-generated entries
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun create(transaction: Transaction, trustedSource: Boolean = true): Transaction

    /**
     * Updates existing [Transaction] based on two states of it, both transactions should have same ID.
     * [oldTransaction] required in order to preserve consistency of related [Wallet]s.
     * Making a remittance transaction from an income/expense and vice versa one is not allowed.
     *
     * **Note:**
     * * Any operations with the transactions also affects the related [Wallet.balance] values
     * @param oldTransaction previous *outdated* transaction entity.
     * @param newTransaction new *updated* transaction entity
     * @param trustedSource indicates that all passed transactions already contains all
     * filled nested entities and there is no need for additional querying of them.
     * @throws StorageException in case of any unexpected issues with the storage
     * @throws IllegalArgumentException if there is an attempt to make remittance transaction from income/expense one and vice versa
     */
    fun update(oldTransaction: Transaction, newTransaction: Transaction, trustedSource: Boolean = true)

    /**
     * Removes existing [Transaction] based on it's ID
     *
     * **Note:**
     * * Any operations with the transactions also affects the related [Wallet.balance] values
     * @param trustedSource indicates that [transaction] that passed already contains all
     * filled nested entities and there is no need for additional querying of them.
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun remove(transaction: Transaction, trustedSource: Boolean = true)
}