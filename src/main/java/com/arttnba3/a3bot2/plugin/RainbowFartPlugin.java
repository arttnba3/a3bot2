package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class RainbowFartPlugin extends A3Plugin
{
    String request_url = "https://chp.shadiao.app/api.php?level=";
    String level = "114514";
    final String help_info = "🌈彩虹屁插件🌈\n"
            + "用法：\n"
            + "/rainbow  --获取一句彩虹屁\n"
            + "以下语句仅管理员可以使用:\n"
            + "/rainbow set [level]  --设置彩虹屁等级";

    public RainbowFartPlugin()
    {
        this.setCommand("/rainbow");
        this.setPluginName("RainbowFartPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long userId = event.getUserId();
        String[] args = this.getArgs();

        if (args.length == 1)
        {
            try
            {
                URL url = new URL(request_url + level);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                String rainbow_msg = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8")).readLine();
                bot.sendPrivateMsg(userId, rainbow_msg,false);

                httpURLConnection.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return MESSAGE_BLOCK;
        }

        else
        {
            if (args[1].equals("set"))
            {
                if (args.length == 2)
                {
                    bot.sendPrivateMsg(userId, help_info, false);
                    return MESSAGE_BLOCK;
                }

                if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                {
                    bot.sendPrivateMsg(userId, "Permission denied, authorization limited.", false);
                    return MESSAGE_BLOCK;
                }

                this.level = args[2];
                bot.sendPrivateMsg(userId,"Success.",false);
                return MESSAGE_BLOCK;
            }
            else
            {
                bot.sendPrivateMsg(userId, help_info, false);
                return MESSAGE_BLOCK;
            }
        }
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        String[] args = this.getArgs();

        if (args.length == 1)
        {
            try
            {
                URL url = new URL(request_url + level);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                String rainbow_msg = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8")).readLine();
                bot.sendGroupMsg(groupId,rainbow_msg,false);

                httpURLConnection.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return MESSAGE_BLOCK;
        }

        else
        {
            if (args[1].equals("set"))
            {
                if (args.length == 2)
                {
                    bot.sendGroupMsg(groupId, help_info, false);
                    return MESSAGE_BLOCK;
                }

                if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                {
                    bot.sendGroupMsg(groupId, "Permission denied, authorization limited.", false);
                    return MESSAGE_BLOCK;
                }

                this.level = args[2];
                bot.sendGroupMsg(groupId,"Success.",false);
                return MESSAGE_BLOCK;
            }
            else
            {
                bot.sendGroupMsg(groupId, help_info, false);
                return MESSAGE_BLOCK;
            }
        }
    }
}
