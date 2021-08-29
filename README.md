# aw-watcher-netbeans

This extension allows [ActivityWatch](https://activitywatch.net), the free and open-source time tracker, to keep track of the projects and programming languages you use in Netbeans IDE.

The extension is published on [GitHub](https://github.com/Domingos-Masta/aw-watcher-netbeans).

The source code is available at https://github.com/Domingos-Masta/aw-watcher-netbeans

## Features

Sends following data to ActivityWatch:
- current project name
- programming language
- current file name

## Requirements

This extension requires ActivityWatch to be running on your machine.

## Install Instructions

To install this extension, search for aw-watcher-netbeans on [github](https://github.com/Domingos-Masta/aw-watcher-netbeans/releases), and find releases link download last release of .nbm file.

Extract the contents of the NetBeans Plugins file to a directory on your local machine. Make note of the location.

Open the NetBeans Plugins Manager. Select:
Tools > Plugins
click the Downloaded tab,
Click the Add Plugins button.
In the file browser, go to the directory in which you have extracted the contents of the NetBeans Plugins.
Select all the .nbm files and click Open.
Accept the license terms and click Install.
If additional Validation screens appear, click Continue.
Click Finish to restart NetBeans.
And that's it, if Activity Watch was running, it should detect this Netbeans watcher automatically.
Give it some time to have some data to display and it should show in the ActivityWatch Timeline and Activity sections soon.

## Extension Settings

This extension adds the following settings:

- `aw-watcher-netbeans.maxHeartbeatsPerSec`: Controls the maximum number of heartbeats sent per second.

## Error reporting

If you run into any errors or have feature requests, please [open an issue](https://github.com/Domingos-Masta/aw-watcher-netbeans).

## Release Notes

### 0.1.0

Initial release of aw-watcher-vscode.
