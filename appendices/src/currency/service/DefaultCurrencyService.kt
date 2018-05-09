package org.monium.android.currency.service

import org.monium.android.common.Ordering
import org.monium.android.currency.storage.CurrencyStorage

/**
 * Default implementation of [CurrencyService].
 * Simply delegates all to [CurrencyStorage] with some error wrapping
 *
 * @author Mikhail Snitavets
 */
open class DefaultCurrencyService(private val storage: CurrencyStorage) : CurrencyService {
    override fun findAll(ordering: Ordering) = storage.findAll(ordering)

    override fun findAllPreferred(ordering: Ordering) = storage.findAllPreferred(ordering)
}