package dev.mattramotar.updatingitem.runtime.normalizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import dev.mattramotar.updatingitem.runtime.UpdatingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A default implementation of the Normalizer interface.
 *
 * @param ItemId A stable, unique identifier type for each item.
 * @param ItemValue The type of the underlying data associated with each item.
 * @param idExtractor A function that extracts a stable [ItemId] from an [ItemValue].
 */
internal class DefaultNormalizer<ItemId : Any, ItemValue : Any>(
    private val coroutineScope: CoroutineScope,
    private val idExtractor: (ItemValue) -> ItemId,
    private val recompositionMode: RecompositionMode = RecompositionMode.ContextClock,
) : Normalizer<ItemId, ItemValue> {

    // Internal map of ItemId to a concrete UpdatingItemImpl
    private val itemsMap = mutableMapOf<ItemId, UpdatingItemImpl>()

    // StateFlow that holds the current list of UpdatingItems
    private val _updatingItemsFlow = MutableStateFlow<List<UpdatingItem<ItemId, ItemValue>>>(emptyList())
    override val updatingItemsFlow: StateFlow<List<UpdatingItem<ItemId, ItemValue>>> = _updatingItemsFlow.asStateFlow()

    override fun updateItems(newItems: List<ItemValue>) {
        val newIds = newItems.map(idExtractor).toSet()

        // Remove items no longer present
        val iterator = itemsMap.keys.iterator()
        while (iterator.hasNext()) {
            val existingId = iterator.next()
            if (existingId !in newIds) {
                iterator.remove()
            }
        }

        // For each new item, update or create an UpdatingItem
        val updatedList = buildList {
            for (value in newItems) {
                val id = idExtractor(value)
                val item = itemsMap.getOrPut(id) {
                    UpdatingItemImpl(id)
                }
                // Check if value changed; if so, update item value
                item.internalUpdateValueIfChanged(value)
                add(item)
            }
        }

        // Update the flow if the list is different
        _updatingItemsFlow.value = updatedList
    }

    override fun getUpdatingItem(itemId: ItemId): UpdatingItem<ItemId, ItemValue> {
        return itemsMap.getOrPut(itemId) {
            UpdatingItemImpl(itemId)
        }
    }

    /**
     * A concrete implementation of UpdatingItem, backed by a StateFlow of ItemState.
     *
     * Actions:
     * - Refresh: Sets load state to Loading and clears the value.
     * - Update: Sets the new value and load state to Loaded.
     * - Clear: Clears the value and sets load state to Loaded.
     */
    @Stable
    private inner class UpdatingItemImpl(
        private val itemId: ItemId
    ) : UpdatingItem<ItemId, ItemValue> {

        // Internal state for this item
        private val _state = MutableStateFlow(
            UpdatingItem.ItemState<ItemValue>(value = null, loadState = UpdatingItem.LoadState.Initial)
        )

        // Start a Molecule composition once
        private val moleculeFlow: StateFlow<UpdatingItem.ItemState<ItemValue>> by lazy {
            coroutineScope.launchMolecule(mode = recompositionMode) {
                UpdatingItemPresenter(_state)
            }
        }

        /**
         * Called by the Normalizer to update the value if it has changed.
         */
        fun internalUpdateValueIfChanged(newValue: ItemValue) {
            val current = _state.value
            val oldValue = current.value
            if (oldValue != newValue) {
                // We have a new or changed value, mark it as loaded
                _state.value = UpdatingItem.ItemState(newValue, UpdatingItem.LoadState.Loaded)
            }
        }

        override suspend fun dispatch(action: UpdatingItem.Action<ItemValue>) {
            when (action) {
                is UpdatingItem.Action.Refresh -> {
                    // The item is being refreshed:
                    // - Clear value
                    // - Set loadState to Loading
                    _state.value = UpdatingItem.ItemState(value = null, loadState = UpdatingItem.LoadState.Loading)
                }

                is UpdatingItem.Action.Clear -> {
                    // Clear the item:
                    // - value = null
                    // - loadState = Loaded (or considered as empty but stable)
                    _state.value = UpdatingItem.ItemState(value = null, loadState = UpdatingItem.LoadState.Loaded)
                }

                is UpdatingItem.Action.Update<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val typedValue = action.value as ItemValue
                    // Update the item's value:
                    // - Set loadState to Loaded
                    _state.value = UpdatingItem.ItemState(value = typedValue, loadState = UpdatingItem.LoadState.Loaded)
                }
            }
        }

        @Composable
        override operator fun invoke(): UpdatingItem.ItemState<ItemValue> = moleculeFlow.value

        override val stateFlow: StateFlow<UpdatingItem.ItemState<ItemValue>> = moleculeFlow

        @Composable
        private fun UpdatingItemPresenter(state: MutableStateFlow<UpdatingItem.ItemState<ItemValue>>): UpdatingItem.ItemState<ItemValue> {
            // By collecting the state in Compose, we get recompositions
            // only when the state changes for this particular item.
            val currentState = state.collectAsState()
            return currentState.value
        }
    }
}