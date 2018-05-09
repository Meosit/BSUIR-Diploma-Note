package org.monium.android.wallet.storage

import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageException
import org.monium.android.wallet.Wallet

/**
 * @author Mikhail Snitavets
 */
interface WalletStorage {

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
     * Updates only [Wallet.balance] value based on it's ID
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun updateBalance(wallet: Wallet)

    /**
     * Removes [Wallet] with specified ID
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun remove(id: Long)
}