// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LogWordPriorFileParser.java

package edu.msu.cme.rdp.readseq.utils.orientation;

import java.io.*;
import java.util.StringTokenizer;

// Referenced classes of package edu.msu.cme.rdp.reverseengine:
//            TrainingDataException

class LogWordPriorFileParser
{

    LogWordPriorFileParser()
    {
    }

    void createLogWordPriorArr(Reader r, float arr[])
        throws IOException, TrainingDataException
    {
        BufferedReader reader = new BufferedReader(r);
        String line = reader.readLine();
        do
        {
            if((line = reader.readLine()) == null)
                break;
            StringTokenizer st = new StringTokenizer(line, "\t");
            if(st.countTokens() != 2)
                throw new TrainingDataException("\nError: " + line + " does not have exact two numbers");
            try
            {
                int wordIndex = Integer.parseInt(st.nextToken());
                float logWordPrior = Float.parseFloat(st.nextToken());
                arr[wordIndex] = logWordPrior;
            }
            catch(NumberFormatException e)
            {
                reader.close();
                throw new TrainingDataException("\nError: The value for wordIndex or word prior is not a number at line : " + line);
            }
        } while(true);
        reader.close();
    }
}
