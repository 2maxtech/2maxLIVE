package com.twomax.live.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.twomax.live.R
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*

data class NavItem(val screen: Screen, val label: String, val icon: String)

val navItems = listOf(
    NavItem(Screen.Home, "Home", "\uD83C\uDFE0"),
    NavItem(Screen.LiveTv, "Live TV", "\uD83D\uDCFA"),
    NavItem(Screen.Movies, "Movies", "\uD83C\uDFAC"),
    NavItem(Screen.Series, "TV Shows", "\uD83C\uDFAD"),
    NavItem(Screen.Epg, "EPG", "\uD83D\uDCC5"),
    NavItem(Screen.Favorites, "Favorites", "\u2B50"),
    NavItem(Screen.Settings, "Settings", "\u2699\uFE0F"),
    NavItem(Screen.Profiles, "Playlist", "\uD83D\uDCCB"),
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SideNav(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Surface)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // App logo/title
        Image(
            painter = painterResource(id = R.drawable.logo_2max),
            contentDescription = "2maX Live",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, bottom = 12.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(navItems) { item ->
                val isSelected = currentRoute == item.screen.route
                var isFocused by remember { mutableStateOf(false) }

                Surface(
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent,
                        focusedContainerColor = SurfaceElevated,
                        pressedContainerColor = SurfaceElevated
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, FocusBorder)
                        )
                    ),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.icon,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Primary else TextSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
