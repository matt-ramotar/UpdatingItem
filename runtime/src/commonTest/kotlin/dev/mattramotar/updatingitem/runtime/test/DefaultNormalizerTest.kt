@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.mattramotar.updatingitem.runtime.test

import app.cash.molecule.RecompositionMode
import app.cash.turbine.test
import dev.mattramotar.updatingitem.runtime.UpdatingItemAction
import dev.mattramotar.updatingitem.runtime.UpdatingItemLoadState
import dev.mattramotar.updatingitem.runtime.impl.DefaultNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class DefaultNormalizerTest {

    data class TestItem(val id: String, val content: String)

    private val idExtractor: (TestItem) -> String = { it.id }

    @Test
    fun testAddingNewItems() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        normalizer.updateItems(listOf(TestItem("1", "A"), TestItem("2", "B")))
        advanceUntilIdle()
        val items = normalizer.updatingItemsFlow.first()
        assertEquals(2, items.size)
        assertEquals("A", items[0].stateFlow.value.value?.content)
        assertEquals("B", items[1].stateFlow.value.value?.content)
    }

    @Test
    fun testUpdatingExistingItemValue() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        normalizer.updateItems(listOf(TestItem("1", "A")))
        advanceUntilIdle()
        val initialItems = normalizer.updatingItemsFlow.first()
        val item = initialItems.first()

        normalizer.updateItems(listOf(TestItem("1", "A-updated")))
        advanceUntilIdle()
        val updatedItems = normalizer.updatingItemsFlow.first()
        val sameItemRef = updatedItems.first()

        assertSame(item, sameItemRef)
        assertEquals("A-updated", sameItemRef.stateFlow.value.value?.content)
    }

    @Test
    fun testRemovingItems() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        normalizer.updateItems(listOf(TestItem("1", "A"), TestItem("2", "B")))
        advanceUntilIdle()

        normalizer.updateItems(listOf(TestItem("1", "A")))
        advanceUntilIdle()
        val updatedItems = normalizer.updatingItemsFlow.first()

        assertEquals(1, updatedItems.size)
        assertEquals("A", updatedItems[0].stateFlow.value.value?.content)
    }

    @Test
    fun testStableReferences() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        normalizer.updateItems(listOf(TestItem("1", "A"), TestItem("2", "B")))
        advanceUntilIdle()
        val initialItems = normalizer.updatingItemsFlow.first()

        val item1InitialRef = initialItems[0]
        val item2InitialRef = initialItems[1]

        normalizer.updateItems(listOf(TestItem("1", "A-updated"), TestItem("2", "B")))
        advanceUntilIdle()
        val updatedItems = normalizer.updatingItemsFlow.first()

        val item1UpdatedRef = updatedItems[0]
        val item2UpdatedRef = updatedItems[1]

        assertSame(item1InitialRef, item1UpdatedRef)
        assertEquals("A-updated", item1UpdatedRef.stateFlow.value.value?.content)

        assertSame(item2InitialRef, item2UpdatedRef)
        assertEquals("B", item2UpdatedRef.stateFlow.value.value?.content)
    }

    @Test
    fun testGetUpdatingItemUnknownId() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        val newItem = normalizer.getUpdatingItem("non-existent")
        assertNull(newItem.stateFlow.value.value)

        normalizer.updateItems(listOf(TestItem("non-existent", "Now here")))
        advanceUntilIdle()
        assertEquals("Now here", newItem.stateFlow.value.value?.content)
    }

    @Test
    fun testDispatchActionsOnItem() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val normalizer = DefaultNormalizer(scope, idExtractor, RecompositionMode.Immediate)

        val item = TestItem("1", "A")
        normalizer.updateItems(listOf(item))
        val updatingItem = normalizer.getUpdatingItem("1")

        updatingItem.stateFlow.test {

            val actual0 = awaitItem()
            assertEquals(UpdatingItemLoadState.Initial, actual0.loadState)
            assertEquals(null, actual0.value)

            val actual1 = awaitItem()
            assertEquals(UpdatingItemLoadState.Loaded, actual1.loadState)
            assertEquals("A", actual1.value?.content)

            updatingItem.dispatch(UpdatingItemAction.Refresh)
            val actual2 = awaitItem()
            assertNull(actual2.value)
            assertEquals(UpdatingItemLoadState.Loading, actual2.loadState)

            val action = UpdatingItemAction.Update(item.copy(content = "A2"))
            updatingItem.dispatch(action)
            val actual3 = awaitItem()
            assertEquals("A2", actual3.value?.content)
            assertEquals(UpdatingItemLoadState.Loaded, actual3.loadState)

            updatingItem.dispatch(UpdatingItemAction.Clear)
            val actual4 = awaitItem()
            assertNull(actual4.value)
            assertEquals(UpdatingItemLoadState.Loaded, actual4.loadState)
        }
    }
}
