
import java.util.*;

public class Sketch_Generator_Flat {

    Influence_Maximizer_Flat max;
    List<List<Integer>> I;
    int sketch_num;
    // Flat arrays to keep: sketches - all sketch numbers in order, like 0000...1111...2222...;
    //                      nodes - all corresponding node IDs participating in sketch, like 13 17 100 230 ... 13 23 300 10000 ...
    //                      node_infl - counts of sketches the node is participating into;
    int[] sketches, iSketch;
    int[] nodes, iNode;
    int[] node_infl;
    int nMAX = 10000000;

    int count_sketches; // the length of sketches and nodes arrays


    public Sketch_Generator_Flat(Influence_Maximizer_Flat max) {
        this.max = max;
        // Flat arrays created: sketches and nodes are very large, nMAX equals one billion
        // node_infl - the size is n (the number of nodes)
        sketches = new int[nMAX];
        nodes = new int[nMAX];
        node_infl = new int[max.n];

        System.out.println("Initializing index I...");
        for(int i=0;i<nMAX;i++)
        {
            sketches[i] = -1;
            nodes[i] = -1;
        }

        System.out.println("index I initialized.");
    }

    void get_sketch() {
        double weight_of_current_index = 0.0;
        int index = 0;

        long startTime = System.currentTimeMillis();
        count_sketches = 0;
        Random gen_rnd = new Random();

        while(weight_of_current_index < max.W)
        {
            if(index % 100000 == 0) {
                double sofarTimeInMin = (System.currentTimeMillis() - startTime)/(1000.0 * 60);
                double pctDone = 100*weight_of_current_index/max.W;

                System.out.println(
                        "sketch=" + sketch_num +
                                ",  index=" + index +
                                ", weight_of_current_index=" + weight_of_current_index +
                                ", this is " + pctDone + "% of W" +
                                ", elapsed " + sofarTimeInMin + " min");

            }

            int v = max.permutation[gen_rnd.nextInt(max.n)];
            max.marked.clear();
            BFS(v,max.marked);

            int total_out_degree = 0;
            int iteration = 0;
            for (int u = max.marked.nextSetBit(0); u >= 0; u = max.marked.nextSetBit(u+1))
            {
                sketches[count_sketches + iteration] = sketch_num;
                nodes[count_sketches + iteration]  = u;
                node_infl[u] = node_infl[u] + 1;
                iteration = iteration++;
                total_out_degree += max.G.outdegree(u);
            }

            weight_of_current_index += (max.marked.cardinality() + total_out_degree);
            index = ( index + 1 ) % max.n;
            sketch_num++;
            count_sketches += max.marked.cardinality();
        }

        System.out.println("\nIndex: " + index + ", Number of Sketches: " + sketch_num + ", Size of array iSketch: " + count_sketches + "\n");

        // Cutting off the tails of sketches and nodes arrays, making the arrays shorter
        iSketch = new int[count_sketches + 1];
        System.arraycopy(sketches,0,iSketch,0,count_sketches);

        iNode = new int[count_sketches + 1];
        System.arraycopy(nodes,0,iNode,0,count_sketches);

        System.gc();

    }

    void BFS(int v, BitSet marked) {

        Random random = new Random();

        Deque<Integer> queue = new ArrayDeque<Integer>();

        queue.add(v);
        marked.set(v);

        while (!queue.isEmpty()) {
            int u = queue.remove();
            int[] u_neighbors = max.G.successorArray(u);
            int u_deg = max.G.outdegree(u);

            for (int ni = 0; ni < u_deg; ni++) {
                int uu = u_neighbors[ni];
                double xi = random.nextDouble();

                if (!marked.get(uu) && xi < max.p) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }
}
