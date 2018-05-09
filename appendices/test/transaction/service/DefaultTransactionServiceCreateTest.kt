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
 * Tests for [DefaultTransactionService.create] method.
 *
 * @author Mikhail Snitavets
 */
class DefaultTransactionServiceCreateTest {

    @Test
    fun `create test for trusted expense, should charge from related wallet`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val transaction = Transaction(
                srcWallet = Wallet(balance = balance),
                srcSum = sum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage> {
            on { create(transaction) } doReturn transaction
        }
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.create(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, only()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance - sum) shouldEqualTo firstValue.balance
        }
    }

    @Test
    fun `create test for trusted income, should credit related wallet`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val transaction = Transaction(
                destWallet = Wallet(balance = balance),
                destSum = sum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage> {
            on { create(transaction) } doReturn transaction
        }
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.create(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, only()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance + sum) shouldEqualTo firstValue.balance
        }
    }

    @Test
    fun `create test for trusted remittance, should credit dest wallet and charge from source wallet`() {
        //Given
        val srcWalletId = 42L
        val destWalletId = 22L
        val srcSum = 7.0.toBigDecimal()
        val destSum = 10.5.toBigDecimal()
        val srcWalletBalance = 37.0.toBigDecimal()
        val destWalletBalance = 20.5.toBigDecimal()
        val transaction = Transaction(
                srcWallet = Wallet(id = srcWalletId, balance = srcWalletBalance),
                srcSum = srcSum,
                destWallet = Wallet(id = destWalletId, balance = destWalletBalance),
                destSum = destSum
        )
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage> {
            on { create(transaction) } doReturn transaction
        }
        val defaultTransactionService = DefaultTransactionService(
                transactionStorageMock,
                walletStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        //When
        defaultTransactionService.create(transaction, trustedSource = true)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, twice()).update(capture())
            verify(walletStorageMock, never()).findById(any())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)


            //src
            val capturedSrcWallet = allValues.find { it.id == srcWalletId }
                    ?: fail("Src wallet was not updated")
            (srcWalletBalance - srcSum) shouldEqualTo capturedSrcWallet.balance
            //dest
            val capturedDestWallet = allValues.find { it.id == destWalletId }
                    ?: fail("Dest wallet was not updated")
            (destWalletBalance + destSum) shouldEqualTo capturedDestWallet.balance
        }
    }

    @Test
    fun `create test for non-trusted income, should query wallet by id and credit it`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val walletId = 42L
        val wallet = Wallet(id = walletId, balance = balance)
        val transaction = Transaction(
                destWallet = Wallet(id = walletId),
                destSum = sum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(walletId) } doReturn wallet
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
        defaultTransactionService.create(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, once()).update(capture())
            verify(walletStorageMock, once()).findById(any())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(walletStorageMock, transactionStorageMock)

            (balance + sum) shouldEqualTo firstValue.balance
            walletId shouldEqualTo firstValue.id
        }
    }

    @Test
    fun `create test for non-trusted expense, should query wallet by id and charge from it`() {
        //Given
        val sum = 10.5.toBigDecimal()
        val balance = 20.5.toBigDecimal()
        val walletId = 42L
        val wallet = Wallet(id = walletId, balance = balance)
        val transaction = Transaction(
                srcWallet = Wallet(id = walletId),
                srcSum = sum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(walletId) } doReturn wallet
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
        defaultTransactionService.create(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, once()).findById(any())
            verify(walletStorageMock, once()).update(capture())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)

            (balance - sum) shouldEqualTo firstValue.balance
            walletId shouldEqualTo firstValue.id
        }
    }

    @Test
    fun `create test for non-trusted remittance, should find wallets by id and credit dest wallet and charge from source wallet`() {
        //Given
        val srcWalletId = 42L
        val destWalletId = 22L
        val srcSum = 7.0.toBigDecimal()
        val destSum = 10.5.toBigDecimal()
        val srcWalletBalance = 37.0.toBigDecimal()
        val destWalletBalance = 20.5.toBigDecimal()
        val srcWallet = Wallet(id = srcWalletId, balance = srcWalletBalance)
        val testDestWallet = Wallet(id = destWalletId, balance = destWalletBalance)
        val transaction = Transaction(
                srcWallet = Wallet(id = srcWalletId),
                srcSum = srcSum,
                destWallet = Wallet(id = destWalletId),
                destSum = destSum
        )
        val walletStorageMock = mock<WalletStorage> {
            on { findById(srcWalletId) } doReturn srcWallet
            on { findById(destWalletId) } doReturn testDestWallet
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
        defaultTransactionService.create(transaction, trustedSource = false)

        //Then
        argumentCaptor<Wallet>().apply {
            verify(walletStorageMock, twice()).findById(any())
            verify(walletStorageMock, twice()).update(capture())
            verify(transactionStorageMock, only()).create(transaction)
            verifyNoMoreInteractions(transactionStorageMock, walletStorageMock)


            //src
            val capturedSrcWallet = allValues.find { it.id == srcWalletId }
                    ?: fail("Src wallet was not updated")
            (srcWalletBalance - srcSum) shouldEqualTo capturedSrcWallet.balance
            //dest
            val capturedDestWallet = allValues.find { it.id == destWalletId }
                    ?: fail("Dest wallet was not updated")
            (destWalletBalance + destSum) shouldEqualTo capturedDestWallet.balance
        }
    }

}
