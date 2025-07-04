package top.yogiczy.mytv.tv.ui.screen.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screen.components.AppScreen
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.tooling.PreviewWithLayoutGrids
import top.yogiczy.mytv.tv.ui.utils.focusOnLaunched
import top.yogiczy.mytv.tv.ui.utils.gridColumns
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents
import top.yogiczy.mytv.tv.ui.utils.ifElse
import top.yogiczy.mytv.tv.ui.utils.saveFocusRestorer
import top.yogiczy.mytv.tv.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsCategoriesScreen(
    modifier: Modifier = Modifier,
    toSettingsCategoryScreen: (category: SettingsCategories) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()

    AppScreen(
        modifier = modifier
            .padding(top = 10.dp)
            .focusOnLaunched(),
        header = { Text(stringResource(R.string.ui_dashboard_module_settings)) },
        canBack = true,
        onBackPressed = onBackPressed,
    ) {
        val firstItemFocusRequester = remember { FocusRequester() }

        LazyVerticalGrid(
            modifier = Modifier.ifElse(
                settingsVM.uiFocusOptimize,
                Modifier.saveFocusRestorer { firstItemFocusRequester },
            ),
            columns = GridCells.Fixed(6),
            contentPadding = childPadding.copy(top = 10.dp).paddingValues,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(SettingsCategories.entries) {
                SettingsCategoryItem(
                    modifier = Modifier.ifElse(
                        it == SettingsCategories.entries.first(),
                        Modifier.focusRequester(firstItemFocusRequester)
                    ),
                    title = it.title,
                    imageVector = it.icon,
                    onSelected = { toSettingsCategoryScreen(it) },
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    modifier: Modifier = Modifier,
    title: Int,
    imageVector: ImageVector,
    onSelected: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .size(2.gridColumns())
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .handleKeyEvents(onSelect = onSelected),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onSurface.copy(0.1f),
        ),
        onClick = {},
    ) {
        if (title == SettingsCategories.THEME.title && isFocused) {
            SettingsCategoryItemThemeBackground()
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(imageVector, contentDescription = null, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(10.dp))
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SettingsCategoryItemThemeBackground() {
    val colors = listOf(
        Color(0xFFE8FBFC),
        Color(0xFFB3EAFD),
        Color(0xFFBED6FF),
        Color(0xFFD7CFFE),
        Color(0xFFE8CEF2),
        Color(0xFFFBCFDB),
        Color(0xFFFFECC0),
    )

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "",
    )

    var size by remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(
                        -animatedOffset.value * size.width / 2,
                        -animatedOffset.value * size.height / 2
                    ),
                    end = Offset(
                        -animatedOffset.value * size.width + size.width * 2,
                        -animatedOffset.value * size.height + size.height * 2
                    ),
                )
            )
    )
}

enum class SettingsCategories(
    val icon: ImageVector,
    val title: Int,
) {
    APP(Icons.Outlined.Devices, R.string.ui_channel_view_general),
    IPTV(Icons.Outlined.LiveTv, R.string.ui_channel_view_source),
    EPG(Icons.AutoMirrored.Outlined.LibraryBooks, R.string.ui_channel_view_epg),
    UI(Icons.Outlined.DisplaySettings, R.string.ui_channel_view_interface),
    THEME(Icons.Outlined.ColorLens, R.string.ui_channel_view_theme),
    CONTROL(Icons.Outlined.ControlCamera, R.string.ui_channel_view_control),
    VIDEO_PLAYER(Icons.Outlined.SmartDisplay, R.string.ui_channel_view_player),
    UPDATE(Icons.Outlined.Update, R.string.ui_channel_view_update),
    // FAVORITE(Icons.Outlined.FavoriteBorder, "收藏"),
    // EPG_RESERVE(Icons.Default.BookmarkBorder, "预约"),
    NETWORK(Icons.Outlined.Wifi, R.string.ui_channel_view_network),
    CLOUD_SYNC(Icons.Outlined.CloudSync, R.string.ui_channel_view_cloud_sync),
    PERMISSIONS(Icons.Outlined.Shield, R.string.ui_channel_view_permissions),
    DEBUG(Icons.Outlined.BugReport, R.string.ui_channel_view_debug),
    LOG(Icons.AutoMirrored.Outlined.FormatListBulleted, R.string.ui_channel_view_log),
}

enum class SettingsSubCategories {
    IPTV_SOURCE,
    IPTV_SOURCE_CACHE_TIME,
    CHANNEL_GROUP_VISIBILITY,
    IPTV_HYBRID_MODE,
    EPG_SOURCE,
    EPG_REFRESH_TIME_THRESHOLD,
    UI_CONTROL_ACTION,
    UI_TIME_SHOW_MODE,
    UI_SCREEN_AUTO_CLOSE_DELAY,
    UI_DENSITY_SCALE_RATIO,
    UI_FONT_SCALE_RATIO,
    UI_VIDEO_PLAYER_SUBTITLE,
    VIDEO_PLAYER_CORE,
    WEBVIEW_CORE,
    VIDEO_PLAYER_RENDER_MODE,
    VIDEO_PLAYER_DISPLAY_MODE,
    VIDEO_PLAYER_LOAD_TIMEOUT,
    VIDEO_PLAYER_BUFFER_TIME,
    UPDATE_CHANNEL,
    CLOUD_SYNC_PROVIDER,
    UI_SETTINGS_DECODER_INFO,
    NETWORK_RETRY_COUNT,
    NETWORK_RETRY_INTERVAL
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun SettingsCategoriesScreenPreview() {
    MyTvTheme {
        SettingsCategoriesScreen()
        PreviewWithLayoutGrids { }
    }
}

@Preview
@Composable
private fun SettingsCategoryItemThemePreview() {
    MyTvTheme {
        SettingsCategoryItem(
            modifier = Modifier
                .focusOnLaunched()
                .padding(20.dp),
            title = SettingsCategories.THEME.title,
            imageVector = SettingsCategories.THEME.icon,
        )
    }
}