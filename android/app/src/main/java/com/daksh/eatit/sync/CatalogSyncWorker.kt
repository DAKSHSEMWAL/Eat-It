package com.daksh.eatit.sync

import android.content.Context
import androidx.work.*
import com.daksh.eatit.DemoCatalog
import com.daksh.eatit.db.EatItDatabase
import com.daksh.eatit.db.toEntity
import java.util.concurrent.TimeUnit

class CatalogSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = EatItDatabase.getInstance(applicationContext)
            val catalog = DemoCatalog
            val categoryEntities = catalog.categories.map { it.toEntity() }
            val dishEntities = catalog.dishes.map { it.toEntity() }
            val now = System.currentTimeMillis()

            db.catalogDao().replaceCatalog(categoryEntities, dishEntities, now)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "catalog_sync_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<CatalogSyncWorker>(
                1, TimeUnit.HOURS
            ).setConstraints(constraints).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
