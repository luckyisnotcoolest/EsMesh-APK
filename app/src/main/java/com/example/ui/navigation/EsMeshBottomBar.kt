package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EsMeshBlack
import com.example.ui.theme.EsMeshBorder
import com.example.ui.theme.EsMeshRed
import com.example.ui.theme.EsMeshTextMuted
import com.example.ui.theme.EsMeshTextPrimary
import com.example.ui.theme.EsMeshYellow

@Composable
fun EsMeshBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color(0xFF0A0A0E),
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, EsMeshBorder)
            .navigationBarsPadding()
            .height(64.dp)
            .testTag("esmesh_bottom_nav_bar")
    ) {
        EsMeshDestination.bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(20.dp),
                        tint = if (selected) EsMeshRed else EsMeshTextMuted
                    )
                },
                label = {
                    Text(
                        text = item.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) EsMeshYellow else EsMeshTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF1E0A0C),
                    selectedIconColor = EsMeshRed,
                    unselectedIconColor = EsMeshTextMuted,
                    selectedTextColor = EsMeshYellow,
                    unselectedTextColor = EsMeshTextMuted
                )
            )
        }
    }
}
