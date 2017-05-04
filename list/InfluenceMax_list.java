import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import it.unimi.dsi.webgraph.ImmutableGraph;

public class InfluenceMax_list {
	ImmutableGraph G;
	int n;
	long m;
	double eps = .1;
    double p; // given probability of arc existence
    int k = 5; // given number of maximum influence nodes
    int beta = 2;
    double W;
    
    int[] permutation;
    BitSet marked;
	
	public InfluenceMax_list(String basename, Double  p) throws Exception {
		G = ImmutableGraph.load(basename);
		
		n = G.numNodes();
		m = G.numArcs();
		W = beta * (n + m) * Math.log(n);

        System.out.println("n="+n + ", m=" +m  + ", W=" + W);
		
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

        this.p = p;
	}
    
	
	void get_sketch() {

        List<List<Integer>> I = new ArrayList<List<Integer>>();
        for(int j=0;j<n;j++)
        {
            I.add(new ArrayList<Integer>());
        }

	    double weight_of_current_index = 0.0;
	    int index = 0;
        int sketch_num = 0;

	    long startTime = System.currentTimeMillis();
        
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
	        BFS(v,marked); // marked now contains all nodes influenced by v. i.e., in the original graph, all the nodes which influence v.
            
            int j=0;
	        int total_out_degree = 0;
	        for (int u = marked.nextSetBit(0); u >= 0; u = marked.nextSetBit(u+1))
	        {
                I.get(u).add(sketch_num);
	            total_out_degree += G.outdegree(u);
	        }
	        weight_of_current_index += (marked.cardinality() + total_out_degree);
	        index = ( index + 1 ) % n;
            sketch_num++;
	    }
        System.out.println();
	    System.out.println("Index: " + index +
                           " Number of Sketches: " + sketch_num);
        System.out.println();

        System.gc();
        int set_infl = 0;
        get_seeds(I, k, sketch_num, set_infl);
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
    
    void get_seeds(List<List<Integer>> I, int k, int sketch_num, int set_infl) {

        int infl_max = 0;
        int max_node = 0;
        int total_infl = 0;

        for(int v=0;v<n;v++)
        {
            if(I.get(v).size() < 1)
                continue;
            else {
                int infl = I.get(v).size() * n/sketch_num;
                if(infl > infl_max) {
                    infl_max = infl;
                    max_node = v;
                }
            }
        }

        total_infl = set_infl + infl_max;

        System.out.println(
                           "\nMax Node = " + max_node +
                           ", Maximum Influence = " + total_infl);

        if((k - 1)==0)
            return;
        
        // Re-calculating the influence of the remaining nodes: remove max node and the sketches it participated in
        // plus re-calculate the influence

        List<Integer> nodes_in_max = I.get(max_node);
        for(int u=0;u<n;u++) {
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

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		InfluenceMax_list iml = new InfluenceMax_list("/Users/akshaykhot/Desktop/Thesis/infl_max/sym-noself/cnr-2000-t", 0.1);
		iml.get_sketch();

        long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("\nTime elapsed = " + estimatedTime / 1000.0 + " sec");
	}
}
