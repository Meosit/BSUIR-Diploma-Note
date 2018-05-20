package org.monium.android.common.storage

/**
 * Provides ability to execute several statements related to *Storage*
 * in a single database transaction
 * @author Mikhail Snitavets
 */
interface StorageTransactionManager {

    /**
     * Executes the specified [block] in a database transaction and returns it's result.
     * The transaction will be marked as successful unless an exception is thrown in the [block].
     */
    fun <R> inDbTransaction(block: () -> R): R

}