package com.example.features.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.core.constants.AppIcons
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@Composable
fun ProductImageWidget(
    imagePath: String?,
    productName: String,
    size: Dp = 56.dp,
    testTag: String = "product_image_widget",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasLocalImage = !imagePath.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(Dimensions.radius12),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = modifier
            .size(size)
            .testTag(testTag)
    ) {
        if (hasLocalImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimensions.radius12))
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (productName.isNotBlank()) {
                    Text(
                        text = productName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    Icon(
                        imageVector = AppIcons.Inventory,
                        contentDescription = "Product",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .padding(Spacing.xs)
                            .size(size * 0.5f)
                    )
                }
            }
        }
    }
}
