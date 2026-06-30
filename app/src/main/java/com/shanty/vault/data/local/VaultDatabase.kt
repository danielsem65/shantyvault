package com.shanty.vault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shanty.vault.data.model.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        VaultFileEntity::class,
        FolderEntity::class,
        NoteEntity::class,
        ActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun vaultFileDao(): VaultFileDao
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun activityDao(): ActivityDao

    companion object {
        private const val DB_NAME = "shanty_vault_encrypted.db"
        private const val PASSPHRASE = "sh4nty_v4ult_db_p4ssphr4se"

        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): VaultDatabase {
            val passphraseBytes = PASSPHRASE.toByteArray(Charsets.UTF_8)
            val passphrase = SQLiteDatabase.getBytes(passphraseBytes)
            val supportFactory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(supportFactory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
