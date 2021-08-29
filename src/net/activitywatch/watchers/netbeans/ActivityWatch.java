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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.activitywatch.watchers.netbeans.model.Buckets;
import net.activitywatch.watchers.netbeans.requests.RequestHandler;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
import org.openide.*;
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

    public static final String NAMESPACE = "net.activitywatch.watchers.netbeans";
    public static final String CODE_NAME = "net_activitywatch_watchers_netbeans_update_center";
    public static final String IDE_NAME = "NetBeans";
    public static final short FREQUENCY = 2; // minutes between pings
    public static final Integer MAX_HEART_BEAT_TIME = 1;
    public static final String AW_WATCHER = "aw-watcher-";

    // For not error
    public static final String CONFIG = ".wakatime.cfg";

    public static String IDE = "netbeans";
    public static String VERSION = "Unknown";
    public static String IDE_VERSION = "Unknown";
    public static String HOST_NAME = "Unknown";
    public static String bucketClientNamePrefix;
    public static Boolean DEBUG = false;
    public static CustomDocumentListener documentListener = null;

    public static Boolean READY = false;
    public static String lastFile = null;
    public static long lastTime = 0;
    public static Buckets buckets;

    public ActivityWatch()
    {
        log.setLevel(Level.INFO);
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        String createdAt = df.format(now);
        ActivityWatch.buckets = new Buckets(AW_WATCHER + IDE + "_" + HOST_NAME, createdAt,
                "This is may new ActivityWatcher", "app.editor.activity", AW_WATCHER + IDE, HOST_NAME);
    }

    @Override
    public void run()
    {
        log.info("ActivityWatch is loaded");

        ActivityWatch.VERSION = ActivityWatch.getPluginVersion();
        ActivityWatch.IDE_VERSION = System.getProperty("netbeans.buildnumber");

        ActivityWatch.bucketClientNamePrefix = AW_WATCHER + IDE + HOST_NAME;
        ActivityWatch.log.log(Level.INFO, "Initializing ActivityWatch plugin v{0} (https://activitywatch.net/)", ActivityWatch.VERSION);

        ActivityWatch.DEBUG = ActivityWatch.isDebugEnabled();
        if (ActivityWatch.DEBUG) {
            log.setLevel(Level.CONFIG);
            ActivityWatch.debug("Logging level set to DEBUG");
        }

        // Set class to run as a true
        ActivityWatch.READY = true;

        //Skip api key configuration because is not neded enymore
        ActivityWatch.debug("API Key: " + "No needed enymore for this version");

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

    public static void errorDialog(String msg)
    {
        int msgType = NotifyDescriptor.ERROR_MESSAGE;
        NotifyDescriptor d = new NotifyDescriptor.Message(msg, msgType);
        DialogDisplayer.getDefault().notify(d);
    }

    public static void debug(String msg)
    {
        log.log(Level.CONFIG, msg);
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
                if (ActivityWatch.CODE_NAME.equals(updateElement.getCodeName())) {
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

//    Send event test to the storage api
    private static void sendHeartbeat(final String file, final Project currentProject, final boolean isWrite, final int tries)
    {
//        final String[] cmds = buildCliCommand(file, currentProject, isWrite);
        ActivityWatch.info("Executing CLI: Send data ... " + ActivityWatch.buckets);
        Runnable r = new Runnable()
        {
            Event e = new Event(new EventData(file, currentProject, lastFile));

            public void run()
            {
                ActivityWatch.info("Executing CLI: Send data ... " + RequestHandler.javaObjToJSON(e));
                try {
//                    /api/0/buckets/aw-watcher-netbeans_Domingoss-Maptss-MacBook-Pro.local
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
}
