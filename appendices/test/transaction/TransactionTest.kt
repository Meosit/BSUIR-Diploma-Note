package org.monium.android.transaction

import org.junit.Test
import org.monium.android.shouldEqualTo
import org.monium.android.wallet.Wallet
import kotlin.test.assertFailsWith

/**
 * @author Mikhail Snitavets
 */
class TransactionTest {

    @Test
    fun `transaction type test, should determine transaction type or throw an exception `() {
        //Given with When
        val remittanceTransaction = Transaction(
                srcWallet = Wallet(id = 12L),
                destWallet = Wallet(id = 42L)
        )
        val expenseTransaction = Transaction(
                srcWallet = Wallet(id = 42L)
        )
        val incomeTransaction = Transaction(
                destWallet = Wallet(id = 42L)
        )

        //Then
        remittanceTransaction.type shouldEqualTo TransactionType.REMITTANCE
        expenseTransaction.type shouldEqualTo TransactionType.EXPENSE
        incomeTransaction.type shouldEqualTo TransactionType.INCOME
        assertFailsWith(IllegalStateException::class) {
            Transaction(
                    srcWallet = null,
                    destWallet = null
            )
        }
    }

    @Test
    fun `same wallet not allowed test, should throw an exception`() {
        assertFailsWith(IllegalStateException::class) {
            val sameWalletId = 42L
            Transaction(
                    srcWallet = Wallet(id = sameWalletId),
                    destWallet = Wallet(id = sameWalletId)
            )
        }
    }


}