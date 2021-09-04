# a3bot2 - documentation

[中文文档](https://github.com/arttnba3/a3bot2/tree/master/doc/README.md)

## Introduction

This is a **private** qq-bot developed by arttnba3, which is based on [pbbot](https://github.com/ProtobufBot/ProtobufBot) and [GMC](https://github.com/protobufbot/go-Mirai-Client/releases), which uses the reverse web socket proxy and the default port is 8081.

The project aims to develop a qq bot which can be used almost immediately after downloading, which also provides some basic functions

## Usage

### Step I. clone the repository

Just clone the repository to your disk and open it as a Intellij IDEA project, all dependencies are configured with maven and will be automatically downloaded by IDEA

### Step II. run it with [GMC](https://github.com/protobufbot/go-Mirai-Client/releases)

Before we start to run the bot, we need to download [GMC](https://github.com/protobufbot/go-Mirai-Client/releases) at first.

Then do as the following:

- Run the GMC
- Run the a3bot2

You should configure the information of your qq account across `localhost:9000` as the [GMC doc](https://github.com/ProtobufBot/Go-Mirai-Client)

## Package

The project is based on maven, which means that you can simply package the a3bot2 with the following command:

```shell
$ mvn clean install package -Dmaven.test.skip=true
```

> For the windows powershell, it shall be:
>
> ```powershell
> PS > mvn clean install package '-Dmaven.test.skip=true'
> ```

The packed `.jar` file will be in the ```target/``` directory

## Settings

You can adjust the setting of a3bot in `src/main/resources/application.yml`

### port

The default port is `8081`:

```yaml
server:
  port: 8081
```

### plugins

You can simply set the order of messages passing between plugins that received by bot as follow:

```yaml
spring:
  bot:
    plugin-list:
      - com.arttnba3.a3bot2.plugin.AntiWithdrawPlugin
      - com.arttnba3.a3bot2.plugin.RepeaterPlugin
      - com.arttnba3.a3bot2.plugin.PluginSystemPlugin
```

### *plugin-system plugin

For better use, I've built an inner plugin system ```PluginSystemPlugin``` to control all the plugins

You shall set your own admin qq number under the `conf.ini`:

```ini
[config]
admin = 1145141919
```

## Pre-installed plugins

For better experience, I've built some plugins for you to use:

- plugin system 1.2.1: a plugin to control all the plugins, all messages will(and shall) be dealt by it **with a simple parser**. command: `/plugin`
- repeater plugin 1.0: to simply repeat the message that was sent continuously for more than one time in a group
- anti-withdraw plugin 1.0: a plugin to resend the message that was withdrawn by someone
- rainbow-fart plugin 1.1: to simply get a sentence from [https://chp.shadiao.app](https://chp.shadiao.app) by the command `/rainbow`
- mother-killing plugin 1.0.1: to simply get a sentence from [https://nmsl.shadiao.app](https://chp.shadiao.app) by the command `/nmsl`
- sign-in plugin 1.0.1: to simply record the sign-in in order by the command `/signin`
- roll plugin 1.1: to roll something out by the command `/roll`
- echo plugin 1.0: to simply echo a sentence with command `/echo` (only for admin temporarily)
- teach plugin 1.1: to teach the bot "how to talk" with commands `/teach` and `/delete`
- message walls 1.0: a plugin to intercepts all the message which may not be dealt by the plugin system
- ...

## I'd like to develop new plugin...

A new plugin shall be like the following **to serve the inner plugin system**:

```java
package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;


@Component
public class DemoPlugin extends A3Plugin
{
    public DemoPlugin()
    {
        this.addCommand("/demo");
        this.setPluginName("DemoPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();
        String[] args = this.getArgs();

        /*do something*/
        
        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long group_id = event.getGroupId();
        long user_id = event.getUserId();
        String[] args = this.getArgs();

        /*do something*/
        
        return MESSAGE_BLOCK;
    }
}

```

For most plugins whose functions based on command line, the plugin system will analyze the message and **explicitly call its inner functions** if the message satisfied a plugin's own command. You shall set the order of the plugin after the `MessageWallPlugin` and make it **always return the MESSAGE\_BLOCK**

If you need a plugin that shall not be disturbed by the plugin system, just set its order before the `PluginSystemPlugin` and write it as a normal plugin extends the `BotPlugin`, **don't forget to make it return the MESSAGE\_IGNORE to get those messages which shall not be processed by it**

### returned value

The return value of a function decides whether to continue to pass the message to the next plugin or not:

- `MESSAGE_BLOCK`: The message received would **not** be passed to the next plugin
- `MESSAGE_IGNORE`: The message received would be passed to the next plugin

### available APIs and more...

The project is almost all based on the [pbbot](https://github.com/ProtobufBot/ProtobufBot), so you shall check [pbbot doc](https://blog.lz1998.net/blogs/bot/2020/pbbot-doc/) to get more information

## To-do List...

- Optimize the plugin system (Always doing now)
- move available plugins from the old [a3bot](https://github.com/arttnba3/a3bot)
- Add more playable plugins
- ...