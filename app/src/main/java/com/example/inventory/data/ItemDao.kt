/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.inventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Inventory database
 */
@Dao
interface ItemDao {

    @Query("SELECT * from ListItem ORDER BY title ASC")
    fun getItems(): Flow<MutableList<ListItem>>

    @Query("SELECT * from ListItem WHERE id = :id")
    fun getItem(id: Int): Flow<ListItem>

    @Query("SELECT * from ListItem WHERE id = :id")
    fun getSyncItem(id: Int): ListItem

    @Query("SELECT title from ListItem WHERE id = :id")
    fun getTitle(id: Int): Flow<String>

    // Specify the conflict strategy as IGNORE, when the user tries to add an
    // existing Item into the database Room ignores the conflict.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ListItem)

    @Update
    suspend fun update(item: ListItem)

    @Query("DELETE from ListItem WHERE id = :id")
    suspend fun delete(id: Int)




    @Query("SELECT * from Session WHERE list_id = :list_id")
    fun getSessions(list_id: Int): Flow<MutableList<Session>>

    @Query("SELECT * from Session WHERE id = :id")
    fun getSession(id: Long) : Session

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: Session) : Long

    @Update
    suspend fun update(session: Session)

    @Query("DELETE from Session WHERE id = :id")
    suspend fun sessionDelete(id: Int)

    @Query("DELETE from Session")
    suspend fun sessionsDelete()

    @Query("DELETE FROM Session WHERE id = (SELECT id FROM Session ORDER BY id LIMIT 1)")
    suspend fun deleteLast()

    @Query("SELECT * FROM Session WHERE list_id = :list_id ORDER BY id DESC LIMIT 1")
    fun getLast(list_id: Int) : Flow<Session>

    @Query("DELETE from Session WHERE id < :cutoff")
    suspend fun deleteCutoff(cutoff: Int)

    @Query("SELECT COUNT(*) as count FROM Session")
    fun sessionNum() : Int

    @Query("SELECT id FROM Session ORDER BY id DESC LIMIT 1")
    fun lowestId() : Int
}
