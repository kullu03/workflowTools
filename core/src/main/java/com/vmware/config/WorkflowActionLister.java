/*
 * Project Horizon
 * (c) 2013 VMware, Inc. All rights reserved.
 * VMware Confidential.
 */
package com.vmware.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.vmware.action.BaseAction;
import com.vmware.util.ReflectionUtils;
import com.vmware.util.exception.RuntimeIOException;

/**
 * Used to list all workflow action classes.
 * Supports listing actions in a jar or on the file system.
 */
public class WorkflowActionLister {

    public List<Class<? extends BaseAction>> findWorkflowActions() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL actionDirectoryUrl = classLoader.getResource("com/vmware/action");
            List<Class<? extends BaseAction>> actionsList = new ArrayList<Class<? extends BaseAction>>();
            if (actionDirectoryUrl.getFile().contains(".jar!")) {
                addClassesFromJar(actionDirectoryUrl, actionsList);
            } else {
                File directoryFile = new File(actionDirectoryUrl.getFile());
                addClassesFromDirectory(directoryFile, actionsList, "com.vmware");
            }

            Collections.sort(actionsList, new Comparator<Class<? extends BaseAction>>() {
                @Override
                public int compare(Class<? extends BaseAction> o1, Class<? extends BaseAction> o2) {
                    return o1.getSimpleName().compareTo(o2.getSimpleName());
                }
            });
            return actionsList;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private void addClassesFromJar(URL fileUrl, List<Class<? extends BaseAction>> actionsList) throws IOException {
        String jarName = parseFilePath(fileUrl.getFile());
        ZipInputStream zip=new ZipInputStream(new FileInputStream(jarName));
        for(ZipEntry entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry()) {
            if(entry.getName().endsWith(".class") && !entry.isDirectory()) {
                String className = determineClassNameFromFileName(entry);
                addClassIfActionClass(actionsList, className);
            }
        }
    }

    private void addClassesFromDirectory(File directoryFile, List<Class<? extends BaseAction>> actionsList, String packagePrefix) {
        for (File actionFile : directoryFile.listFiles()) {
            if (actionFile.isDirectory()) {
                String newPackagePrefix = packagePrefix + "." + directoryFile.getName();
                addClassesFromDirectory(actionFile, actionsList, newPackagePrefix);
            } else {
                if (!actionFile.getName().endsWith(".class")) {
                    continue;
                }
                String className = packagePrefix + "." + directoryFile.getName() + "." + actionFile.getName();
                className = className.substring(0, className.lastIndexOf("."));
                addClassIfActionClass(actionsList, className);
            }
        }
    }

    private String determineClassNameFromFileName(ZipEntry entry) {
        StringBuilder className=new StringBuilder();
        for(String part : entry.getName().split("/")) {
            if(className.length() != 0) {
                className.append(".");
            }
            className.append(part);
            if(part.endsWith(".class")) {
                className.setLength(className.length()-".class".length());
            }
        }
        return className.toString();
    }

    private String parseFilePath(String fileName) {
        String jarName = fileName.substring("file:".length(),fileName.indexOf("!"));
        return jarName;
    }

    private void addClassIfActionClass(List<Class<? extends BaseAction>> actionsList, String className) {
        if (!className.startsWith("com.vmware")) {
            return;
        }

        Class actionClass = ReflectionUtils.forName(className);

        if (!Modifier.isAbstract(actionClass.getModifiers())
                && BaseAction.class.isAssignableFrom(actionClass)) {
            actionsList.add(actionClass);
        }
    }
}
