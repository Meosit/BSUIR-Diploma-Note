package org.monium.android.transaction.service

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.monium.android.*
import org.monium.android.transaction.Transaction
import org.monium.android.transaction.storage.TransactionStorage
import org.monium.android.wallet.Wallet
import org.monium.android.wallet.storage.WalletStorage
import kotlin.test.fail

/**
 * @author Mikhail Snitavets
 */
class DefaultTransactionServiceRemoveTest {

    @Test
    fun `remove test for trusted income, should charge from related wallet`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val transactionId = 42L
        val transaction = Transaction(
                id = transactionId,
                destWallet = Wallet(balance = balance),
                destSum = sum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, only()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance - sum) shouldEqualTo firstValue.balance
        }
    }

    @Test
    fun `remove test for trusted expense, should credit related wallet`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val transactionId = 42L
        val transaction = Transaction(
                id = transactionId,
                srcWallet = Wallet(balance = balance),
                srcSum = sum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, only()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance + sum) shouldEqualTo firstValue.balance
        }
    }

    @Test
    fun `remove test for trusted remittance, should charge from dest wallet and credit source wallet`() {
        //Given
        val srcSum = 7.0.toBigDecimal()
        val destSum = 10.5.toBigDecimal()
        val srcWalletBalance = 37.0.toBigDecimal()
        val destWalletBalance = 20.5.toBigDecimal()
        val transactionId = 42L
        val srcWalletId = 42L
        val destWalletId = 21L
        val transaction = Transaction(
                id = transactionId,
                srcWallet = Wallet(id = srcWalletId, balance = srcWalletBalance),
                srcSum = srcSum,
                destWallet = Wallet(id = destWalletId, balance = destWalletBalance),
                destSum = destSum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, twice()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            //src
            val capturedSrcWallet = allValues.find { it.id == srcWalletId }
                    ?: fail("Src wallet was not updated")
            (srcWalletBalance + srcSum) shouldEqualTo capturedSrcWallet.balance
            //dest
            val capturedDestWallet = allValues.find { it.id == destWalletId }
                    ?: fail("Dest wallet was not updated")
            (destWalletBalance - destSum) shouldEqualTo capturedDestWallet.balance
        }
    }

    @Test
    fun `remove test for non-trusted income, should query wallet by id and charge from it`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val walletId = 42L
        val wallet = Wallet(id = walletId, balance = balance)
        val transactionId = 42L
        val transaction = Transaction(
                id = transactionId,
                destWallet = Wallet(id = walletId),
                destSum = sum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(walletId) } doReturn wallet
        }
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, once()).update(capture())
            verify(walletStorageMock, once()).findById(any())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(walletStorageMock, transactionStorageMock)

            (balance - sum) shouldEqualTo firstValue.balance
            walletId shouldEqualTo firstValue.id
        }
    }

    @Test
    fun `remove test for non-trusted expense, should query wallet by id and credit it`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val walletId = 42L
        val wallet = Wallet(id = walletId, balance = balance)
        val transactionId = 42L
        val transaction = Transaction(
                id = transactionId,
                srcWallet = Wallet(id = walletId),
                srcSum = sum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(walletId) } doReturn wallet
        }
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, once()).findById(any())
            verify(walletStorageMock, once()).update(capture())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance + sum) shouldEqualTo firstValue.balance
            walletId shouldEqualTo firstValue.id
        }
    }

    @Test
    fun `remove test for non-trusted remittance, should find wallets by id and charge from dest wallet and credit source wallet`() {
        //Given
        val srcWalletId = 42L
        val destWalletId = 22L
        val srcSum = 7.0.toBigDecimal()
        val destSum = 10.5.toBigDecimal()
        val srcWalletBalance = 37.0.toBigDecimal()
        val destWalletBalance = 20.5.toBigDecimal()
        val srcWallet = Wallet(id = srcWalletId, balance = srcWalletBalance)
        val destWallet = Wallet(id = destWalletId, balance = destWalletBalance)
        val transactionId = 42L
        val transaction = Transaction(
                id = transactionId,
                srcWallet = Wallet(id = srcWalletId),
                srcSum = srcSum,
                destWallet = Wallet(id = destWalletId),
                destSum = destSum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(srcWalletId) } doReturn srcWallet
            on { findById(destWalletId) } doReturn destWallet
        }
        val transactionStorageMock = mock<TransactionStorage> {
            on { create(transaction) } doReturn transaction
        }
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.remove(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, twice()).findById(any())
            verify(walletStorageMock, twice()).update(capture())
            verify(transactionStorageMock, only()).remove(transactionId)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            //src
            val capturedSrcWallet = allValues.find { it.id == srcWalletId }
                    ?: fail("Src wallet was not updated")
            (srcWalletBalance + srcSum) shouldEqualTo capturedSrcWallet.balance
            //dest
            val capturedDestWallet = allValues.find { it.id == destWalletId }
                    ?: fail("Dest wallet was not updated")
            (destWalletBalance - destSum) shouldEqualTo capturedDestWallet.balance
        }
    }

}