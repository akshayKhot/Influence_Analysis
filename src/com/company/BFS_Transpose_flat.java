package com.company;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class BFS_Transpose_flat {
    ImmutableGraph G;
    int n;
    long m;
    double eps = .1;
    double p; // given probability of arc existence
    int k; // given number of maximum influence nodes
    int nMAX = 1000000;

    int[] permutation;
    BitSet marked;

    // Flat arrays to keep: sketches - all sketch numbers in order, like 0000...1111...2222...;
    //                      nodes - all corresponding node IDs participating in sketch, like 13 17 100 230 ... 13 23 300 10000 ...
    //                      node_infl - counts of sketches the node is participating into
    int[] sketches;
    int[] nodes;
    int[] node_infl;
    int total_nodes_in_sketch; // the length of sketches and nodes arrays

    public BFS_Transpose_flat(String basename, Double  p) throws Exception {
        G = ImmutableGraph.load(basename);

        n = G.numNodes();
        m = G.numArcs();
        k = 5;
        System.out.println("n="+n + ", m=" +m  + ", W=" +( 64 * (n+m)*Math.log(n)  ));

        marked = new BitSet(n);

        permutation = new int[n];

        // Flat arrays created: sketches and nodes are very large, nMAX equals one billion
        //                      node_infl - the size is n (the number of nodes)
        sketches = new int[nMAX];
        nodes = new int[nMAX];
        node_infl = new int[n];

        System.out.println("Initializing the permutation array...");
        for(int i=0; i<n; i++)
            permutation[i] = i;
        System.out.println("Permutation array initialized.");

        System.out.println("Shuffling the permutation array...");
        Random rnd = new Random();
        // Shuffle array
        for (int i=n; i>1; i--) {
            int j = rnd.nextInt(i);
            //swap
            int temp = permutation[i-1];
            permutation[i-1] = permutation[j];
            permutation[j] = temp;
        }
        System.out.println("Permutation array shuffled.");

        System.out.println("Initializing index I...");
        for(int i=0;i<nMAX;i++)
        {
            sketches[i] = -1;
            nodes[i] = -1;
        }

        System.out.println("index I initialized.");

        this.p = p;
    }

    void get_sketch() {
        //double W = (1/Math.pow(eps, 3)) * (n + m) * Math.log(n);
        double W = 64 * (n + m) * Math.log(n);

        double weight_of_current_index = 0.0;
        int index = 0;
        int sketch_num = 0;

        long startTime = System.currentTimeMillis();
        total_nodes_in_sketch = 0;
        while(weight_of_current_index < W)
        {
            if(index % 10000 == 0) {
                double sofarTimeInMin = (System.currentTimeMillis() - startTime)/(1000.0 * 60);
                double pctDone = 100*weight_of_current_index/W;

                System.out.println(
                        "sketch=" + sketch_num +
                                ",  index=" + index +
                                ", weight_of_current_index=" + weight_of_current_index +
                                ", this is " + pctDone + "% of W" +
                                ", elapsed " + sofarTimeInMin + " min");

            }

            int v = permutation[index];
            marked.clear();
            BFS(v,marked);

            int total_out_degree = 0;

            int iteration = 0;
            for (int nextMarkedNode = marked.nextSetBit(0); nextMarkedNode >= 0; nextMarkedNode = marked.nextSetBit(nextMarkedNode+1))
            {
                sketches[total_nodes_in_sketch + iteration] = sketch_num;
                nodes[total_nodes_in_sketch + iteration]  = nextMarkedNode;
                node_infl[nextMarkedNode] = node_infl[nextMarkedNode] + 1;
                iteration = iteration + 1;
                total_out_degree += G.outdegree(nextMarkedNode);
            }

            weight_of_current_index += (marked.cardinality() + total_out_degree);
            index = ( index + 1 ) % n;
            sketch_num++;
            total_nodes_in_sketch += marked.cardinality();
        }

        System.out.println();
        System.out.println("Index: " + index +
                ", Number of Sketches: " + sketch_num +
                ", Size of array iSketch: " + total_nodes_in_sketch);
        System.out.println();

        // Cutting off the tails of sketches and nodes arrays, making the arrays shorter
        int[] iSketch = new int[total_nodes_in_sketch +1];
        System.arraycopy(sketches,0,iSketch,0, total_nodes_in_sketch);

        int[] iNode = new int[total_nodes_in_sketch +1];
        System.arraycopy(nodes,0,iNode,0, total_nodes_in_sketch);

        System.gc();
        get_seeds(iSketch, iNode, node_infl, k, total_nodes_in_sketch);
    }

    void BFS(int v, BitSet marked) {

        Random random = new Random();

        Deque<Integer> queue = new ArrayDeque<Integer>();

        queue.add(v);
        marked.set(v);

        while (!queue.isEmpty()) {
            int u = queue.remove();
            int[] u_neighbors = G.successorArray(u);
            int u_deg = G.outdegree(u);

            for (int ni = 0; ni < u_deg; ni++) {
                int uu = u_neighbors[ni];
                double xi = random.nextDouble();

                if (!marked.get(uu) && xi < p) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }

    void get_seeds(int[] sketches, int[] nodes, int[] node_infl, int k, int count_sketches) {

        // Calculating the node with max influence
        int infl_max = 0;
        int max_node = 0;

        for(int i=0;i<n;i++)
        {
            if(node_infl[i] > infl_max)
            {
                infl_max = node_infl[i];
                max_node = i;
            }
        }

        System.out.println();
        System.out.println(
                "Max Node = " + max_node +
                        ", Maximum Influence = " + infl_max);
        System.out.println();

        // Stopping condition: no need to re-calculate the influence, if we already got the k seeds
        if((k - 1)==0)
            return;

        // Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // plus re-calculate the influence
        node_infl[max_node] = 0;
        for(int j=0;j<count_sketches;j++)
        {
            if(nodes[j] == max_node)
            {
                int redundant_sketch = sketches[j];

                // As sketches are added to the array in numerical order, the same redundant sketch can be found before and after the max node
                int l = j+1;
                while(sketches[l] == redundant_sketch) {
                    node_infl[nodes[l]] = node_infl[nodes[l]] - 1;
                    sketches[l] = -1;
                    nodes[l] = -1;
                    l++;
                }
                if(j>0) // (j!=0) Boundary of the arrays sketches and nodes
                {
                    int ll = j-1;
                    while(sketches[ll] == redundant_sketch) {
                        node_infl[nodes[ll]] = node_infl[nodes[ll]] - 1;
                        sketches[ll] = -1;
                        nodes[ll] = -1;
                        ll--;
                    }
                }
                sketches[j] = -1;
                nodes[j] = -1;
            }
        }

        get_seeds(sketches, nodes, node_infl, k-1, count_sketches);
    }


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long estimatedTime;

        //args = new String[] {"edges02", "0.2"};
        args = new String[] {"/Users/akshaykhot/Desktop/Thesis/code/cnr-2000-t", "0.1"};
        //args = new String[] {"uk100Tnoself", "0.1"};


        String basename  = args[0];
        double p = Double.valueOf(args[1]);

        BFS_Transpose_flat bt = new BFS_Transpose_flat(basename, p);
        bt.get_sketch();

        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed = " + estimatedTime / 1000.0);
    }
}
