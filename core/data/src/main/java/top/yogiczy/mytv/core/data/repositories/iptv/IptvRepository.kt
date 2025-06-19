package top.yogiczy.mytv.core.data.repositories.iptv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList
import top.yogiczy.mytv.core.data.entities.iptvsource.IptvSource
import top.yogiczy.mytv.core.data.network.HttpException
import top.yogiczy.mytv.core.data.network.request
import top.yogiczy.mytv.core.data.repositories.FileCacheRepository
import top.yogiczy.mytv.core.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.core.data.repositories.iptv.parser.IptvParser.ChannelItem.Companion.toChannelGroupList
import top.yogiczy.mytv.core.data.utils.Globals
import top.yogiczy.mytv.core.data.utils.Logger
import kotlin.time.measureTimedValue
import top.yogiczy.mytv.core.data.utils.JSEngine
/**
 * 订阅源数据获取
 */
class IptvRepository(private val source: IptvSource) :
    FileCacheRepository(source.cacheFileName("json")) {

    private val log = Logger.create("IptvRepository")
    private val rawRepository = IptvRawRepository(source)

    private fun isExpired(lastModified: Long, cacheTime: Long): Boolean {
        val timeout =
            System.currentTimeMillis() - lastModified >= (if (source.sourceType == 1) Long.MAX_VALUE else cacheTime)
        val rawChanged = lastModified < rawRepository.lastModified()

        return timeout || rawChanged
    }

    private suspend fun refresh(
        transform: suspend ((List<IptvParser.ChannelItem>) -> List<IptvParser.ChannelItem>) = { it -> it },
    ): String {
        val raw = rawRepository.getRaw()
        val parser = IptvParser.instances.first { it.isSupport(source.url, raw) }

        log.d("开始解析订阅源（${source.name}）...")
        return measureTimedValue {
            val list = parser.parse(raw)
            val processedList = list.map { item ->
                if (item.httpUserAgent.isNullOrBlank() && !source.httpUserAgent.isNullOrBlank()) {
                    item.copy(httpUserAgent = source.httpUserAgent)
                } else {
                    item
                }
            }
            Globals.json.encodeToString(withContext(Dispatchers.Default) {
                runCatching { transform(processedList) }
                    .getOrDefault(processedList)
                    .toChannelGroupList()
            })
        }.let {
            log.i("解析订阅源（${source.name}）完成", null, it.duration)
            it.value
        }
    }

    private suspend fun transform(channelList: List<IptvParser.ChannelItem>): List<IptvParser.ChannelItem> {
        if (source.transformJs.isNullOrBlank()) return channelList
        val scriptString = """
                (function() {
                    var channelList = ${Globals.json.encodeToString(channelList)};
                    ${source.transformJs}
                    return JSON.stringify(main(channelList));
                })();
                """.trimIndent()

        val result = runCatching {
            JSEngine().executeJSString(scriptString) as String
        }
        return try {
            Globals.json.decodeFromString(result.getOrNull()!!)
        } catch (e: Exception) {
            log.e("解析转换结果异常: ${e.message}", e)
            channelList
        }
        }

    /**
     * 获取订阅源分组列表
     */
    suspend fun getChannelGroupList(cacheTime: Long): ChannelGroupList {
        try {
            val json = getOrRefresh({ lastModified, _ -> isExpired(lastModified, cacheTime) }) {
                refresh { transform(it) }
            }

            return Globals.json.decodeFromString<ChannelGroupList>(json).also { groupList ->
                log.i("加载订阅源（${source.name}）：${groupList.size}个分组，${groupList.sumOf { it.channelList.size }}个频道")
            }
        } catch (ex: Exception) {
            log.e("加载订阅源（${source.name}）失败", ex)
            throw ex
        }
    }

    suspend fun getEpgUrl(): String? {
        return runCatching {
            val sourceData = rawRepository.getRaw(Long.MAX_VALUE)
            val parser = IptvParser.instances.first { it.isSupport(source.url, sourceData) }
            parser.getEpgUrl(sourceData)
        }.getOrNull()
    }

    override suspend fun clearCache() {
        rawRepository.clearCache()
        super.clearCache()
    }

    companion object {
        suspend fun clearAllCache() = withContext(Dispatchers.IO) {
            IptvSource.cacheDir.deleteRecursively()
        }
    }
}

private class IptvRawRepository(private val source: IptvSource) : FileCacheRepository(
    if (source.sourceType == 1) source.url else source.cacheFileName("txt"),
    source.sourceType == 1,
) {

    private val log = Logger.create("IptvRawRepository")

    suspend fun getRaw(cacheTime: Long = 0): String {
        return getOrRefresh(if (source.sourceType == 1) Long.MAX_VALUE else cacheTime) {

            val url = when (source.sourceType) {
                0 -> source.url
                1 -> source.url // 本地文件直接使用url
                2 -> getXtreamUrl(source)
                else -> throw IllegalArgumentException("不支持的订阅源类型: ${source.sourceType}")
            }

            log.d("获取订阅源: $source")

            try {
                url.request({ builder ->
                    source.httpUserAgent?.let { builder.addHeader("User-Agent", it) }
                    builder
                }) { body -> body.string() } ?: ""
            } catch (ex: Exception) {
                log.e("获取订阅源（${source.name}）失败", ex)
                throw HttpException("获取订阅源失败，请检查网络连接", ex)
            }
        }
    }

    override suspend fun clearCache() {
        if (source.sourceType == 1) return
        super.clearCache()
    }
}

private fun getXtreamUrl(source: IptvSource): String {
    return "${source.url}/get.php?username=${source.userName}&password=${source.password}" + if (source.format.isNullOrBlank()) "" else "&type=${source.format}"
        .also { Logger.create("IptvRepository").d("获取xtream订阅源URL: $it") }
}