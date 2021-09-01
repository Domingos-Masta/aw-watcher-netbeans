/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author domingos.fernando
 */
public class HeartbeatQueueUtilComposer
{

    private boolean isProcessing;
    private ArrayList<HeartbeatQueueItem> heartbeatQueueItems;

    public HeartbeatQueueUtilComposer(boolean isProcessing, ArrayList<HeartbeatQueueItem> heartbeatQueueItems)
    {
        this.isProcessing = isProcessing;
        this.heartbeatQueueItems = heartbeatQueueItems;
    }

    public boolean isIsProcessing()
    {
        return isProcessing;
    }

    public void setIsProcessing(boolean isProcessing)
    {
        this.isProcessing = isProcessing;
    }

    public Collection<HeartbeatQueueItem> getHeartbeatQueueItems()
    {
        return heartbeatQueueItems;
    }

}
