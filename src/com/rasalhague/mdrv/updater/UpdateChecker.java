package com.rasalhague.mdrv.updater;

import com.rasalhague.mdrv.logging.ApplicationLogger;
import javafx.application.Platform;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker implements Runnable
{
    private static UpdateChecker instance;
    private        Version       currentVersion;
    private        Version       repoVersion;
    private static final int CONNECTION_TIMEOUT = 3000;

    private UpdateChecker()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    public static void initialize()
    {
        instance = new UpdateChecker();
    }

    public static UpdateChecker getInstance()
    {
        return instance;
    }

    @Override
    public void run()
    {
        initCurrentVersion();

        if (checkInternetConnection())
        {
            initRepoVersion();

            if (repoVersion.compare(currentVersion) == 1)
            {
                showUpdateDialog();
            }
        }
        else
        {
            ApplicationLogger.LOGGER.info("Can not connect to update server");
        }
    }

    private void showUpdateDialog()
    {
        Platform.runLater(() -> {

            String message = "Update available\n\nDo you want to update?";

            Action action = Dialogs.create()
                                   .owner(null)
                                   .style(DialogStyle.UNDECORATED)
                                   .lightweight()
                                   .title(null)
                                   .masthead(null)
                                   .message(message)
                                   .actions(Dialog.Actions.NO, Dialog.Actions.YES)
                                   .showConfirm();

            if (action == Dialog.Actions.YES)
            {
                try
                {
                    Runtime.getRuntime()
                           .exec("xdg-open " + "https://github.com/RasAlhague/MDRV");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initCurrentVersion()
    {
        //        Properties prop = new Properties();
        //        try
        //        {
        //            prop.load(getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
        //        }
        //        catch (IOException e)
        //        {
        //            ApplicationLogger.LOGGER.info(e.getMessage());
        //            e.printStackTrace();
        //        }
        //
        //        currentVersion = prop.getProperty("Implementation-Version");

        String manifestVersion = getCurrentManifestVersion();

        if (manifestVersion == null)
        {
            //For testing purposes only
            currentVersion = new Version("0.2");

            ApplicationLogger.LOGGER.warning("Debugging: currentVersion = new Version(\"0.2\");");
        }
        else
        {
            currentVersion = new Version(getClass().getPackage()
                                                   .getImplementationVersion());
        }

        ApplicationLogger.LOGGER.info("Current version: " + currentVersion);
    }

    private boolean checkInternetConnection()
    {
        Socket socket = new Socket();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("github.com", 80);
        try
        {
            socket.connect(inetSocketAddress, CONNECTION_TIMEOUT);
        }
        catch (IOException e)
        {
            ApplicationLogger.LOGGER.warning(e.getMessage());

            return false;
        }

        return true;
    }

    private void initRepoVersion()
    {
        String content;
        URLConnection connection;
        try
        {
            connection = new URL("https://raw.githubusercontent.com/RasAlhague/MDRV/master/src/META-INF/MANIFEST.MF").openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();

            Matcher matcher = Pattern.compile("Implementation-Version: (?<ImplementationVersion>.*)")
                                     .matcher(content);

            while (matcher.find())
            {
                repoVersion = new Version(matcher.group("ImplementationVersion"));
            }

            ApplicationLogger.LOGGER.info("repoVersion: " + repoVersion);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String getCurrentManifestVersion()
    {
        return getClass().getPackage()
                         .getImplementationVersion();
    }
}
