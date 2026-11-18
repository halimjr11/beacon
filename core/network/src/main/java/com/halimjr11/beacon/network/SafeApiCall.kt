package com.halimjr11.beacon.network

import com.google.gson.JsonParseException
import com.halimjr11.beacon.common.NetworkResult
import com.halimjr11.beacon.common.NewsError
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** Runs [block] and maps every failure to [NewsError]. Cancellation is always rethrown. */
suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    NetworkResult.Error(ErrorParser.parse(e.code(), body))
} catch (e: IOException) {
    NetworkResult.Error(NewsError.Offline)
} catch (e: JsonParseException) {
    NetworkResult.Error(NewsError.Parse)
} catch (e: Exception) {
    NetworkResult.Error(NewsError.Unknown(e::class.java.simpleName))
}
