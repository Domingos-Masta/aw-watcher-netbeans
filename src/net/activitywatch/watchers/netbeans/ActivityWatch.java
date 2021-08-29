/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans;

import net.activitywatch.watchers.netbeans.util.ConfigFile;
import net.activitywatch.watchers.netbeans.util.UpdateHandler;
import net.activitywatch.watchers.netbeans.model.EventData;
import net.activitywatch.watchers.netbeans.model.Event;
import net.activitywatch.watchers.netbeans.listeners.CustomDocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.activitywatch.watchers.netbeans.model.Buckets;
import net.activitywatch.watchers.netbeans.requests.RequestHandler;
import net.activitywatch.watchers.netbeans.util.Consts;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;
import org.openide.windows.WindowManager;

/**
 *
 * @author domingos.fernando
 */
@OnShowing
public class ActivityWatch extends ModuleInstall implements Runnable
{

    public static final Logger log = Logger.getLogger("ActivityWatch");
    public static final short FREQUENCY = 2; // minutes between pings
    public static final Integer MAX_HEART_BEAT_TIME = 1;

    // For not error
    public static String VERSION = "Unknown";
    public static String IDE_VERSION = "Unknown";
    public static String HOST_NAME = "Unknown";
    public static Boolean DEBUG = false;
    public static Boolean READY = false;

    public static String lastFile = null;
    public static long lastTime = 0;

    public static Buckets buckets;
    public static CustomDocumentListener documentListener = null;

    public ActivityWatch()
    {
        log.setLevel(Level.INFO);
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        ActivityWatch.buckets = new Buckets(Calendar.getInstance().getTime().toString(),
                "This is a watcher for netbeans, send information and events "
                + "to ActivityWatch",
                "app.editor.activity", Consts.AW_CLIENT_NAME, HOST_NAME);
    }

    @Override
    public void run()
    {
        ActivityWatch.VERSION = ActivityWatch.getPluginVersion();
        ActivityWatch.IDE_VERSION = System.getProperty("netbeans.buildnumber");
        ActivityWatch.log.log(Level.INFO, "Initializing ActivityWatch plugin v{0} "
                                          + "(https://activitywatch.net/)", ActivityWatch.VERSION);
        ActivityWatch.info("ActivityWatch is loaded");

        ActivityWatch.DEBUG = ActivityWatch.isDebugEnabled();
        if (ActivityWatch.DEBUG) {
            log.setLevel(Level.CONFIG);
            ActivityWatch.debug("Logging level set to DEBUG");
        }

        // Set class to run as a true
        ActivityWatch.READY = true;
        // Listen for changes to documents
        PropertyChangeListener l = new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                JTextComponent jtc = EditorRegistry.lastFocusedComponent();
                if (jtc != null) {
                    Document d = jtc.getDocument();
                    CustomDocumentListener listener = new CustomDocumentListener(d);
                    d.addDocumentListener(listener);
                    listener.update();
                }
            }
        };

        // Register event change listener
        EditorRegistry.addPropertyChangeListener(l);
        // Finish initilizations
        ActivityWatch.info("Finished initializing ActivityWatch plugin.");
        // install update checker when UI is ready (main window shown)
        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    UpdateHandler.checkAndHandleUpdates();
                }
                catch (NullPointerException e) {
                    ActivityWatch.error(e.toString());
                }
            }
        });
    }

    public static boolean enoughTimePassed(long currentTime)
    {
        return ActivityWatch.lastTime + FREQUENCY * 60 < currentTime;
    }

    public static Boolean isDebugEnabled()
    {
        String debug = NbPreferences.forModule(ActivityWatch.class).get("Debug", "");
        if (debug == null || debug.equals("")) {
            debug = ConfigFile.get("settings", "debug");
            try {
                NbPreferences.forModule(ActivityWatch.class).put("Debug", debug);
            }
            catch (Exception e) {
                ActivityWatch.warn(e.toString());
            }
        }
        return debug != null && debug.equals("true");
    }

    public static String getPluginVersion()
    {
        for (UpdateUnit updateUnit : UpdateManager.getDefault().getUpdateUnits()) {
            UpdateElement updateElement = updateUnit.getInstalled();
            if (updateElement != null) {
                if (Consts.CODE_NAME.equals(updateElement.getCodeName())) {
                    return updateElement.getSpecificationVersion();
                }
            }
        }
        return "Unknown";
    }

    public static void sendHeartbeat(String file, Project currentProject, boolean isWrite)
    {
        if (ActivityWatch.READY) {
            sendHeartbeat(file, currentProject, isWrite, 0);
        }
    }

    private static void sendHeartbeat(final String file, final Project currentProject, final boolean isWrite, final int tries)
    {
        ActivityWatch.info("Executing CLI: Send data ... " + ActivityWatch.buckets);
        Runnable r = new Runnable()
        {
            Event e = new Event(new EventData(file, currentProject, lastFile));

            public void run()
            {
                ActivityWatch.info("Executing CLI: Send data ... " + RequestHandler.javaObjToJSON(e));
                try {
                    String requestResponse = RequestHandler.sendGET("/api/0/buckets/" + ActivityWatch.buckets.getId());
                    ActivityWatch.info("Bucket:  " + ActivityWatch.buckets + ";  Query response: " + requestResponse);
                    if (requestResponse.equalsIgnoreCase("404")) {
                        requestResponse = RequestHandler.postJSON(ActivityWatch.buckets, "/api/0/buckets/" + ActivityWatch.buckets.getId());
                        ActivityWatch.info("Bucket:  " + ActivityWatch.buckets + ";  Post response: " + requestResponse);
                    }
                    requestResponse = RequestHandler.postJSON(e, "/api/0/buckets/" + ActivityWatch.buckets.getId() + "/events");
                    ActivityWatch.info("Sending data to server data ... " + requestResponse);
                }
                catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        };
        new Thread(r).start();
    }

    public static BigDecimal getCurrentTimestamp()
    {
        return new BigDecimal(String.valueOf(System.currentTimeMillis() / 1000.0)).setScale(4, BigDecimal.ROUND_HALF_UP);
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
