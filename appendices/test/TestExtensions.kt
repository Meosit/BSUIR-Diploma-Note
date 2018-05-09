package org.monium.android

import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.times
import org.mockito.verification.VerificationMode
import org.monium.android.common.storage.StorageTransactionManager
import kotlin.test.assertEquals

/**
 * @author Mikhail Snitavets
 */

/**
 * Simple Mock for [StorageTransactionManager] which just executes passed block of code and returns it's result
 */
object SimpleInvokeStorageTransactionManager : StorageTransactionManager {
    override fun <R> inDbTransaction(block: () -> R) = block()
}

infix fun <T> T.shouldEqualTo(actual: T) = assertEquals(this, actual)
/**
 * Alias for [times] with one occurrence
 */
fun once(): VerificationMode = times(1)

/**
 * Alias for [times] with two occurrences
 */
fun twice(): VerificationMode = times(2)

/**
 * Alias for first value from [KArgumentCaptor.allValues], may throw [IndexOutOfBoundsException]
 */
val <T : Any> KArgumentCaptor<T>.firstValue get() = this.allValues[0]

/**
 * Alias for second value from [KArgumentCaptor.allValues], may throw [IndexOutOfBoundsException]
 */
val <T : Any> KArgumentCaptor<T>.secondValue get() = this.allValues[1]

/**
 * Alias for third value from [KArgumentCaptor.allValues], may throw [IndexOutOfBoundsException]
 */
val <T : Any> KArgumentCaptor<T>.thirdValue get() = this.allValues[2]
