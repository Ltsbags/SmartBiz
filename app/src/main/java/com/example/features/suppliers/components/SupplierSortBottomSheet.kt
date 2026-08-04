package com.example.features.suppliers.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.features.suppliers.SupplierSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierSortBottomSheet(
    currentSort: SupplierSortOption,
    onDismiss: () -> Unit,
    onSortSelected: (SupplierSortOption) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Sort Suppliers By",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val sortOptions = listOf(
                SupplierSortOption.NAME_ASC to "Name (A to Z)",
                SupplierSortOption.NAME_DESC to "Name (Z to A)",
                SupplierSortOption.OUTSTANDING_HIGH to "Highest Outstanding Balance",
                SupplierSortOption.OUTSTANDING_LOW to "Lowest Outstanding Balance",
                SupplierSortOption.RECENT to "Recently Updated"
            )

            sortOptions.forEach { (option, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortSelected(option) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSort == option,
                        onClick = { onSortSelected(option) }
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 12.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
