/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans;

import com.google.common.io.Files;
import net.activitywatch.watchers.netbeans.util.ConfigFile;
import net.activitywatch.watchers.netbeans.util.UpdateHandler;
import net.activitywatch.watchers.netbeans.model.Event;
import net.activitywatch.watchers.netbeans.listeners.CustomDocumentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import net.activitywatch.watchers.netbeans.model.Buckets;
import net.activitywatch.watchers.netbeans.model.Data;
import net.activitywatch.watchers.netbeans.requests.RequestHandler;
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
    public static CustomDocumentListener documentListener = null;

    public static String lastFilePath = "";
    public static Long lastHeartbeatTime = Calendar.getInstance().getTimeInMillis();

    public static Buckets buckets;

    public static Boolean READY = false;
    public static String lastFile = null;
    public static long lastTime = 0;

    public ActivityWatch()
    {
        Util.log.setLevel(Level.INFO);
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        buckets = new Buckets(Calendar.getInstance(TimeZone.getTimeZone("utc")).getTime(),
                "This is a watcher for netbeans, send information and events "
                + "to ActivityWatch",
                "app.editor.activity", Util.AW_CLIENT_NAME, HOST_NAME);
    }

    @Override
    public void run()
    {
        ActivityWatch.VERSION = ActivityWatch.getPluginVersion();
        ActivityWatch.IDE_VERSION = System.getProperty("netbeans.buildnumber");
        Util.log.log(Level.INFO, "Initializing ActivityWatch plugin v{0} "
                                 + "(https://activitywatch.net/)", ActivityWatch.VERSION);
        Util.info("ActivityWatch is loaded");
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

    public static boolean enoughTimePassed(long currentTime)
    {
        return ActivityWatch.lastTime + Util.FREQUENCY * 60 < currentTime;
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
                if (Util.CODE_NAME.equals(updateElement.getCodeName())) {
                    return updateElement.getSpecificationVersion();
                }
            }
        }
        return "Unknown";
    }

    public static void sendHeartbeat(Event event, boolean isWrite)
    {
        if (ActivityWatch.READY) {
            sendHeartbeat(event, isWrite, 0);
        }
    }

    private static void sendHeartbeat(final Event event, final boolean isWrite, final int tries)
    {
        Util.debug("Executing CLI: ");
        Runnable r = new Runnable()
        {
            public void run()
            {
                try {
                    Util.info("Executing CLI: Send data ... " + RequestHandler.javaObjToJSON(event));

                    HashMap<String, String> requestResponse = executeRequest(null, "/api/0/buckets/" + buckets.getId(), Util.GET_REQ);
                    Util.info("Bucket:  " + buckets + ";  Query response: " + requestResponse);
                    if (requestResponse.get(Util.RESP_CODE).equalsIgnoreCase("404")) {
                        requestResponse = executeRequest(buckets, "/api/0/buckets/" + buckets.getId(), Util.POST_REQ);
                        Util.info("Bucket:  " + buckets + ";  Post response: " + requestResponse);
                    }
                    requestResponse = executeRequest(event, "/api/0/buckets/" + buckets.getId() + "/events", Util.POST_REQ);
//                    requestResponse = executeRequest(event, "/api/0/buckets/" + buckets.getId() + "/heartbeat?pulsetime=" + 0, ActivityWatch.DEBUG);
                    Util.info("Sending data to server data ... " + requestResponse);

                }
                catch (Exception e) {
                    if (tries < 3) {
                        Util.debug(e.toString());
                        try {
                            Thread.sleep(30);
                        }
                        catch (InterruptedException e1) {
                            Util.error(e1.toString());
                        }
                        sendHeartbeat(event, isWrite, tries + 1);
                    }
                    else {
                        Util.error(e.toString());
                    }
                }
            }
        };
        new Thread(r).start();
    }

    public static HashMap<String, String> executeRequest(Object objData, String endPoint, String request) throws IOException
    {
        if (Util.POST_REQ.equalsIgnoreCase(request)) {
            return RequestHandler.postRquest(objData, endPoint, ActivityWatch.DEBUG);
        }
        else {
            return RequestHandler.getRequest(endPoint, ActivityWatch.DEBUG);
        }

    }

    public static Event createHeartbeat(String currentFile, Project currentProject)
    {
        Data data = new Data(currentFile, currentProject, getFileExtension(currentFile));
        Event heartbeat = new Event(data);
        return heartbeat;
    }

    public static String getFileExtension(String filePath)
    {
        String fileExt = Files.getFileExtension(filePath);
        return fileExt.equals("") ? "Unknown" : fileExt;
    }

}
