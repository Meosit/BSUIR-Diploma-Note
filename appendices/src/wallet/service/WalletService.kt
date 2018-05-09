package org.monium.android.wallet.service

import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageException
import org.monium.android.transaction.Transaction
import org.monium.android.wallet.Wallet

/**
 * @author Mikhail Snitavets
 */
interface WalletService {

    /**
     * Finds a single entity by it's [id]. Entity is fetched eagerly (subentities are fetched as well).
     *
     * @return entity with specified [id] or `null` if that entity doesn't exist
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findById(id: Long): Wallet?

    /**
     * Finds all wallets with using specified filters.
     * Entities are fetched eagerly (subentities are fetched as well).
     *
     * @param ordering defines specific [Ordering] for the entity list,
     * by default it's [Ordering.AS_ADDED]
     * @param showHidden if `true`, fetches also [Wallet.isHidden] marked entities
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAll(ordering: Ordering = Ordering.AS_ADDED,
                showHidden: Boolean = false): List<Wallet>

    /**
     * Creates [Wallet]. [Wallet.id] is ignored.
     *
     * **Note:** for nested entities ([Wallet.currency]) only id value taken into account.
     * @return created entity with filled auto-generated entries
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun create(wallet: Wallet): Wallet

    /**
     * Updates existing [Wallet] based on it's ID
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun update(wallet: Wallet)

    /**
     * Removes [Wallet] with specified ID.
     * Also deletes all [Transaction]s related to this wallet.
     *
     * **Note:** deleting without will not affect balances of other wallets, but remittance will be deleted as well
     * @param id of wallet to remove
     * @return count of transactions that was deleted
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun remove(id: Long): Int

}