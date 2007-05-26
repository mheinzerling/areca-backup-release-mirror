package com.application.areca.metadata.content;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.application.areca.RecoveryEntry;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.myJava.file.FileNameUtil;

/**
 * Class defining the physical content of an archive.
 * <BR>It is implemented as a set of RecoveryEntries.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class ArchiveContent {

    /**
     * Separator
     */
    protected static final String DATA_SEP = "#-#";
    
    private Set content = new HashSet();;

    private File defaultRootDirectory;
    
    public ArchiveContent() {
        this(new File("/"));
    }
    
    public ArchiveContent(File defaultRootDirectory) {
        this.defaultRootDirectory = defaultRootDirectory;
    }

    public Iterator getContent() {
        return this.content.iterator();
    }
    
    public void add(FileSystemRecoveryEntry entry) {
        this.content.add(entry);
    }
    
    public void parse(String[] serialized) {
        for (int i=0; i<serialized.length; i++) {
            this.content.add(this.deserialize(serialized[i]));
        }
    }
    
    protected static String serialize(FileSystemRecoveryEntry entry) {
        return entry.getName() + DATA_SEP + entry.getSize();
    }
    
    public boolean contains(FileSystemRecoveryEntry entry) {
        return content.contains(entry);
    }
    
    protected RecoveryEntry deserialize(String serialized) {
        if (serialized == null || serialized.length() == 0) {
            return null;
        }
        int i = serialized.indexOf(DATA_SEP);
        if (i == -1) {
            return null;
        }
        String name = FileNameUtil.normalizePath(serialized.substring(0, i));
        long length = Long.parseLong(serialized.substring(i + DATA_SEP.length()));
        
        return new FileSystemRecoveryEntry(defaultRootDirectory, new File(defaultRootDirectory, name), RecoveryEntry.STATUS_STORED, length); 
    }
    
    public void override(ArchiveContent previousContent) {
        this.content.removeAll(previousContent.content);
        this.content.addAll(previousContent.content);
    }
    
    /**
     * Removes the entries from the ArchiveContent which are not referenced by the archive trace.
     * <BR>This process is useful after archive merge to ensure that deleted files are also removed from the ArchiveContent object.
     */
    public void clean(ArchiveTrace trace) {
        Iterator iter = this.content.iterator();
        List entriesToRemove = new ArrayList();
        while (iter.hasNext()) {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
            if (! trace.contains(entry)) {
                entriesToRemove.add(entry);
            }
        }
        
        this.content.removeAll(entriesToRemove);
    }
}
