package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import kotlin.Pair;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotBase;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class SignInPlugin extends A3Plugin
{
    Map<Long, Pair<ArrayList<String>, ArrayList<Long>>> sign_in_data; // group_id -> (ranking_info<String>, user_id<Long>)
    String help_info = "Kira⭐Kira~签到插件v1.0\n"
            + "用法：/signin\n"
            + "以下指令仅供管理员使用：\n"
            + "/signin add [rank] [group] [msg] ------添加签到提示信息\n"
            + "/signin save             ------保存签到数据\n"
            + "/signin load             ------载入签到数据\n"
            + "/signin clear [group]    ------手动清理签到数据";

    ArrayList<String> per_command;
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;
    File file = null;

    public SignInPlugin()
    {
        this.setCommand("/signin");
        this.setPluginName("SignInPlugin");
        this.setDataPath("data/sign_in_list.data");
        sign_in_data = new HashMap<>();
        per_command = new ArrayList<>();
        per_command.add("add");
        per_command.add("save");
        per_command.add("load");
        per_command.add("clear");

        file = new File(this.getDataPath());
        try
        {
            if (!file.exists())
                return ;

            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(file,true);
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            sign_in_data = (Map<Long, Pair<ArrayList<String>, ArrayList<Long>>>) objectInputStream.readObject();
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
        String[] args = this.getArgs();

        bot.sendPrivateMsg(user_id, "请在群聊中使用签到插件哦 (> <) !", false);

        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long group_id = event.getGroupId();
        long user_id = event.getUserId();
        String[] args = this.getArgs();
        Pair<ArrayList<String>, ArrayList<Long>> per_group = null;
        ArrayList<String> per_group_msg = null;
        ArrayList<Long> per_group_list = null;
        boolean signed = false;
        int rank;

        per_group = sign_in_data.get(group_id);
        if (per_group == null)
        {
            per_group = new Pair<ArrayList<String>, ArrayList<Long>>(new ArrayList<String>(100), new ArrayList<Long>());
            per_group_msg = per_group.getFirst();
            for (int i = 0; i < 100; i++) // Initialization
                per_group_msg.add(null);
            sign_in_data.put(group_id, per_group);
        }
        per_group_msg = per_group.getFirst();
        per_group_list = per_group.getSecond();

        if (args.length == 1)
        {
            // check whether the sender is an annoymous
            if (user_id == 80000000L)
            {
                bot.sendGroupMsg(group_id, "匿名你签个🔨到", false);
                return MESSAGE_BLOCK;
            }

            signed = per_group_list.contains(user_id);
            if (signed)
            {
                bot.sendGroupMsg(group_id, "你今天已经签过到了🔨", false);
                return MESSAGE_BLOCK;
            }

            per_group_list.add(user_id);
            rank = per_group_list.size();
            bot.sendGroupMsg(group_id, "签到成功！你是本群今天第" + String.valueOf(rank) + "个签到的>w<！", false);
            if (per_group_msg.size() >= rank && per_group_msg.get(rank - 1) != null)
                bot.sendGroupMsg(group_id, per_group_msg.get(rank - 1), false);
        }
        else if (per_command.contains(args[1]))
        {
            if (user_id != this.getAdmin())
            {
                bot.sendGroupMsg(group_id,"Permission denied, authorization limited.",false);
                return MESSAGE_BLOCK;
            }

            if (args[1].equals("add"))
            {
                if (args.length < 5)
                {
                    bot.sendGroupMsg(group_id, help_info, false);
                    return MESSAGE_BLOCK;
                }

                try
                {
                    int per_rank = Integer.valueOf(args[2]);
                    if (per_rank > 100 || per_rank < 1)
                    {
                        bot.sendGroupMsg(group_id, "Invalid rank set, 100 max", false);
                        return MESSAGE_BLOCK;
                    }
                    long per_group_id = Long.valueOf(args[3]);
                    per_group = sign_in_data.get(per_group_id);
                    {
                        per_group = new Pair<ArrayList<String>, ArrayList<Long>>(new ArrayList<String>(100), new ArrayList<Long>());
                        per_group_msg = per_group.getFirst();
                        for (int i = 0; i < 100; i++) // Initialization
                            per_group_msg.add(null);
                        sign_in_data.put(per_group_id, per_group);
                    }

                    per_group_msg.set(per_rank - 1, args[4]);
                    bot.sendGroupMsg(group_id, "Message added successfully.", false);
                }
                catch (NumberFormatException e)
                {
                    bot.sendGroupMsg(group_id, "Invalid arguments.", false);
                    return MESSAGE_BLOCK;
                }

            }
            else if (args[1].equals("load"))
            {
                try
                {
                    File signin_data = new File(this.getDataPath());
                    if (!signin_data.exists())
                    {
                        bot.sendGroupMsg(group_id, "Signin data not existed.", false);
                        return MESSAGE_BLOCK;
                    }

                    InputStream inputStream = new FileInputStream(signin_data);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    sign_in_data = (Map<Long, Pair<ArrayList<String>, ArrayList<Long>>>) objectInputStream.readObject();
                    objectInputStream.close();
                    inputStream.close();
                    bot.sendGroupMsg(group_id,"Signin data loaded successfully.",false);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    bot.sendGroupMsg(group_id, "Unexpected errors occurred, check terminal for more info.", false);
                }
            }
            else if (args[1].equals("save"))
            {
                try
                {
                    File file = new File(this.getDataPath());
                    if (!file.exists())
                        file.createNewFile();
                    OutputStream outputStream = new FileOutputStream(file);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(sign_in_data);
                    objectOutputStream.close();
                    outputStream.close();
                    bot.sendGroupMsg(group_id,"Signin data saved successfully.",false);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    bot.sendGroupMsg(group_id, "Unexpected errors occurred, check terminal for more info.", false);
                }
            }
            else if (args[1].equals("clear"))
            {
                try
                {
                    long clear_grp_id = Long.valueOf(args[2]);
                    per_group_list = sign_in_data.get(clear_grp_id).getSecond();
                    per_group_list.clear();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    bot.sendGroupMsg(group_id, "Unable to clear the data, errors existed.", false);
                    return MESSAGE_BLOCK;
                }
            }
        }
        else
        {
            bot.sendGroupMsg(group_id, help_info, false);
        }

        return MESSAGE_BLOCK;
    }

    @Scheduled(cron = "0 0 5 * * ? ")
    public void resetData()
    {
        if (this.isEnabled())
            return;

        Pair<ArrayList<String>, ArrayList<Long>> per_group = null;
        ArrayList<Long> per_group_list = null;
        Iterator<Map.Entry<Long, Pair<ArrayList<String>, ArrayList<Long>>>> entries = sign_in_data.entrySet().iterator();
        while (entries.hasNext())
        {
            per_group_list = entries.next().getValue().getSecond();
            per_group_list.clear();
        }
    }
}
