package com.example.shared.forms

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.core.constants.AppIcons

@Composable
fun SmartBizNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "0",
    testTag: String = "number_field",
    modifier: Modifier = Modifier
) {
    SmartBizTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        label = label,
        placeholder = placeholder,
        leadingIcon = AppIcons.Number,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun SmartBizPriceField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Price",
    currencySymbol: String = "$",
    placeholder: String = "0.00",
    testTag: String = "price_field",
    modifier: Modifier = Modifier
) {
    SmartBizTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = AppIcons.Currency,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun SmartBizGSTField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Tax Rate / GST (%)",
    placeholder: String = "10.0",
    testTag: String = "gst_field",
    modifier: Modifier = Modifier
) {
    SmartBizTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = AppIcons.TaxGST,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun SmartBizPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Phone Number",
    placeholder: String = "+1 (555) 000-0000",
    testTag: String = "phone_field",
    modifier: Modifier = Modifier
) {
    SmartBizTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = AppIcons.Phone,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun SmartBizNotesField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Notes & Terms",
    placeholder: String = "Enter terms or remarks...",
    testTag: String = "notes_field",
    modifier: Modifier = Modifier
) {
    SmartBizTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        leadingIcon = AppIcons.Notes,
        singleLine = false,
        maxLines = 4,
        testTag = testTag,
        modifier = modifier
    )
}
