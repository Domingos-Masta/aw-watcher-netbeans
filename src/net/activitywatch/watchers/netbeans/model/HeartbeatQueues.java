/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.util.Collection;

/**
 *
 * @author domingos.fernando
 */
public class HeartbeatQueues
{

    private Buckets buckets;
    private Boolean isProcessing;
    private Collection<EventHeartbeat> heartbeats;

    public HeartbeatQueues(Buckets buckets, Boolean isProcessing, Collection<EventHeartbeat> heartbeats)
    {
        this.buckets = buckets;
        this.isProcessing = isProcessing;
        this.heartbeats = heartbeats;
    }

}
