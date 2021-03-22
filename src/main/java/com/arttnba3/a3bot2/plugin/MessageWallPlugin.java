package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class MessageWallPlugin extends A3Plugin
{
    public MessageWallPlugin()
    {
        this.setPluginName("MessageWallPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        if (!this.isEnabled())
            return MESSAGE_IGNORE;

        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        if (!this.isEnabled())
            return MESSAGE_IGNORE;

        return MESSAGE_BLOCK;
    }
}
