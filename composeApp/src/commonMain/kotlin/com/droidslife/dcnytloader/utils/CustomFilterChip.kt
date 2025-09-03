package com.droidslife.dcnytloader.utils

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFilterChip(
    defaultOption: Int,
    list: List<String>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isTextField: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember {
        mutableStateOf(
            if (list.isNotEmpty() && defaultOption >= 0 && defaultOption < list.size) {
                list[defaultOption]
            } else {
                ""
            },
        )
    }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
    ) {
        if (!isTextField) {
            ElevatedFilterChip(
                modifier = Modifier.menuAnchor(),
                selected = true,
                onClick = { },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                    )
                },
                label = {
                    Text(
                        text = selectedOptionText,
                    )
                },
                colors =
                    FilterChipDefaults.elevatedFilterChipColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        } else {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedOptionText,
                onValueChange = { },
                label = { Text("select Format") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            list.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = selectionOption,
                        )
                    },
                    onClick = {
                        selectedOptionText = selectionOption
                        onClick(list.indexOf(selectedOptionText))
                        expanded = false
                    },
                )
            }
        }
    }
}
