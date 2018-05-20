package org.monium.android.common.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.monium.android.category.storage.RoomCategory
import org.monium.android.category.storage.RoomCategoryDao
import org.monium.android.currency.storage.RoomCurrency
import org.monium.android.currency.storage.RoomCurrencyDao
import org.monium.android.monthlyincome.storage.RoomMonthlyIncome
import org.monium.android.monthlyincome.storage.RoomMonthlyIncomeDao
import org.monium.android.transaction.storage.RoomTransaction
import org.monium.android.transaction.storage.RoomTransactionDao
import org.monium.android.wallet.storage.RoomWallet
import org.monium.android.wallet.storage.RoomWalletDao

/**
 *
 * @author Mikhail Snitavets.
 */
@Database(
        entities = [
            RoomCurrency::class,
            RoomCategory::class,
            RoomWallet::class,
            RoomTransaction::class,
            RoomMonthlyIncome::class
        ],
        version = 1,
        exportSchema = true
)
@TypeConverters(CommonTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun currencyDao(): RoomCurrencyDao

    abstract fun categoryDao(): RoomCategoryDao

    abstract fun walletDao(): RoomWalletDao

    abstract fun transactionDao(): RoomTransactionDao

    abstract fun monthlyIncomeDao(): RoomMonthlyIncomeDao

}