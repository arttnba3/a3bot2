package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotApi;
import onebot.OnebotBase;
import onebot.OnebotEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class AntiWithdrawPlugin extends A3Plugin
{
    final String available_host = "gchat.qpic.cn";
    public AntiWithdrawPlugin()
    {
        this.setPluginName("AntiWithdrawPlugin");
    }

    @Override
    public int onGroupRecallNotice(Bot bot, OnebotEvent.GroupRecallNoticeEvent event)
    {
        if (!isEnabled())
            return MESSAGE_IGNORE;

        long group_id = event.getGroupId();
        long user_id = event.getUserId();
        int message_id = event.getMessageId();

        // to prevent the unstoppable nesting message
        if (user_id == bot.getSelfId())
            return MESSAGE_IGNORE;

        String raw_message = bot.getMsg(message_id).getRawMessage();
        OnebotApi.GetMsgResp msg_resp = bot.getMsg(message_id);

        List<OnebotBase.Message> withdraw_msg_list = bot.getMsg(message_id).getMessageList();
        List<OnebotBase.Message> msg_list = new ArrayList<>();

        // check whether the sender is an annoymous
        if (user_id == 80000000L)
            msg_list.addAll(Msg.builder().text("[匿名用户] 撤回了消息：\n").build());
        else
            msg_list.addAll(Msg.builder().at(user_id).text("撤回了消息：\n").build());

        msg_list.addAll(withdraw_msg_list);

        bot.sendGroupMsg(group_id,msg_list,false);

        return MESSAGE_BLOCK;
    }
}
