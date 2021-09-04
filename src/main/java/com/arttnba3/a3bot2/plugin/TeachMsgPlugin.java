package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TeachMsgPlugin extends A3Plugin
{
    Map<String, String> teach_info;
    int TYPE_SAVE = 1, TYPE_LOAD = 2, TYPE_CLEAR = 3, TYPE_UNDEFINE = 0;
    String help_info = "教学插件~来教bot说话吧~\n"
            + "用法：\n"
            + "/teach {ask} {reply}\n"
            + "/delete {ask}\n"
            + "多余的输入项会被自动忽略";

    public TeachMsgPlugin()
    {
        this.addCommand("/teach");
        this.addCommand("/delete");
        this.setPluginName("TeachMsgPlugin");
        this.setDataPath("data/teach.data");
        Object object = this.readData(this.getDataPath());
        teach_info = (object != null ? (HashMap) object : new HashMap<String, String>());
    }

    public String getTeachMsg(String msg)
    {
        return teach_info.get(msg);
    }

    public void addTeachMsg(String ask, String reply)
    {
        teach_info.put(ask, reply);
    }

    public boolean delTeachMsg(String msg)
    {
        return (teach_info.remove(msg) == null) ? false : true;
    }

    public String dataManager(int type)
    {
        if (type == TYPE_SAVE)
        {
            if (this.saveData(this.teach_info, this.getDataPath()))
                return "数据保存成功 OvO";
            else
                return "出现了一些问题，数据没办法保存呐 >꒫<";
        }
        else if (type == TYPE_LOAD)
        {
            Object object = this.readData(this.getDataPath());
            if (object != null)
            {
                this.teach_info = (HashMap<String, String>) object;
                return "数据恢复成功 > <";
            }
            else
                return "出现了一些问题，数据没办法载入呐 >꒫<";
        }
        else if (type == TYPE_CLEAR)
        {
            this.teach_info.clear();
            return "数据清除成功 > <";
        }

        return "Invalid type, inner error.";
    }

    public String argsAnalyzer(String[] args, long user_id)
    {
        if (args == null)
            return null;

        if (args[0].equals("/teach"))
        {
            if (args.length < 3)
            {
                int type;
                if ((type = (args[1].equals("save")?TYPE_SAVE:(args[1].equals("load")?TYPE_LOAD:(args[1].equals("clear")?TYPE_CLEAR:TYPE_UNDEFINE)))) != TYPE_UNDEFINE)
                    if (user_id != this.getAdmin() && !this.getPermissionList().contains(user_id))
                        return this.MSG_PERMISSION_DENIED;
                    else
                        return this.dataManager(type);

                return help_info;
            }

            addTeachMsg(args[1], args[2]);
            return "呐，我学会了哟，你呢w";
        }
        else if (args[0].equals("/delete"))
        {
            if (args.length < 2)
                return help_info;

            if (delTeachMsg(args[1]))
                return "anosa..我好像...忘了点什么...";
            else
                return "呐，人家还没学过这个呀，不如你先教教人家好啦w";
        }

        return null;
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        if (!isEnabled())
            return MESSAGE_IGNORE;

        long group_id = event.getGroupId();
        long user_id = event.getUserId();
        String msg = argsAnalyzer(getArgs(), user_id);
        this.setArgs(null);

        if (msg == null)
            msg = getTeachMsg(event.getRawMessage());
        if (msg != null)
        {
            bot.sendGroupMsg(group_id, msg, true);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        if (!isEnabled())
            return MESSAGE_IGNORE;

        long user_id = event.getUserId();
        String msg = argsAnalyzer(getArgs(), user_id);
        this.setArgs(null);

        if (msg == null)
            msg = getTeachMsg(event.getRawMessage());
        if (msg != null)
        {
            bot.sendPrivateMsg(user_id, msg, true);
            return MESSAGE_BLOCK;
        }

        return MESSAGE_IGNORE;
    }
}
