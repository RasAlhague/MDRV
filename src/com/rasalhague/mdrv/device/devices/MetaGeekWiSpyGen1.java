package com.rasalhague.mdrv.device.devices;

import com.rasalhague.mdrv.Utility.NativeUtils;
import com.rasalhague.mdrv.device.core.Device;
import com.rasalhague.mdrv.logging.ApplicationLogger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Device class template. Use it for add new device support.
 */
public class MetaGeekWiSpyGen1 extends Device
{
    public final static String  FRIENDLY_NAME         = "MetaGeek Wi-Spy Gen 1";
    public final static String  VENDOR_ID             = "1781";
    public final static String  PRODUCT_ID            = "083E";
    public final static float   INITIAL_FREQUENCY     = 2399f;
    public final static float   CHANNEL_SPACING       = 989f;
    public final static byte[]  END_PACKET_SEQUENCE   = new byte[]{-1};
    public final static boolean MANUAL_DEVICE_CONTROL = true;

    public final static String NATIVE_LIB_PATH_LINUX      = "/com/rasalhague/mdrv/natives/MetaGeekWiSpyGen1.co";
    public final static String NATIVE_LIB_PATH_WINDOWS    = "/com/rasalhague/mdrv/natives/MetaGeekWiSpyGen1.dll";
    public final static String NATIVE_LIB_PATH_WINDOWS_32 = "/com/rasalhague/mdrv/natives/MetaGeekWiSpyGen1_32bit.dll";
    public final static String NATIVE_LIB_PATH_WINDOWS_64 = "/com/rasalhague/mdrv/natives/MetaGeekWiSpyGen1_64bit.dll";

    static
    {
        try
        {
            if (SystemUtils.IS_OS_LINUX)
            {
                NativeUtils.loadLibraryFromJar(NATIVE_LIB_PATH_LINUX);
            }
            //TODO dll wont work - java.lang.UnsatisfiedLinkError:  Can't find dependent libraries
            //            else if (SystemUtils.IS_OS_WINDOWS)
            //            {
            //                //for debugging
            //                //                System.load("D:\\Gallery\\Development_And_Programming\\C++\\Wi_Spy_Gen1_Detatch\\MetaGeekWiSpyGen1_64bit.dll");
            //
            //                if (SystemUtils.OS_ARCH.endsWith("64"))
            //                {
            //                    NativeUtils.loadLibraryFromJar(NATIVE_LIB_PATH_WINDOWS_64);
            //                }
            //                else
            //                {
            //                    NativeUtils.loadLibraryFromJar(NATIVE_LIB_PATH_WINDOWS_32);
            //                }
            //            }
        }
        catch (UnsatisfiedLinkError | IOException e)
        {
            ApplicationLogger.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Use this method for initialize your device. If device does not need initialization - leave this blank
     */
    @Override
    public void initializeDevice()
    {
        if ((SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS) && MANUAL_DEVICE_CONTROL)
        {
            releaseBeforeUnplugWindow();

            try
            {
                int errCode;

                errCode = libusbInitSession();
                assertErrorCode(errCode, "libusbInitSession");

                errCode = openDevice();
                assertErrorCode(errCode, "openDevice");

                errCode = detachKernelDriver();
                assertErrorCode(errCode, "detachKernelDriver");

                errCode = claimInterface();
                assertErrorCode(errCode, "claimInterface");
            }
            catch (UnsatisfiedLinkError unsatisfiedLinkError)
            {
                ApplicationLogger.getLogger().severe(unsatisfiedLinkError.getMessage());
                ApplicationLogger.getLogger().severe(Arrays.toString(unsatisfiedLinkError.getStackTrace()));
                ApplicationLogger.getLogger().severe("Problem with library loading");
                unsatisfiedLinkError.printStackTrace();
            }
            catch (Error error)
            {
                releaseResources();

                ApplicationLogger.getLogger().severe(error.getMessage());
                error.printStackTrace();
            }
        }
    }

    /**
     * Use this method for parse data which you device out. Return format - Byte array. Every item - RSSI in format
     * "-100".
     */
    @Override
    public ArrayList<Byte> parse(ArrayList<Byte> dataToParse)
    {
        System.out.println("parse: " + dataToParse);
        //        System.out.println(dataToParse.size());

        dataToParse.remove(0);

        for (int i = 0; i < dataToParse.size(); i++)
        {
            Byte aByte = dataToParse.get(i);
            dataToParse.set(i, (byte) ((aByte * 1.5) - 100));
        }

        //        System.out.println("parse: " + dataToParse + " : " + dataToParse.size());

        return new ArrayList<Byte>(dataToParse);
    }

    /**
     * Use this method for override default HIDUSB / COM read behavior. In most cases its usable for HIDUSB devices,
     * when default com.codeminders.hidapi library read method does not work.
     * <p>
     * !!! IMPORTANT !!! If you want to use this method you need to set USE_CUSTOM_READ_METHOD field to TRUE
     */
    @Override
    public byte[] customReadMethod()
    {
        try
        {
            final char[] rawReadedChars = readData();
            int rawArrayLength = rawReadedChars.length;
            byte[] rawReadedBytes = new byte[rawArrayLength];
            byte[] finalArray;

            //converting to decimal
            for (int i = 0; i < rawArrayLength; i++)
            {
                rawReadedBytes[i] = (byte) rawReadedChars[i];
            }

            if (rawReadedBytes[0] == 77) //mixing END_PACKET_SEQUENCE
            {
                finalArray = new byte[rawArrayLength + END_PACKET_SEQUENCE.length - 1];
                System.arraycopy(rawReadedBytes, 1, finalArray, 0, rawArrayLength - 1);
                System.arraycopy(END_PACKET_SEQUENCE, 0, finalArray, rawArrayLength - 1, END_PACKET_SEQUENCE.length);
            }
            else
            {
                finalArray = Arrays.copyOfRange(rawReadedBytes, 1, rawArrayLength);
            }

            //            System.out.println(Arrays.toString(rawReadedBytes));
            //            System.out.println(Arrays.toString(finalArray));

            return finalArray;
        }
        catch (Exception e)
        {
            releaseResources();

            ApplicationLogger.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }

        return new byte[0];
    }

    private void assertErrorCode(int errCode, String msg)
    {
        if (errCode != 0) throw new Error(msg + " " + errCode);
    }

    private void releaseBeforeUnplugWindow()
    {
        //need to release before unplugging coz otherwise after plugging in (second time and +) reads affected data
        //(problem with packet size)
        Platform.runLater(() -> {

            Stage dialog = new Stage();
            dialog.initStyle(StageStyle.UTILITY);

            Text text = new Text(25, 25, "Release " +
                    deviceInfo.getFriendlyNameWithId() +
                    " on " +
                    deviceInfo.getPortName() +
                    " before unplugging");
            Button button = new Button("Release");
            button.setOnMouseClicked(event -> {
                releaseResources();
                dialog.close();
            });
            button.setAlignment(Pos.CENTER);
            VBox vBox = new VBox(text, button);

            dialog.setScene(new Scene(vBox));
            dialog.show();
        });

    }

    private native int libusbInitSession();

    private native int openDevice();

    private native int detachKernelDriver();

    private native int claimInterface();

    private native char[] readData();

    private native int releaseResources();
}
