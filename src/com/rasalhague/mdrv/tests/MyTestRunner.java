package com.rasalhague.mdrv.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class MyTestRunner
{
    public static void run()
    {
        Result result = JUnitCore.runClasses(CP2102.class);
        for (Failure failure : result.getFailures())
        {
            System.out.println(failure.toString());
        }
    }
}
