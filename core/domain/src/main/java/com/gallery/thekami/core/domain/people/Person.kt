package com.gallery.thekami.core.domain.people

import com.gallery.thekami.core.domain.media.MediaItem
import com.gallery.thekami.core.domain.media.sortMedia
import com.gallery.thekami.core.domain.text.normalizeLabel

const val MAX_PERSON_NAME_LENGTH = 40
const val MAX_RELATIONSHIP_LABEL_LENGTH = 30

/** Predefined relationships. A relationship is only ever chosen by the user, never inferred. */
enum class RelationshipType {
    GirlfriendBoyfriend, PartnerSpouse, Parent, Grandparent, Sibling, Child,
    Relative, Friend, BestFriend, Colleague, Other,
}

/** [customLabel] is only used with [RelationshipType.Other]. */
data class Relationship(val type: RelationshipType, val customLabel: String? = null) {
    fun encode(): String = if (customLabel != null) "${type.name}|$customLabel" else type.name

    companion object {
        fun decode(value: String?): Relationship? {
            if (value == null) return null
            val type = RelationshipType.entries.firstOrNull { it.name == value.substringBefore('|') } ?: return null
            val label = value.substringAfter('|', "").ifEmpty { null }
            return Relationship(type, if (type == RelationshipType.Other) label else null)
        }
    }
}

fun normalizePersonName(raw: String): String? = normalizeLabel(raw, MAX_PERSON_NAME_LENGTH)

/** Null type = no relationship chosen. A custom label is kept only for "someone else". */
fun makeRelationship(type: RelationshipType?, rawCustomLabel: String?): Relationship? = when (type) {
    null -> null
    RelationshipType.Other ->
        Relationship(type, rawCustomLabel?.let { normalizeLabel(it, MAX_RELATIONSHIP_LABEL_LENGTH) })
    else -> Relationship(type)
}

/**
 * A person the user created. Photos are linked by the user (media keys), never by guessing a
 * face. Nothing here touches the media files.
 */
data class Person(
    val id: String,
    val name: String,
    val relationship: Relationship?,
    val createdAtMillis: Long,
    val coverKey: String?,
    val mediaKeys: Set<String>,
)

data class PersonView(
    val person: Person,
    val items: List<MediaItem>,
    val cover: MediaItem?,
    val missingCount: Int,
) {
    val id: String get() = person.id
    val name: String get() = person.name
}

fun resolvePerson(person: Person, byKey: Map<String, MediaItem>): PersonView {
    val items = sortMedia(person.mediaKeys.mapNotNull { byKey[it] })
    val chosen = person.coverKey?.takeIf { it in person.mediaKeys }?.let { byKey[it] }
    return PersonView(person, items, chosen ?: items.firstOrNull(), person.mediaKeys.size - items.size)
}

/** [source] is folded into [target]. The target keeps its name; blanks are filled from the source. */
fun mergePerson(target: Person, source: Person): Person = target.copy(
    mediaKeys = target.mediaKeys + source.mediaKeys,
    relationship = target.relationship ?: source.relationship,
    coverKey = target.coverKey ?: source.coverKey,
)
