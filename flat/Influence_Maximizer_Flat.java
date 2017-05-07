import it.unimi.dsi.webgraph.ImmutableGraph;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;

public class Influence_Maximizer_Flat {
    ImmutableGraph G;
    int n;
    long m;
    double eps = .1;
    double p;
    int k = 5;
    int beta = 2;
    double W;
    double sketchTime, seedTime;

    int[] permutation;
    BitSet marked;



    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        Influence_Maximizer_Flat imfl = new Influence_Maximizer_Flat("/Users/akshaykhot/Desktop/Thesis/infl_max/sym-noself/cnr-2000-t", 0.1);
        imfl.maximize();

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Total Time = " + estimatedTime /(1000.0) + " sec");
    }

    public Influence_Maximizer_Flat(String basename, Double  p) throws Exception {
        G = ImmutableGraph.load(basename);

        n = G.numNodes();
        m = G.numArcs();
        W = beta * (n + m) * Math.log(n);

        System.out.println("n=" + n + ", m=" + m  + ", W=" + W);

        marked = new BitSet(n);

        permutation = new int[n];



        for(int i=0; i<n; i++)
            permutation[i] = i;

        Random rnd = new Random();
        for (int i=n; i>1; i--) {
            int j = rnd.nextInt(i);
            int temp = permutation[i-1];
            permutation[i-1] = permutation[j];
            permutation[j] = temp;
        }
        System.out.println("Permutation array shuffled.");



        this.p = p;
    }

    public void maximize() {
        long start, time;

        start = System.currentTimeMillis();
        Sketch_Generator_Flat sketcher = new Sketch_Generator_Flat(this);
        sketcher.get_sketch();
        time = System.currentTimeMillis() - start;
        this.sketchTime = time/1000.0;


        start = System.currentTimeMillis();
        Seed_Computer_Flat seeder = new Seed_Computer_Flat(this);
        seeder.get_seeds(sketcher.iSketch, sketcher.iNode, sketcher.node_infl, k, sketcher.count_sketches, sketcher.sketch_num, 0);

        time = System.currentTimeMillis() - start;
        this.seedTime = time/1000.0;


        System.out.println("\nTime to create sketches = " + this.sketchTime + " sec");
        System.out.println("Time to compute seeds = " + this.seedTime + " sec");

    }







}
