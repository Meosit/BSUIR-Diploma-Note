package org.monium.android.transaction.service

import mu.KLogging
import org.monium.android.common.DateRange
import org.monium.android.common.nullableMinus
import org.monium.android.common.nullablePlus
import org.monium.android.common.storage.StorageTransactionManager
import org.monium.android.transaction.Transaction
import org.monium.android.transaction.storage.TransactionStorage
import org.monium.android.wallet.Wallet
import org.monium.android.wallet.storage.WalletStorage
import java.math.BigDecimal

/**
 * @author Mikhail Snitavets.
 */
open class DefaultTransactionService(
        private val transactionStorage: TransactionStorage,
        private val walletStorage: WalletStorage,
        private val stm: StorageTransactionManager
) : TransactionService {

    companion object Static : KLogging()

    override fun findById(id: Long) = transactionStorage.findById(id)

    override fun findAll(range: DateRange) = transactionStorage.findAll(range)

    override fun create(transaction: Transaction, trustedSource: Boolean): Transaction {
        //straight logic (srcWallet charged, destWallet credited)
        val updatedSrcWallet: Wallet? = prepareWalletForTransactionChange(transaction.srcWallet, transaction.srcSum, trustedSource, true)
        val updatedDestWallet: Wallet? = prepareWalletForTransactionChange(transaction.destWallet, transaction.destSum, trustedSource, false)
        return stm.inDbTransaction {
            updatedSrcWallet?.let { walletStorage.updateBalance(it) }
            updatedDestWallet?.let { walletStorage.updateBalance(it) }
            transactionStorage.create(transaction)
        }
    }

    override fun update(oldTransaction: Transaction, newTransaction: Transaction, trustedSource: Boolean) {
        if (oldTransaction.isRemittance && !newTransaction.isRemittance
                || !oldTransaction.isRemittance && newTransaction.isRemittance) {
            logger.warn { "Erroneous data passed, cannot change remittance type: \nOLD: $oldTransaction\nNEW: $newTransaction" }
            throw IllegalArgumentException("Cannot make an income/expense transaction from a remittance and vice versa")
        }
        when {
        // just sum change (for all transaction types)
            oldTransaction.srcWallet?.id == newTransaction.srcWallet?.id
                    && oldTransaction.destWallet?.id == newTransaction.destWallet?.id -> {
                logger.debug { "Only sum changes" }

                val srcDelta = newTransaction.srcSum nullableMinus oldTransaction.srcSum
                val destDelta = newTransaction.destSum nullableMinus oldTransaction.destSum

                val updatedSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, srcDelta, trustedSource, true)
                val updatedDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, destDelta, trustedSource, false)
                stm.inDbTransaction {
                    updatedSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // wallet switch (or transit for income/expense)
            oldTransaction.srcWallet?.id == newTransaction.destWallet?.id
                    && oldTransaction.destWallet?.id == newTransaction.srcWallet?.id -> {
                logger.debug { "Wallets switch" }

                val srcToDestDelta = oldTransaction.srcSum nullablePlus newTransaction.destSum
                val destToSrcDelta = oldTransaction.destSum nullablePlus newTransaction.srcSum

                val updatedDestToSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, destToSrcDelta, trustedSource, true)
                val updatedSrcToDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, srcToDestDelta, trustedSource, false)
                stm.inDbTransaction {
                    updatedDestToSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedSrcToDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // source sum change + another new dest (or income wallet change)
            oldTransaction.srcWallet?.id == newTransaction.srcWallet?.id
                    && oldTransaction.destWallet?.id != newTransaction.destWallet?.id -> {
                logger.debug {
                    if (oldTransaction.isRemittance)
                        "Source sum change and new dest in the remittance"
                    else
                        "Income wallet change"
                }

                val srcDelta = newTransaction.srcSum nullableMinus oldTransaction.srcSum

                val updatedSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, srcDelta, trustedSource, true)
                val updatedOldDestWallet = prepareWalletForTransactionChange(oldTransaction.destWallet, oldTransaction.destSum, trustedSource, true)
                val updatedNewDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, newTransaction.destSum, trustedSource, false)
                stm.inDbTransaction {
                    updatedSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedOldDestWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // dest sum change + another new source (or expense wallet change)
            oldTransaction.srcWallet?.id != newTransaction.srcWallet?.id
                    && oldTransaction.destWallet?.id == newTransaction.destWallet?.id -> {
                logger.debug {
                    if (oldTransaction.isRemittance)
                        "Dest sum change and new source in the remittance"
                    else
                        "Expense wallet change"
                }

                val destDelta = newTransaction.destSum nullableMinus oldTransaction.destSum

                val updatedOldSrcWallet = prepareWalletForTransactionChange(oldTransaction.srcWallet, oldTransaction.srcSum, trustedSource, false)
                val updatedNewSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, newTransaction.srcSum, trustedSource, true)
                val updatedDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, destDelta, trustedSource, false)
                stm.inDbTransaction {
                    updatedOldSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // source -> dest + another new source
            oldTransaction.srcWallet?.id == newTransaction.destWallet?.id
                    && oldTransaction.destWallet?.id != newTransaction.srcWallet?.id -> {
                logger.debug { "New source, but dest if from old source in the remittance" }

                val srcToDestDelta = oldTransaction.srcSum nullablePlus newTransaction.destSum

                val updatedOldDestWallet = prepareWalletForTransactionChange(oldTransaction.destWallet, oldTransaction.destSum, trustedSource, true)
                val updatedSrcToDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, srcToDestDelta, trustedSource, false)
                val updatedNewSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, newTransaction.srcSum, trustedSource, true)
                stm.inDbTransaction {
                    updatedOldDestWallet?.let { walletStorage.updateBalance(it) }
                    updatedSrcToDestWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewSrcWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // dest -> source + another new dest
            oldTransaction.srcWallet?.id != newTransaction.destWallet?.id
                    && oldTransaction.destWallet?.id == newTransaction.srcWallet?.id -> {
                logger.debug { "New dest, but source if from old dest in the remittance" }

                val destToSrcDelta = oldTransaction.destSum nullablePlus newTransaction.srcSum

                val updatedOldSrcWallet = prepareWalletForTransactionChange(oldTransaction.srcWallet, oldTransaction.srcSum, trustedSource, false)
                val updatedDestToSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, destToSrcDelta, trustedSource, true)
                val updatedNewDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, newTransaction.destSum, trustedSource, false)
                stm.inDbTransaction {
                    updatedOldSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedDestToSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        // all different wallets (same for income/expense as well)
            else -> {
                logger.debug { "Completely new wallets" }

                val updatedOldSrcWallet = prepareWalletForTransactionChange(oldTransaction.srcWallet, oldTransaction.srcSum, trustedSource, false)
                val updatedOldDestWallet = prepareWalletForTransactionChange(oldTransaction.destWallet, oldTransaction.destSum, trustedSource, true)
                val updatedNewSrcWallet = prepareWalletForTransactionChange(newTransaction.srcWallet, newTransaction.srcSum, trustedSource, true)
                val updatedNewDestWallet = prepareWalletForTransactionChange(newTransaction.destWallet, newTransaction.destSum, trustedSource, false)
                stm.inDbTransaction {
                    updatedOldSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedOldDestWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewSrcWallet?.let { walletStorage.updateBalance(it) }
                    updatedNewDestWallet?.let { walletStorage.updateBalance(it) }
                    transactionStorage.update(newTransaction)
                }
            }
        }
    }

    override fun remove(transaction: Transaction, trustedSource: Boolean) {
        // inverted logic (srcWallet credited, destWallet charged)
        val updatedSrcWallet: Wallet? = prepareWalletForTransactionChange(transaction.srcWallet, transaction.srcSum, trustedSource, false)
        val updatedDestWallet: Wallet? = prepareWalletForTransactionChange(transaction.destWallet, transaction.destSum, trustedSource, true)
        stm.inDbTransaction {
            updatedSrcWallet?.let { walletStorage.updateBalance(it) }
            updatedDestWallet?.let { walletStorage.updateBalance(it) }
            transactionStorage.remove(transaction.id)
        }
    }

    /**
     * Updates appropriate [wallet]
     *
     * @param charge indicates that [sum] should be either charged or credited for the [wallet]
     * @param trustedSource if `false`, then wallet will be queried from the database using it's ID
     * @return updated [Wallet], which is ready to store or `null` in case the `null` parameters are passed
     */
    @Suppress("NAME_SHADOWING")
    private fun prepareWalletForTransactionChange(wallet: Wallet?, sum: BigDecimal?, trustedSource: Boolean, charge: Boolean): Wallet? {
        if (wallet == null) {
            logger.trace { "Null wallet passed, nothing to update" }
            return null
        }
        val sum = checkNotNull(sum) { "Both wallet and sum should be non-null" }
        if (sum == BigDecimal.ZERO) {
            logger.trace { "Zero delta passed, update is obsolete." }
            return null
        }
        val walletForChange: Wallet = if (trustedSource) {
            logger.trace { "Using existing entity: $wallet" }
            wallet
        } else {
            logger.trace { "Fetching Wallet from database id = ${wallet.id}" }
            checkNotNull(walletStorage.findById(wallet.id)) { "Wallet with id ${wallet.id} not found" }
        }
        val updatedSum = if (charge) walletForChange.balance - sum else walletForChange.balance + sum
        logger.trace { "New balance of Wallet[id=${walletForChange.id}, name=${walletForChange.name}, ...] is $updatedSum (was ${walletForChange.balance})" }
        return walletForChange.copy(balance = updatedSum)
    }

}