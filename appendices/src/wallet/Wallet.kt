package org.monium.android.wallet

import org.monium.android.currency.Currency
import java.math.BigDecimal

/**
 * Represents a money container for a specific currency.
 *
 * ***Note:** default values were set in order to have an empty constructor.
 * This is because sometimes we need an entity where only id is set (e.g. foreign keys)*
 * @author Mikhail Snitavets
 */
data class Wallet(
        /**
         * **Required**. Internal entity id
         */
        val id: Long = -1L,
        /**
         * **Required**. Currency of a wallet, [balance] will be in the in that currency
         * All transactions with this wallet will be using this currency.
         */
        val currency: Currency = Currency(),
        /**
         * **Required**. Amount of money that this wallet have
         */
        val balance: BigDecimal = BigDecimal.ZERO,
        /**
         * **Required**. Name of the wallet, up to 50 chars.
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
         * **Required**. If `true` - this wallet will not be used in calculating daily budget values.
         */
        val isReserved: Boolean = false,
        /**
         * **Required**. All wallets marked as hidden should disappear from all listings except
         * transactions related to this wallets.
         */
        val isHidden: Boolean = false,
        /**
         * **Required**. Value which is used for user-specific ordering in the list of the entities.
         * By default should be inserted in the bottom of the list.
         */
        val ordering: Int = -1,
        /**
         * **Optional**. User comment about that wallet, *up to 300 chars.*
         */
        val note: String? = null
)