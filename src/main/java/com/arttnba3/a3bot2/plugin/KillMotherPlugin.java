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
        this.setCommand("/nmsl");
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
        if (!this.isEnabled())
            return MESSAGE_IGNORE;
        // 获取 消息内容 群号 发送者QQ
        String msg = event.getRawMessage();
        String[] args = this.getArgs();
        long userId = event.getUserId();

        if (args[0].equals("/nmsl"))
        {
            if (userId != this.getAdmin() && !killer_list.contains(userId))
            {
                bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                return MESSAGE_BLOCK;
            }
            if (args.length == 1)
            {
                try
                {
                    URL url = new URL(request_url + level);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    String mother_killing_msg = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8")).readLine();
                    bot.sendPrivateMsg(userId, mother_killing_msg, false);

                    httpURLConnection.disconnect();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return MESSAGE_BLOCK;
            }
            else if (args.length < 3)
            {
                bot.sendPrivateMsg(userId, help_info, false);
                return MESSAGE_BLOCK;
            }
            else
            {
                if (args[1].equals("add") || args[1].equals("del"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        long obj_user = Long.valueOf(args[2]);//  /nmsl add userId

                        if (args[0].equals("add"))
                        {
                            if (killer_list.contains(obj_user))
                            {
                                bot.sendPrivateMsg(userId,"Already permitted.",false);
                                return MESSAGE_BLOCK;
                            }
                            killer_list.add(obj_user);
                            bot.sendPrivateMsg(userId,"Success.",false);
                        }
                        else
                        {
                            if (!killer_list.contains(obj_user))
                            {
                                bot.sendPrivateMsg(userId,"Permitted user not found.",false);
                                return MESSAGE_BLOCK;
                            }
                            killer_list.remove(obj_user);
                            bot.sendPrivateMsg(userId,"Success.",false);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId,"incorrect argument(s) input",false);
                    }

                    return MESSAGE_BLOCK;
                }
                else if (args[1].equals("set"))
                {
                    if (userId != this.getAdmin() && !killer_list.contains(userId))
                    {
                        bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }
                    this.level = args[2];
                    return MESSAGE_BLOCK;
                }
                else if (args[1].equals("save"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

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
                        bot.sendPrivateMsg(userId,"Killer data saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args[1].equals("load"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File killer_data = new File(this.getDataPath());
                        if (!killer_data.exists())
                        {
                            bot.sendPrivateMsg(userId, "Killer data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(killer_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        killer_list = (List) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();
                        bot.sendPrivateMsg(userId,"Killer data loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args[1].equals("clear"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendPrivateMsg(userId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    killer_list.clear();
                    bot.sendPrivateMsg(userId, "Killer data cleared.", false);
                }
                else
                    bot.sendPrivateMsg(userId, help_info, false);

                return MESSAGE_BLOCK;
            }
        }

        return MESSAGE_IGNORE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        if (!this.isEnabled())
            return MESSAGE_IGNORE;

        // 获取 消息内容 群号 发送者QQ
        String msg = event.getRawMessage();
        String[] args = this.getArgs();
        long groupId = event.getGroupId();
        long userId = event.getUserId();

        if (args[0].equals("/nmsl"))
        {
            if (userId != this.getAdmin() && !killer_list.contains(userId))
            {
                bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                return MESSAGE_BLOCK;
            }
            if (args.length == 1)
            {
                try
                {
                    URL url = new URL(request_url + level);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    String mother_killing_msg = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8")).readLine();
                    bot.sendGroupMsg(groupId, mother_killing_msg, false);

                    httpURLConnection.disconnect();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                return MESSAGE_BLOCK;
            }
            else if (args.length < 3)
            {
                bot.sendGroupMsg(groupId, help_info, false);
                return MESSAGE_BLOCK;
            }
            else
            {
                if (args[1].equals("add") || args[1].equals("del"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        long obj_user = Long.valueOf(args[2]);//  /nmsl add userId

                        if (args[0].equals("add"))
                        {
                            if (killer_list.contains(obj_user))
                            {
                                bot.sendGroupMsg(groupId,"Already permitted.",false);
                                return MESSAGE_BLOCK;
                            }
                            killer_list.add(obj_user);
                            bot.sendGroupMsg(groupId,"Success.",false);
                        }
                        else
                        {
                            if (!killer_list.contains(obj_user))
                            {
                                bot.sendGroupMsg(groupId,"Permitted user not found.",false);
                                return MESSAGE_BLOCK;
                            }
                            killer_list.remove(obj_user);
                            bot.sendGroupMsg(groupId,"Success.",false);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId,"incorrect argument(s) input",false);
                    }

                    return MESSAGE_BLOCK;
                }
                else if (args[1].equals("set"))
                {
                    if (userId != this.getAdmin() && !killer_list.contains(userId))
                    {
                        bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }
                    this.level = args[2];
                    return MESSAGE_BLOCK;
                }
                else if (args[1].equals("save"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

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
                        bot.sendGroupMsg(groupId,"Killer data saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args[1].equals("load"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File killer_data = new File(this.getDataPath());
                        if (!killer_data.exists())
                        {
                            bot.sendGroupMsg(groupId, "Killer data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(killer_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        killer_list = (List) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();
                        bot.sendGroupMsg(groupId,"Killer data loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args[1].equals("clear"))
                {
                    if (userId != this.getAdmin() && !this.getPermissionList().contains(userId))
                    {
                        bot.sendGroupMsg(groupId,"Permission denied, authorization limited.",false);
                        return MESSAGE_BLOCK;
                    }

                    killer_list.clear();
                    bot.sendGroupMsg(groupId, "Killer data cleared.", false);
                }
                else
                    bot.sendGroupMsg(groupId, help_info, false);

                return MESSAGE_BLOCK;
            }
        }

        return MESSAGE_IGNORE;
    }

}
