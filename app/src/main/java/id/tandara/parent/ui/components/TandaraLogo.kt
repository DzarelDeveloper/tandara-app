package id.tandara.parent.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.tandara.parent.R

/**
 * Canonical Tandara product mark. Gesture handling remains with each caller.
 */
@Composable
fun TandaraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    Image(
        painter = painterResource(R.drawable.ic_tandara_logo),
        contentDescription = "Logo Tandara",
        modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.22f)),
        contentScale = ContentScale.Fit
    )
}
