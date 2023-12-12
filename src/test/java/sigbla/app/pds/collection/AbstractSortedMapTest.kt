/*
 * Copyright (c) 2014 Andrew O'Malley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package sigbla.app.pds.collection

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class AbstractSortedMapTest() : AbstractMapTest() {
    override abstract fun <K, V> mapFactory(comparator: Comparator<in K>?): BuilderFactory<Pair<K, V>, out SortedMap<K, V>>

    private fun <K, V> buildMap(vararg entries: kotlin.Pair<K, V>) = buildMap_(*entries) as SortedMap<K, V>

    @Test fun sortedForEach() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        val actual = arrayListOf<Int>()
        map.forEach { actual.add(it.component1()!!) }
        assertEquals(listOf(1, 2, 3, 4, 5), actual)
    }

    @Test fun range() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.range(0, false, 4, false))
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.range(1, true, 3, true))
        assertEquals(buildMap(2 to "B", 3 to "C"), map.range(1, false, 3, true))
        assertEquals(buildMap(1 to "A", 2 to "B"), map.range(1, true, 3, false))
        assertEquals(buildMap(2 to "B"), map.range(1, false, 3, false))
        assertEquals(buildMap(2 to "B"), map.range(2, true, 2, true))
        assertEquals(buildMap(1 to "A"), map.range(1, true, 1, true))
        assertEquals(buildMap<Int, String>(), map.range(4, true, 1, true))
    }

    @Test fun from() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.from(0, false))
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.from(1, true))
        assertEquals(buildMap(2 to "B", 3 to "C"), map.from(1, false))
        assertEquals(buildMap(2 to "B", 3 to "C"), map.from(2, true))
        assertEquals(buildMap(3 to "C"), map.from(3, true))
        assertEquals(buildMap<Int, String>(), map.from(3, false))
    }

    @Test fun until() {
        val map = buildMap(1 to "A", 2 to "B", 3 to "C")
        assertEquals(buildMap<Int, String>(), map.to(0, false))
        assertEquals(buildMap(1 to "A"), map.to(1, true))
        assertEquals(buildMap(1 to "A"), map.to(2, false))
        assertEquals(buildMap(1 to "A", 2 to "B"), map.to(2, true))
        assertEquals(buildMap(1 to "A", 2 to "B"), map.to(3, false))
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.to(3, true))
        assertEquals(buildMap(1 to "A", 2 to "B", 3 to "C"), map.to(4, false))
    }

    @Test fun firstSortedMap() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        assertEquals(Pair(1, "A"), map.first())
    }

    @Test fun firstSortedMapEmpty() {
        assertNull(buildMap<Int, Int>().first())
    }

    @Test fun lastSortedMap() {
        val map = buildMap(1 to "A", 3 to "C", 2 to "B", 5 to "E", 4 to "D")
        assertEquals(Pair(5, "E"), map.last())
    }

    @Test fun lastSortedMapEmpty() {
        assertNull(buildMap<Int, Int>().last())
    }

    @Test fun sortedWithCustomComparator() {
        val c = Comparator<Int> { o1, o2 -> o1.compareTo(o2) * -1 }
        val builder = mapFactory<Int, Int>(c).newBuilder()
        builder.add(Pair(2, 20))
        builder.add(Pair(1, 10))
        builder.add(Pair(3, 30))
        builder.add(Pair(7, 70))
        builder.add(Pair(4, 40))

        val map = builder.build()
        val actual = arrayListOf<Int>()

        for (pair in map) {
            actual.add(pair.component1()!!)
        }

        assertEquals(listOf(7, 4, 3, 2, 1), actual)
        assertEquals(c, map.comparator() as Comparator<Int>)
    }

    @Test fun compartorIsNullWhenNotSupplied() {
        val map = buildMap<Int, Int>()
        assertNull(map.comparator())
    }

    @Test fun take() {
        assertSequence(sequence(10).take(5), 0, 5)
    }

    @Test fun takeNone() {
        val map = sequence(10)
        assertSequence(map.take(0), 0, 0)
        assertSequence(map.take(-1), 0, 0)
    }

    @Test fun takeLoop() {
        val max = 100
        val map = sequence(max)
        for (i in 1..max) {
            assertSequence(map.take(i), 0, i)
        }
    }

    @Test fun dropLoop() {
        val max = 100
        val map = sequence(max)
        for (i in 1..max) {
            assertSequence(map.drop(i), i, max - i)
        }
    }

    @Test fun takeAll() {
        assertSequence(sequence(10).take(100), 0, 10)
    }

    @Test fun drop() {
        assertSequence(sequence(10).drop(5), 5, 5)
    }

    @Test fun dropNone() {
        val map = sequence(10)
        assertSequence(map.drop(0), 0, 10)
        assertSequence(map.drop(-1), 0, 10)
    }

    @Test fun dropAll() {
        assertSequence(sequence(10).drop(100), 0, 0)
    }

    @Test fun asSortedMap() {
        assertEquals(sortedMapOf(1 to "A", 2 to "B"), buildMap(2 to "B", 1 to "A").asSortedMap())
    }

    fun sequence(size: Int): SortedMap<Int, Int> {
        val builder = mapFactory<Int, Int>().newBuilder()
        for (i in 0..size - 1) {
            builder.add(Pair(i, i))
        }
        return builder.build()
    }

    fun assertSequence(map: SortedMap<Int, Int>, from: Int, length: Int) {
        var from_ = from
        assertEquals(length, map.size())
        for (pair in map) {
            assertEquals(from_++, pair.component1())
        }
    }
}
