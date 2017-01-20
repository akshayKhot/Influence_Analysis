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
        double W = (1/Math.pow(eps, 3)) * (numVertices + numEdges) * Math.log(numVertices);

        double weight_of_current_index = 0.0;
        int index = 0;

        while(weight_of_current_index < W)
        {
            System.out.println(weight_of_current_index);
            int v = permutation[index];
            marked.clear();
            BFS(v,marked);

            int total_out_degree = 0;
            for (int i = marked.nextSetBit(0); i >= 0; i = marked.nextSetBit(i+1))
            {
                total_out_degree += Graph.outdegree(i);
            }
            weight_of_current_index += (marked.cardinality() + total_out_degree);
            index++;
        }
        System.out.println("Index: " + index);

    }

    void BFS(int z, BitSet reached_nodes) {

        Random random = new Random();

        Deque<Integer> queue = new ArrayDeque<Integer>();

        queue.add(z);
        reached_nodes.set(z);

        while (!queue.isEmpty()) {
            int v = queue.remove();
            int[] v_neighbors = Graph.successorArray(v);
            int v_deg = Graph.outdegree(v);

            for (int ni = 0; ni < v_deg; ni++) {
                int uv = v_neighbors[ni];
                double xi = random.nextDouble(); // activation is random Zi is v

                if (!reached_nodes.get(uv) && xi < prob) {
                    queue.add(uv);
                    reached_nodes.set(uv);
                }
            }
        }
    }
}