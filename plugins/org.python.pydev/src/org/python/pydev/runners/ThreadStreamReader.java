/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on 03/08/2005
 */
package org.python.pydev.runners;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.python.pydev.core.structure.FastStringBuffer;

public final class ThreadStreamReader extends Thread {
    
    /**
     * Input stream read.
     */
    private final InputStream is;
    
    /**
     * Buffer with the contents gotten.
     */
    private final FastStringBuffer contents;
    
    /**
     * Access to the buffer should be synchronized.
     */
    private final Object lock = new Object();

    /**
     * Whether the read should be synchronized.
     */
    private final boolean synchronize;
    
    /**
     * Keeps the next unique identifier.
     */
    private static int next=0;
    
    /**
     * Get a unique identifier for this thread. 
     */
    private static synchronized int next(){
        next ++;
        return next;
    }

    public ThreadStreamReader(InputStream is) {
        this(is, true); //default is synchronize.
    }
    
    public ThreadStreamReader(InputStream is, boolean synchronize) {
        this.setName("ThreadStreamReader: "+next());
        this.setDaemon(true);
        contents = new FastStringBuffer();
        this.is = is;
        this.synchronize = synchronize;
    }

    public void run() {
        try {
            InputStreamReader in = new InputStreamReader(is);
            int c;
            if(synchronize){
                while ((c = in.read()) != -1) {
                    synchronized(lock){
                        contents.append((char) c);
                    }
                }
            }else{
                while ((c = in.read()) != -1) {
                    contents.append((char) c);
                }
            }
        } catch (Exception e) {
            //that's ok
        }
    }

    /**
     * @return the contents that were obtained from this instance since it was started or since
     * the last call to this method.
     */
    public String getAndClearContents() {
        synchronized(lock){
            String string = contents.toString();
            contents.clear();
            return string;
        }
    }

    public String getContents() {
        synchronized(lock){
            return contents.toString();
        }
    }
}
