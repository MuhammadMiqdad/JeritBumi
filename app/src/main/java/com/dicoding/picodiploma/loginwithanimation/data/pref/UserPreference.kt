package com.dicoding.picodiploma.loginwithanimation.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.userId  // Menyimpan userId
            preferences[NAME_KEY] = user.name        // Menyimpan name
            preferences[TOKEN_KEY] = user.token      // Menyimpan token
            preferences[ROLE_KEY] = user.role
            preferences[IS_LOGIN_KEY] = user.isLogin        // Menyimpan status login
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[USER_ID_KEY] ?: "",      // Ambil userId
                preferences[NAME_KEY] ?: "",         // Ambil name
                preferences[TOKEN_KEY] ?: "",        // Ambil token
                preferences[ROLE_KEY] ?: "",
                preferences[IS_LOGIN_KEY] ?: false  // Ambil status login

            )
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()  // Menghapus data session
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val USER_ID_KEY = stringPreferencesKey("userId")  // Kunci untuk userId
        private val NAME_KEY = stringPreferencesKey("name")       // Kunci untuk name
        private val TOKEN_KEY = stringPreferencesKey("token")     // Kunci untuk token
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin") // Kunci untuk status login
        private val ROLE_KEY = stringPreferencesKey("role")


        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
