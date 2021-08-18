package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
public class RollPlugin extends A3Plugin
{
    String help_info = "Roll it all~\n"
            + "用法：\n"
            + "/roll boom    ------俄罗斯轮盘，下一个中枪的会不会是你呢w\n"
            + "/roll dice    ------普通的骰子🎲里隐藏着什么呢？\n"
            + "/roll random  ------好像能召唤出意想不到的东西呀\n"
            + "random 可以配合 [add/del/list] [item]使用\n"
            + "多余的参数会被自动丢弃哦\n"
            + "以下指令只有管理员才能使用哦：\n"
            + "/roll set [op] [num]  ------设置数值/概率\n"
            + "/roll save            ------保存当前列表\n"
            + "/roll load            ------载入原有列表";

    Map<Long, Integer> roll_group_map;     // group num, current
    List<String> random_list;
    Random random;
    int dice_range = 6;
    int boom_probability = 12;

    File file = null;
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;

    public RollPlugin()
    {
        this.addCommand("/roll");
        this.setPluginName("RollPlugin");
        this.setDataPath("data/ramdon_item.data");
        roll_group_map = new HashMap<>();
        random = new Random();
        random_list = new ArrayList();

        file = new File(this.getDataPath());
        try
        {
            if (!file.exists())
                return ;

            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(file,true);
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            random_list = (ArrayList<String>) objectInputStream.readObject();
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
        String retn_msg = "";
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

            if (user_id != getAdmin() && !getPermissionList().contains(user_id))
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
        else if (args[1].equals("random"))
        {
            if (args.length == 2)
            {
                if (random_list.size() == 0)
                    return  "异世界还空无一物哦O O\n使用 add 指令添加物品叭>  <";

                retn_msg = "你从异界之门召唤出了「" + random_list.get(random.nextInt(random_list.size())) + "」！>  <";
            }
            else if (args[2].equals("add"))
            {
                if (args.length < 4)
                    return help_info;

                if (random_list.contains(args[3]))
                    return "异世界已经有这样东西了呀O O";
                random_list.add(args[3]);
                retn_msg = "异世界又多了一样宝物呢w";
            }
            else if (args[2].equals("del"))
            {
                if (args.length < 4)
                    return help_info;

                if (!random_list.remove(args[3]))
                    return "异世界不存在这样的东西哦0 0";
                retn_msg = "异世界失去了一样宝物呜呜呜T T";
            }
            else if (args[2].equals("list"))
            {
                retn_msg += "当前所能从异界之门召唤出来的物品有：\n";
                for (int i = 0, size = random_list.size(); i < size; i++)
                    retn_msg += random_list.get(i) + "\n";
            }
            else if (args[2].equals("clear"))
            {
                if (user_id != getAdmin() && !getPermissionList().contains(user_id))
                    return "Permission denied, authorization limited.";

                random_list.clear();
                retn_msg = "Success.";
            }
            else
                retn_msg = help_info;
        }
        else if (args[1].equals("save"))
        {
            if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                return "Permission denied, authorization limited.";

            try
            {
                file = new File(this.getDataPath());
                if (!file.exists())
                    file.createNewFile();
                OutputStream outputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(random_list);
                objectOutputStream.close();
                outputStream.close();
                retn_msg = "Random data saved successfully.";
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
                file = new File(this.getDataPath());
                if (!file.exists())
                    return "Random data not existed.";

                InputStream inputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                random_list = (ArrayList<String>) objectInputStream.readObject();
                objectInputStream.close();
                inputStream.close();
                return "Random data loaded successfully.";
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return "Unexpected errors occurred, check terminal for more info.";
            }
        }
        else
            retn_msg = help_info;

        return retn_msg;
    }

}
