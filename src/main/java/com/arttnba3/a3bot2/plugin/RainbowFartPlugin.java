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
        long user_id = event.getUserId();
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
                bot.sendPrivateMsg(user_id, rainbow_msg,false);

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
                    bot.sendPrivateMsg(user_id, help_info, false);
                    return MESSAGE_BLOCK;
                }

                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                {
                    bot.sendPrivateMsg(user_id, "Permission denied, authorization limited.", false);
                    return MESSAGE_BLOCK;
                }

                this.level = args[2];
                bot.sendPrivateMsg(user_id,"Success.",false);
                return MESSAGE_BLOCK;
            }
            else
            {
                bot.sendPrivateMsg(user_id, help_info, false);
                return MESSAGE_BLOCK;
            }
        }
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long group_id = event.getGroupId();
        long user_id = event.getUserId();
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
                bot.sendGroupMsg(group_id,rainbow_msg,false);

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
                    bot.sendGroupMsg(group_id, help_info, false);
                    return MESSAGE_BLOCK;
                }

                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                {
                    bot.sendGroupMsg(group_id, "Permission denied, authorization limited.", false);
                    return MESSAGE_BLOCK;
                }

                this.level = args[2];
                bot.sendGroupMsg(group_id,"Success.",false);
                return MESSAGE_BLOCK;
            }
            else
            {
                bot.sendGroupMsg(group_id, help_info, false);
                return MESSAGE_BLOCK;
            }
        }
    }
}
