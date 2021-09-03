/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import java.util.Date;

/**
 *
 * @author domingos.fernando
 */
public class Buckets
{

    private final String id;
    private final Date created;
    private final String name;
    private final String type;
    private final String client;
    private final String hostname;

    public Buckets(Date created, String name, String type, String client, String hostname)
    {
        this.id = client + "_" + hostname;
        this.created = created;
        this.name = name;
        this.type = type;
        this.client = client;
        this.hostname = hostname;
    }

    public String getId()
    {
        return id;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getClient()
    {
        return client;
    }

    public String getHostname()
    {
        return hostname;
    }

    @Override
    public String toString()
    {
        return "Buckets{" + "id=" + id + ", created=" + created + ", name=" + name + ", type=" + type + ", client=" + client + ", hostname=" + hostname + '}';
    }

}
