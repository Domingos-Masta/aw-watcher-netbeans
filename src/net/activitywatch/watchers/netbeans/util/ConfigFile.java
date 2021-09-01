/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.activitywatch.watchers.netbeans.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author domingos.fernando
 */
public class ConfigFile
{

    private static File userHomeDir = new File(System.getProperty(Consts.USER_HOME));
    private static File configFile = new File(userHomeDir, Consts.CONFIG);

//    Podemos adicionar sempre mais uma informação apra testes
    public static String get(String section, String key)
    {
        String val = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile.getAbsolutePath()));
            String currentSection = "";
            try {
                String line = br.readLine();
                while (line != null) {
                    if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                        currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
                    }
                    else {
                        if (section.toLowerCase().equals(currentSection)) {
                            String[] parts = line.split("=");
                            if (parts.length == 2 && parts[0].trim().equals(key)) {
                                val = parts[1].trim();
                                br.close();
                                return val;
                            }
                        }
                    }
                    line = br.readLine();
                }
            }
            catch (Exception e) {
                Util.error(e.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    br.close();
                }
                catch (IOException e) {
                    Util.error(e.toString());
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e1) {
            /*
             * ignored
             */ }
        return val;
    }

    public static void set(String section, String key, String val)
    {
        StringBuilder contents = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile.getAbsolutePath()));
            try {
                String currentSection = "";
                String line = br.readLine();
                Boolean found = false;
                while (line != null) {
                    if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                        if (section.toLowerCase().equals(currentSection) && !found) {
                            contents.append(key + " = " + val + "\n");
                            found = true;
                        }
                        currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
                        contents.append(line + "\n");
                    }
                    else {
                        if (section.toLowerCase().equals(currentSection)) {
                            String[] parts = line.split("=");
                            String currentKey = parts[0].trim();
                            if (currentKey.equals(key)) {
                                if (!found) {
                                    contents.append(key + " = " + val + "\n");
                                    found = true;
                                }
                            }
                            else {
                                contents.append(line + "\n");
                            }
                        }
                        else {
                            contents.append(line + "\n");
                        }
                    }
                    line = br.readLine();
                }
                if (!found) {
                    if (!section.toLowerCase().equals(currentSection)) {
                        contents.append("[" + section.toLowerCase() + "]\n");
                    }
                    contents.append(key + " = " + val + "\n");
                }
            }
            catch (Exception e) {
                Util.error(e.toString());
                e.printStackTrace();
            }
            finally {
                try {
                    br.close();
                }
                catch (IOException e) {
                    Util.error(e.toString());
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e1) {

            // cannot read config file, so create it
            contents = new StringBuilder();
            contents.append("[" + section.toLowerCase() + "]\n");
            contents.append(key + " = " + val + "\n");
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(configFile.getAbsolutePath(), "UTF-8");
        }
        catch (FileNotFoundException e) {
            Util.error(e.toString());
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            Util.error(e.toString());
            e.printStackTrace();
        }
        if (writer != null) {
            writer.print(contents.toString());
            writer.close();
        }
    }

}
