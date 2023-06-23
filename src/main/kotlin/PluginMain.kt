package org.example.mirai.QQPriceBot

import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.info
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import org.json.JSONArray
import org.json.JSONObject


/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

// 机器人基本信息
object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.QQPriceBot",
        name = "QQ报价机器人",
        version = "0.1.0"
    ) {
        author("涛行")
        info(
            """
            这是一个Mirai插件, 
            用于QQ群中报价.
        """.trimIndent()
        )
        // author 和 info 可以删除.
    }
) {

    // 获取币价函数
    fun getPrice(coin: String): String {
        var proxyHost = "127.0.0.1"
        var proxyPort = 7890
        // var proxySelector = ProxySelector.getDefault()
        var proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort))
        var client  = OkHttpClient().newBuilder()
            .proxy(proxy)
            .build()

        var apiRequest = Request.Builder()
            .url("https://pro-api.coinmarketcap.com/v2/tools/price-conversion?symbol=" + coin + "&amount=1")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-CMC_PRO_API_KEY", "522fcb01-b6db-465c-85d4-24586e6ba714")
            .build()

        var msg = "暂未收录"
        client.newCall(apiRequest).execute().use { responce -> 
            if (responce.isSuccessful) {
                var responseBody = responce.body?.string()
                var originalJson = responseBody?.replace("\\bnull\\b".toRegex(), "0")

                var jsonObject = JSONObject(originalJson)
                var dataArray = jsonObject.getJSONArray("data")

                // var newArray = JSONArray()
                var result = StringBuilder()
                for (i in 0 until dataArray.length()) {
                    var item = dataArray.getJSONObject(i)
                    val symbol = item.getString("symbol")
                    val name = item.getString("name")
                    val price = item.getJSONObject("quote").getJSONObject("USD").getDouble("price")
                    val formattedPrice = if (price == 0.0) "暂无" else price.toString()

                    // val newItem = JSONObject()
                    // newItem.put("符号", symbol ?: "没名字的土狗")
                    // newItem.put("名称", name ?: "没名字的土狗")
                    // newItem.put("价格", price ?: "暂无价格")

                    // newArray.put(newItem)

                    var line = "符号: $symbol, 名称: $name, 价格: $formattedPrice"
                    result.append(line).append("\n")
                }

                // var newJson = newArray.toString()

                msg = result.toString()
            }
        }
        return msg;
    }

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            //群消息
            // //复读示例
            // if (message.contentToString().startsWith("复读")) {
            //     group.sendMessage(message.contentToString().replace("复读", ""))
            // }

            // 把复读示例转化为机器人命令指令
            if (message.contentToString().startsWith("[mirai:at:${bot.id}] ")) {
                if (message.contentToString().startsWith("[mirai:at:3545524709] /")) {
                    var coin = message.contentToString().replace("[mirai:at:3545524709] /", "")

                    
                    group.sendMessage(getPrice(coin))
                }
                else if (message.contentToString().startsWith("[mirai:at:3545524709] 帮助")) {
                    group.sendMessage("基本指令：\n@${bot.nick} /币种 显示币种价格\n@${bot.nick} 帮助 显示帮助")
                }
                else {
                    group.sendMessage("您好，请输入 @${bot.nick} 帮助 查看帮助。")
                }
            }
            // if (message.contentToString() == "hi") {
            //     //群内发送
            //     group.sendMessage("hi")
            //     //向发送者私聊发送消息
            //     sender.sendMessage("hi")
            //     //不继续处理
            //     return@subscribeAlways
            // }
            // //分类示例
            message.forEach {
                if (it is At && it.target == bot.id) {
                    var text = message.subList(message.indexOf(it) + 1, message.size).joinToString("")
                    if (text.startsWith("/")) {
                        var coin = text.replace("/", "")
                        group.sendMessage(getPrice(coin))
                    }
                    else if (text.isNotBlank()) {
                        group.sendMessage(text)
                    }
                    else {
                        group.sendMessage("请添加指令")
                    }
                }
            }
            // message.forEach {
            //     //循环每个元素在消息里
            //     if (it is Image) {
            //         //如果消息这一部分是图片
            //         val url = it.queryUrl()
            //         group.sendMessage("图片，下载地址$url")
            //     }
            //     if (it is PlainText) {
            //         //如果消息这一部分是纯文本
            //         group.sendMessage("纯文本，内容:${it.content}")
            //     }
            // }
        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            //好友信息
            if (message.contentToString().startsWith("/")) {
                var coin = message.contentToString().replace("/", "")

                
                sender.sendMessage(getPrice(coin))
            }
        }

        // 不允许添加好友，不允许拉群
        // eventChannel.subscribeAlways<NewFriendRequestEvent> {
        //     //自动同意好友申请
        //     accept()
        // }
        // eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        //     //自动同意加群申请
        //     accept()
        // }

        myCustomPermission // 注册权限
    }

    // region console 权限系统示例
    private val myCustomPermission by lazy { // Lazy: Lazy 是必须的, console 不允许提前访问权限系统
        // 注册一条权限节点 org.example.mirai-example:my-permission
        // 并以 org.example.mirai-example:* 为父节点

        // @param: parent: 父权限
        //                 在 Console 内置权限系统中, 如果某人拥有父权限
        //                 那么意味着此人也拥有该权限 (org.example.mirai-example:my-permission)
        // @func: PermissionIdNamespace.permissionId: 根据插件 id 确定一条权限 id
        PermissionService.INSTANCE.register(permissionId("my-permission"), "一条自定义权限", parentPermission)
    }

    public fun hasCustomPermission(sender: User): Boolean {
        return when (sender) {
            is Member -> AbstractPermitteeId.ExactMember(sender.group.id, sender.id)
            else -> AbstractPermitteeId.ExactUser(sender.id)
        }.hasPermission(myCustomPermission)
    }
    // endregion
}
