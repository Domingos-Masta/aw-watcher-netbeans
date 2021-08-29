/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.activitywatch.watchers.netbeans.ActivityWatch;

/**
 *
 * @author domingos.fernando
 */
public class Event
{

    public final String timestamp;
    public final BigDecimal duration;
    public final EventData data;

    public Event(EventData data)
    {
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        this.timestamp = df.format(now);

        this.duration = ActivityWatch.getCurrentTimestamp().subtract(new BigDecimal(ActivityWatch.lastTime));
        this.data = data;
    }

}
