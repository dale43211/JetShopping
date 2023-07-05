package hoods.com.jetshopping.ui.repository

import hoods.com.jetshopping.data.room.ItemDao
import hoods.com.jetshopping.data.room.ListDao
import hoods.com.jetshopping.data.room.StoreDao
import hoods.com.jetshopping.data.room.models.Item
import hoods.com.jetshopping.data.room.models.ShoppingList
import hoods.com.jetshopping.data.room.models.Store

class Repository(
    val listDao: ListDao,
    val itemDao: ItemDao,
    val storeDao: StoreDao
) {
    val store = storeDao.getAllStores()
    val getItemsWithListAndStore = listDao.getItemsWithStoreAndList()

    fun getItemWithStoreAndList(id: Int) =
        listDao.getItemWithStoreAndListFilteredById(id)

    fun getItemsWithStoreAndListFilteredById(id: Int) =
        listDao.getItemsWithStoreAndListFilteredById(id)

    suspend fun insertList(shoppingList: ShoppingList){
        listDao.insertShoppingList(shoppingList)
    }
    suspend fun insertStore(store: Store){
        storeDao.insert(store)
    }
    suspend fun insertItem(item: Item){
        itemDao.insert(item)
    }
    suspend fun deleteItem(item: Item){
        itemDao.delete(item)
    }

    suspend fun updateItem(item: Item){
        itemDao.update(item)
    }



}   // end class Repository