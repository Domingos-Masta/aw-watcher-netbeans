/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

/**
 *
 * @author domingos.fernando
 */
public class HeartbeatQueueItem
{

    private final Integer pulseTime;
    private final EventHeartbeat heartbeat;

    public HeartbeatQueueItem(Integer pulseTime, EventHeartbeat heartbeat)
    {
        this.pulseTime = pulseTime;
        this.heartbeat = heartbeat;
    }

    public Integer getPulseTime()
    {
        return pulseTime;
    }

    public EventHeartbeat getHeartbeat()
    {
        return heartbeat;
    }

}
