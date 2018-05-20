package org.monium.android.common.storage

import android.arch.persistence.room.RoomDatabase

/**
 * @author Mikhail Snitavets.
 */
class RoomStorageTransactionManager(
        private val roomDatabase: RoomDatabase
) : StorageTransactionManager {
    override fun <R> inDbTransaction(block: () -> R): R = roomDatabase.runInTransaction(block)
}