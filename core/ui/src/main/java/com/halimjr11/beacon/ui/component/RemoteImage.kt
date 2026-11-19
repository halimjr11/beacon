package com.halimjr11.beacon.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.halimjr11.beacon.ui.R
import com.halimjr11.beacon.ui.theme.BeaconIcons

/** Image with a placeholder for a blank url (AC 3.6) or a failed load. Callers set the size. */
@Composable
fun RemoteImage(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val description = stringResource(R.string.image_placeholder)
    // The tint stays visible while the image loads, when the url is blank, and if loading fails.
    Box(modifier.background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        if (url.isBlank()) {
            Icon(BeaconIcons.BrokenImage, contentDescription = description)
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                error = rememberVectorPainter(BeaconIcons.BrokenImage),
            )
        }
    }
}
