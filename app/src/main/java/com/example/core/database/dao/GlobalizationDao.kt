package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.CurrencySettingsEntity
import com.example.core.database.entity.LocaleEntity
import com.example.core.database.entity.TaxProfileEntity
import com.example.core.database.entity.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalizationDao {

    // Locales
    @Query("SELECT * FROM locales WHERE isActive = 1")
    fun getActiveLocales(): Flow<List<LocaleEntity>>

    @Query("SELECT * FROM locales WHERE code = :code LIMIT 1")
    suspend fun getLocaleByCode(code: String): LocaleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocale(locale: LocaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocales(locales: List<LocaleEntity>)

    // Translations
    @Query("SELECT * FROM translations WHERE localeCode = :localeCode")
    fun getTranslationsForLocale(localeCode: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE localeCode = :localeCode AND key = :key LIMIT 1")
    suspend fun getTranslation(localeCode: String, key: String): TranslationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    // Currency Settings
    @Query("SELECT * FROM currency_settings")
    fun getAllCurrencies(): Flow<List<CurrencySettingsEntity>>

    @Query("SELECT * FROM currency_settings WHERE code = :code LIMIT 1")
    suspend fun getCurrencyByCode(code: String): CurrencySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencySettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencySettingsEntity>)

    // Tax Profiles
    @Query("SELECT * FROM tax_profiles")
    fun getAllTaxProfiles(): Flow<List<TaxProfileEntity>>

    @Query("SELECT * FROM tax_profiles WHERE countryCode = :countryCode")
    suspend fun getTaxProfilesForCountry(countryCode: String): List<TaxProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxProfile(taxProfile: TaxProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxProfiles(profiles: List<TaxProfileEntity>)
}
