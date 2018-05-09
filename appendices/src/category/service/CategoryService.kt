package org.monium.android.category.service

import org.monium.android.category.Category
import org.monium.android.common.Ordering
import org.monium.android.common.storage.StorageException
import org.monium.android.transaction.Transaction

/**
 * @author Mikhail Snitavets.
 */
interface CategoryService {

    /**
     * Finds a single entity by it's [id]
     * @return entity with specified [id] or `null` if that entity doesn't exist
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findById(id: Long): Category?

    /**
     * Finds all categories with using specified filters.
     *
     * @param ordering defines specific [Ordering] for the entity list, by default it's [Ordering.ALPHANUMERIC]
     * @param showHidden if true, fetches also [Category.isHidden] marked entities
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun findAll(ordering: Ordering = Ordering.ALPHANUMERIC,
                showHidden: Boolean = false): List<Category>

    /**
     * Creates [Category]. [Category.id] is ignored.
     *
     * @return created entity with filled auto-generated entries
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun create(category: Category): Category

    /**
     * Updates existing [Category] based on it's ID
     * @throws StorageException in case of any unexpected issues with the storage
     */
    fun update(category: Category)

    /**
     * Removes [Category] with specified ID.
     * Also shifts all [Transaction]s to a different category with specified [shiftId]
     * (or to default category if `null` value passed)
     * @param id of category to remove
     * @param shiftId new category id for all transactions which are related to the category to delete
     * @return count of transactions that was shifted
     */
    fun remove(id: Long, shiftId: Long? = null): Int
}