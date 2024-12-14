package dev.mattramotar.updatingitem.runtime

import dev.mattramotar.updatingitem.runtime.impl.DefaultNormalizer
import dev.mattramotar.updatingitem.runtime.impl.Normalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Transforms a [StateFlow] of items into a [StateFlow] of [UpdatingItem]s. This function connects
 * your raw data stream (e.g., from a database, network, or paging) to a [Normalizer] that tracks
 * stable item references and issues minimal updates. The result is a flow of stable [UpdatingItem]
 * instances that can be used to drive efficient UIs.
 *
 * When the source [StateFlow] emits a new list:
 * - The [Normalizer] updates its internal store of items.
 * - Items that have changed trigger localized updates, while unchanged items remain stable, preventing unnecessary
 *   global recompositions.
 *
 * @param ItemId A stable identifier type for each item.
 * @param ItemValue The underlying data type of each item.
 * @param idExtractor A lambda that extracts a stable [ItemId] from an [ItemValue]. Items must have stable IDs for correct normalization.
 * @param coroutineScope A [CoroutineScope] used to launch a collector that updates the [Normalizer].
 *
 * @return A [StateFlow] of [UpdatingItem]s. Observers of this flow receive stable item references
 * that only recompose on actual item-level changes.
 */
fun <ItemId : Any, ItemValue : Any> StateFlow<List<ItemValue>>.asUpdatingItems(
    coroutineScope: CoroutineScope,
    idExtractor: (ItemValue) -> ItemId,
): StateFlow<List<UpdatingItem<ItemId, ItemValue>>> {
    val normalizer = DefaultNormalizer( coroutineScope, idExtractor)

    // Launch a coroutine to listen to the upstream items
    // and update the normalizer whenever they change.
    coroutineScope.launch {
        this@asUpdatingItems.collect { items ->
            normalizer.updateItems(items)
        }
    }

    // The normalizer provides a stable StateFlow of UpdatingItems.
    // This flow emits whenever the set or order of items changes.
    // Internal item-level updates happen without triggering this flow.
    return normalizer.updatingItemsFlow
}