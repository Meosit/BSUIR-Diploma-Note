package org.monium.android.transaction.service

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.monium.android.SimpleInvokeStorageTransactionManager
import org.monium.android.shouldEqualTo
import org.monium.android.transaction.Transaction
import org.monium.android.transaction.storage.TransactionStorage
import org.monium.android.twice
import org.monium.android.wallet.Wallet
import org.monium.android.wallet.storage.WalletStorage
import kotlin.test.assertFailsWith
import kotlin.test.fail

/**
 * Tests for [DefaultTransactionService.update] method.
 *
 * @author Mikhail Snitavets.
 */
class DefaultTransactionServiceUpdateTest {

    private val walletStorageMock = mock<WalletStorage>()
    private val transactionStorageMock = mock<TransactionStorage>()
    private val defaultTransactionService = DefaultTransactionService(
            transactionStorageMock,
            walletStorageMock,
            SimpleInvokeStorageTransactionManager
    )

    @Test
    fun `update test, should restrict to make income or expense transactions from remittances and vice versa`() {
        //Given
        val expenseTransaction = Transaction(
                srcWallet = Wallet(id = 1L)
        )
        val incomeTransaction = Transaction(
                destWallet = Wallet(id = 2L)
        )
        val remittanceTransaction = Transaction(
                srcWallet = Wallet(id = 3L),
                destWallet = Wallet(id = 4L)
        )

        //When - Then
        assertFailsWith(IllegalArgumentException::class) {
            defaultTransactionService.update(remittanceTransaction, incomeTransaction)
        }
        assertFailsWith(IllegalArgumentException::class) {
            defaultTransactionService.update(remittanceTransaction, expenseTransaction)
        }
        assertFailsWith(IllegalArgumentException::class) {
            defaultTransactionService.update(incomeTransaction, remittanceTransaction)
        }
        assertFailsWith(IllegalArgumentException::class) {
            defaultTransactionService.update(expenseTransaction, remittanceTransaction)
        }
    }

    @Test
    fun `update test for income with sum change and same wallet transition (will become an expense, but same wallet)`() {
        //Given
        val balance = 100.0.toBigDecimal()
        val wallet = Wallet(id = 42L, balance = balance)
        val oldSum = 50.0.toBigDecimal()
        val newSum = 20.0.toBigDecimal()

        val oldTransaction = Transaction(
                destWallet = wallet,
                destSum = oldSum
        )
        val newTransaction = Transaction(
                srcWallet = wallet,
                srcSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, only()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance - oldSum - newSum) shouldEqualTo value.balance
        }
    }

    @Test
    fun `update test for expense with same wallet transition and sum change (will become an income, but same wallet)`() {
        //Given
        val balance = 100.0.toBigDecimal()
        val wallet = Wallet(id = 42L, balance = balance)
        val oldSum = 50.0.toBigDecimal()
        val newSum = 25.0.toBigDecimal()

        val oldTransaction = Transaction(
                srcWallet = wallet,
                srcSum = oldSum
        )
        val newTransaction = Transaction(
                destWallet = wallet,
                destSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, only()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance + oldSum + newSum) shouldEqualTo value.balance
        }
    }

    @Test
    fun `update test for income, just sum change`() {
        //Given
        val balance = 100.0.toBigDecimal()
        val wallet = Wallet(id = 42L, balance = balance)
        val oldSum = 50.0.toBigDecimal()
        val newSum = 25.0.toBigDecimal()

        val oldTransaction = Transaction(
                destWallet = wallet,
                destSum = oldSum
        )
        val newTransaction = Transaction(
                destWallet = wallet,
                destSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, only()).update(capture())

            (balance - oldSum + newSum) shouldEqualTo value.balance
        }
    }

    @Test
    fun `update test for expense, just sum change`() {
        //Given
        val balance = 100.0.toBigDecimal()
        val wallet = Wallet(id = 42L, balance = balance)
        val oldSum = 50.0.toBigDecimal()
        val newSum = 25.0.toBigDecimal()

        val oldTransaction = Transaction(
                srcWallet = wallet,
                srcSum = oldSum
        )
        val newTransaction = Transaction(
                srcWallet = wallet,
                srcSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, only()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance + oldSum - newSum) shouldEqualTo value.balance
        }
    }

    @Test
    fun `update test for income, different wallets`() {
        //Given
        val oldWalletId = 42L
        val oldWalletBalance = 100.0.toBigDecimal()
        val oldWallet = Wallet(id = oldWalletId, balance = oldWalletBalance)
        val oldSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                destWallet = oldWallet,
                destSum = oldSum
        )

        val newWalletId = 12L
        val newWalletBalance = 100.0.toBigDecimal()
        val newWallet = Wallet(id = newWalletId, balance = newWalletBalance)
        val newSum = 25.0.toBigDecimal()
        val newTransaction = Transaction(
                destWallet = newWallet,
                destSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, twice()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedOldWallet = allValues.find { it.id == oldWalletId }
                    ?: fail("Old wallet was not updated")
            (oldWalletBalance - oldSum) shouldEqualTo capturedOldWallet.balance

            val capturedNewWallet = allValues.find { it.id == newWalletId }
                    ?: fail("Dest wallet was not updated")
            (newWalletBalance + newSum) shouldEqualTo capturedNewWallet.balance

        }
    }

    @Test
    fun `update test for expense, different wallets`() {
        //Given
        val oldWalletId = 42L
        val oldWalletBalance = 100.0.toBigDecimal()
        val oldWallet = Wallet(id = oldWalletId, balance = oldWalletBalance)
        val oldSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = oldWallet,
                srcSum = oldSum
        )

        val newWalletId = 12L
        val newWalletBalance = 100.0.toBigDecimal()
        val newWallet = Wallet(id = newWalletId, balance = newWalletBalance)
        val newSum = 25.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = newWallet,
                srcSum = newSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, twice()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedOldWallet = allValues.find { it.id == oldWalletId }
                    ?: fail("Old wallet was not updated")
            (oldWalletBalance + oldSum) shouldEqualTo capturedOldWallet.balance

            val capturedNewWallet = allValues.find { it.id == newWalletId }
                    ?: fail("Dest wallet was not updated")
            (newWalletBalance - newSum) shouldEqualTo capturedNewWallet.balance

        }
    }

    @Test
    fun `update test for remittance, just sum change`() {
        //Given
        val srcWalletId = 42L
        val srcBalance = 100.0.toBigDecimal()
        val srcWallet = Wallet(id = srcWalletId, balance = srcBalance)
        val destWalletId = 12L
        val destBalance = 200.0.toBigDecimal()
        val destWallet = Wallet(id = destWalletId, balance = destBalance)
        val oldSrcSum = 50.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = srcWallet,
                srcSum = oldSrcSum,
                destWallet = destWallet,
                destSum = oldDestSum
        )

        val newSrcSum = 100.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = srcWallet,
                srcSum = newSrcSum,
                destWallet = destWallet,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, twice()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedSrcWallet = allValues.find { it.id == srcWalletId }
                    ?: fail("Old wallet was not updated")
            (srcBalance + oldSrcSum - newSrcSum) shouldEqualTo capturedSrcWallet.balance

            val capturedDestWallet = allValues.find { it.id == destWalletId }
                    ?: fail("Dest wallet was not updated")
            (destBalance - oldDestSum + newDestSum) shouldEqualTo capturedDestWallet.balance

        }
    }


    @Test
    fun `update test for remittance, all different wallets`() {
        //Given
        val oldSrcWalletId = 42L
        val oldSrcBalance = 100.0.toBigDecimal()
        val oldSrcWallet = Wallet(id = oldSrcWalletId, balance = oldSrcBalance)
        val oldDestWalletId = 12L
        val oldDestBalance = 200.0.toBigDecimal()
        val oldDestWallet = Wallet(id = oldDestWalletId, balance = oldDestBalance)
        val oldSrcSum = 50.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = oldSrcWallet,
                srcSum = oldSrcSum,
                destWallet = oldDestWallet,
                destSum = oldDestSum
        )

        val newSrcWalletId = 55L
        val newSrcBalance = 500.0.toBigDecimal()
        val newSrcWallet = Wallet(id = newSrcWalletId, balance = newSrcBalance)
        val newDestWalletId = 155L
        val newDestBalance = 1000.0.toBigDecimal()
        val newDestWallet = Wallet(id = newDestWalletId, balance = newDestBalance)
        val newSrcSum = 100.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = newSrcWallet,
                srcSum = newSrcSum,
                destWallet = newDestWallet,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, times(4)).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedOldSrcWallet = allValues.find { it.id == oldSrcWalletId }
                    ?: fail("Old src wallet was not updated")
            (oldSrcBalance + oldSrcSum) shouldEqualTo capturedOldSrcWallet.balance

            val capturedOldDestWallet = allValues.find { it.id == oldDestWalletId }
                    ?: fail("Dest dest wallet was not updated")
            (oldDestBalance - oldDestSum) shouldEqualTo capturedOldDestWallet.balance

            val capturedNewSrcWallet = allValues.find { it.id == newSrcWalletId }
                    ?: fail("New src wallet was not updated")
            (newSrcBalance - newSrcSum) shouldEqualTo capturedNewSrcWallet.balance

            val capturedNewDestWallet = allValues.find { it.id == newDestWalletId }
                    ?: fail("New dest wallet was not updated")
            (newDestBalance + newDestSum) shouldEqualTo capturedNewDestWallet.balance
        }
    }

    @Test
    fun `update test for remittance, wallet switch`() {
        //Given
        val walletOneId = 42L
        val walletOneBalance = 100.0.toBigDecimal()
        val walletOne = Wallet(id = walletOneId, balance = walletOneBalance)

        val walletTwoId = 12L
        val walletTwoBalance = 200.0.toBigDecimal()
        val walletTwo = Wallet(id = walletTwoId, balance = walletTwoBalance)

        val oldSrcSum = 25.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = oldSrcSum,
                destWallet = walletTwo,
                destSum = oldDestSum
        )
        val newSrcSum = 10.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = walletTwo,
                srcSum = newSrcSum,
                destWallet = walletOne,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, twice()).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedWalletOne = allValues.find { it.id == walletOneId }
                    ?: fail("Wallet one was not updated")
            (walletOneBalance + oldSrcSum + newDestSum) shouldEqualTo capturedWalletOne.balance

            val capturedWalletTwo = allValues.find { it.id == walletTwoId }
                    ?: fail("Wallet two was not updated")
            (walletTwoBalance - oldDestSum - newSrcSum) shouldEqualTo capturedWalletTwo.balance
        }
    }


    @Test
    fun `update test for remittance, source sum change and another new dest`() {
        //Given
        val walletOneId = 42L
        val walletOneBalance = 100.0.toBigDecimal()
        val walletOne = Wallet(id = walletOneId, balance = walletOneBalance)

        val walletTwoId = 12L
        val walletTwoBalance = 150.0.toBigDecimal()
        val walletTwo = Wallet(id = walletTwoId, balance = walletTwoBalance)

        val walletThreeId = 22L
        val walletThreeBalance = 200.0.toBigDecimal()
        val walletThree = Wallet(id = walletThreeId, balance = walletThreeBalance)

        val oldSrcSum = 25.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = oldSrcSum,
                destWallet = walletTwo,
                destSum = oldDestSum
        )


        val newSrcSum = 10.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = newSrcSum,
                destWallet = walletThree,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, times(3)).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedWalletOne = allValues.find { it.id == walletOneId }
                    ?: fail("Wallet one was not updated")
            (walletOneBalance + oldSrcSum - newSrcSum) shouldEqualTo capturedWalletOne.balance

            val capturedWalletTwo = allValues.find { it.id == walletTwoId }
                    ?: fail("Wallet two was not updated")
            (walletTwoBalance - oldDestSum) shouldEqualTo capturedWalletTwo.balance

            val capturedWalletThree = allValues.find { it.id == walletThreeId }
                    ?: fail("Wallet three was not updated")
            (walletThreeBalance + newDestSum) shouldEqualTo capturedWalletThree.balance
        }
    }

    @Test
    fun `update test for remittance, dest sum change and another new source`() {
        //Given
        val walletOneId = 42L
        val walletOneBalance = 100.0.toBigDecimal()
        val walletOne = Wallet(id = walletOneId, balance = walletOneBalance)

        val walletTwoId = 12L
        val walletTwoBalance = 150.0.toBigDecimal()
        val walletTwo = Wallet(id = walletTwoId, balance = walletTwoBalance)

        val walletThreeId = 22L
        val walletThreeBalance = 200.0.toBigDecimal()
        val walletThree = Wallet(id = walletThreeId, balance = walletThreeBalance)

        val oldSrcSum = 25.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = oldSrcSum,
                destWallet = walletTwo,
                destSum = oldDestSum
        )


        val newSrcSum = 10.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = walletThree,
                srcSum = newSrcSum,
                destWallet = walletTwo,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, times(3)).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedWalletOne = allValues.find { it.id == walletOneId }
                    ?: fail("Wallet one was not updated")
            (walletOneBalance + oldSrcSum) shouldEqualTo capturedWalletOne.balance

            val capturedWalletTwo = allValues.find { it.id == walletTwoId }
                    ?: fail("Wallet two was not updated")
            (walletTwoBalance - oldDestSum + newDestSum) shouldEqualTo capturedWalletTwo.balance

            val capturedWalletThree = allValues.find { it.id == walletThreeId }
                    ?: fail("Wallet three was not updated")
            (walletThreeBalance - newSrcSum) shouldEqualTo capturedWalletThree.balance
        }
    }

    @Test
    fun `update test for remittance, source to dest transit + another new source`() {
        //Given
        val walletOneId = 42L
        val walletOneBalance = 100.0.toBigDecimal()
        val walletOne = Wallet(id = walletOneId, balance = walletOneBalance)

        val walletTwoId = 12L
        val walletTwoBalance = 150.0.toBigDecimal()
        val walletTwo = Wallet(id = walletTwoId, balance = walletTwoBalance)

        val walletThreeId = 22L
        val walletThreeBalance = 200.0.toBigDecimal()
        val walletThree = Wallet(id = walletThreeId, balance = walletThreeBalance)

        val oldSrcSum = 25.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = oldSrcSum,
                destWallet = walletTwo,
                destSum = oldDestSum
        )


        val newSrcSum = 10.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = walletThree,
                srcSum = newSrcSum,
                destWallet = walletOne,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, times(3)).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedWalletOne = allValues.find { it.id == walletOneId }
                    ?: fail("Wallet one was not updated")
            (walletOneBalance + oldSrcSum + newDestSum) shouldEqualTo capturedWalletOne.balance

            val capturedWalletTwo = allValues.find { it.id == walletTwoId }
                    ?: fail("Wallet two was not updated")
            (walletTwoBalance - oldDestSum) shouldEqualTo capturedWalletTwo.balance

            val capturedWalletThree = allValues.find { it.id == walletThreeId }
                    ?: fail("Wallet three was not updated")
            (walletThreeBalance - newSrcSum) shouldEqualTo capturedWalletThree.balance
        }
    }

    @Test
    fun `update test for remittance, dest to source transit + another new dest`() {
        //Given
        val walletOneId = 42L
        val walletOneBalance = 100.0.toBigDecimal()
        val walletOne = Wallet(id = walletOneId, balance = walletOneBalance)

        val walletTwoId = 12L
        val walletTwoBalance = 150.0.toBigDecimal()
        val walletTwo = Wallet(id = walletTwoId, balance = walletTwoBalance)

        val walletThreeId = 22L
        val walletThreeBalance = 200.0.toBigDecimal()
        val walletThree = Wallet(id = walletThreeId, balance = walletThreeBalance)

        val oldSrcSum = 25.0.toBigDecimal()
        val oldDestSum = 50.0.toBigDecimal()
        val oldTransaction = Transaction(
                srcWallet = walletOne,
                srcSum = oldSrcSum,
                destWallet = walletTwo,
                destSum = oldDestSum
        )


        val newSrcSum = 10.0.toBigDecimal()
        val newDestSum = 100.0.toBigDecimal()
        val newTransaction = Transaction(
                srcWallet = walletTwo,
                srcSum = newSrcSum,
                destWallet = walletThree,
                destSum = newDestSum
        )

        //When
        defaultTransactionService.update(oldTransaction, newTransaction)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(transactionStorageMock, only()).update(newTransaction)
            verify(walletStorageMock, times(3)).update(capture())
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            val capturedWalletOne = allValues.find { it.id == walletOneId }
                    ?: fail("Wallet one was not updated")
            (walletOneBalance + oldSrcSum) shouldEqualTo capturedWalletOne.balance

            val capturedWalletTwo = allValues.find { it.id == walletTwoId }
                    ?: fail("Wallet two was not updated")
            (walletTwoBalance - oldDestSum - newSrcSum) shouldEqualTo capturedWalletTwo.balance

            val capturedWalletThree = allValues.find { it.id == walletThreeId }
                    ?: fail("Wallet three was not updated")
            (walletThreeBalance + newDestSum) shouldEqualTo capturedWalletThree.balance
        }
    }


}