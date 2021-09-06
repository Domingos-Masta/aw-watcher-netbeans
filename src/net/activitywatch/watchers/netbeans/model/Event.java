/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.activitywatch.watchers.netbeans.util.Util;

/**
 *
 * @author domingos.fernando
 */
public class Event
{

    private final String timestamp;
    private final Long duration;
    private final Data data;

    public Event(Data data)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(Util.DATE_FORMAT_PATTERN);
        this.timestamp = sdf.format(Calendar.getInstance().getTime());
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
