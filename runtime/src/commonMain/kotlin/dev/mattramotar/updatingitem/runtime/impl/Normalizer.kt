package dev.mattramotar.updatingitem.runtime.impl

import dev.mattramotar.updatingitem.runtime.UpdatingItem
import kotlinx.coroutines.flow.StateFlow

/**
 * A normalizer maintains a stable set of items keyed by [ItemId]. It ensures:
 * - Stable [UpdatingItem] references for each item.
 * - Minimal recompositions by only updating items that have changed.
 *
 * This is not tied to any particular data source. You feed it new sets of items,
 * and it provides a normalized representation and a flow of stable updating items.
 *
 * @param ItemId The key type for identifying items uniquely.
 * @param ItemValue The type of the item's underlying data.
 */
internal interface Normalizer<ItemId : Any, ItemValue : Any> {

    /**
     * Updates the normalized store with a new list of items.
     * Only items that have actually changed will trigger updates in their associated [UpdatingItem].
     *
     * @param newItems The current list of items from the data source.
     */
    fun updateItems(newItems: List<ItemValue>)

    /**
     * Retrieves an [UpdatingItem] corresponding to the given [itemId].
     * If no item exists for that ID, a new one can be created.
     *
     * @param itemId The unique identifier for the desired item.
     * @return The stable [UpdatingItem] instance associated with [itemId].
     */
    fun getUpdatingItem(itemId: ItemId): UpdatingItem<ItemId, ItemValue>

    /**
     * A [StateFlow] that emits the current list of [UpdatingItem]s whenever their set changes.
     * Each [UpdatingItem] handles its own recompositions, so global changes do not cause full-list
     * recompositions in the UI.
     */
    val updatingItemsFlow: StateFlow<List<UpdatingItem<ItemId, ItemValue>>>
}