package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotBase;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RepeaterPlugin extends A3Plugin
{
    Map map = new HashMap();

    public RepeaterPlugin()
    {
        this.setPluginName("RepeaterPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        if (!this.isEnabled())
            return MESSAGE_IGNORE;
        return MESSAGE_IGNORE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        if (!this.isEnabled())
            return MESSAGE_IGNORE;

        List<OnebotBase.Message> msg = event.getMessageList();
        long groupId = event.getGroupId();

        if (!map.containsKey(groupId))
        {
            map.put(groupId, new RepeatInfo(msg, false));
            return MESSAGE_IGNORE;
        }

        if (msg.equals(((RepeatInfo)(map.get(groupId))).msg))
        {
            if (!((RepeatInfo)(map.get(groupId))).is_repeated)
            {
                bot.sendGroupMsg(groupId,msg,true);
                ((RepeatInfo)(map.get(groupId))).is_repeated = true;
            }
        }
        else
            ((RepeatInfo)(map.get(groupId))).is_repeated = false;
        ((RepeatInfo)(map.get(groupId))).msg = msg;

        return MESSAGE_IGNORE;
    }
}

class RepeatInfo
{
    public List<OnebotBase.Message> msg;
    public boolean is_repeated;
    public RepeatInfo(List<OnebotBase.Message> msg,boolean flag)
    {
        this.msg = msg;
        this.is_repeated = flag;
    }
}