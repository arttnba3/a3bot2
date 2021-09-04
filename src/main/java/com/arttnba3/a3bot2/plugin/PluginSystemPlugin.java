package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import com.arttnba3.a3bot2.a3lib.A3PluginWrapper;
import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.ini4j.Wini;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PluginSystemPlugin extends A3PluginWrapper
{
    final int TYPE_MESSAGE_GROUP = 0;
    final int TYPE_MESSAGE_PRIVATE = 1;

    final String help_info = "a3bot插件系统v2.0\n"
            + "用法：\n"
            + "/plugin        --显示已装载的插件\n"
            + "/plugin all    --显示所有的插件\n"
            + "以下指令仅被授权者可使用：\n"
            + "/plugin load [name]    -- 装载某个插件\n"
            + "/plugin unload [name]  -- 卸载某个插件\n"
            + "以下指令仅管理员可使用: \n"
            + "/plugin add [num]     --授予某人插件系统权限\n"
            + "/plugin del [num]     --剥夺某人插件系统权限\n"
            + "/plugin enable-load   --载入插件原有开启状态\n"
            + "/plugin enable-save   --保存插件开启状态\n"
            + "/plugin sys-load      --载入被授权者名单\n"
            + "/plugin sys-save      --保存被授权者名单\n"
            + "/plugin sys-clear     --清空被授权者名单\n"
            + "/plugin sys-init      --重新赋值各插件授权者名单\n"
            + "多余的参数会被自动丢弃";

    long admin;
    List permission_list = new ArrayList<Long>();

    @Autowired
    private List<A3Plugin> plugin_list;

    @SuppressWarnings("unchecked")
    public PluginSystemPlugin()
    {
        File conf_file = new File("conf.ini");
        File data_dir = new File("data");
        try
        {
            if (!conf_file.exists())
            {
                System.out.println("\033[31m\033[1m[x][a3bot2 Plugin System:] config file not existed!\033[0m");
                System.exit(-1);
            }

            if (!data_dir.exists())
                data_dir.mkdirs();

            Wini ini = new Wini(conf_file);
            admin = ini.get("config", "admin", int.class);
            System.out.println("\033[32m\033[1m[+] admin: " + String.valueOf(admin) + "\033[0m");

            Object object = readData("data/permission.data");
            if (object != null)
                permission_list = (List) object;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("\033[31m\033[1m[x] Unable to initialize , something\'s wrong." + "\033[0m");
        }
    }

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        String msg = event.getRawMessage();

        String[] args = messageParser(msg);//msg.split(" ");
        if (args == null) // failed to analyze message into available args
            return MESSAGE_IGNORE;

        // not the command
        if (args[0].charAt(0) != '/')
            return MESSAGE_IGNORE;

        // analyse command for other plugins
        return analyseArgs(bot, event, null, TYPE_MESSAGE_GROUP, args);
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        String msg = event.getRawMessage();

        String[] args = messageParser(msg);//msg.split(" ");
        if (args == null) // failed to analyze message into available args
            return MESSAGE_IGNORE;

        // not the command
        if (args[0].charAt(0) != '/')
            return MESSAGE_IGNORE;

        // analyse command for other plugins
        return analyseArgs(bot, null, event, TYPE_MESSAGE_PRIVATE, args);
    }

    public String[] messageParser(String msg)
    {
        ArrayList<String> args = new ArrayList<>();
        StringBuilder per_arg = new StringBuilder();
        boolean backslash = false, str_start = false, quotation = false, is_space = false;
        char current;

        for (int i = 0, len = msg.length(); i < len; i++)
        {
            current = msg.charAt(i);
            if (backslash)
            {
                backslash = false;
                switch (current)
                {
                    case 'n':
                        current = '\n';
                        break;
                    case 'b':
                        current = '\b';
                        break;
                    case 'f':
                        current = '\f';
                        break;
                    case 't':
                        current = '\t';
                        break;
                    default:
                        break;
                }
                per_arg.append(current);
                continue;
            }
            if (current == '\\') // backslash is false
            {
                backslash = true;
                continue;
            }

            if (per_arg.length() > 0)
                str_start = true;

            if (current == '"')
            {
                if (str_start && !quotation)
                    return null;    // illegal string

                if (!str_start && !quotation)
                {
                    quotation = true;
                    str_start = true;
                    continue;
                }

                args.add(per_arg.toString());
                per_arg = new StringBuilder();
                str_start = false;
                quotation = false;
                continue;
            }

            if (current == ' ')
            {
                if (!str_start)
                    continue;
                if (str_start && !quotation)
                {
                    args.add(per_arg.toString());
                    per_arg = new StringBuilder();
                    str_start = false;
                    continue;
                }
            }

            per_arg.append(current);
        }

        if (quotation) // unaccomplished string with '"'
            return null;

        if (per_arg.length() > 0)
            args.add(per_arg.toString());

        return args.toArray(new String[args.size()]);
    }

    public int analyseArgs(@NotNull Bot bot, OnebotEvent.GroupMessageEvent group_event, OnebotEvent.PrivateMessageEvent private_event, int type, String[] args)
    {
        A3Plugin plugin = null;
        boolean command_available = false;

        if (args[0].equals("/plugin"))
            return pluginSystemCommander(bot, group_event, private_event, type, args);

        for (int i = 0; i < plugin_list.size(); i++)
        {
            plugin = plugin_list.get(i);
            if (plugin.containsCommand(args[0]) && plugin.isEnabled())
            {
                plugin.setArgs(args);
                plugin.setAdmin(admin);
                plugin.setPermissionList(permission_list);
                command_available = true;
                break;
            }
        }

        if (!command_available)
            return MESSAGE_IGNORE;

        // call corresponding method to deal with messages
        switch (type)
        {
            case TYPE_MESSAGE_GROUP:
                plugin.onGroupMessage(bot, group_event);
                break;
            case TYPE_MESSAGE_PRIVATE:
                plugin.onPrivateMessage(bot, private_event);
                break;
        }

        return MESSAGE_BLOCK;
    }

    // dealing with the specific command "/plugin" for the plugin system
    public int pluginSystemCommander(@NotNull Bot bot, OnebotEvent.GroupMessageEvent group_event, OnebotEvent.PrivateMessageEvent private_event, int type, String[] args)
    {
        String reply_msg = null;
        long user_id = (type == TYPE_MESSAGE_GROUP) ? group_event.getUserId() : private_event.getUserId();

        if (args.length == 1)
        {
            reply_msg = "当前所启用的插件有：";
            A3Plugin a3_plugin;
            for (int i = 0; i < plugin_list.size(); i++)
            {
                a3_plugin = plugin_list.get(i);
                if (a3_plugin.isEnabled())
                    reply_msg += "\n"+a3_plugin.getPluginName();
            }
        }
        else
        {
            if (args[1].toLowerCase().equals("all"))
            {
                reply_msg = "当前所启用的插件有：";
                A3Plugin a3_plugin;
                for (int i = 0; i < plugin_list.size(); i++)
                {
                    a3_plugin = plugin_list.get(i);
                    if (a3_plugin.isEnabled())
                        reply_msg  += "\n" + a3_plugin.getPluginName() + (a3_plugin.isEnabled()?"":"（未启用）");
                }
            }

            // plugin load & unload system
            else if (args[1].equals("load") || args[1].equals("unload"))
            {
                if(user_id != admin && !permission_list.contains(user_id))
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                if (args.length < 3)
                    return messageSender(bot, group_event, private_event, type, "Invalid plugin name: (null)");

                String plugin_name = args[2];
                A3Plugin plugin = null;
                for (int i = 0; i < plugin_list.size(); i++)
                    if (plugin_list.get(i).getPluginName().equals(plugin_name))
                    {
                        plugin = plugin_list.get(i);
                        break;
                    }

                if (plugin == null)
                    return messageSender(bot, group_event, private_event, type, "Invalid plugin name: " + plugin_name);

                if (args[1].equals("load"))
                {
                    if (plugin.isEnabled())
                        return messageSender(bot, group_event, private_event, type, "Plugin: [" + plugin_name + "] already loaded.");

                    plugin.setEnabled(true);
                    return messageSender(bot, group_event, private_event, type, "Plugin: [" + plugin_name + "] loaded successfully.");
                }
                else
                {
                    if (!plugin.isEnabled())
                        return messageSender(bot, group_event, private_event, type, "Plugin: [" + plugin_name + "] already unloaded.");

                    plugin.setEnabled(false);
                    return messageSender(bot, group_event, private_event, type, "Plugin: [" + plugin_name + "] unloaded successfully.");
                }
            }

            // load and save enable status
            else if (args[1].equals("enable-save"))
            {
                if(user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                try
                {
                    Map plugin_status = new HashMap<String, Boolean>();
                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        plugin = plugin_list.get(i);
                        plugin_status.put(plugin.getPluginName(), plugin.isEnabled());
                    }

                    if (this.saveData(plugin_status, "data/plugin_status.data"))
                        reply_msg = "Plugins\'s statuses saved successfully.";
                    else
                        reply_msg = this.MSG_ERRORS_OCCUR;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return messageSender(bot, group_event, private_event, type,  this.MSG_ERRORS_OCCUR);
                }
            }
            else if (args[1].equals("enable-load"))
            {
                if(user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                Object object = this.readData("data/plugin_status.data");
                if (object != null)
                {
                    Map plugin_status;
                    plugin_status = (HashMap) object;
                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        plugin = plugin_list.get(i);
                        if (plugin_status.containsKey(plugin.getPluginName()))
                            plugin.setEnabled((boolean) plugin_status.get(plugin.getPluginName()));
                    }
                    reply_msg = "Plugins\'s statuses loaded successfully.";
                }
                else
                    reply_msg = "Statuses data not existed or errors occurred.";
            }

            // permission data for plugin system
            else if (args[1].equals("sys-save"))
            {
                if(user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                reply_msg = (this.saveData(permission_list, "data/permission.data") ? "Permission data saved successfully." : this.MSG_ERRORS_OCCUR);
            }
            else if (args[1].equals("sys-load"))
            {
                if(user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                Object object = this.readData("data/permission.data");
                if (object != null)
                {
                    permission_list = (List) object;
                    reply_msg = "Permission data loaded successfully.";
                }
                else
                    reply_msg = "Permission data not existed or errors occurred.";
            }
            else if (args[1].equals("sys-clear"))
            {
                if(user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                permission_list.clear();
                reply_msg = "Permision list cleared.";
            }
            else if (args[1].equals("sys-init"))
            {
                if (user_id != admin)
                    return messageSender(bot, group_event, private_event, type, this.MSG_PERMISSION_DENIED);

                A3Plugin plugin = null;
                for (int i = 0; i < plugin_list.size(); i++)
                {
                    plugin = plugin_list.get(i);
                    plugin.setAdmin(admin);
                    plugin.setPermissionList(permission_list);
                }
                reply_msg = this.MSG_SUCCESS;
            }
            else // others under "/plugin", print help info for it
                reply_msg = help_info;
        }

        return messageSender(bot, group_event, private_event, type, reply_msg);
    }

    public int messageSender(@NotNull Bot bot, OnebotEvent.GroupMessageEvent group_event, OnebotEvent.PrivateMessageEvent private_event, int type, String msg)
    {
        switch (type)
        {
            case TYPE_MESSAGE_GROUP:
                bot.sendGroupMsg(group_event.getGroupId(), msg, false);
                break;
            case TYPE_MESSAGE_PRIVATE:
                bot.sendPrivateMsg(private_event.getUserId(), msg, false);
                break;
        }

        return MESSAGE_BLOCK;
    }
}
