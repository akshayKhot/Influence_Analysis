import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class Seed_Computer {

    Influence_Maximizer max;

    public Seed_Computer(Influence_Maximizer max) {
        this.max = max;
    }

    void get_seeds(List<List<Integer>> I, int k, int sketch_num, int set_infl) {

        int infl_max = 0;
        int max_node = 0;
        int total_infl = 0;

        for(int v=0;v<this.max.n;v++)
        {
            if(I.get(v).size() < 1)
                continue;
            else {
                int infl = I.get(v).size() * this.max.n/sketch_num;
                if(infl > infl_max) {
                    infl_max = infl;
                    max_node = v;
                }
            }
        }

        total_infl = set_infl + infl_max;

        System.out.println("\nMax Node = " + max_node + ", Influence = " + infl_max + ", Maximum Influence = " + total_infl);

        if((k - 1)==0)
            return;

        // Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // re-calculate the influence

        List<Integer> nodes_in_max = I.get(max_node);
        for(int u=0;u<this.max.n;u++) {
            if((I.get(u).size() < 1) || (u == max_node))
                continue;
            else
            {
                I.get(u).removeAll(nodes_in_max);
            }
        }
        I.get(max_node).clear();

        get_seeds(I, k-1, sketch_num, total_infl);
    }


}
