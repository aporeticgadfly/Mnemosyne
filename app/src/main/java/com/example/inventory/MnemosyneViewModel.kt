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

package com.example.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemDao
import com.example.inventory.data.ListItem
import kotlinx.coroutines.launch

/**
 * View Model to keep a reference to the Inventory repository and an up-to-date list of all items.
 *
 */
class MnemosyneViewModel(private val itemDao: ItemDao) : ViewModel() {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<ListItem>> = itemDao.getItems().asLiveData()

    /**
     * Updates an existing Item in the database.
     */
    fun updateItem(
        itemId: Int,
        list_title: String,
        list_items: MutableList<String>
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, list_items, list_title)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(item: ListItem) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(list_title: String, list_items: MutableList<String>) {
        val newItem = getNewItemEntry(list_title, list_items)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: ListItem) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(id: Int) {
        viewModelScope.launch {
            itemDao.delete(id)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<ListItem> {
        return itemDao.getItem(id).asLiveData()
    }

    fun retrieveSyncItem(id: Int): ListItem {
        return itemDao.getSyncItem(id)
    }

    /**
     * RETRIEVE ALL ITEMS BAHAHAHAHAHHAHAHAHAHAHAHA
     */
    fun retrieveItems(): LiveData<MutableList<ListItem>> {
        return itemDao.getItems().asLiveData()
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(list_title: String): Boolean {
        if (list_title.isBlank()) {
            return false
        }
        return true
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the Inventory database.
     */
    private fun getNewItemEntry(list_title: String, list_items: MutableList<String>): ListItem {
        return ListItem(
            list_title = list_title,
            list_items = list_items
        )
    }

    /**
     * Called to update an existing entry in the Inventory database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        itemId: Int,
        list_items: MutableList<String>,
        list_title: String
    ): ListItem {
        return ListItem(
            id = itemId,
            list_title = list_title,
            list_items = list_items
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class MnemosyneViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MnemosyneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MnemosyneViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

