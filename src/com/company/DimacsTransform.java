package com.company;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/*
 * @author  Christian Sommer, sommer@nii.ac.jp
 *
 * transforms a webgraph
 * (from http://law.dsi.unimi.it/index.php?option=com_include&Itemid=65 )
 * into DIMACS format
 * http://www.dis.uniroma1.it/~challenge9/format.shtml
 *
 * uses webgraph
 * http://webgraph.dsi.unimi.it/docs/it/unimi/dsi/webgraph/package-summary.html
 *
 * in class ImmutableGraph
 * http://webgraph.dsi.unimi.it/docs/it/unimi/dsi/webgraph/ImmutableGraph.html
 * static ImmutableGraph load(CharSequence basename) Creates a new
 * ImmutableGraph by loading a graph file from disk to memory, with all
 * offsets, using no progress logger.
 *
 * in class ArcListASCIIGraph
 * http://webgraph.dsi.unimi.it/docs/it/unimi/dsi/webgraph/ArcListASCIIGraph.html
 * static void store(ImmutableGraph graph, CharSequence basename, int
 * shift) Stores an arc-list ASCII graph with a given shift.
 */

public class DimacsTransform {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args == null || args.length ==0) {
            System.out
                    .println("usage: java webgraphToDIMACS.app.Application basename");
            return;
        }

        ProgressLogger pl = new ProgressLogger();
        pl.start();
        try {
            ImmutableGraph g = ImmutableGraph.load(args[0], pl);
            System.out.println("G=(V,E)");
            System.out.println("|V|=" + g.numNodes()+", |E|=" + g.numArcs());
            DimacsASCIIGraph.storeDimacs(g, args[0]+".gr", 1,false);
            System.out.println("finished.");
        } catch (IOException e) {
            System.out.println("IO problems");
            e.printStackTrace(System.out);
        }

    }
}

class DimacsASCIIGraph extends ArcListASCIIGraph {
    DimacsASCIIGraph(final InputStream is, final int shift)
            throws NumberFormatException, IOException {
        super(is, shift);
    }

    static void storeDimacs(final ImmutableGraph graph,
                            final CharSequence basename, final int shift,boolean normalized) throws IOException {
        final PrintWriter pw = new PrintWriter(new BufferedWriter(
                new FileWriter(basename.toString())));
        pw.println("p sp " + graph.numNodes() + " " + graph.numArcs());
        int d, s;
        int[] successor;
        for (NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator
                .hasNext();) {
            s = nodeIterator.nextInt();
            d = nodeIterator.outdegree();
            int length=(normalized?1:new Double(Math.ceil(Math.log(d))).intValue());
            successor = nodeIterator.successorArray();
            for (int i = 0; i < d; i++)
                pw.println("a " + (s + shift) + " " + (successor[i] + shift)
                        + " "+length);
        }
        pw.close();
    }

}
