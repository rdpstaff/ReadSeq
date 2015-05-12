/*
 * Copyright (C) 2014 wangqion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.msu.cme.rdp.readseq;

import edu.msu.cme.rdp.readseq.utils.QualityTrimmer;
import edu.msu.cme.rdp.readseq.utils.ResampleSeqFile;
import edu.msu.cme.rdp.readseq.utils.RevComplement;
import edu.msu.cme.rdp.readseq.utils.RmDupSeqs;
import edu.msu.cme.rdp.readseq.utils.SeqFileSplitter;
import edu.msu.cme.rdp.readseq.utils.SequenceSelector;
import edu.msu.cme.rdp.readseq.writers.StkWriter;
import java.util.Arrays;

/**
 *
 * @author wangqion
 */
public class ReadSeqMain {
    public static void main(String [] args) throws Exception {
        String usage = "USAGE: ReadSeqMain <subcommand> <subcommand args ...>" +  
                "\n\tquality-trim   - trim input fastq based on quality score" +
                "\n\trandom-sample  - random select a subset or subregion of sequences" +
                "\n\treverse-comp   - reverse complement sequences" +
                "\n\trm-dupseq      - remove identical or substring of sequences" + 
                "\n\tselect-seqs    - select or deselect sequences from a file" +
                "\n\tsplit          - split sequences" +  
                "\n\tto-fasta       - convert to fasta format" +
                "\n\tto-fastq       - convert to fastq format" +
                "\n\tto-stk         - convert to stk format" ;
        if(args.length == 0 ) {
            System.err.println(usage);
            return;
        }

        String cmd = args[0];
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        if(cmd.equals("select-seqs")) {
            SequenceSelector.main(newArgs);
        }else if(cmd.equals("quality-trim")) {
            QualityTrimmer.main(newArgs);
        }else if(cmd.equals("random-sample")) {
            ResampleSeqFile.main(newArgs);
        }else if(cmd.equals("reverse-comp")) {
            RevComplement.main(newArgs);
        }else if(cmd.equals("rm-dupseq")) {
            RmDupSeqs.main(newArgs);
        }else if(cmd.equals("to-fasta")) {
            ToFasta.main(newArgs);
        }else if(cmd.equals("to-fastq")) {
            ToFastq.main(newArgs);
        }else if(cmd.equals("to-stk")) {
            StkWriter.main(newArgs);
        }else if(cmd.equals("split")) {
            SeqFileSplitter.main(newArgs);
        } else {
            System.err.println("ERROR: " + "wrong subcommand");
            System.err.println(usage);
            return;
        }
    }
}
