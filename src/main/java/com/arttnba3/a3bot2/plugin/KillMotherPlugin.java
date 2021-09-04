package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class KillMotherPlugin extends A3Plugin
{
    String request_url = "https://nmsl.shadiao.app/api.php?level=";
    String level = "min";
    String help_info = "杀🦄插件，享受最激情的嘴臭\n"
            + "以下指令仅供杀🦄客使用：\n"
            + "/nmsl                 --亲切问候您的家人\n"
            + "/nmsl set [level]     ----改变嘴臭等级\n"
            + "以下指令仅供插件系统（授权）管理员使用：\n"
            + "/nmsl add [qq num]    ----让您的好友成为杀🦄客\n"
            + "/nmsl del [qq num]    ----让杀🦄客成为🤡\n"
            + "/nmsl save            ----保存杀🦄客数据\n"
            + "/nmsl load            ----载入杀🦄客数据\n"
            + "/nmsl clear           ----清除杀🦄客数据\n"
            + "多余的参数会自动与您的母亲一起身体力行解决🗾的少子化问题";
    List killer_list;

    public KillMotherPlugin()
    {
        this.setPluginName("KillMotherPlugin");
        this.addCommand("/nmsl");
        this.setDataPath("data/nmsl_killer_list.data");
        Object object = this.readData(this.getDataPath());
        killer_list = (object != null ? (ArrayList) object : new ArrayList<Long>());
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();
        bot.sendPrivateMsg(user_id, getKillingMsg(user_id, this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        bot.sendGroupMsg(event.getGroupId(), getKillingMsg(event.getUserId(), this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    public String getKillingMsg(long user_id, String[] args)
    {
        String mother_killing_msg;

        if (user_id != this.getAdmin() && !getPermissionList().contains(user_id) &&!killer_list.contains(user_id))
            return this.MSG_PERMISSION_DENIED;

        // basic getting message from the link
        if (args.length == 1)
        {
            try
            {
                URL url = new URL(request_url + level);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                mother_killing_msg = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8")).readLine();
                httpURLConnection.disconnect();
                return mother_killing_msg;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return this.MSG_ERRORS_OCCUR;
            }
        }
        else if (args.length < 3) // not the command
            return help_info;
        else
        {
            if (args[1].equals("add") || args[1].equals("del"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return this.MSG_PERMISSION_DENIED;

                try
                {
                    long obj_user = Long.valueOf(args[2]);//  /nmsl add user_id

                    if (args[0].equals("add"))
                    {
                        if (killer_list.contains(obj_user))
                            return "Already permitted.";

                        killer_list.add(obj_user);
                        return this.MSG_SUCCESS;
                    }
                    else
                    {
                        if (!killer_list.contains(obj_user))
                            return "Permitted user not found.";

                        killer_list.remove(obj_user);
                        return this.MSG_SUCCESS;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Incorrect argument(s) input";
                }

            }
            else if (args[1].equals("set"))
            {
                if (user_id != this.getAdmin() && !killer_list.contains(user_id))
                    return this.MSG_PERMISSION_DENIED;

                this.level = args[2];
                return this.MSG_SUCCESS;
            }
            else if (args[1].equals("save"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return this.MSG_PERMISSION_DENIED;

                if (saveData(this.killer_list, this.getDataPath()))
                    return "Killer data saved successfully.";
                else
                    return this.MSG_ERRORS_OCCUR;
            }
            else if (args[1].equals("load"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return this.MSG_PERMISSION_DENIED;

                Object object = this.readData(this.getDataPath());
                if (object != null)
                {
                    killer_list = (ArrayList) object;
                    return "Killer data loaded successfully.";
                }
                else
                    return this.MSG_ERRORS_OCCUR;
            }
            else if (args[1].equals("clear"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return this.MSG_PERMISSION_DENIED;

                killer_list.clear();
                    return "Killer data cleared.";
            }
            else
                return help_info;
        }
    }

}
