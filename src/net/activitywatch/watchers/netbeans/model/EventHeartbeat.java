/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import net.activitywatch.watchers.netbeans.util.Util;

/**
 *
 * @author domingos.fernando
 */
public class EventHeartbeat
{

    private final String timestamp;
    private final Long duration;
    private final EventData data;

    public EventHeartbeat(EventData data)
    {
        this.timestamp = Util.getDateFormat();
        this.duration = Long.valueOf(0);
        this.data = data;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public Long getDuration()
    {
        return duration;
    }

    public EventData getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return "Event{" + "timestamp=" + timestamp + ", duration=" + duration + ", data=" + data + '}';
    }

}
