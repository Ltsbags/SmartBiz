package com.example.shared.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core.constants.AppIcons

@Composable
fun SmartBizDatePickerField(
    label: String,
    selectedDateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "date_picker_field"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        SmartBizTextField(
            value = selectedDateText,
            onValueChange = {},
            label = label,
            placeholder = "Select date",
            leadingIcon = AppIcons.Calendar,
            singleLine = true,
            testTag = testTag
        )
    }
}
