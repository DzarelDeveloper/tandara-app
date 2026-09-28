package id.tandara.parent.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey val cacheId: String,
    val accountId: String,
    val studentId: String?,
    val kind: String,
    val itemKey: String,
    val payload: String,
    val fetchedAt: Long
)

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache_entries WHERE cacheId = :id LIMIT 1") suspend fun get(id: String): CacheEntry?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(entry: CacheEntry)
    @Query("DELETE FROM cache_entries WHERE cacheId = :id") suspend fun delete(id: String)
    @Query("DELETE FROM cache_entries WHERE accountId = :accountId") suspend fun clearAccount(accountId: String)
}

@Database(entities = [CacheEntry::class], version = 1, exportSchema = false)
abstract class TandaraCacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    companion object {
        fun create(context: Context): TandaraCacheDatabase = Room.databaseBuilder(
            context.applicationContext, TandaraCacheDatabase::class.java, "tandara_parent_cache.db"
        ).build()
    }
}
