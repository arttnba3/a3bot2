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
    List killer_list = new ArrayList<Long>();
    File file = null;
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;

    @SuppressWarnings("unchecked")
    public KillMotherPlugin()
    {
        this.setPluginName("KillMotherPlugin");
        this.addCommand("/nmsl");
        this.setDataPath("data/nmsl_killer_list.data");
        file = new File(this.getDataPath());
        try
        {
            if (!file.exists())
                return ;

            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(file,true);
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            killer_list = (List) objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();
        bot.sendPrivateMsg(user_id, getKillingMsg(user_id, this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        bot.sendGroupMsg(event.getGroupId(), getKillingMsg(event.getUserId(), this.getArgs()), false);
        return MESSAGE_BLOCK;
    }

    public String getKillingMsg(long user_id, String[] args)
    {
        String mother_killing_msg;

        if (user_id != this.getAdmin() && !getPermissionList().contains(user_id) &&!killer_list.contains(user_id))
            return "Permission denied, authorization limited.";

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
                return "Inner error, see the terminal for more infomation.";
            }
        }
        else if (args.length < 3)
            return help_info;
        else
        {
            if (args[1].equals("add") || args[1].equals("del"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                    return "Permission denied, authorization limited.";

                try
                {
                    long obj_user = Long.valueOf(args[2]);//  /nmsl add user_id

                    if (args[0].equals("add"))
                    {
                        if (killer_list.contains(obj_user))
                            return "Already permitted.";

                        killer_list.add(obj_user);
                        return "Success.";
                    }
                    else
                    {
                        if (!killer_list.contains(obj_user))
                            return "Permitted user not found.";

                        killer_list.remove(obj_user);
                        return "Success.";
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
                    return "Permission denied, authorization limited.";

                this.level = args[2];
                return "Success.";
            }
            else if (args[1].equals("save"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                return "Permission denied, authorization limited.";

                try
                {
                    File killer_data = new File(this.getDataPath());
                    if (!killer_data.exists())
                        killer_data.createNewFile();
                    OutputStream outputStream = new FileOutputStream(killer_data);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(killer_list);
                    objectOutputStream.close();
                    outputStream.close();
                    return "Killer data saved successfully.";
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Unexpected errors occurred, check terminal for more info.";
                }
            }
            else if (args[1].equals("load"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                return "Permission denied, authorization limited.";

                try
                {
                    File killer_data = new File(this.getDataPath());
                    if (!killer_data.exists())
                    return "Killer data not existed.";

                    InputStream inputStream = new FileInputStream(killer_data);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    killer_list = (List) objectInputStream.readObject();
                    objectInputStream.close();
                    inputStream.close();
                    return "Killer data loaded successfully.";
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Unexpected errors occurred, check terminal for more info.";
                }
            }
            else if (args[1].equals("clear"))
            {
                if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                return "Permission denied, authorization limited.";

                killer_list.clear();
                return "Killer data cleared.";
            }
            else
                return help_info;
        }
    }

}
