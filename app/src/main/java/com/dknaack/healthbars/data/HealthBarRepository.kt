package com.dknaack.healthbars.data

class HealthBarRepository(
    private val healthBarDao: HealthBarDao,
    private val logDao: LogDao,
) {
    suspend fun upsert(healthBar: HealthBar) {
        healthBarDao.upsert(healthBar)
    }

    suspend fun delete(healthBar: HealthBar) {
        healthBarDao.delete(healthBar)
        logDao.deleteLogsForHealthBar(healthBar.id)
    }
}