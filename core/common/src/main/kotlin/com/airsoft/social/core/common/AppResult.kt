package com.airsoft.social.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Unknown : AppError
    data object Unsupported : AppError
    data object Unauthorized : AppError
    data class Validation(val message: String) : AppError
    data class Network(val message: String? = null) : AppError
    data class ThrowableError(val throwable: Throwable) : AppError
}

inline fun <T> AppResult<T>.getOrNull(): T? = when (this) {
    is AppResult.Success -> value
    is AppResult.Failure -> null
}

