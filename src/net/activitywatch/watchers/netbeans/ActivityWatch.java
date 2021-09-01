/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans;

import net.activitywatch.watchers.netbeans.util.ConfigFile;
import net.activitywatch.watchers.netbeans.util.UpdateHandler;
import net.activitywatch.watchers.netbeans.model.EventData;
import net.activitywatch.watchers.netbeans.model.EventHeartbeat;
import net.activitywatch.watchers.netbeans.listeners.CustomDocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.activitywatch.watchers.netbeans.model.Buckets;
import net.activitywatch.watchers.netbeans.requests.RequestHandler;
import net.activitywatch.watchers.netbeans.util.Consts;
import net.activitywatch.watchers.netbeans.util.DateUtils;
import org.apache.commons.io.FilenameUtils;
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

    // For not error
    public static String VERSION = "Unknown";
    public static String IDE_VERSION = "Unknown";
    public static String HOST_NAME = "Unknown";

    public static Boolean DEBUG = false;

    public static String lastFilePath = "";
    public static Long lastHeartbeatTime = Long.valueOf(0);

    public static Buckets buckets;
    public static CustomDocumentListener documentListener = null;

//  IMPLEMENTATION OF VS CODE WATCHER LOGIC.
    // Heartbeat handling
    private static final Integer PULSE_TIME = 20;
    private static final Integer MAX_HEARTBEATS_PER_SEC = 1;
//  END OF IMPLEMENTATION OF VSCODE WATCHER LOGIC

    public ActivityWatch()
    {
        log.setLevel(Level.INFO);
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        ActivityWatch.buckets = new Buckets(DateUtils.dataTimeAgoraYYYY(),
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

    public static Boolean enoughTimePassed(Long currentTime)
    {
        return ActivityWatch.lastHeartbeatTime + (1000 / (ActivityWatch.MAX_HEARTBEATS_PER_SEC)) < currentTime;
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

    public static EventHeartbeat createHeartbeat(String currentFile, Project currentProject)
    {
        EventData data = new EventData(currentFile, currentProject, getFileExtension(currentFile));
        EventHeartbeat heartbeat = new EventHeartbeat(data);
        return heartbeat;
    }

    public static void sendHeartbeat(final EventHeartbeat heartbeat)
    {
        ActivityWatch.info("Executing CLI: Send data ... " + ActivityWatch.buckets);
        Runnable r = new Runnable()
        {
            public void run()
            {
                ActivityWatch.info("Executing CLI: Send data ... " + RequestHandler.javaObjToJSON(heartbeat));
                try {
                    String requestResponse = RequestHandler.getRequest("/api/0/buckets/" + ActivityWatch.buckets.getId(), ActivityWatch.DEBUG);
                    ActivityWatch.info("Bucket:  " + ActivityWatch.buckets + ";  Query response: " + requestResponse);
                    if (requestResponse.equalsIgnoreCase("404")) {
                        requestResponse = RequestHandler.postRquest(ActivityWatch.buckets, "/api/0/buckets/" + ActivityWatch.buckets.getId(), ActivityWatch.DEBUG);
                        ActivityWatch.info("Bucket:  " + ActivityWatch.buckets + ";  Post response: " + requestResponse);
                    }
                    requestResponse = RequestHandler.postRquest(heartbeat, "/api/0/buckets/" + ActivityWatch.buckets.getId() + "/pulsetime=" + PULSE_TIME, ActivityWatch.DEBUG);
//                    requestResponse = RequestHandler.postRquest(heartbeat, "/api/0/buckets/" + ActivityWatch.buckets.getId() + "/events",  ActivityWatch.DEBUG);
                    ActivityWatch.info("Sending data to server data ... " + requestResponse);
                }
                catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        };
        new Thread(r).start();
    }

    private static String getFileExtension(String filePath)
    {
        String fileExt = FilenameUtils.getExtension(filePath);
        return fileExt.equals("") ? "Unknown" : fileExt;
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
