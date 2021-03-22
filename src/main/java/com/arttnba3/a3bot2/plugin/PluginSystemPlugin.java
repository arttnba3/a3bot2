package com.arttnba3.a3bot2.plugin;

import com.arttnba3.a3bot2.a3lib.A3Plugin;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
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
public class PluginSystemPlugin extends BotPlugin
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
    FileInputStream fileInputStream = null;
    FileOutputStream fileOutputStream = null;

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

            File permission_data = new File("data/permission.data");
            if (!permission_data.exists())
                return ;

            InputStream inputStream = new FileInputStream(permission_data);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            permission_list = (List) objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("\033[31m\033[1m[x] Unable to initialize permission list, something\'s wrong." + "\033[0m");
        }
    }

    public int analyseArgs(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent group_event, @NotNull OnebotEvent.PrivateMessageEvent private_event, int type, String[] args)
    {
        A3Plugin plugin = null;
        boolean command_available = false;
        for (int i = 0; i < plugin_list.size(); i++)
        {
            plugin = plugin_list.get(i);
            if (args[0].equals(plugin.getCommand()) && plugin.isEnabled())
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

    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        long userId = event.getUserId();
        long groupId = event.getGroupId();
        String msg = event.getRawMessage();

        String[] args = msg.split(" ");

        // not the command
        if (args[0].length() == 0 || args[0].charAt(0) != '/')
            return MESSAGE_IGNORE;

        // command for plugin system
        if (args[0].equals("/plugin"))
        {
            if (args.length == 1)
            {
                String message = "当前所启用的插件有：";
                A3Plugin a3_plugin;
                for (int i = 0; i < plugin_list.size(); i++)
                {
                    a3_plugin = plugin_list.get(i);
                    if (a3_plugin.isEnabled())
                        message += "\n"+a3_plugin.getPluginName();
                }
                bot.sendGroupMsg(groupId,message,false);
            }
            else
            {
                String args_1 = args[1].toLowerCase();
                if (args_1.equals("all"))
                {
                    String message = "当前所装载的插件有：";
                    A3Plugin a3_plugin;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        a3_plugin = plugin_list.get(i);
                        message += "\n" + a3_plugin.getPluginName() + (a3_plugin.isEnabled()?"":"（未启用）");
                    }
                    bot.sendGroupMsg(groupId,message,false);
                }

                // plugin load & unload system
                else if (args_1.equals("load") || args_1.equals("unload"))
                {
                    if(userId != admin && !permission_list.contains(userId))
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    if (args.length < 3)
                    {
                        bot.sendGroupMsg(groupId,  "Invalid plugin name: (null)", false);
                        return MESSAGE_BLOCK;
                    }

                    String plugin_name = args[2];
                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                        if (plugin_list.get(i).getPluginName().equals(plugin_name))
                        {
                            plugin = plugin_list.get(i);
                            break;
                        }

                    if (plugin == null)
                    {
                        bot.sendGroupMsg(groupId,  "Invalid plugin name: " + plugin_name, false);
                        return MESSAGE_BLOCK;
                    }

                    if (args_1.equals("load"))
                    {
                        if (plugin.isEnabled())
                        {
                            bot.sendGroupMsg(groupId,  "Plugin: [" + plugin_name + "] already loaded.", false);
                            return MESSAGE_BLOCK;
                        }
                        plugin.setEnabled(true);
                        bot.sendGroupMsg(groupId,  "Plugin: [" + plugin_name + "] loaded successfully.", false);
                        return MESSAGE_BLOCK;
                    }
                    else
                    {
                        if (!plugin.isEnabled())
                        {
                            bot.sendGroupMsg(groupId,  "Plugin: [" + plugin_name + "] already unloaded.", false);
                            return MESSAGE_BLOCK;
                        }
                        plugin.setEnabled(false);
                        bot.sendGroupMsg(groupId,  "Plugin: [" + plugin_name + "] unloaded successfully.", false);
                        return MESSAGE_BLOCK;
                    }
                }

                // load and save enable status
                else if (args_1.equals("enable-save"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        Map plugin_status = new HashMap<String, Boolean>();
                        A3Plugin plugin = null;
                        for (int i = 0; i < plugin_list.size(); i++)
                        {
                            plugin = plugin_list.get(i);
                            plugin_status.put(plugin.getPluginName(), plugin.isEnabled());
                        }

                        File permission_data = new File("data/plugin_status.data");
                        if (!permission_data.exists())
                            permission_data.createNewFile();
                        OutputStream outputStream = new FileOutputStream(permission_data);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(plugin_status);
                        objectOutputStream.close();
                        outputStream.close();
                        bot.sendGroupMsg(groupId,"Plugins\'s statuses saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("enable-load"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        Map plugin_status;
                        File permission_data = new File("data/plugin_status.data");
                        if (!permission_data.exists())
                        {
                            bot.sendGroupMsg(groupId, "statuses data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(permission_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        plugin_status = (HashMap) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();

                        A3Plugin plugin = null;
                        for (int i = 0; i < plugin_list.size(); i++)
                        {
                            plugin = plugin_list.get(i);
                            if (plugin_status.containsKey(plugin.getPluginName()))
                                plugin.setEnabled((boolean) plugin_status.get(plugin.getPluginName()));
                        }

                        bot.sendGroupMsg(groupId,"Plugins\'s statuses loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }

                // permission data for plugin system
                else if (args_1.equals("sys-save"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File permission_data = new File("data/permission.data");
                        if (!permission_data.exists())
                            permission_data.createNewFile();
                        OutputStream outputStream = new FileOutputStream(permission_data);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(permission_list);
                        objectOutputStream.close();
                        outputStream.close();
                        bot.sendGroupMsg(groupId,"Permission data saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("sys-load"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File permission_data = new File("data/permission.data");
                        if (!permission_data.exists())
                        {
                            bot.sendGroupMsg(groupId, "permission data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(permission_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        permission_list = (List) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();
                        bot.sendGroupMsg(groupId,"Permission data loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendGroupMsg(groupId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("sys-clear"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    permission_list.clear();
                    bot.sendGroupMsg(groupId, "Permision list cleared.", false);
                }
                else if (args_1.equals("sys-init"))
                {
                    if(userId != admin)
                    {
                        bot.sendGroupMsg(groupId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        plugin = plugin_list.get(i);
                        plugin.setAdmin(admin);
                        plugin.setPermissionList(permission_list);
                    }
                    bot.sendGroupMsg(groupId, "Success.", false);
                }

                // others under "/plugin", print help info for it
                else
                {
                    bot.sendGroupMsg(groupId, help_info, false);
                    return MESSAGE_BLOCK;
                }
            }

            return MESSAGE_BLOCK;
        }

        // analyse command for other plugins
        return analyseArgs(bot, event, null, TYPE_MESSAGE_GROUP, args);
    }

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        long userId = event.getUserId();
        String msg = event.getRawMessage();

        String[] args = msg.split(" ");

        // not the command
        if (args[0].length() == 0 || args[0].charAt(0) != '/')
            return MESSAGE_IGNORE;

        // command for plugin system
        if (args[0].equals("/plugin"))
        {
            if (args.length == 1)
            {
                String message = "当前所启用的插件有：";
                A3Plugin a3_plugin;
                for (int i = 0; i < plugin_list.size(); i++)
                {
                    a3_plugin = plugin_list.get(i);
                    if (a3_plugin.isEnabled())
                        message += "\n"+a3_plugin.getPluginName();
                }
                bot.sendPrivateMsg(userId,message,false);
            }
            else
            {
                String args_1 = args[1].toLowerCase();
                if (args_1.equals("all"))
                {
                    String message = "当前所装载的插件有：";
                    A3Plugin a3_plugin;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        a3_plugin = plugin_list.get(i);
                        message += "\n" + a3_plugin.getPluginName() + (a3_plugin.isEnabled()?"":"（未启用）");
                    }
                    bot.sendPrivateMsg(userId,message,false);
                }

                // plugin load & unload system
                else if (args_1.equals("load") || args_1.equals("unload"))
                {
                    if(userId != admin && !permission_list.contains(userId))
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    if (args.length < 3)
                    {
                        bot.sendPrivateMsg(userId,  "Invalid plugin name: (null)", false);
                        return MESSAGE_BLOCK;
                    }

                    String plugin_name = args[2];
                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                        if (plugin_list.get(i).getPluginName().equals(plugin_name))
                        {
                            plugin = plugin_list.get(i);
                            break;
                        }

                    if (plugin == null)
                    {
                        bot.sendPrivateMsg(userId,  "Invalid plugin name: " + plugin_name, false);
                        return MESSAGE_BLOCK;
                    }

                    if (args_1.equals("load"))
                    {
                        if (plugin.isEnabled())
                        {
                            bot.sendPrivateMsg(userId,  "Plugin: [" + plugin_name + "] already loaded.", false);
                            return MESSAGE_BLOCK;
                        }
                        plugin.setEnabled(true);
                        bot.sendPrivateMsg(userId,  "Plugin: [" + plugin_name + "] loaded successfully.", false);
                        return MESSAGE_BLOCK;
                    }
                    else
                    {
                        if (!plugin.isEnabled())
                        {
                            bot.sendPrivateMsg(userId,  "Plugin: [" + plugin_name + "] already unloaded.", false);
                            return MESSAGE_BLOCK;
                        }
                        plugin.setEnabled(false);
                        bot.sendPrivateMsg(userId,  "Plugin: [" + plugin_name + "] unloaded successfully.", false);
                        return MESSAGE_BLOCK;
                    }
                }

                // load and save enable status
                else if (args_1.equals("enable-save"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        Map plugin_status = new HashMap<String, Boolean>();
                        A3Plugin plugin = null;
                        for (int i = 0; i < plugin_list.size(); i++)
                        {
                            plugin = plugin_list.get(i);
                            plugin_status.put(plugin.getPluginName(), plugin.isEnabled());
                        }

                        File permission_data = new File("data/plugin_status.data");
                        if (!permission_data.exists())
                            permission_data.createNewFile();
                        OutputStream outputStream = new FileOutputStream(permission_data);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(plugin_status);
                        objectOutputStream.close();
                        outputStream.close();
                        bot.sendPrivateMsg(userId,"Plugins\'s statuses saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("enable-load"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        Map plugin_status;
                        File permission_data = new File("data/plugin_status.data");
                        if (!permission_data.exists())
                        {
                            bot.sendPrivateMsg(userId, "statuses data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(permission_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        plugin_status = (HashMap) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();

                        A3Plugin plugin = null;
                        for (int i = 0; i < plugin_list.size(); i++)
                        {
                            plugin = plugin_list.get(i);
                            if (plugin_status.containsKey(plugin.getPluginName()))
                                plugin.setEnabled((boolean) plugin_status.get(plugin.getPluginName()));
                        }

                        bot.sendPrivateMsg(userId,"Plugins\'s statuses loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }

                // permission data for plugin system
                else if (args_1.equals("sys-save"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File permission_data = new File("data/permission.data");
                        if (!permission_data.exists())
                            permission_data.createNewFile();
                        OutputStream outputStream = new FileOutputStream(permission_data);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(permission_list);
                        objectOutputStream.close();
                        outputStream.close();
                        bot.sendPrivateMsg(userId,"Permission data saved successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("sys-load"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    try
                    {
                        File permission_data = new File("data/permission.data");
                        if (!permission_data.exists())
                        {
                            bot.sendPrivateMsg(userId, "permission data not existed.", false);
                            return MESSAGE_BLOCK;
                        }

                        InputStream inputStream = new FileInputStream(permission_data);
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        permission_list = (List) objectInputStream.readObject();
                        objectInputStream.close();
                        inputStream.close();
                        bot.sendPrivateMsg(userId,"Permission data loaded successfully.",false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        bot.sendPrivateMsg(userId, "Unexpected errors occurred, check terminal for more info.", false);
                    }
                }
                else if (args_1.equals("sys-clear"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    permission_list.clear();
                    bot.sendPrivateMsg(userId, "Permision list cleared.", false);
                }
                else if (args_1.equals("sys-init"))
                {
                    if(userId != admin)
                    {
                        bot.sendPrivateMsg(userId, "Permission denied. Authorization limited.", false);
                        return MESSAGE_BLOCK;
                    }

                    A3Plugin plugin = null;
                    for (int i = 0; i < plugin_list.size(); i++)
                    {
                        plugin = plugin_list.get(i);
                        plugin.setAdmin(admin);
                        plugin.setPermissionList(permission_list);
                    }
                    bot.sendPrivateMsg(userId, "Success.", false);
                }

                //others
                else
                {
                    bot.sendPrivateMsg(userId, help_info, false);
                    return MESSAGE_BLOCK;
                }
            }

            return MESSAGE_BLOCK;
        }

        // analyse command for other plugins
        return analyseArgs(bot, null, event, TYPE_MESSAGE_PRIVATE, args);
    }

}
