/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author domingos.fernando
 */
public class Buckets
{

    @SerializedName("id")
    private String id;

    @SerializedName("created")
    private String created;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("client")
    private String client;

    @SerializedName("hostname")
    private String hostname;

    public Buckets(String id, String created, String name, String type, String client, String hostname)
    {
        this.id = id;
        this.created = created;
        this.name = name;
        this.type = type;
        this.client = client;
        this.hostname = hostname;
    }

    public Buckets(String created, String name, String type, String client, String hostname)
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

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCreated()
    {
        return created;
    }

    public void setCreated(String created)
    {
        this.created = created;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getClient()
    {
        return client;
    }

    public void setClient(String client)
    {
        this.client = client;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    @Override
    public String toString()
    {
        return "Buckets{" + "id=" + id + ", created=" + created + ", name=" + name + ", type=" + type + ", client=" + client + ", hostname=" + hostname + '}';
    }

}
