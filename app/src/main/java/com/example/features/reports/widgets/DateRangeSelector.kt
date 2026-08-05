package com.example.features.reports.widgets

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.reports.models.DateFilterOption
import java.util.Calendar

@Composable
fun DateRangeSelector(
    selectedOption: DateFilterOption,
    onOptionSelected: (DateFilterOption, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateFilterOption.values().forEach { option ->
            val isSelected = selectedOption == option
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (option == DateFilterOption.CUSTOM) {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val startCal = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth, 0, 0, 0)
                                }
                                DatePickerDialog(
                                    context,
                                    { _, endYear, endMonth, endDay ->
                                        val endCal = Calendar.getInstance().apply {
                                            set(endYear, endMonth, endDay, 23, 59, 59)
                                        }
                                        onOptionSelected(
                                            DateFilterOption.CUSTOM,
                                            startCal.timeInMillis,
                                            endCal.timeInMillis
                                        )
                                    },
                                    year, month, dayOfMonth
                                ).show()
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    } else {
                        onOptionSelected(option, null, null)
                    }
                },
                label = { Text(text = option.displayName) },
                leadingIcon = if (option == DateFilterOption.CUSTOM) {
                    { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("chip_date_${option.name.lowercase()}")
            )
        }
    }
}
