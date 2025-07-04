package top.yogiczy.mytv.tv.ui.screen.settings.subcategories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screen.components.AppScreen
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.utils.Configs
import top.yogiczy.mytv.tv.ui.utils.handleKeyEvents
import top.yogiczy.mytv.tv.R
import androidx.compose.ui.res.stringResource
@Composable
fun SettingsWebViewCoreScreen(
    modifier: Modifier = Modifier,
    coreProvider: () -> Configs.WebViewCore = { Configs.WebViewCore.SYSTEM },
    onCoreChanged: (Configs.WebViewCore) -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()

    AppScreen(
        modifier = modifier.padding(top = 10.dp),
        header = { Text("${stringResource(R.string.ui_dashboard_module_settings)} / ${stringResource(R.string.ui_channel_view_player)} / ${stringResource(R.string.ui_player_view_webview_core)}") },
        canBack = true,
        onBackPressed = onBackPressed,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = childPadding.copy(top = 10.dp).paddingValues,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(Configs.WebViewCore.entries) { core ->
                ListItem(
                    modifier = Modifier.handleKeyEvents(
                        onSelect = { onCoreChanged(core) },
                    ),
                    headlineContent = { Text(core.label) },
                    supportingContent = {
                        Text(
                            when (core) {
                                Configs.WebViewCore.SYSTEM -> stringResource(R.string.ui_video_player_webview_core_system_desc)
                                Configs.WebViewCore.X5 -> stringResource(R.string.ui_video_player_webview_core_x5_desc)
                            }
                        )
                    },
                    trailingContent = {
                        if (coreProvider() == core) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(0.1f),
                    ),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun SettingsWebViewCoreScreenPreview() {
    MyTvTheme {
        SettingsWebViewCoreScreen()
    }
}