import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*********************************
 * COMP9331 Assignment 2
 * Programmed by Chunnan Sheng
 * Student Code: z5100764
 ********************************/

/**
 * DvrPr which has an extra method to switch to another cost of each neighbor node.
 * <p>
 * It extends DvrBase,
 * @author Chunnan Sheng
 *
 */
public class DvrPr extends DvrBase
{
    private boolean poison_reverse = false;

    public DvrPr(String host_id, int port, String file_name) throws IOException
    {
        super(host_id, port, file_name);
    }

    /**
     * Override the config reading function of DvrBase since Poison Reverse mode
     * needs a different kind of config file.
     */
    protected void readConfig(String file_name) throws IOException
    {
        ArrayList<String[]> neighbors = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(file_name)))
        {
            String line = null;

            line = br.readLine();
            int nei_number = Integer.parseInt(line);
            // System.out.println(line);

            for (int i = 0; i < nei_number; i++)
            {
                line = br.readLine();
                String[] neighbor_info = line.split("[ \t]+");
                neighbors.add(neighbor_info);
            }
        }

        for (String[] nei : neighbors)
        {
            double cost = Double.parseDouble(nei[1]);
            double pr_cost = Double.parseDouble(nei[2]);
            int port = Integer.parseInt(nei[3]);
            NeighborNode neighbor = new NeighborNode(nei[0], new Cost(cost), new Cost(pr_cost), port);
            // this.m_table.addNeighbor(neighbor);
            this.m_neighbors.put(neighbor.getName(), neighbor);
        }
    }

    /**
     * Switch to another cost of each neighbor to emulate Count-to-Infinity problem
     * which needs Poisoned Reverse solution.
     */
    protected void switchToNewCostsOfNeighbors()
    {
        synchronized (this)
        {
            if (!this.poison_reverse)
            {
                this.m_old_table = new DvrTable(this.m_table);

                // Change costs of all neighbors
                for (String nei_id : this.m_neighbors.keySet())
                {
                    NeighborNode nei_node = this.m_neighbors.get(nei_id);
                    nei_node.setCost(nei_node.getPrCost());
                }

                this.m_old_table = new DvrTable(this.m_table);
                this.m_table.updateNeighborCostToTable();

                Globals.println(
                        "------------- Costs to neighbors are changed (Poison Reversse may be needed) ------------");
                this.printDvrTable();

                this.poison_reverse = true;

                last_time_table_change = System.currentTimeMillis();
                path_printed = false;
            }
        }
    }

    /**
     * Switch to new costs of neighbors when DVR table converges.
     */
    protected void doSomethingWhenDvrTableConverges()
    {
        this.switchToNewCostsOfNeighbors();
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            Globals.println("Insufficient arguments!");
            Globals.println("Example 1: DvrPr A 3000 config.txt");
            Globals.println("Example 2: DvrPr A 3000 config.pr.txt -p");
            return;
        }
        else
        {
            String host_id = args[0];
            int port = Integer.parseInt(args[1]);
            String file_name = args[2];
            DvrBase dvr = null;

            if (args.length == 3)
            {
                dvr = new DvrBase(host_id, port, file_name);
                (new Thread(dvr)).start();
                dvr.start();
            }
            else if (args.length == 4 && args[3].equalsIgnoreCase("-p"))
            {
                dvr = new DvrPr(host_id, port, file_name);
                (new Thread(dvr)).start();
                dvr.start();
            }
        }
    }
}
