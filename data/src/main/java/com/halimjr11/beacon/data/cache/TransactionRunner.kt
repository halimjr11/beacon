package com.halimjr11.beacon.data.cache

import com.halimjr11.beacon.database.BeaconDatabase
import com.halimjr11.beacon.database.inTransaction
import org.koin.core.annotation.Single

/** Seam over Room transactions so mediators can be tested with in-memory fakes. */
interface TransactionRunner {
    suspend fun <R> run(block: suspend () -> R): R
}

@Single(binds = [TransactionRunner::class])
class RoomTransactionRunner(private val database: BeaconDatabase) : TransactionRunner {
    override suspend fun <R> run(block: suspend () -> R): R = database.inTransaction(block)
}
