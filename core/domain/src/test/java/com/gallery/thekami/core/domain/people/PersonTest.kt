package com.gallery.thekami.core.domain.people

import com.gallery.thekami.core.domain.media.testMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonTest {
    @Test
    fun nameIsTrimmedLimitedAndBlankRejected() {
        assertEquals("Mom A", normalizePersonName("  Mom   A "))
        assertNull(normalizePersonName("   "))
        assertEquals(MAX_PERSON_NAME_LENGTH, normalizePersonName("x".repeat(200))?.length)
    }

    @Test
    fun noTypeMeansNoRelationship() {
        assertNull(makeRelationship(null, "anything"))
    }

    @Test
    fun customLabelIsOnlyKeptForSomeoneElse() {
        assertEquals(Relationship(RelationshipType.Friend), makeRelationship(RelationshipType.Friend, "ignored"))
        assertEquals(
            Relationship(RelationshipType.Other, "Neighbour"),
            makeRelationship(RelationshipType.Other, "  Neighbour "),
        )
        assertEquals(Relationship(RelationshipType.Other, null), makeRelationship(RelationshipType.Other, "  "))
    }

    @Test
    fun relationshipSurvivesEncodeAndDecode() {
        val all = RelationshipType.entries.filter { it != RelationshipType.Other }.map { Relationship(it) } +
            Relationship(RelationshipType.Other, null) +
            Relationship(RelationshipType.Other, "Pen | pal")
        all.forEach { assertEquals(it, Relationship.decode(it.encode())) }
        assertNull(Relationship.decode("Nope"))
        assertNull(Relationship.decode(null))
    }

    private val old = testMedia(1, taken = 100)
    private val newer = testMedia(2, taken = 300)
    private val library = listOf(old, newer).associateBy { it.key }

    private fun person(keys: Set<String>, cover: String? = null, rel: Relationship? = null) =
        Person("p", "Sam", rel, 0, cover, keys)

    @Test
    fun resolveSortsCountsMissingAndPicksCover() {
        val view = resolvePerson(person(setOf(old.key, newer.key, "content://gone/9")), library)
        assertEquals(listOf(2L, 1L), view.items.map { it.id })
        assertEquals(1, view.missingCount)
        assertEquals(newer, view.cover)
        assertEquals(old, resolvePerson(person(setOf(old.key, newer.key), cover = old.key), library).cover)
    }

    @Test
    fun mergeUnionsPhotosAndFillsBlanks() {
        val target = person(setOf("a"))
        val source = Person("q", "Samuel", Relationship(RelationshipType.Friend), 0, "b", setOf("b", "a"))
        val merged = mergePerson(target, source)
        assertEquals(setOf("a", "b"), merged.mediaKeys)
        assertEquals("Sam", merged.name)
        assertEquals(Relationship(RelationshipType.Friend), merged.relationship)
        assertEquals("b", merged.coverKey)
    }

    @Test
    fun mergeKeepsTargetRelationshipWhenSet() {
        val target = person(setOf("a"), rel = Relationship(RelationshipType.Sibling))
        val source = Person("q", "x", Relationship(RelationshipType.Friend), 0, null, emptySet())
        assertEquals(Relationship(RelationshipType.Sibling), mergePerson(target, source).relationship)
    }
}
