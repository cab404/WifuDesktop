package com.cab404.wifu.util;

import com.cab404.wifu.base.WifiLoginModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Well, sorry for no comments here!
 * Still you can send me your question to me@cab404.ru!
 * <p/>
 * Created at 14:42 on 04/12/15
 *
 * @author cab404
 */
public class LogManager {

    private static LogManager instance;
    private File dir;

    public static LogManager getInstance() {
        return instance;
    }

    public static void init(File dir){
        instance = new LogManager(dir);
    }

    private LogManager(File dir) {
        this.dir = dir;
    }

    public class FileLog implements WifiLoginModule.Log {
        private PrintStream in;
        private String tag;

        public FileLog(String tag, File file) throws FileNotFoundException {
            this.tag = tag;
            in = new PrintStream(new FileOutputStream(file, true), true);
        }
        
        String ts(){
            return SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Calendar.getInstance().getTime());
        }

        @Override
        public synchronized void e(Throwable t, String message) {
            System.out.printf(" %-15s\t%s\t[E] %s%n", tag, ts(), message);
            in.println(ts() + " [E] " + message);
            t.printStackTrace(in);
            t.printStackTrace(System.out);
            in.flush();
        }

        @Override
        public synchronized void v(String message) {
            System.out.printf(" %-15s\t%s\t[V] %s%n", tag, ts(), message);
            in.println(ts() + " [V] " + message);
        }

        @Override
        public synchronized void alert(String message) {
            System.out.printf(" %-15s\t%s\t[A] %s%n", tag, ts(), message);
            in.println(ts() + " [A] " + message);
            in.flush();
        }
    }

    Map<String, FileLog> logs = new HashMap<>();

    public WifiLoginModule.Log generateLog(String tag) {
        try {
        if (logs.containsKey(tag)) return logs.get(tag);
            FileLog nlog = new FileLog(tag, new File(dir, tag + ".txt"));
            logs.put(tag, nlog);
            return nlog;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot create log!", e);
        }
    }

}
