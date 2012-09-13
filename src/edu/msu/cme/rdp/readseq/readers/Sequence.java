/*
 * Sequence.java
 *
 * Copyright 2006 Michigan State University Board of Trustees
 *
 * Created on November 7, 2003, 5:41 PM
 */
package edu.msu.cme.rdp.readseq.readers;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * An abstract class, providing an interface for accessing a sequence.
 * @author  wangqion, fishjord
 */
public class Sequence implements Serializable {
    @XmlAttribute
    protected String seqName;
    @XmlAttribute
    protected String desc;
    @XmlElement
    protected String seqString;

    private Sequence() {}

    public Sequence(Sequence seq) {
        this(seq.seqName, seq.desc, seq.seqString);
    }

    public Sequence(String seqName, String desc, String seqString) {
        this.seqName = seqName;
        this.desc = desc;
        this.seqString = seqString;
    }

    public String getDesc() {
        return desc;
    }

    public String getSeqName() {
        return seqName;
    }

    public String getSeqString() {
        return seqString;
    }

    void setSeqString(String seqString) {
        this.seqString = seqString;
    }
}

