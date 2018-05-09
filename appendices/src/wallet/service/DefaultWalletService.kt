package org.monium.android.wallet.service

import mu.KLogging
import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageTransactionManager
import org.monium.android.transaction.storage.TransactionStorage
import org.monium.android.wallet.Wallet
import org.monium.android.wallet.storage.WalletStorage

/**
 * @author Mikhail Snitavets
 */
open class DefaultWalletService(
        private val walletStorage: WalletStorage,
        private val transactionStorage: TransactionStorage,
        private val stm: StorageTransactionManager
) : WalletService {
    companion object Static : KLogging()

    override fun findById(id: Long): Wallet? = walletStorage.findById(id)

    override fun findAll(ordering: Ordering, showHidden: Boolean) =
            walletStorage.findAll(ordering, showHidden)

    override fun create(wallet: Wallet) = walletStorage.create(wallet)

    override fun update(wallet: Wallet) = walletStorage.update(wallet)

    override fun remove(id: Long): Int {
        logger.trace { "Removing wallet with id $id, deleting transactions" }
        return stm.inDbTransaction {
            val deletedCount = transactionStorage.batchDeleteByWalletId(id)
            logger.debug { "Deleted $deletedCount transactions while removing wallet with $id" }
            walletStorage.remove(id)
            deletedCount
        }
    }

}

