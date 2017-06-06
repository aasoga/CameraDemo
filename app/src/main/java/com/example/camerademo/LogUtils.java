package com.example.camerademo;

import android.util.Log;

public class LogUtils {
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int LEVEL = VERBOSE;
    private static final boolean IS_DEBUG_MODEL = true;

    public static void v(String tag, String message) {
        if (LEVEL <= VERBOSE && IS_DEBUG_MODEL) {
            Log.v(tag, message);
        }
    }

    public static void d(String tag, String message) {
        if (LEVEL <= DEBUG && IS_DEBUG_MODEL) {
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LEVEL <= INFO && IS_DEBUG_MODEL) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (LEVEL <= WARN && IS_DEBUG_MODEL) {
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LEVEL <= ERROR && IS_DEBUG_MODEL) {
            Log.e(tag, message);
        }
    }
    
//    public static void f(String tag, String message, Context context,
//            boolean isDivider) {
//        if (LEVEL <= DEBUG && Config.IS_DEBUG_MODEL) {
//            File logFile = getLogFile(context);
//
//            if (logFile != null) {
//                FileWriter writer = null;
//
//                try {
//                    writer = new FileWriter(logFile, true);
//
//                    if (!isDivider) {
//                        SimpleDateFormat formater = new SimpleDateFormat(
//                                "yyyy-MM-dd HH:mm:ss SSS");
//                        String date = formater.format(new Date(System
//                                .currentTimeMillis()));
//
//                        writer.write(date + " : ");
//                    }
//                    writer.write(tag);
//                    writer.write(message);
//                    writer.write("\r\n");
//                    writer.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                writer = null;
//                logFile = null;
//            }
//        }
//    }

//    private static File getLogFile(Context context) {
//        File logFile = null;
//        File cachDir = StorageUtils.getCacheDirectory(context);
//        if (cachDir != null) {
//            logFile = new File(cachDir, "/log.txt");
//            if (logFile != null) {
//                return logFile;
//            }
//        }
//        return null;
//    }
}
