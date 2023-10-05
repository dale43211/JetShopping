package hoods.com.jetshopping.ui.detail
//  https://www.youtube.com/watch?v=voMTReNRvUA&t=2957s   1:25:34

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import hoods.com.jetshopping.Graph
import hoods.com.jetshopping.data.room.models.Item
import hoods.com.jetshopping.data.room.models.ShoppingList
import hoods.com.jetshopping.data.room.models.Store
import hoods.com.jetshopping.ui.Category
import hoods.com.jetshopping.ui.Utils
import hoods.com.jetshopping.ui.repository.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


class DetailViewModel
constructor(
    private val itemId: Int,
    private val repository: Repository = Graph.repository
) : ViewModel() {

    var state by mutableStateOf(DetailState())
        private set

    init{
        addListItem()
        getStores()
        if(itemId != -1){
            viewModelScope.launch{
                repository
                    .getItemWithStoreAndList(itemId)
                    .collectLatest {
                        state = state.copy(
                            item = it.item.itemName,
                            store = it.store.storeName,
                            date = it.item.date,
                            category = Utils.category.find{ c ->
                                c.id == it.shoppingList.id
                            } ?: Category(),
                            qty = it.item.qty
                        )
                    }
            }
        }
    }   // end init 1

    init{
        state = if(itemId != -1){
            state.copy(isUpdatingItem = true)
        } else {
            state.copy(isUpdatingItem = false)
        }
    }   // end init 2   (does not require viewModelScope)


    val isFieldsNotEmpty: Boolean
        get() = state.item.isNotEmpty() &&
                state.store.isNotEmpty() &&
                state.qty.isNotEmpty()

    fun onItemChange(newValue: String){
        state = state.copy(item = newValue)
    }

    fun onStoreChange(newValue: String){
        state = state.copy(store = newValue)
    }

    fun onQtyChange(newValue: String){
        state = state.copy(qty = newValue)
    }

    fun onDateChange(newValue: Date){
        state = state.copy(date = newValue)
    }

    fun onCategoryChange(newValue: Category){
        state = state.copy(category = newValue)
    }

    fun onScreenDialogDismissed(newValue: Boolean){
        state = state.copy(isScreenDialogDismissed = newValue)
    }

    private fun addListItem(){
        viewModelScope.launch{
            Utils.category.forEach {
                repository.insertList(
                    ShoppingList(
                        id = it.id,
                        name = it.title
                    )
                )
            }
        }
    }   // end fun addListItem

    fun addShoppingItem(){
        viewModelScope.launch{
            repository.insertItem(
                Item(
                    itemName = state.item,
                    listId = state.category.id,
                    date = state.date,
                    qty = state.qty,
                    storeIdFk = state.storeList.find{
                        it.storeName == state.store
                    }?.id ?: 0,
                    isChecked = false
                )
            )
        }
    }   // end fun addShoppingItem


    fun updateShoppingItem(id: Int){
        viewModelScope.launch{
            repository.insertItem(
                Item(
                    itemName = state.item,
                    listId = state.category.id,
                    date = state.date,
                    qty = state.qty,
                    storeIdFk = state.storeList.find{
                        it.storeName == state.store
                    }?.id ?: 0,
                    isChecked = false,
                    id = id
                )
            )
        }
    }   // end fun updateShoppingItem

    fun addStore(){
        viewModelScope.launch{
            repository.insertStore(
                Store(
                    storeName = state.store,
                    listIdFk = state.category.id
                )
            )
        }
    }   // end fun addStore


    fun getStores(){
        viewModelScope.launch{
            repository.store.collectLatest{
                state = state.copy(storeList = it)
            }
        }
    }   // end fun getStores



}   // end DetailViewModel

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactor(private val id: Int): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(itemId = id) as T
    }
}   // end class DetailViewModelFactor

data class DetailState(
    val storeList: List<Store> = emptyList(),
    val item: String = "",
    val store: String = "",
    val date: Date = Date(),
    val qty: String = "",
    val isScreenDialogDismissed: Boolean = true,
    val isUpdatingItem: Boolean = false,
    val category: Category = Category()
)   // end data class DetailState


