package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class RollPlugin extends A3Plugin
{
    String help_info = "Roll it all~\n"
            + "用法：\n"
            + "/roll boom    ------俄罗斯轮盘，下一个中枪的会不会是你呢w\n"
            + "/roll dice    ------普通的骰子🎲里隐藏着什么呢？\n"
            + "多余的参数会被自动丢弃哦\n"
            + "以下指令只有管理员才能使用哦：\n"
            + "/roll set [op] [num]  ------设置数值/概率";

    Map<Long, Integer> roll_group_map;     // group num, current
    Random random;
    int dice_range = 6;
    int boom_probability = 12;

    public RollPlugin()
    {
        this.addCommand("/roll");
        this.setPluginName("RollPlugin");
        roll_group_map = new HashMap<>();
        random = new Random();
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long user_id = event.getUserId();

        bot.sendPrivateMsg(user_id, rollAnalyzer(false, user_id, 0xdeadbeef), false);

        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long group_id = event.getGroupId();
        long user_id = event.getUserId();

        bot.sendGroupMsg(group_id, rollAnalyzer(true, user_id, group_id), false);

        return MESSAGE_BLOCK;
    }

    public String rollAnalyzer(boolean is_group, long user_id, long group_id)
    {
        String[] args = this.getArgs();
        String retn_msg = null;
        int boom_count = 0;
        int dice_num, hide_probability;

        if (args.length < 2)
            return help_info;

        if (args[1].equals("boom"))
        {
            if (!is_group)
                return "轮盘只能在群聊中启动哦>  <";

            if (!roll_group_map.containsKey(group_id))
                roll_group_map.put(group_id, boom_count);
            boom_count = roll_group_map.get(group_id);

            int roll = random.nextInt(boom_probability);
            boom_count++;
            if (roll == 0)
            {
                retn_msg = "boom!你死le！\n该轮轮盘进行至第 "+String.valueOf(boom_count)+" 轮\n/roll boom 开启新一轮俄罗斯转盘>  <";
                boom_count = 0;
            }
            else
                retn_msg = "恭喜你在第 " + String.valueOf(boom_count) +" 轮轮盘中存活O.O\n/roll boom 进行下一轮> <";

            roll_group_map.replace(group_id, boom_count);
        }
        else if (args[1].equals("dice"))
        {
            dice_num = random.nextInt(dice_range) + 1;
            hide_probability = random.nextInt(100);
            if (hide_probability == 99)
                retn_msg = "恭喜你掷出了隐藏数值114514！\n>  <！";
            else
                retn_msg = "你掷出的点数O.O是： " + String.valueOf(dice_num);
        }
        else if (args[1].equals("set"))
        {
            if (args.length < 4)
                return help_info;

            if (user_id != getAdmin())
                return "Permission denied, authorization limited.";

            try
            {
                int val = Integer.valueOf(args[3]);
                if (args[2].equals("dice"))
                    dice_range = val;
                else if (args[2].equals("boom"))
                    boom_probability = val;
                else
                    return "Invalid arguments.";

                retn_msg = "Success.";
            }
            catch (Exception e)
            {
                retn_msg = "Invalid arguments.";
            }
        }
        else
            retn_msg = help_info;

        return retn_msg;
    }

}
