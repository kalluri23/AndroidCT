package com.verizon.contenttransfer.utils;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import com.verizon.contenttransfer.base.VZTransferConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class LogUtil {

    public static boolean LOCAL_DEBUG = false;

    private static final String LOG_FILE = "contenttransfer.log";
    private static final String SDCARD_PATH = VZTransferConstants.VZTRANSFER_DIR;

    /*
     *  FILE_DEBUG and DEBUG flag will be reset in P2PStartupActivity based on BuildConfig setup.
     *
     */
    public static boolean FILE_DEBUG = false;//Do not change it.
    public static boolean DEBUG = false;   //Do not change it.
    /**
     * Log error
     *
     * @param tag
     *            The tag to use for this logging event
     * @param msg
     *            The message to print out for this logging event
     */
    public static void e(String tag, String msg) {
        if (msg != null) {
            if (DEBUG) {
                Log.e(tag, msg);
                // Log.e(tag, msg);
            }
            if (FILE_DEBUG) {
                writeToFile(tag, msg);
            }
        }
    }

    /**
     * Log debug
     *
     * @param tag
     *            The tag to use for this logging event
     * @param msg
     *            The message to print out for this logging event
     */
    public static void d(String tag, String msg) {
        if (msg != null) {
            if (DEBUG) {
                Log.d(tag,msg);
                // Log.d(tag, msg);
            }
            if (FILE_DEBUG) {
                writeToFile(tag, msg);
            }
        }
    }

    /**
     * Log verbose
     *
     * @param tag
     *            The tag to use for this logging event
     * @param msg
     *            The message to print out for this logging event
     */
    public static void v(String tag, String msg) {
        if (msg != null) {
            if (DEBUG) {
                Log.d(tag, msg);
                // LogUtil.d(tag, msg);
            }
            if (FILE_DEBUG) {
                writeToFile(tag, msg);
            }
        }
    }


    /**
     * Log Info
     *  Sang Added Jan2016
     * @param tag
     *            The tag to use for this logging event
     * @param msg
     *            The message to print out for this logging event
     */
    public static void i(String tag, String msg) {
        if (msg != null) {
            if (DEBUG) {
                Log.i(tag, msg);
                // LogUtil.d(tag, msg);
            }
            if (FILE_DEBUG) {
                writeToFile(tag, msg);
            }
        }
    }


    /**
     * Print the stack trace
     *
     * @param e
     *            The exception to use for this stack trace
     */
    public static void pst(Exception e) {
        if (DEBUG) {
            e.printStackTrace();
        }
        if (FILE_DEBUG) {
            writeStackTraceToFile(e);
        }
    }

    private static void writeToFile(String TAG, String msg) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        FileOutputStream dio = null;
        StringBuffer sf = new StringBuffer();
        sf.append(TAG + "\t");
        long time = System.currentTimeMillis();
        sf.append(DateFormat.format("MMM dd, yyyy h:mmaa", time));
        sf.append("\t" + msg + "\n");
        File file = new File(SDCARD_PATH);
        file.mkdirs();
        try {
            dio = new FileOutputStream(SDCARD_PATH + LOG_FILE, true);
            dio.write(sf.toString().getBytes());
            dio.close();
            sf = null;
            dio = null;
            file = null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dio != null) {
                try {
                    dio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dio = null;
            }
        }
    }

    private static void writeStackTraceToFile(Exception ex) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        File file = new File(SDCARD_PATH);
        file.mkdirs();
        file = null;
        FileOutputStream dio = null;

        try {
            dio = new FileOutputStream(SDCARD_PATH + LOG_FILE, true);
            ex.printStackTrace(new PrintStream(dio));
            dio.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dio != null) {
                try {
                    dio.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dio = null;
            }
        }
    }

    public static void setFileDebugging(boolean isOn) {
        FILE_DEBUG = isOn;
        if (FILE_DEBUG) {
            File f = new File(SDCARD_PATH + LOG_FILE);
            if (f.exists()) {
                f.delete();
            }
        }
    }

}
