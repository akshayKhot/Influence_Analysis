/**
 * Created by akshaykhot on 2017-05-06.
 */
public class Seed_Computer_Flat {

    Influence_Maximizer_Flat max;

    public Seed_Computer_Flat(Influence_Maximizer_Flat max) { this.max = max; }

    void get_seeds(int[] sketch_I, int[] node_I, int[] node_infl, int k, int count_sketches, int sketch_num, int set_infl) {

        // Calculating the node with max influence
        int infl_max = 0;
        int max_node = 0;
        int node_number = 1;
        int total_infl = 0;

        for(int v=0;v<this.max.n;v++)
        {
            if(node_infl[v] < node_number)
                continue;
            else
            {
                if(node_infl[v] * this.max.n / sketch_num > infl_max)
                {
                    infl_max = node_infl[v] * this.max.n / sketch_num;
                    max_node = v;
                }
            }
        }

        total_infl = set_infl + infl_max;

        System.out.println();
        System.out.println("Max Node = " + max_node +
                ", Maximum Influence = " + total_infl);
        System.out.println();

        if((k - 1)==0)
            return;

        // Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // plus re-calculate the influence
        for(int j=0;j<count_sketches;j++)
        {
            if(node_I[j] == -1)
                continue;
            else
            {
                if(node_I[j] == max_node)
                {
                    int redundant_sketch = sketch_I[j];

                    // As sketches are added to the array in numerical order, the same redundant sketch can be found before and after the max node
                    int l = j+1;
                    while(sketch_I[l] == redundant_sketch) {
                        node_infl[node_I[l]] = node_infl[node_I[l]] - 1;
                        sketch_I[l] = -1;
                        node_I[l] = -1;
                        l++;
                    }
                    if(j>0) // (j!=0) Boundary of the arrays sketch_I and node_I
                    {
                        int ll = j-1;
                        while(sketch_I[ll] == redundant_sketch) {
                            node_infl[node_I[ll]] = node_infl[node_I[ll]] - 1;
                            sketch_I[ll] = -1;
                            node_I[ll] = -1;
                            ll--;
                        }
                    }
                    sketch_I[j] = -1;
                    node_I[j] = -1;
                }
            }
        }
        node_infl[max_node] = 0;

        get_seeds(sketch_I, node_I, node_infl, k-1, count_sketches, sketch_num, total_infl);
    }


}
