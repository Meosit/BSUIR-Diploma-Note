package org.monium.android.currency

/**
 * Read-only (from the app side) entity which refers to money in any form when in
 * actual use or circulation as a medium of exchange
 *
 * **This entity is a global one, not user-specific.**
 *
 * ***Note:** default values were set in order to have an empty constructor.
 * This is because sometimes we need an entity where only id is set (e.g. foreign keys)*
 * @author Mikhail Snitavets
 */
data class Currency(
        /**
         * **Required**. Internal entity id
         */
        val id: Long = -1L,
        /**
         * **Required**. 3-character international currency code by [ICO 4217](https://en.wikipedia.org/wiki/ISO_4217)
         */
        val code: String = "",
        /**
         * **Required**. One or more symbols which is used in amount recording,
         * e.g. [Unicode currency symbols](https://en.wikipedia.org/wiki/Currency_Symbols_(Unicode_block))
         */
        val symbol: String = "",
        /**
         * **Required**. `true` whenever the [symbol] should be typed before the particular amount
         */
        val isPrefix: Boolean = false
)