package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import kotlin.Pair;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class SignInPlugin extends A3Plugin
{
    Map<Long, Pair<ArrayList<String>, HashMap<Long, Long>>> sign_in_data; // group_id -> (ranking_info<String>, (user_id<Long>, signed_time<long>))
    String help_info = "Kira⭐Kira~签到插件v1.0\n"
            + "用法：/signin  ------签到\n"
            + "/signout      ------签退\n"
            + "以下指令仅供管理员使用：\n"
            + "/signin add [rank] [group] [msg] ------添加签到提示信息\n"
            + "/signin save             ------保存签到数据\n"
            + "/signin load             ------载入签到数据\n"
            + "/signin clear [group]    ------手动清理签到数据";

    ArrayList<String> per_command;

    public SignInPlugin()
    {
        this.addCommand("/signin");
        this.addCommand("/signout");
        this.setPluginName("SignInPlugin");
        this.setDataPath("data/sign_in_list.data");
        sign_in_data = new HashMap<>();
        per_command = new ArrayList<>();
        per_command.add("add");
        per_command.add("save");
        per_command.add("load");
        per_command.add("clear");

        Object object = this.readData(this.getDataPath());
        sign_in_data = (object != null ? (HashMap<Long, Pair<ArrayList<String>, HashMap<Long, Long>>>) object : new HashMap<>());
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

        Pair<ArrayList<String>, HashMap<Long, Long>> per_group;
        ArrayList<String> per_group_msg;
        HashMap<Long, Long> per_group_list;
        boolean signed;
        long signed_time, duration, per_hour, per_minute, per_second;
        int rank;

        per_group = sign_in_data.get(group_id);
        if (per_group == null)
        {
            per_group = new Pair<ArrayList<String>, HashMap<Long, Long>>(new ArrayList<String>(100), new HashMap<Long, Long>());
            per_group_msg = per_group.getFirst();
            for (int i = 0; i < 100; i++) // Initialization
                per_group_msg.add(null);
            sign_in_data.put(group_id, per_group);
        }
        per_group_msg = per_group.getFirst();
        per_group_list = per_group.getSecond();

        // check whether the sender is an annoymous
        if (user_id == 80000000L)
        {
            bot.sendGroupMsg(group_id, "匿名你签个🔨到", false);
            return MESSAGE_BLOCK;
        }

        if (args[0].equals("/signout"))
        {
            signed = per_group_list.containsKey(user_id);
            if (!signed)
            {
                bot.sendGroupMsg(group_id, "你今天还没签过到呢🔨", false);
                return MESSAGE_BLOCK;
            }

            signed_time = per_group_list.get(user_id);
            if (signed_time == -1) // already signed out
            {
                bot.sendGroupMsg(group_id, "你今天已经签退过了🔨", false);
                return MESSAGE_BLOCK;
            }

            duration = (new Date().getTime() / 1000) - signed_time;
            per_hour = duration / 3600;
            per_minute = (duration - per_hour * 3600) / 60;
            per_second = duration % 60;
            bot.sendGroupMsg(group_id, "今天一共工作了 " + String.valueOf(per_hour) + " 时 " + String.valueOf(per_minute) + " 分 " + String.valueOf(per_second) + " 秒", false);
            if(duration < 28800)
                bot.sendGroupMsg(group_id, "懒狗能不能爪巴啊\uD83D\uDE21\uD83D\uDE21\uD83D\uDE21！ヾ(≧へ≦)〃！", false);
            else
                bot.sendGroupMsg(group_id, "奖励辛勤工作的人一朵小花花🌸~", false);
            per_group_list.replace(user_id, (long) -1);
        }
        else if (args.length == 1 && args[0].equals("/signin"))
        {
            signed = per_group_list.containsKey(user_id);
            if (signed)
            {
                bot.sendGroupMsg(group_id, "你今天已经签过到了🔨", false);
                return MESSAGE_BLOCK;
            }

            per_group_list.put(user_id, new Date().getTime() / 1000);
            rank = per_group_list.size();
            bot.sendGroupMsg(group_id, "签到成功！你是本群今天第" + String.valueOf(rank) + "个签到的>w<！", false);
            if (per_group_msg.size() >= rank && per_group_msg.get(rank - 1) != null)
                bot.sendGroupMsg(group_id, per_group_msg.get(rank - 1), false);
        }
        else if (per_command.contains(args[1]))
        {
            if (user_id != this.getAdmin())
            {
                bot.sendGroupMsg(group_id,this.MSG_PERMISSION_DENIED,false);
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
                    if (per_group == null)
                    {
                        per_group = new Pair<ArrayList<String>, HashMap<Long, Long>>(new ArrayList<String>(100), new HashMap<Long, Long>());
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
                Object object = this.readData(this.getDataPath());
                if (object != null)
                {
                    sign_in_data = (HashMap<Long, Pair<ArrayList<String>, HashMap<Long, Long>>>) object;
                    bot.sendGroupMsg(group_id,"Signin data loaded successfully.",false);
                }
                else
                    bot.sendGroupMsg(group_id, "Unable to load data, file not existed or errors occurred.", false);
            }
            else if (args[1].equals("save"))
            {
                if (this.saveData(sign_in_data, this.getDataPath()))
                    bot.sendGroupMsg(group_id,"Signin data saved successfully.",false);
                else
                    bot.sendGroupMsg(group_id, this.MSG_ERRORS_OCCUR, false);
            }
            else if (args[1].equals("clear"))
            {
                try
                {
                    long clear_grp_id = Long.valueOf(args[2]);
                    per_group_list = sign_in_data.get(clear_grp_id).getSecond();
                    per_group_list.clear();
                    bot.sendGroupMsg(group_id, this.MSG_SUCCESS, false);
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

    @Scheduled(cron = "0 0 5 * * ? ") // messages shall be saved
    public void resetData()
    {
        if (!this.isEnabled())
            return;

        Pair<ArrayList<String>, ArrayList<Long>> per_group = null;
        HashMap<Long, Long> per_group_list = null;
        Iterator<Map.Entry<Long, Pair<ArrayList<String>, HashMap<Long, Long>>>> entries = sign_in_data.entrySet().iterator();
        while (entries.hasNext())
        {
            per_group_list = entries.next().getValue().getSecond();
            per_group_list.clear();
        }

        System.out.println("\033[32m\033[1m[+] [SignInPlugin] Data automatically cleared. \033[0m");
    }
}
