package org.monium.android.common.storage

/**
 * Represents a generic error occurred while operating with storage
 *
 * @author Mikhail Snitavets
 */
open class StorageException(
        message: String? = null,
        cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(cause: Throwable?) : this(null, cause)
    constructor(message: String?) : this(message, null)
}