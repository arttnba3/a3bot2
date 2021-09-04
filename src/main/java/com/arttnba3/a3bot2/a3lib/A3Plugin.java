package com.arttnba3.a3bot2.a3lib;

import net.lz1998.pbbot.bot.Bot;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/*
 * basic template of a plugin
 * by arttnba3
 * */

//a plugin in use should be with @Component
//@Component
public class A3Plugin extends A3PluginWrapper
{
    // the name of the plugin, needed for the plugin system
    String plugin_name;

    // define the command that would be used for this plugin, which shall not be more than one type
    List<String> command = null;

    // args analysed by plugin system
    String[] args;

    // file path of the data(may not be used for every plugin)
    String data_path = null;

    // whether the plugin is enabled, the default setting is true
    boolean is_enabled = true;

    // the admin of the bot
    long admin;

    // the permitted men
    List<Long> permission_list;

    public A3Plugin()
    {
        command = new ArrayList<String>();
    }

    // function which deal with private message
    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event)
    {
        if (!is_enabled)
            return MESSAGE_IGNORE;

        return MESSAGE_IGNORE;
    }

    // function which deal with group message
    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event)
    {
        if (!is_enabled)
            return MESSAGE_IGNORE;

        return MESSAGE_IGNORE;
    }

    public String getPluginName()
    {
        return plugin_name;
    }

    public String getCommand(int index)
    {
        return command.get(index);
    }

    public String[] getArgs()
    {
        return args;
    }

    public String getDataPath()
    {
        return data_path;
    }

    public long getAdmin()
    {
        return admin;
    }

    public List<Long> getPermissionList()
    {
        return permission_list;
    }

    public boolean isEnabled()
    {
        return is_enabled;
    }

    public boolean containsCommand(String command)
    {
        return this.command.contains(command);
    }

    public void addCommand(String command)
    {
        this.command.add(command);
    }

    public void setEnabled(boolean is_enabled)
    {
        this.is_enabled = is_enabled;
    }

    public void setArgs(String[] args)
    {
        this.args = args;
    }

    public void setAdmin(long admin)
    {
        this.admin = admin;
    }

    public void setPermissionList(List<Long> permission_list)
    {
        this.permission_list = permission_list;
    }

    public void setPluginName(String plugin_name)
    {
        this.plugin_name = plugin_name;
    }

    public void setDataPath(String data_path)
    {
        this.data_path = data_path;
    }
}
