/*
 * Sequence.java
 *
 * Copyright 2006 Michigan State University Board of Trustees
 * 
 * Created on November 7, 2003, 5:41 PM
 */
package edu.msu.cme.rdp.readseq;

import edu.msu.cme.rdp.readseq.readers.Sequence;

/**
 * An abstract class, providing an interface for accessing a sequence.
 * @author  wangqion, fishjord
 */
public class QSequence extends Sequence{
    protected byte[] quality;

    public QSequence(String seqName, String desc, String seqString, byte[] quality) {
        super(seqName, desc, seqString);
        this.quality = quality;
    }

    public QSequence(Sequence s, byte[] quality) {
        super(s.getSeqName(), s.getDesc(), s.getSeqString());
        this.quality = quality;
    }

    public byte[] getQuality() {
        return quality;
    }
}

