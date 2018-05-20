package org.monium.android.common.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.RoomDatabase
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import org.monium.android.currency.Currency
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 *
 * @author Mikhail Snitavets.
 */
class AppDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        populateWithCurrencies(db)
    }

    private fun populateWithCurrencies(db: SupportSQLiteDatabase) {
        fun Currency.insert() {
            val values = ContentValues()
            values.put("id", this.id)
            values.put("code", this.code)
            values.put("symbol", this.id)
            values.put("is_prefix", this.isPrefix)
            db.insert("currency", SQLiteDatabase.CONFLICT_ABORT, values)
        }

        val DEFAULT_MATH_CONTEXT = MathContext(4, RoundingMode.FLOOR)
        fun insertSampleWallet(id: Long, name: String, balance: BigDecimal, currency: Long) {
            val values = ContentValues()
            values.put("id", id)
            values.put("name", name)
            values.put("balance", balance.round(DEFAULT_MATH_CONTEXT).toPlainString())
            values.put("currency_id", currency)
            values.put("icon_code", "")
            values.put("is_reserved", false)
            values.put("is_hidden", false)
            values.put("ordering", id)
            db.insert("wallet", SQLiteDatabase.CONFLICT_ABORT, values)
        }



        Currency(1, "USD", "$1234.1", true).insert()
        Currency(2, "EUR", "€100.23", true).insert()
        Currency(3, "BYN", "Br", false).insert()
        Currency(4, "RUB", "\u20BD", true).insert()
        Currency(5, "PLN", "zł", false).insert()

        insertSampleWallet(1, "USD Wallet", 12.12.toBigDecimal(), 1)
        insertSampleWallet(2, "EUR Wallet", 34.100.toBigDecimal(), 1)
        insertSampleWallet(3, "BYN Wallet", 56.1234.toBigDecimal(), 1)
        insertSampleWallet(4, "RUB Wallet", 78.99.toBigDecimal(), 1)
        insertSampleWallet(5, "PLN Wallet", 90.22.toBigDecimal(), 1)

    }

    override fun onOpen(db: SupportSQLiteDatabase) {

    }

}