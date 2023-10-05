package hoods.com.jetshopping.ui.detail
// https://www.youtube.com/watch?v=voMTReNRvUA&t=6292s   1:45:04

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import hoods.com.jetshopping.ui.Category
import hoods.com.jetshopping.ui.Utils
import hoods.com.jetshopping.ui.home.CategoryItem
import hoods.com.jetshopping.ui.home.formatDate
import hoods.com.jetshopping.ui.theme.Shapes
import java.util.*


@Composable
fun DetailScreen(
    id: Int,
    navigateUp: () -> Unit
) {
    val detailViewModel = viewModel<DetailViewModel>(factory = DetailViewModelFactor(id))

    Scaffold {
        DetailEntry(
            state = detailViewModel.state,
            onDateSelected = detailViewModel::onDateChange,
            onStoreChange = detailViewModel::onStoreChange,
            onItemChange = detailViewModel::onItemChange,
            onQtyChange = detailViewModel::onQtyChange,
            onCategoryChange = detailViewModel::onCategoryChange,
            onDialogDismissed = detailViewModel::onScreenDialogDismissed,
            onSaveStore = detailViewModel::addStore,
            updateItem = {detailViewModel.updateShoppingItem(id)},
            saveItem = detailViewModel::addShoppingItem
        ) {
            navigateUp.invoke()
        }

    }   // end Scaffold

}   // end fun DetailScreen


@Composable
private fun DetailEntry(
    modifier: Modifier = Modifier,
    state: DetailState,
    onDateSelected: (Date) -> Unit,
    onStoreChange: (String) -> Unit,
    onItemChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDialogDismissed: (Boolean) -> Unit,
    onSaveStore: () -> Unit,
    updateItem: () -> Unit,
    saveItem: () -> Unit,
    navigateUp: () -> Unit
) {
    var isNewEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        TextField(
            value = state.item,
            onValueChange = { onItemChange(it) },
            label = { Text(text = "Item") },
            colors = TextFieldDefaults.textFieldColors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = Shapes.large
        )
        Spacer(modifier = Modifier.Companion.size(12.dp))
        Row {
            TextField(
                value = state.store,
                onValueChange = { if (isNewEnabled) onStoreChange.invoke(it) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.textFieldColors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Cyan
                ),
                shape = Shapes.large,
                label = { Text(text = "Store") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable {
                            onDialogDismissed.invoke(!state.isScreenDialogDismissed)

                        })
                })
            if (!state.isScreenDialogDismissed) {
                Popup(onDismissRequest = {
                    onDialogDismissed.invoke(!state.isScreenDialogDismissed)
                }) {   // begin popup
                    Surface(modifier = Modifier.padding(16.dp)) {
                        Column {
                            state.storeList.forEach {
                                Text(
                                    text = it.storeName,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            onStoreChange.invoke(it.storeName)
                                            onDialogDismissed(!state.isScreenDialogDismissed)
                                        }
                                )
                            }
                        }
                    }
                }   // end Popup
            }
            TextButton(onClick = {
                isNewEnabled = if (isNewEnabled) {
                    onSaveStore.invoke()
                    !isNewEnabled
                } else {
                    !isNewEnabled
                }
            }) {
                Text(text = if (isNewEnabled) "Save" else "New")
            }
        }   // end Row 1  (store)
        Spacer(modifier = Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange, contentDescription = null
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = formatDate(state.date))
                Spacer(modifier = Modifier.size(4.dp))
                val mDatePicker =
                    datePickerDialog(context = LocalContext.current,
                        onDateSelected = { date -> onDateSelected.invoke(date) })
                IconButton(onClick = { mDatePicker.show() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null
                    )
                }
            }
            TextField(
                value = state.qty,
                onValueChange = { onQtyChange(it) },
                label = { Text(text = "Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.textFieldColors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Cyan
                ),
                shape = Shapes.large
            )
        }   // end Row 2 (Date/Qty)
        Spacer(modifier = Modifier.size(12.dp))
        LazyRow {
            items(Utils.category) { category: Category ->
                CategoryItem(
                    iconRes = category.resId,
                    title = category.title,
                    selected = category == state.category
                ) {
                    onCategoryChange(state.category)
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
        }   // end LazyRow (Categories)
        val buttonTitle =
            if (state.isUpdatingItem) "Update Item" else "AddItem"
        Button(
            onClick = {
                when (state.isUpdatingItem) {
                    true -> {
                        updateItem.invoke()
                    }
                    false -> {
                        saveItem.invoke()
                    }
                }
                navigateUp.invoke()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.item.isNotEmpty() &&
                    state.store.isNotEmpty() &&
                    state.qty.isNotEmpty(),
            shape = Shapes.large
        ) {
            Text(text = buttonTitle)
        }

    }   // end Column
}   // end fun DetailEntry

@Composable
fun datePickerDialog(
    context: Context, onDateSelected: (Date) -> Unit
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.time = Date()

    val mDatePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, mDay: Int ->
            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay)
            onDateSelected.invoke(calendar.time)
        },
        year, month, day
    )
    return mDatePickerDialog
}   // end fun datePickerDialog

@Preview(showSystemUi = true)
@Composable
fun PreviewDetailEntry() {
    DetailEntry(
        state = DetailState(),
        onDateSelected = {},
        onStoreChange = {},
        onItemChange = {},
        onQtyChange = {},
        onCategoryChange = {},
        onDialogDismissed = {},
        onSaveStore = { /*TODO*/ },
        updateItem = { /*TODO*/ },
        saveItem = { /*TODO*/ }) {

    }
}   // end fun PreviewDetailEntry