import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Definition of a DVR table
 * @author Chunnan Sheng
 *
 */
public class DvrTable
{
    private class CostComparator implements Comparator<TableItem>
    {

        @Override
        public int compare(TableItem o1, TableItem o2)
        {
            if (o1.getCost().isInfinite() && o2.getCost().isInfinite())
            {
                return 0;
            }

            if (o1.getCost().isInfinite() && !o2.getCost().isInfinite())
            {
                return 1;
            }

            if (!o1.getCost().isInfinite() && o2.getCost().isInfinite())
            {
                return -1;
            }

            if (o1.getCost().getValue() > o2.getCost().getValue())
            {
                return 1;
            }
            else if (o1.getCost().getValue() == o2.getCost().getValue())
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
    }

    private class TableItem
    {
        String m_column_name;
        Cost m_cost;

        public TableItem(String col_name, Cost c)
        {
            this.m_column_name = col_name;
            this.m_cost = c;
        }

        public Cost getCost()
        {
            return this.m_cost;
        }

        public String getName()
        {
            return this.m_column_name;
        }

        public boolean equals(Object other)
        {
            if (null == other || !(other instanceof TableItem))
            {
                return false;
            }

            TableItem other_item = (TableItem) other;
            return this.m_column_name.equals(other_item.getName());
        }
    }

    private class Row
    {
        private Map<String, TableItem> m_items;
        private PriorityQueue<TableItem> m_queue;
        private long m_created_time;

        public Row(Map<String, Cost> neighbors)
        {
            this.m_items = new HashMap<String, TableItem>();
            this.m_queue = new PriorityQueue<TableItem>(new CostComparator());

            this.m_created_time = System.currentTimeMillis();

            // Add all columns of infinite cost
            for (String column : neighbors.keySet())
            {
                TableItem new_item = new TableItem(column, new Cost());
                this.m_items.put(column, new_item);
                this.m_queue.add(new_item);
            }
        }

        public long getCreatedTime()
        {
            return this.m_created_time;
        }

        /**
         * If the item already exists in this row, update it.
         * 
         * @param item
         */
        public void addItem(TableItem item)
        {
            this.m_items.put(item.getName(), item);

            if (this.m_queue.contains(item))
            {
                this.m_queue.remove(item);
            }

            this.m_queue.add(item);
        }

        public TableItem getItem(String name)
        {
            return this.m_items.get(name);
        }

        public TableItem removeItem(String name)
        {
            TableItem item = this.m_items.remove(name);
            if (null != item)
            {
                this.m_queue.remove(item);
            }

            return item;
        }

        public boolean containsItem(String name)
        {
            return this.m_items.containsKey(name);
        }

        public Cost getMinimumCost()
        {
            if (!this.m_queue.isEmpty())
            {
                return this.m_queue.peek().getCost();
            }

            return null;
        }

        public TableItem getItemOfMinCost()
        {
            if (!this.m_queue.isEmpty())
            {
                return this.m_queue.peek();
            }

            return null;
        }

        public boolean equals(Object other)
        {
            if (null == other || !(other instanceof Row))
            {
                return false;
            }

            Row other_row = (Row) other;

            if (this.m_items.size() != other_row.m_items.size())
            {
                return false;
            }

            for (String column_name : this.m_items.keySet())
            {
                TableItem item = this.m_items.get(column_name);
                TableItem other_item = other_row.m_items.get(column_name);

                if (null == other_item)
                {
                    return false;
                }

                if (!item.getCost().equals(other_item.getCost()))
                {
                    return false;
                }
            }

            return true;
        }

        public Collection<TableItem> allItems()
        {
            return this.m_items.values();
        }

        public Collection<String> allColumnNames()
        {
            return this.m_items.keySet();
        }
    }

    private Map<String, Row> m_table;
    
    /**
     * Alive neighbors
     */
    private Map<String, Cost> m_alive_neighbors;
    
    /**
     * Remembered dead nodes that are not neighbors
     */
    private Map<String, Long> m_dead_nodes;
    
    /**
     * All neighbors
     */
    private Map<String, NeighborNode> m_all_neighbors;
    private String m_host_id;

    public DvrTable(String host, Map<String, NeighborNode> all_neighors)
    {
        this.m_host_id = host;
        this.m_table = new HashMap<String, Row>();
        this.m_alive_neighbors = new HashMap<String, Cost>();
        this.m_dead_nodes = new HashMap<String, Long>();
        this.m_all_neighbors = all_neighors;
    }

    /**
     * Copy constructor of DVR table. This is used to backup old DVR table for comparison.
     * @param dt
     */
    public DvrTable(DvrTable dt)
    {
        this.m_host_id = dt.m_host_id;
        this.m_table = new HashMap<String, Row>();
        this.m_alive_neighbors = new HashMap<String, Cost>();
        this.m_dead_nodes = new HashMap<String, Long>();
        this.m_all_neighbors = new HashMap<String, NeighborNode>();

        for (String nei_name : dt.m_alive_neighbors.keySet())
        {
            this.m_alive_neighbors.put(nei_name, dt.m_alive_neighbors.get(nei_name));
        }

        for (String nei_name : dt.m_all_neighbors.keySet())
        {
            this.m_all_neighbors.put(nei_name, dt.m_all_neighbors.get(nei_name));
        }
        
        for (String node_name : dt.m_dead_nodes.keySet())
        {
            this.m_dead_nodes.put(node_name, dt.m_dead_nodes.get(node_name));
        }

        for (String row_name : dt.m_table.keySet())
        {
            Row row = dt.m_table.get(row_name);
            Row backup_row = new Row(this.m_alive_neighbors);

            Collection<TableItem> items = row.allItems();
            for (TableItem item : items)
            {
                Cost cost = item.getCost();
                backup_row.addItem(new TableItem(item.getName(), cost));
            }

            this.m_table.put(row_name, backup_row);
        }
    }

    private String getTableContentFrom(byte[] input) throws Exception
    {
        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(input);

        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = br.readLine();
        if (!line.equals("Scn DVR"))
        {
            throw new Exception("Wrong Dvr table format!");
        }

        line = br.readLine();
        this.m_host_id = line.replaceFirst("ID: ", "");

        line = br.readLine();
        line = line.replaceFirst("Length: ", "");
        int length = Integer.parseInt(line);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            int ch = br.read();

            if (ch == -1)
            {
                throw new Exception("Content length mismatch in message!");
            }

            sb.append((char) ch);
        }

        is.close();

        return sb.toString();
    }

    /**
     * This constructor constructs a DVR table from a String (chat array) input.
     * It is used to build a DVR table via the UDP data stream received from the neighbor.
     * @param input
     * @throws Exception
     */
    public DvrTable(byte[] input) throws Exception
    {
        String table_str = getTableContentFrom(input);
        this.m_alive_neighbors = new HashMap<String, Cost>();
        this.m_dead_nodes = new HashMap<String, Long>();
        this.m_table = new HashMap<String, Row>();
        this.m_all_neighbors = new HashMap<String, NeighborNode>();

        // convert String into InputStream
        InputStream is = new ByteArrayInputStream(table_str.getBytes());

        // read it with BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = br.readLine();
        line = line.replaceFirst("\t", "");
        String[] nei_name_array = line.split("\t");

        for (int i = 0; i < nei_name_array.length; i++)
        {
            this.m_alive_neighbors.put(nei_name_array[i], new Cost());
            this.m_all_neighbors.put(nei_name_array[i], new NeighborNode(nei_name_array[i], new Cost(), new Cost(), 0));
        }

        while (null != (line = br.readLine()))
        {
            String[] cols = line.split("\t");
            String node_name = cols[0];

            Row row = new Row(this.m_alive_neighbors);
            for (int c = 0; c < nei_name_array.length; c++)
            {
                String column_name = nei_name_array[c];
                Cost cost = new Cost(Double.parseDouble(cols[c + 1]));
                row.addItem(new TableItem(column_name, cost));

                if (column_name.equals(node_name))
                {
                    this.m_alive_neighbors.put(column_name, cost);
                }
            }

            this.m_table.put(node_name, row);
            this.m_dead_nodes.put(node_name, new Long(0));
        }

        br.close();
    }

    public String getHostName()
    {
        return this.m_host_id;
    }

    public void addAliveNeighbor(NeighborNode nei)
    {
        this.m_alive_neighbors.put(nei.getName(), nei.getCost());

        for (Row row : this.m_table.values())
        {
            row.addItem(new TableItem(nei.getName(), new Cost()));
        }

        // Find a row which has the same name as the neighbor
        Row row = this.m_table.get(nei.getName());
        if (null == row)
        {
            // If the row does not exist in the table
            // put the row into table
            row = new Row(this.m_alive_neighbors);
            this.m_table.put(nei.getName(), row);
        }

        // All values of this row should be infinite except that
        // the row name equals to the column name
        for (String column_name : this.m_alive_neighbors.keySet())
        {
            if (column_name.equals(nei.getName()))
            {// Add cost of the neighbor to the row
                TableItem new_item = new TableItem(column_name, nei.getCost());
                row.addItem(new_item);
            }
        }
    }

    /**
     * 
     * @param node_name
     */
    public void removeNode(String node_name)
    {
        // If the node is neighbor
        if (this.m_alive_neighbors.containsKey(node_name))
        {
            Globals.debugPrintln("Remove neighbor " + node_name);
            this.m_alive_neighbors.remove(node_name);

            // Remove column of this node
            for (Row row : this.m_table.values())
            {
                row.removeItem(node_name);
            }
        }

        Globals.debugPrintln("Remove node " + node_name);
        // Remove the row of this node
        this.m_table.remove(node_name);
    }

    public boolean equals(Object obj)
    {
        if (null == obj || !(obj instanceof DvrTable))
        {
            return false;
        }

        DvrTable dt = (DvrTable) obj;
        if (dt.m_table.size() != this.m_table.size())
        {
            return false;
        }

        for (String row_name : this.m_table.keySet())
        {
            Row row = this.m_table.get(row_name);
            Row dt_row = dt.m_table.get(row_name);

            if (null == dt_row)
            {
                return false;
            }

            if (!row.equals(dt_row))
            {
                return false;
            }
        }

        return true;
    }

    public boolean hasInfiniteCost()
    {
        for (String row_name : this.m_table.keySet())
        {
            Row row = this.m_table.get(row_name);
            Collection<TableItem> items = row.allItems();
            for (TableItem item : items)
            {
                if (item.getCost().isInfinite())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * The DVR table is updated by DVR table of the neighbor.
     * <p>
     * If there are new rows in the neighbor's table, which means new nodes have
     * recently joined the network, new rows are added to the local DVR table.
     * <p>
     * If there are less rows in the neighbor's table, some nodes have recently
     * become offline. The corresponding rows of the local table should be
     * removed.
     * 
     * @param table
     */
    public void updateTable(DvrTable table)
    {
        String your_host_name = table.getHostName();

        // This is unlikely to happen
        if (!this.m_alive_neighbors.containsKey(your_host_name))
        {
            return;
        }

        for (String your_row_name : table.m_table.keySet())
        {
            // The DVR table from the neighbor node may include row with the
            // local host ID. We should ignore it
            if (your_row_name.equals(this.m_host_id))
            {
                continue;
            }

            Row row = this.m_table.get(your_row_name);
            Row your_row = table.m_table.get(your_row_name);

            // The row of the other table does not exist in my table
            if (null == row)
            {
                // If the row is not my dead neighbor, and it has been dead for quite a long time,
                // add it!
                if (!this.m_all_neighbors.containsKey(your_row_name))
                {
                    if (this.m_dead_nodes.containsKey(your_row_name))
                    {
                        Long death_time = this.m_dead_nodes.get(your_row_name);
                        long current_time = System.currentTimeMillis();
                        // This row (node) can be created only when it has been dead for more
                        // than 15 seconds
                        if (current_time - death_time > 15000)
                        {
                            row = new Row(this.m_alive_neighbors);
                            this.m_table.put(your_row_name, row);
                            this.m_dead_nodes.remove(your_row_name);
                        }
                    }
                    else // If the row does not exist in dead list, add it
                    {
                        row = new Row(this.m_alive_neighbors);
                        this.m_table.put(your_row_name, row);
                    }                   
                }
            }

            if (null != row)
            {
                Cost your_min_cost = your_row.getMinimumCost();
                Cost cost_to_you = this.m_alive_neighbors.get(your_host_name);

                row.addItem(new TableItem(your_host_name, new Cost(cost_to_you, your_min_cost)));
            }
        }

        ArrayList<String> rows_to_delete = new ArrayList<String>();
        for (String my_row_name : this.m_table.keySet())
        {
            // Can only remove indirect nodes here.
            // Neighbor should NOT be removed here.
            if (null == table.m_table.get(my_row_name) && !this.m_all_neighbors.containsKey(my_row_name))
            {
                long current_time = System.currentTimeMillis();
                Row my_row = this.m_table.get(my_row_name);

                // This row can be removed only when it has been alive for more
                // than 15 seconds
                if (current_time - my_row.getCreatedTime() > 15000)
                {
                    Globals.debugPrintln("Remove node " + my_row_name);
                    rows_to_delete.add(my_row_name);
                }
            }
        }

        for (String row_name : rows_to_delete)
        {
            long current_time = System.currentTimeMillis();
            this.m_table.remove(row_name);
            this.m_dead_nodes.put(row_name, current_time);
        }
    }

    public void printShortestPaths()
    {
        for (String row_name : this.m_table.keySet())
        {
            if (row_name.equals(this.m_host_id))
            {
                continue;
            }

            Row row = this.m_table.get(row_name);
            TableItem item_min_cost = row.getItemOfMinCost();
            String next_hop = item_min_cost.getName();
            Cost cost = item_min_cost.getCost();

            Globals.println("Shortest path to node " + row_name + ": the next hop is " + next_hop + " and the cost is "
                    + cost.getValue());
        }
    }

    public double getShortestPathToNode(String node_id)
    {
        Row row = this.m_table.get(node_id);
        if (null == row)
        {
            return -1;
        }

        return row.getMinimumCost().getValue();
    }

    public Collection<String> getAllNodes()
    {
        return this.m_table.keySet();
    }

    public void print()
    {
        Globals.println(this.toString());
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Scn DVR\r\n");
        sb.append("ID: " + this.m_host_id + "\r\n");
        sb.append("Length: ");

        StringBuilder table_sb = new StringBuilder();
        table_sb.append("\t");

        for (String nei_name : this.m_alive_neighbors.keySet())
        {
            table_sb.append(nei_name + "\t");
        }

        table_sb.append("\r\n");

        for (String row_name : this.m_table.keySet())
        {
            table_sb.append(row_name + "\t");
            Row row = this.m_table.get(row_name);
            Collection<String> col_names = row.allColumnNames();
            for (String col_name : col_names)
            {
                TableItem item = row.getItem(col_name);
                table_sb.append(item.getCost().getValue() + "\t");
            }

            table_sb.append("\r\n");
        }

        table_sb.deleteCharAt(table_sb.length() - 1);
        table_sb.deleteCharAt(table_sb.length() - 1);

        sb.append(table_sb.length() + "\r\n");
        sb.append(table_sb);

        return sb.toString();
    }

    /**
     * If cost to a neighbor changes, update changes to the entire column of
     * this neighbor
     */
    public void updateNeighborCostToTable()
    {
        ArrayList<String> cost_increase_nei = new ArrayList<String> ();
        for (String nei_id : this.m_all_neighbors.keySet())
        {
            if (null != this.m_alive_neighbors.get(nei_id))
            {
                NeighborNode nei_node = this.m_all_neighbors.get(nei_id);
                Cost old_nei_cost = this.m_alive_neighbors.get(nei_id);
                this.m_alive_neighbors.put(nei_id, nei_node.getCost());
                Cost change_of_cost = Cost.costChange(old_nei_cost, nei_node.getCost());
                if (change_of_cost.getValue() > 0)
                {
                    cost_increase_nei.add(nei_id);
                }

                for (String node_id : this.m_table.keySet())
                {
                    Row row = this.m_table.get(node_id);
                    Cost old_cost = row.getItem(nei_id).getCost();
                    row.addItem(new TableItem(nei_id, new Cost(old_cost, change_of_cost)));
                }
            }
        }
        
        for (String nei_id : cost_increase_nei)
        {
            this.setCostInfinity(nei_id);
        }  
    }
    
    private void setCostInfinity(String node_id)
    {
        Row row = this.m_table.get(node_id);
        if (null != row)
        {
            for (String nei_id : this.m_alive_neighbors.keySet())
            {
                if (nei_id.equals(node_id))
                {
                    continue;
                }
                row.addItem(new TableItem(nei_id, new Cost()));
            }
        }
    }
}
