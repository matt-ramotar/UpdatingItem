package dev.mattramotar.updatingitem.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow


/**
 * Represents a single stable, stateful item. It observes changes to its associated data
 * and load state, exposing them via a composable function that can trigger localized recompositions.
 *
 * @param ItemId A stable, unique identifier type for the item.
 * @param ItemValue The type of the underlying data/value associated with this item.
 */
@Stable
interface UpdatingItem<ItemId : Any, ItemValue : Any> {

    /**
     * A composable function that returns the current state of this item.
     * Calling this function in a composition sets up a reactive relationship:
     * whenever the item's state changes, this composable will recompose.
     *
     * @return The current [ItemState] describing this item's value and load state.
     */
    @Composable
    operator fun invoke(): ItemState<ItemValue>

    val stateFlow: StateFlow<ItemState<ItemValue>>

    /**
     * Dispatches an action to this item. Actions can:
     * - Refresh the item from its source.
     * - Update the item's value.
     * - Clear or reset its state.
     *
     * This function is typically called from event handlers or business logic in a unidirectional
     * data flow (UDF) architecture.
     *
     * @param action The [Action] to apply to this item.
     */
    suspend fun dispatch(action: Action<ItemValue>)

    /**
     * Represents the current state of an item, containing its value (if available) and load state.
     *
     * @property value The current item data, or null if not yet loaded or cleared.
     * @property loadState The current load state of the item.
     */
    class ItemState<ItemValue : Any>(
        val value: ItemValue?,
        val loadState: LoadState
    )

    /**
     * Actions that can be dispatched to an item to alter its state or request changes.
     */
    sealed interface Action<out ItemValue : Any> {
        /**
         * Requests a refresh of the item from its source.
         */
        data object Refresh : Action<Nothing>

        /**
         * Clears the item from memory or resets its state.
         */
        data object Clear : Action<Nothing>

        /**
         * Updates the item's value directly.
         *
         * @property value The new item value.
         */
        data class Update<ItemValue : Any>(val value: ItemValue) :
            Action<ItemValue>
    }

    /**
     * Represents the load state of a single item.
     */
    sealed interface LoadState {
        /**
         * The initial state.
         */
        data object Initial : LoadState

        /**
         * The item is fully loaded and available.
         */
        data object Loaded : LoadState

        /**
         * The item is currently loading or refreshing.
         */
        data object Loading : LoadState

        /**
         * The item failed to load or update due to the given [throwable].
         *
         * @property throwable The error encountered while loading.
         */
        data class Error(val throwable: Throwable) : LoadState
    }
}


