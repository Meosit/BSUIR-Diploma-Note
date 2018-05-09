package org.monium.android.wallet.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.only
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.monium.android.SimpleInvokeStorageTransactionManager
import org.monium.android.transaction.storage.TransactionStorage
import org.monium.android.wallet.storage.WalletStorage

/**
 * @author Mikhail Snitavets
 */
class DefaultWalletServiceTest {

    @Test
    fun `remove test, should delete linked transactions`() {
        // Given
        val walletIdToDelete = 12L
        val walletStorageMock = mock<WalletStorage>()
        val transactionStorageMock = mock<TransactionStorage>()
        val defaultWalletService = DefaultWalletService(
                walletStorageMock,
                transactionStorageMock,
                SimpleInvokeStorageTransactionManager
        )

        // When
        defaultWalletService.remove(walletIdToDelete)

        // Then
        verify(walletStorageMock, only()).remove(walletIdToDelete)
        verify(transactionStorageMock, only()).batchDeleteByWalletId(walletIdToDelete)
    }

}