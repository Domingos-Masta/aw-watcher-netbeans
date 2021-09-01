/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.model;

import org.gradle.internal.impldep.org.apache.commons.io.FilenameUtils;
import org.netbeans.api.project.Project;

/**
 *
 * @author domingos.fernando
 */
public class EventData
{

    private final String file;
    private final String project;
    private final String language;
    private final String eventType = "app.editor.activity";

    public EventData(String file, Project project, String language)
    {
        this.file = file;
        this.project = project.getProjectDirectory().getPath();
        String extension = FilenameUtils.getExtension(file);
        this.language = extension.equals("") ? "Unknown" : extension;
    }

    public String getFile()
    {
        return file;
    }

    public String getProject()
    {
        return project;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getEventType()
    {
        return eventType;
    }

    @Override
    public String toString()
    {
        return "EventData{" + "file=" + file + ", project=" + project + ", language=" + language + ", eventType=" + eventType + '}';
    }
}
