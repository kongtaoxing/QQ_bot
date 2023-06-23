# Mirai Console QQ报价机器人插件

本项目使用[Mirai Console](https://github.com/mamoe/mirai-console) 插件模板, 使用 Kotlin + Gradle.

[模板使用](https://github.com/project-mirai/how-to-use-plugin-template)


## 如何使用
0. 本项目要求的环境为 `java jdk > 11`，并且有安装 `kotlinc` 和 `grable` 
1. 克隆本仓库
2. 打开 `src/main/kotlin/PluginMain.kt` 并修改 `X-CMC_PRO_API_KEY`
3. 在终端中使用 `./grablew buildPlugin` 命令编译插件
4. 编译好的插件在 `build/mirai` 文件夹中，将其复制到 `mirai-console` 项目的 `plugins` 文件夹并启动机器人即可。

## Todo List

- [x] 能够正常回复消息
- [x] 使用cmc的api获取币价
- [x] 添加代理
- [x] 被艾特之后才回复
- [ ] 添加帮助等其他指令
- [ ] 其他功能