import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class Sketch_Generator {

    Influence_Maximizer max;
    List<List<Integer>> I;
    int sketch_num;

    public Sketch_Generator(Influence_Maximizer max) {
        this.max = max;
    }

    void get_sketch() {

        I = new ArrayList<List<Integer>>();
        for(int j=0;j<this.max.n;j++)
        {
            I.add(new ArrayList<Integer>());
        }

        double weight_of_current_index = 0.0;
        int index = 0;
        sketch_num = 0;

        long startTime = System.currentTimeMillis();

        Random gen_rnd = new Random();

        while(weight_of_current_index < this.max.W)
        {
            if(index % 100000 == 0) {
                double sofarTimeInMin = (System.currentTimeMillis() - startTime)/(1000.0 * 60);
                double pctDone = 100*weight_of_current_index/this.max.W;

                System.out.println(
                        "sketch=" + sketch_num +
                                ",  index=" + index +
                                ", weight_of_current_index=" + weight_of_current_index +
                                ", this is " + pctDone + "% of W" +
                                ", elapsed " + sofarTimeInMin + " min");

            }

            int v = this.max.permutation[gen_rnd.nextInt(this.max.n)];
            this.max.marked.clear();
            BFS(v,this.max.marked); // marked now contains all nodes influenced by v. i.e., in the original graph, all the nodes which influence v.

            int j=0;
            int total_out_degree = 0;
            for (int u = this.max.marked.nextSetBit(0); u >= 0; u = this.max.marked.nextSetBit(u+1))
            {
                I.get(u).add(sketch_num);
                total_out_degree += this.max.G.outdegree(u);
            }
            weight_of_current_index += (this.max.marked.cardinality() + total_out_degree);
            index = ( index + 1 ) % this.max.n;
            sketch_num++;
        }

        System.out.println("\nIndex: " + index + "\nNumber of Sketches: " + sketch_num);

        System.gc();
    }

    void BFS(int v, BitSet marked) {

        Random random = new Random();

        Deque<Integer> queue = new ArrayDeque<Integer>();

        queue.add(v);
        marked.set(v);

        while (!queue.isEmpty()) {
            int u = queue.remove();
            int[] u_neighbors = this.max.G.successorArray(u);
            int u_deg = this.max.G.outdegree(u);

            for (int ni = 0; ni < u_deg; ni++) {
                int uu = u_neighbors[ni];
                double xi = random.nextDouble();

                if (!marked.get(uu) && xi < this.max.p) {
                    queue.add(uu);
                    marked.set(uu);
                }
            }
        }
    }

}
