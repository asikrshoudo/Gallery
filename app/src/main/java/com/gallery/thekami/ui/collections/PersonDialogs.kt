@file:OptIn(ExperimentalLayoutApi::class)

package com.gallery.thekami.ui.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gallery.thekami.R
import com.gallery.thekami.core.domain.people.MAX_PERSON_NAME_LENGTH
import com.gallery.thekami.core.domain.people.MAX_RELATIONSHIP_LABEL_LENGTH
import com.gallery.thekami.core.domain.people.Relationship
import com.gallery.thekami.core.domain.people.RelationshipType
import com.gallery.thekami.core.domain.people.makeRelationship
import com.gallery.thekami.core.domain.people.normalizePersonName
import com.gallery.thekami.core.ui.theme.GalleryColors

@Composable
fun relationshipTypeLabel(type: RelationshipType): String = stringResource(
    when (type) {
        RelationshipType.GirlfriendBoyfriend -> R.string.rel_girlfriend_boyfriend
        RelationshipType.PartnerSpouse -> R.string.rel_partner_spouse
        RelationshipType.Parent -> R.string.rel_parent
        RelationshipType.Grandparent -> R.string.rel_grandparent
        RelationshipType.Sibling -> R.string.rel_sibling
        RelationshipType.Child -> R.string.rel_child
        RelationshipType.Relative -> R.string.rel_relative
        RelationshipType.Friend -> R.string.rel_friend
        RelationshipType.BestFriend -> R.string.rel_best_friend
        RelationshipType.Colleague -> R.string.rel_colleague
        RelationshipType.Other -> R.string.rel_other
    },
)

/** The text to show for a chosen relationship (a custom label wins for "someone else"). */
@Composable
fun relationshipLabel(relationship: Relationship): String =
    relationship.customLabel ?: relationshipTypeLabel(relationship.type)

/**
 * Name plus an OPTIONAL relationship. The relationship is exactly what the user picks here;
 * it is never guessed from a face, a photo or a contact.
 */
@Composable
fun PersonDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    initialRelationship: Relationship?,
    onConfirm: (name: String, relationship: Relationship?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialRelationship?.type) }
    var custom by remember { mutableStateOf(initialRelationship?.customLabel.orEmpty()) }
    val normalized = normalizePersonName(name)
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = GalleryColors.Lime,
        selectedLabelColor = GalleryColors.OnLime,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(MAX_PERSON_NAME_LENGTH) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.person_name_label)) },
                )
                Text(
                    stringResource(R.string.person_relationship_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == null,
                        onClick = { type = null },
                        label = { Text(stringResource(R.string.person_relationship_skip)) },
                        colors = chipColors,
                    )
                    RelationshipType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(relationshipTypeLabel(option)) },
                            colors = chipColors,
                        )
                    }
                }
                if (type == RelationshipType.Other) {
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it.take(MAX_RELATIONSHIP_LABEL_LENGTH) },
                        singleLine = true,
                        label = { Text(stringResource(R.string.person_custom_label)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = normalized != null,
                onClick = { normalized?.let { onConfirm(it, makeRelationship(type, custom)) } },
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}
