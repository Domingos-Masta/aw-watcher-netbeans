/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author domingos.fernando
 */
public class Util
{

    public static final Logger log = Logger.getLogger("ActivityWatch");

    public static final String getDateFormat()
    {
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // T15:23:55Z Quoted "Z" to indicate UTC, no timezone offset
        return df.format(now);
    }

    public static String getFileExtension(String filePath)
    {
        String fileExt = FilenameUtils.getExtension(filePath);
        return fileExt.equals("") ? "Unknown" : fileExt;
    }

    public static void info(String msg)
    {
        log.log(Level.INFO, msg);
    }

    public static void warn(String msg)
    {
        log.log(Level.WARNING, msg);
    }

    public static void error(String msg)
    {
        log.log(Level.SEVERE, msg);
    }

    public static void debug(String msg)
    {
        log.log(Level.CONFIG, msg);
    }

    public static void errorDialog(String msg)
    {
        int msgType = NotifyDescriptor.ERROR_MESSAGE;
        NotifyDescriptor d = new NotifyDescriptor.Message(msg, msgType);
        DialogDisplayer.getDefault().notify(d);
    }

}
