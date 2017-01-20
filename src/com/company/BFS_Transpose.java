package com.company;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class BFS_Transpose {
    ImmutableGraph Graph;
    int numVertices;
    long numEdges;
    double eps = .1;
    double prob; // given probability of arc existence

    int[] permutation;
    BitSet marked;

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long estimatedTime;

        //args = new String[] {"edges02", "0.2"};
        args = new String[] {"/Users/akshaykhot/Desktop/Thesis/code/cnr-2000-t", "0.5"};

        String basename  = args[0];
        double prob = Double.valueOf(args[1]);

        BFS_Transpose bt = new BFS_Transpose(basename, prob);
        bt.get_sketch();

        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed = " + estimatedTime / 1000.0);
    }

    public BFS_Transpose(String basename, Double prob) throws Exception {
        Graph = ImmutableGraph.load(basename);

        numVertices = Graph.numNodes();
        numEdges = Graph.numArcs();

        double W = (1/Math.pow(eps, 3)) * (numVertices + numEdges) * Math.log(numVertices);
        System.out.println("numVertices = "+ numVertices + ", numEdges = " + numEdges + ", W = " + W);

        marked = new BitSet(numVertices);

        permutation = new int[numVertices];

        System.out.println("Initializing the permutation array...");
        for(int i = 0; i< numVertices; i++)
            permutation[i] = i;
        System.out.println("Permutation array initialized.");

        System.out.println("Shuffling the permutation array...");
        Random rnd = new Random();
        // Shuffle array
        for (int i = numVertices; i>1; i--) {
            int j = rnd.nextInt(i);
            //swap
            int temp = permutation[i-1];
            permutation[i-1] = permutation[j];
            permutation[j] = temp;
        }
        System.out.println("Permutation array shuffled.");


        this.prob = prob;
    }

    void get_sketch() {
        int num_sketch = 100; // numVertices/10000;

        int sum = 0;


        // repeat this while weight of the index is less than W. How to get the weight of index
        for(int i=0; i<num_sketch; i++) {
            System.out.println(i);
            int v = permutation[i];
            marked.clear();
            BFS(v,marked);

            int nodes_reached = marked.cardinality();
            sum += nodes_reached;
            //System.out.println("Nodes reachable from " + v + ": " + nodes_reached);
        }

        System.out.println("Avg of nodes reached = " + (1.0*sum/num_sketch));
        System.out.println("This is " + (100.0*(sum/num_sketch)/ numVertices) + "% of nodes");
    }

    void BFS(int z, BitSet marked) {

        Random random = new Random();

        Deque<Integer> queue = new ArrayDeque<Integer>();

        queue.add(z);
        marked.set(z);

        while (!queue.isEmpty()) {
            int v = queue.remove();
            int[] v_neighbors = Graph.successorArray(v);
            int v_deg = Graph.outdegree(v);

            for (int ni = 0; ni < v_deg; ni++) {
                int uv = v_neighbors[ni];
                double xi = random.nextDouble(); // activation is random Zi is v

                if (!marked.get(uv) && xi < prob) {
                    queue.add(uv);
                    marked.set(uv);
                }
            }
        }
    }
}