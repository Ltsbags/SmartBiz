package com.example.shared.buttons

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.core.theme.Dimensions

@Composable
fun FloatingActionButtonWidget(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "fab_button"
) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(Dimensions.radius16),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Dimensions.fabElevation),
        modifier = modifier.testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}
