package org.monium.android.category

/**
 * Represents a budget category which is used to
 * to classify transactions and calculate some statistics over it
 *
 * ***Note:** default values were set in order to have an empty constructor.
 * This is because sometimes we need an entity where only id is set (e.g. foreign keys)*
 * @author Mikhail Snitavets
 */
data class Category(
        /**
         * **Required**. Internal entity id
         */
        val id: Long = -1L,
        /**
         * **Required**. Type of the category.
         * For example *Salary* fits only for income categories.
         */
        val type: CategoryType = CategoryType.ANY,
        /**
         * **Required**. Name of the category, up to 50 chars.
         */
        val name: String = "",
        /**
         * **Optional**. Icon name/code which should be mapped to an icon resource.
         *
         * This is a value which should be unique and standardized for the whole
         * app ecosystem, e.g. android client, web client.
         *
         * **If unset or unrecognized, the default value should be used**
         */
        val iconCode: String = "",
        /**
         * **Required**. All categories marked as hidden should disappear from all listings except
         * transactions related to this categories.
         */
        val isHidden: Boolean = false,
        /**
         * **Optional**. User comment about that category, *up to 300 chars.*
         */
        val note: String? = null
)

/**
 * Represents a scope of the transactions where this category is applicable.
 */
enum class CategoryType {
    /**
     * This type should be used when this category fits for both income and expense transactions,
     * such as *Presents*
     */
    ANY,
    /**
     * When the category fits only for income transactions, such as *Salary*
     */
    INCOME,
    /**
     * When the category fits only for expense transactions, such as *Food*
     */
    EXPENSE
}