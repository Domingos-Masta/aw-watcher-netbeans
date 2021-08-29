/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import org.netbeans.api.project.Project;

/**
 *
 * @author domingos.fernando
 */
public class EventData
{

    public final String file;
    public final String project;
    public final String language;
    public final String eventType = "app.editor.activity";

    public EventData(String file, Project project, String language)
    {
        this.file = file;
        this.project = project.getProjectDirectory().getPath();
        this.language = file.substring(file.lastIndexOf("."));
    }

}
