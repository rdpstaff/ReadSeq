/*
 * SequenceParsingException.java
 *
 * Copyright 2006 Michigan State University Board of Trustees
 * 
 * Created on November 7, 2003, 5:47 PM
 */

package edu.msu.cme.rdp.readseq;

/**
 * A class to handle the exception during sequence parsing.
 * @author  wangqion
 */
public class SequenceParsingException extends Exception {

    private int lineno;
    
    /**
     * Creates a new instance of SequenceParsingException with detail message.
     */
    public SequenceParsingException(String msg, int lineno) {
       super(msg);
       this.lineno = lineno;
    }           

    public int getLineno() {
        return lineno;
    }
}

