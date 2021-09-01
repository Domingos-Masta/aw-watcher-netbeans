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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.activitywatch.watchers.netbeans.model.Buckets;
import net.activitywatch.watchers.netbeans.model.HeartbeatQueueItem;
import net.activitywatch.watchers.netbeans.model.HeartbeatQueueUtilComposer;
import net.activitywatch.watchers.netbeans.requests.RequestHandler;
import net.activitywatch.watchers.netbeans.util.Consts;
import net.activitywatch.watchers.netbeans.util.DateUtils;
import net.activitywatch.watchers.netbeans.util.Util;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
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

    private static HashMap<String, HeartbeatQueueUtilComposer> heartbeatQueue = new HashMap<>();

    public ActivityWatch()
    {
        Util.log.setLevel(Level.INFO);
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        buckets = new Buckets(DateUtils.dataTimeAgoraYYYY(),
            "This is a watcher for netbeans, send information and events "
            + "to ActivityWatch",
            "app.editor.activity", Consts.AW_CLIENT_NAME, HOST_NAME);
    }

    @Override
    public void run()
    {
        ActivityWatch.VERSION = ActivityWatch.getPluginVersion();
        ActivityWatch.IDE_VERSION = System.getProperty("netbeans.buildnumber");
        Util.log.log(Level.INFO, "Initializing ActivityWatch plugin v{0} "
                                 + "(https://activitywatch.net/)", ActivityWatch.VERSION);
        Util.info("ActivityWatch is loaded");

        ActivityWatch.DEBUG = ActivityWatch.isDebugEnabled();
        if (ActivityWatch.DEBUG) {
            Util.log.setLevel(Level.CONFIG);
            Util.debug("Logging level set to DEBUG");
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
        Util.info("Finished initializing ActivityWatch plugin.");
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
                    Util.error(e.toString());
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
                Util.warn(e.toString());
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
        EventData data = new EventData(currentFile, currentProject, Util.getFileExtension(currentFile));
        EventHeartbeat heartbeat = new EventHeartbeat(data);
        return heartbeat;
    }

    public static void heartbeat(EventHeartbeat heartbeat)
    {
        insertHeartbeatData(ActivityWatch.buckets, ActivityWatch.PULSE_TIME, heartbeat);
    }

    public static void insertHeartbeatData(Buckets buckets, Integer pulseTime, EventHeartbeat eventHeartbeat)
    {
        if (!heartbeatQueue.containsKey(buckets.getId())) {
            heartbeatQueue.put(buckets.getId(), new HeartbeatQueueUtilComposer(false, new ArrayList<HeartbeatQueueItem>()));
        }
        heartbeatQueue.get(buckets.getId()).getHeartbeatQueueItems().add(new HeartbeatQueueItem(pulseTime, eventHeartbeat));
        updateHeartbeatQueue(buckets, pulseTime);
    }

    public static void updateHeartbeatQueue(Buckets buckets, Integer pulseTime)
    {
        HeartbeatQueueUtilComposer utilComposer = heartbeatQueue.get(buckets.getId());
        if (!utilComposer.isIsProcessing() && utilComposer.getHeartbeatQueueItems().size() > 0) {

            HeartbeatQueueItem item = utilComposer.getHeartbeatQueueItems().stream().findFirst().orElse(null);
            utilComposer.setIsProcessing(true);
            if (item != null) {
                utilComposer.getHeartbeatQueueItems().remove(item);
                sendHeartbeat(buckets, item, utilComposer);
            }
        }
    }

    public static void sendHeartbeat(final Buckets buckets, final HeartbeatQueueItem queueItem, final HeartbeatQueueUtilComposer composer)
    {
        Util.info("Executing CLI: Send data ... " + buckets);
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Util.info("Executing CLI: Send data ... " + RequestHandler.javaObjToJSON(queueItem.getHeartbeat()));
                try {
                    HashMap<String, String> requestResponse = RequestHandler.getRequest("/api/0/buckets/" + buckets.getId(), ActivityWatch.DEBUG);
                    Util.info("Bucket:  " + buckets + ";  Query response: " + requestResponse);
                    if (requestResponse.get(Consts.RESP_CODE).equalsIgnoreCase("404")) {
                        requestResponse = RequestHandler.postRquest(buckets, "/api/0/buckets/" + buckets.getId(), ActivityWatch.DEBUG);
                        Util.info("Bucket:  " + buckets + ";  Post response: " + requestResponse);
                    }
                    requestResponse = RequestHandler.postRquest(queueItem.getHeartbeat(), "/api/0/buckets/" + buckets.getId() + "/heartbeat?pulsetime=" + queueItem.getPulseTime(), ActivityWatch.DEBUG);
                    if (!requestResponse.get(Consts.RESP_CODE).equalsIgnoreCase("200")) {
                        composer.setIsProcessing(false);
                    }
                    Util.info("Sending data to server data ... " + requestResponse);
                }
                catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        };
        new Thread(r).start();
    }
}
