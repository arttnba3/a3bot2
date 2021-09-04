package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class EchoPlugin extends A3Plugin
{
    public EchoPlugin()
    {
        this.addCommand("/echo");
        this.setPluginName("EchoPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();
        String[] args = this.getArgs();

        if (user_id != getAdmin())
            bot.sendPrivateMsg(user_id, this.MSG_PERMISSION_DENIED, false);
        else
            bot.sendPrivateMsg(user_id, args[1], false);

        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long group_id = event.getGroupId();
        long user_id = event.getUserId();
        String[] args = this.getArgs();

        if (user_id != getAdmin())
            bot.sendGroupMsg(group_id, this.MSG_PERMISSION_DENIED, false);
        else
            bot.sendGroupMsg(group_id, args[1], false);

        return MESSAGE_BLOCK;
    }
}
