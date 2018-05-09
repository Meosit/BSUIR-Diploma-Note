package org.monium.android.category.service

import mu.KLogging
import org.monium.android.category.Category
import org.monium.android.category.storage.CategoryStorage
import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageTransactionManager
import org.monium.android.transaction.storage.TransactionStorage

/**
 * @author Mikhail Snitavets.
 */
open class DefaultCategoryService(
        private val categoryStorage: CategoryStorage,
        private val transactionStorage: TransactionStorage,
        private val stm: StorageTransactionManager
) : CategoryService {
    companion object Static : KLogging()

    override fun findById(id: Long) = categoryStorage.findById(id)

    override fun findAll(ordering: Ordering, showHidden: Boolean) =
            categoryStorage.findAll(ordering, showHidden)

    override fun create(category: Category) = categoryStorage.create(category)

    override fun update(category: Category) = categoryStorage.update(category)

    override fun remove(id: Long, shiftId: Long?) = stm.inDbTransaction {
        val affected = transactionStorage.batchUpdateCategoryId(oldId = id, newId = shiftId)
        logger.debug { "Affected $affected transactions while removing category with id $id, shift id: $shiftId" }
        categoryStorage.remove(id)
        affected
    }
}