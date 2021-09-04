package com.arttnba3.a3bot2.a3lib;
import net.lz1998.pbbot.bot.BotPlugin;
import java.io.*;

public class A3PluginWrapper extends BotPlugin
{
    // some macros
    public final String MSG_PERMISSION_DENIED = "Permission denied, authorization limited.",
            MSG_ERRORS_OCCUR = "Unexpected errors occurred, check terminal for more info.",
            MSG_SUCCESS = "Success.";

    // read object from file
    public Object readData(String path)
    {
        File file = new File(path);
        try
        {
            if (!file.exists())
                return null;

            FileInputStream fileInputStream = new FileInputStream(file);
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();
            return object;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // write object into file
    public boolean saveData(Object object, String path)
    {
        File file = new File(path);

        try
        {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            if (!file.exists())
                file.createNewFile();
            OutputStream outputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            outputStream.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
