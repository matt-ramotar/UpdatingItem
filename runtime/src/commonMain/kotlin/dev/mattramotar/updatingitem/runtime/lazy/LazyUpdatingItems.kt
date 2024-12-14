package dev.mattramotar.updatingitem.runtime.lazy


import androidx.compose.runtime.*
import dev.mattramotar.updatingitem.runtime.UpdatingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A stable interface that provides indexed access to a collection of [UpdatingItem].
 *
 * This allows you to:
 * - Integrate seamlessly with Compose lazy layouts like LazyColumn.
 * - Benefit from minimal recompositions (only updated items recompose).
 * - Access items by index, without returning a new list on each emission.
 *
 * @param ItemId A stable, unique identifier for each item.
 * @param ItemValue The type of the item's underlying data.
 */
@Stable
class LazyUpdatingItems<ItemId : Any, ItemValue : Any> internal constructor(
    private val itemsFlow: StateFlow<List<UpdatingItem<ItemId, ItemValue>>>,
) {

    // Current snapshot of items. Stable references to each UpdatingItem ensure that only changed
    // items recompose. The internal StateFlow is updated only when the item set changes, not when
    // individual items update their internal states.
    private var currentItems by mutableStateOf(itemsFlow.value)

    // Observe changes to the underlying itemsFlow. Only update currentItems when the set of items
    // or their order changes, preserving stability and preventing unnecessary recompositions.
    internal fun startObserving(scope: CoroutineScope) {
        scope.launch {
            itemsFlow.collectLatest { newList ->
                if (newList !== currentItems) {
                    // Only update if the reference changes or the order/content changes.
                    // If references are stable and identical, this won't trigger recompositions.
                    currentItems = newList
                }
            }
        }
    }

    /**
     * The number of items currently available.
     * This value updates whenever the underlying StateFlow emits a new list.
     */
    val itemCount: Int
        get() = currentItems.size

    /**
     * Get the [UpdatingItem] at the given [index].
     *
     * @param index The 0-based index of the desired item.
     * @return The stable [UpdatingItem] instance at the given index.
     */
    operator fun get(index: Int): UpdatingItem<ItemId, ItemValue> {
        return currentItems[index]
    }

    /**
     * Convenience method for retrieving the current item state at a given index.
     * Equivalent to `this[index]()` but provided for clarity.
     *
     * @param index The 0-based index of the item.
     * @return The latest [UpdatingItem.ItemState] of the item at [index].
     */
    @Composable
    fun itemStateAt(index: Int): UpdatingItem.ItemState<ItemValue> {
        val item = this[index]
        return item()
    }
}

