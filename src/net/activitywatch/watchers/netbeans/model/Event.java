/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author domingos.fernando
 */
public class Event
{

    private final Date timestamp;
    private final Long duration;
    private final Data data;

    public Event(Data data)
    {
        this.timestamp = Calendar.getInstance().getTime();
        this.duration = Long.valueOf(0);
        this.data = data;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public Long getDuration()
    {
        return duration;
    }

    public Data getData()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return "Event{" + "timestamp=" + timestamp + ", duration=" + duration + ", data=" + data + '}';
    }

}
