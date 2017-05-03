//package com.company;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class InfluenceMax_Compr {
    ImmutableGraph G;
    int n;
    long m;
    double eps = .1;
    double p; // given probability of arc existence
    int k; // given number of maximum influence nodes
    int nMAX = 1000000000;

    int[] permutation;
    BitSet marked;

/* Flat arrays to keep: sketches - from 0 to (sketch_num - 1), this array stores the accumulating number of nodes in sketches;
                        nodes - all corresponding node IDs participating in sketch, like 13 17 100 230 ... 13 23 300 10000 ...
                        node_infl - counts of sketches the node is participating into
*/
    int[] sketches;
    int[] nodes;
    int[] node_infl;

    public InfluenceMax_Compr(String basename, Double  p) throws Exception {
        G = ImmutableGraph.load(basename);

        n = G.numNodes();
        m = G.numArcs();
        k = 5;
        System.out.println("n="+n + ", m=" +m  + ", W=" +( 512 * (n+m)*Math.log(n)));

        marked = new BitSet(n);

        permutation = new int[n];

        // Flat arrays created: arrays are very large,nMAX equals one billion
        //                      node_infl - the size is n (the number of nodes)
        sketches = new int[nMAX/50];
        nodes = new int[nMAX];
        node_infl = new int[n];

//        System.out.println("Initializing the permutation array...");
        for(int i=0; i<n; i++)
            permutation[i] = i;
//        System.out.println("Permutation array initialized.");

//        System.out.println("Shuffling the permutation array...");
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

//        System.out.println("Initializing index I...");
        for(int i=0;i<nMAX;i++)
        {
            nodes[i] = -1;
        }
        for(int i=0;i<nMAX/50;i++)
        {
        sketches[i] = -1;
        }

        System.out.println("index I initialized.");

        this.p = p;
    }

    void get_sketch() {
        //double W = (1/Math.pow(eps, 3)) * (n + m) * Math.log(n);
        double W = 512 * (n + m) * Math.log(n);

        double weight_of_current_index = 0.0;
        int index = 0;
        int sketch_num = 0;

        long startTime = System.currentTimeMillis();
        int accumulated_sketches = 0;
        Random gen_rnd = new Random();

        while(weight_of_current_index < W)
        {
            if(index % 100000 == 0) {
                double sofarTimeInMin = (System.currentTimeMillis() - startTime)/(1000.0 * 60);
                double pctDone = 100*weight_of_current_index/W;

                System.out.println(
                        "sketch=" + sketch_num +
                                ",  index=" + index +
                                ", weight_of_current_index=" + weight_of_current_index +
                                ", this is " + pctDone + "% of W" +
                                ", elapsed " + sofarTimeInMin + " min");

            }

            int v = permutation[gen_rnd.nextInt(n)];
            marked.clear();
            BFS(v,marked);

            int j = 0;

            int total_out_degree = 0;

            int iteration = 0;

            for (int u = marked.nextSetBit(0); u >= 0; u = marked.nextSetBit(u+1))
            {
                node_infl[u] = node_infl[u] + 1;
                nodes[accumulated_sketches + j]  = u;
                j++;
                total_out_degree += G.outdegree(u);
            }
            accumulated_sketches += marked.cardinality();
            sketches[sketch_num] = accumulated_sketches;
            weight_of_current_index += (marked.cardinality() + total_out_degree);
            index = ( index + 1 ) % n;
            sketch_num++;
        }

        System.out.println();
        System.out.println("Index: " + index +
                           ", Number of Sketches: " + sketch_num +
                           ", Size of array iNode " + accumulated_sketches);
        System.out.println();

        int[] iSketch = new int[sketch_num + 1];
        System.arraycopy(sketches,0,iSketch,0,sketch_num);
        
        int[] iNode = new int[accumulated_sketches + 1];
        System.arraycopy(nodes,0,iNode,0, accumulated_sketches);

        System.gc();
        
        int set_infl = 0;
        get_seeds(iSketch, iNode, node_infl, k, accumulated_sketches, sketch_num, set_infl);
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

    void get_seeds(int[] sketches, int[] nodes, int[] node_infl, int k, int accumulated_sketches, int sketch_num, int set_infl) {

        // Calculating the node with max influence
        int infl_max = 0;
        int max_node = 0;
        int node_sketch = 1;
        int redundant_sketch = 0;
        int total_infl = 0;

        for(int v=0;v<n;v++)
        {
            if(node_infl[v] < node_sketch)
                continue;
            else
            {
                if(node_infl[v] * n / sketch_num > infl_max)
                {
                    max_node = v;
                    infl_max = node_infl[v] * n / sketch_num;
                }
            }
        }

        total_infl = set_infl + infl_max;

        System.out.println();
        System.out.println(
                        "Max Node = " + max_node +
                        ", Maximum Influence = " + total_infl);
        System.out.println();

        // Stopping condition: no need to re-calculate the influence, if we already got the k seeds
        if((k - 1)==0)
            return;

        // Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // plus re-calculate the influence
        for(int j=0;j<accumulated_sketches;j++)
        {
            if(nodes[j] == -1)
                continue;
            else
            {
                if(nodes[j] == max_node)
                {
                    for(int sn = 0; sn < sketch_num; sn++)
                    {
                        if(j < sketches[sn])
                        {
                            redundant_sketch = sn;
                            break;
                        }
                        else
                            continue;

                    }

                // As nodes are added to the nodes array in numerical order, the nodes for the same redundant sketch can be found before and after the max node
                    int l = j+1;
                    while(l < sketches[redundant_sketch]) {
                        node_infl[nodes[l]] = node_infl[nodes[l]] - 1;
                        nodes[l] = -1;
                        l++;
                    }
                    
                    if(j>0 && redundant_sketch > 0) // Boundary of the arrays sketches and nodes
                    {
                        int ll = j-1;
                        while(ll >= sketches[redundant_sketch - 1]) {
                            node_infl[nodes[ll]] = node_infl[nodes[ll]] - 1;
                            nodes[ll] = -1;
                            ll--;
                        }
                    }
                    
                    nodes[j] = -1;
                }
            }
        }
        node_infl[max_node] = 0;

        get_seeds(sketches, nodes, node_infl, k-1, accumulated_sketches, sketch_num, total_infl);
    }


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        long estimatedTime;

        args = new String[]  {"cnr2000t", "0.1"};
        //args = new String[] {"uk100Tnoself", "0.1"};


        String basename  = args[0];
        double p = Double.valueOf(args[1]);

        InfluenceMax_Compr sk = new InfluenceMax_Compr(basename, p);
        sk.get_sketch();

        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("Time elapsed = " + estimatedTime / 1000.0 + " sec");
    }
}