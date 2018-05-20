package org.monium.android.common.storage

import android.arch.persistence.room.TypeConverter
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Defines some common type converters for Room
 *
 * @author Mikhail Snitavets.
 */
class CommonTypeConverters {

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
        private val DATE_FORMAT = SimpleDateFormat(DATE_PATTERN, Locale.ROOT)
        private val DEFAULT_MATH_CONTEXT = MathContext(4, RoundingMode.FLOOR)
    }

    @TypeConverter
    fun timestampFromString(value: String?): Date? = value?.let { DATE_FORMAT.parse(value) }

    @TypeConverter
    fun stringFromTimestamp(value: Date?): String? = value?.let { DATE_FORMAT.format(value) }

    @TypeConverter
    fun stringFromBigDecimal(value: BigDecimal?): String? =
            value?.let { value.round(DEFAULT_MATH_CONTEXT).toPlainString() }

    @TypeConverter
    fun bigDecimalFromString(value: String?): BigDecimal? =
            value?.let { BigDecimal(value, DEFAULT_MATH_CONTEXT) }

}