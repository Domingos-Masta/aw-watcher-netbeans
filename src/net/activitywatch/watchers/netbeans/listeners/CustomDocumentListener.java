/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.listeners;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import net.activitywatch.watchers.netbeans.ActivityWatch;
import net.activitywatch.watchers.netbeans.model.Event;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author domingos.fernando
 */
public class CustomDocumentListener implements DocumentListener
{

    private final Document document;

    public CustomDocumentListener(Document d)
    {
        this.document = d;
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        this.handleTyping();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        this.handleTyping();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        this.handleTyping();
    }

    public void update()
    {
        if (ActivityWatch.documentListener != null) {
            ActivityWatch.documentListener.remove();
        }
        ActivityWatch.documentListener = this;
    }

    public void remove()
    {
        this.document.removeDocumentListener(this);
    }

    public void handleTyping()
    {
        final FileObject file = this.getFile();
        if (file != null) {
            final Project currentProject = this.getProject();
            final long currentTime = System.currentTimeMillis() / 1000;
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    final String currentFile = file.getPath();
                    if ((!currentFile.equals(ActivityWatch.lastFile) || ActivityWatch.enoughTimePassed(currentTime))) {
                        Event event = ActivityWatch.createHeartbeat(currentFile, currentProject);
                        ActivityWatch.sendHeartbeat(event, 0);
                        ActivityWatch.lastFile = currentFile;
                        ActivityWatch.lastTime = currentTime;
                    }
                }
            });
        }
    }

    private FileObject getFile()
    {
        if (this.document == null) {
            return null;
        }
        Source source = Source.create(this.document);
        if (source == null) {
            return null;
        }
        FileObject fileObject = source.getFileObject();
        if (fileObject == null) {
            return null;
        }
        return fileObject;
    }

    private Project getProject()
    {
        if (this.document == null) {
            return null;
        }
        Source source = Source.create(document);
        if (source == null) {
            return null;
        }
        FileObject fileObject = source.getFileObject();
        if (fileObject == null) {
            return null;
        }
        return FileOwnerQuery.getOwner(fileObject);
    }
}
