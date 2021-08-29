/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.math.BigDecimal;
import net.activitywatch.watchers.netbeans.ActivityWatch;
import net.activitywatch.watchers.netbeans.util.Util;

/**
 *
 * @author domingos.fernando
 */
public class Event
{

    private final String timestamp;
    private final BigDecimal duration;
    private final EventData data;

    public Event(EventData data)
    {
        this.timestamp = Util.getDateFormat();
        this.duration = ActivityWatch.getCurrentTimestamp().subtract(new BigDecimal(ActivityWatch.lastTime));
        this.data = data;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public BigDecimal getDuration()
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
