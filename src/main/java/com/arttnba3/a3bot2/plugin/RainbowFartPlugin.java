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
        this.addCommand("/rainbow");
        this.setPluginName("RainbowFartPlugin");
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();
        bot.sendPrivateMsg(user_id, getRainbow(user_id, this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        bot.sendGroupMsg(event.getGroupId(), getRainbow(event.getUserId(), this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    public String getRainbow(long user_id, String[] args)
    {
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
                httpURLConnection.disconnect();
                return rainbow_msg;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return this.MSG_ERRORS_OCCUR;
            }
        }
        else
        {
            if (args[1].equals("set"))
            {
                if (args.length == 2)
                    return help_info;

                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return  this.MSG_PERMISSION_DENIED;

                this.level = args[2];
                return  this.MSG_SUCCESS;
            }
            else
                return help_info;
        }
    }
}
