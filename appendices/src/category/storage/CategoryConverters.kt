package org.monium.android.category.storage

import android.arch.persistence.room.TypeConverter
import org.monium.android.category.CategoryType

class CategoryConverters {

    @TypeConverter
    fun categoryTypeFromString(value: String?): CategoryType? =
            value?.let { CategoryType.valueOf(value) }

    @TypeConverter
    fun stringFromCategoryType(value: CategoryType?): String? = value?.name

}