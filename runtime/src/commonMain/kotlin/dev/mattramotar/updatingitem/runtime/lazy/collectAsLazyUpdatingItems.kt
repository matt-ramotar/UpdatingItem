package dev.mattramotar.updatingitem.runtime.lazy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import dev.mattramotar.updatingitem.runtime.UpdatingItem
import dev.mattramotar.updatingitem.runtime.normalizer.DefaultNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collects values from a [Flow] of [ItemValue] lists and normalizes them into a
 * [LazyUpdatingItems] instance. This allows you to seamlessly integrate with lazy Compose lists
 * while preserving fine-grained recomposition benefits.
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen(itemsFlow: Flow<List<MyItem>>) {
 *     val lazyItems = itemsFlow.collectAsLazyUpdatingItems(
 *         coroutineScope = rememberCoroutineScope(),
 *         idExtractor = { it.id }
 *     )
 *
 *     LazyColumn {
 *         items(lazyItems.itemCount) { index ->
 *             val state = lazyItems.itemStateAt(index)
 *             MyItemRow(state.value)
 *         }
 *     }
 * }
 * ```
 *
 * @param ItemId A stable unique identifier for each item.
 * @param ItemValue The type of your underlying item data.
 * @param coroutineScope A [CoroutineScope] used to collect updates from the StateFlow.
 * @param idExtractor A lambda that extracts a stable [ItemId] from an [ItemValue].
 * @return A [LazyUpdatingItems] instance that provides indexed access to stable [UpdatingItem]s.
 */
@Composable
fun <ItemId : Any, ItemValue : Any> Flow<List<ItemValue>>.collectAsLazyUpdatingItems(
    coroutineScope: CoroutineScope,
    idExtractor: (ItemValue) -> ItemId,
): LazyUpdatingItems<ItemId, ItemValue> {
    // The Normalizer transforms raw lists of items into stable, per-item UpdatingItems.
    val normalizer = remember(this, idExtractor) {
        DefaultNormalizer(
            coroutineScope = coroutineScope,
            idExtractor = idExtractor
        )
    }

    // Start collecting upstream items and feeding into the normalizer.
    LaunchedEffect(this) {
        this@collectAsLazyUpdatingItems.collectLatest { items ->
            normalizer.updateItems(items)
        }
    }

    // Create a LazyUpdatingItems instance from the normalizer's StateFlow of UpdatingItems.
    val lazyUpdatingItems = remember(normalizer) {
        LazyUpdatingItems(normalizer.updatingItemsFlow)
    }

    // Start observing changes to the normalized items. This ensures lazyUpdatingItems stays in sync.
    LaunchedEffect(lazyUpdatingItems) {
        lazyUpdatingItems.startObserving(this)
    }

    return lazyUpdatingItems
}
