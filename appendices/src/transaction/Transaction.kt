package org.monium.android.transaction

import khronos.Dates
import org.monium.android.category.Category
import org.monium.android.wallet.Wallet
import java.math.BigDecimal
import java.util.*

/**
 * Basic entity which represents any action with the money. There are three types of such actions:
 * * **Income** - means that amount of money comes from the *other world* into this budget system.
 * Such transactions have `null` [srcSum] and [srcWallet] fields since the system doesn't track
 * external wallets.
 * * **Expense** - means that some money left from one of the wallet out of the system. Fields
 * [destSum] and [destWallet] are `null` there.
 * * **Remittance** - represents a money transition between two wallets within the system. For such
 * transactions both [srcWallet] and [destWallet] should be set. For remittance transaction [category]
 * is always set to `null`.
 *
 * ***Note:** default values were set in order to have an empty constructor.
 * This is because sometimes we need an entity where only id is set (e.g. foreign keys)*
 * @author Mikhail Snitavets
 */
data class Transaction(
        /**
         * **Required**. Internal entity id
         */
        val id: Long = -1L,
        /**
         * **Required**. Defines the category of this category. If unset - treated as "No category" value.
         * Should be always `null` for remittances.
         */
        val category: Category? = null,
        /**
         * **Required**. Represents [Wallet] from which were taken [srcSum] for this transaction.
         *
         * Should be set only for **expense** and **remittance** transactions.
         * Always `null` for **income** transactions.
         */
        val srcWallet: Wallet? = null,
        /**
         * Amount of money which should be charged from [srcWallet].
         *
         * Should be set only for **expense** and **remittance** transactions.
         * Always `null` for **income** transactions.
         */
        val srcSum: BigDecimal? = null,
        /**
         * **Required**. Represents [Wallet] from which were taken [destSum] for this transaction.
         *
         * Should be set only for **income** and **remittance** transactions.
         * Always `null` for **expense** transactions.
         */
        val destWallet: Wallet? = null,
        /**
         * **Required**. [destWallet] should be credited for such amount of money.
         *
         * Should be set only for **income** and **remittance** transactions.
         * Always `null` for **expense** transactions.
         */
        val destSum: BigDecimal? = null,
        /**
         * **Required**. Date when this transaction occurred.
         */
        val occurredAt: Date = Dates.of(),
        /**
         * **Optional**. User comment about that transaction, *up to 300 chars.*
         */
        val note: String? = null
) {
    /**
     * Indicates that this transaction represents a remittance.
     */
    val isRemittance = srcWallet != null && destWallet != null

    /**
     * Indicates that this transaction represents an income.
     */
    val isIncome = srcWallet == null && destWallet != null
    /**
     * Indicates that this transaction represents an expense.
     */
    val isExpense = srcWallet != null && destWallet == null
    /**
     * Shows transaction action as [TransactionType]
     */
    val type: TransactionType

    init {
        type = when {
            isIncome -> TransactionType.INCOME
            isExpense -> TransactionType.EXPENSE
            isRemittance -> TransactionType.REMITTANCE
            else -> throw IllegalStateException("Cannot determine type for $this")
        }
        check(srcWallet?.id != destWallet?.id) { "Same source and dest wallets in the transaction are not allowed (Wallet ID's = '${srcWallet?.id}')" }
    }
}

/**
 * Represents type of the transaction in a single enum
 */
enum class TransactionType {
    INCOME, EXPENSE, REMITTANCE
}