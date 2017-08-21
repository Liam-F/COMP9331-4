import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*********************************
 * COMP9331 Assignment 2
 * Programmed by Chunnan Sheng
 * Student Code: z5100764
 ********************************/

/**
 * DvrBase class implements all basic emulative logics of Distance Vector Routing like
 * <p>
 * 1. Convergence of DVR tables
 * <p>
 * 2. Failures of nodes
 * 
 * @author Chunnan Sheng
 *
 */
public class DvrBase implements Runnable
{
    protected String m_host_id;
    protected int m_port;
    protected DvrTable m_old_table;
    protected DvrTable m_table;

    protected Map<String, NeighborNode> m_neighbors;

    protected boolean m_stop_sending_thread;
    
    ///////////////////////
    long last_time_table_change;
    boolean path_printed;
    long last_time_check_offline_neighor;

    /**
     * Initialize the instance of DvrBase
     * @param host_id
     * @param port
     * @param file_name
     * @throws IOException
     */
    public DvrBase(String host_id, int port, String file_name) throws IOException
    {
        this.m_host_id = host_id;
        this.m_port = port;
        this.m_neighbors = new HashMap<String, NeighborNode>();
        this.readConfig(file_name);
        
        this.m_old_table = null;
        this.m_table = new DvrTable(this.m_host_id, this.m_neighbors);

        this.m_stop_sending_thread = false;
        
        last_time_table_change = System.currentTimeMillis();
        path_printed = false;
        last_time_check_offline_neighor = 0;
    }

    protected boolean sendingThreadShouldStop()
    {
        synchronized (this)
        {
            return this.m_stop_sending_thread;
        }
    }

    /**
     * Update the DVR table.
     * <p>
     * The previous DVR table is copied to a backup instance before it is updated.
     * <p>
     * @param table_from_neighbor
     */
    protected void updateDvrTable(DvrTable table_from_neighbor)
    {
        synchronized (this)
        {
            this.m_old_table = new DvrTable(this.m_table);
            this.m_table.updateTable(table_from_neighbor);
        }
    }
    
    /**
     * Compare the updated DVR table with the previous one.
     * <p>
     * This method will return true if the updated table is identical to the previous one
     * @return
     */
    protected boolean dvrTableEqualsOldOne()
    {
        synchronized (this)
        {
            return this.m_table.equals(this.m_old_table);
        }
    }

    /**
     * Get string print of the DVR table
     * @return
     */
    protected String getStringModeOfDvrTable()
    {
        synchronized (this)
        {
            return this.m_table.toString();
        }
    }

    protected void printDvrTable()
    {
        synchronized (this)
        {
            this.m_table.print();
        }
    }

    /**
     * Print shortest paths indicated by the DVR table.
     */
    protected void printPaths()
    {
        synchronized (this)
        {
            this.m_table.printShortestPaths();
        }
    }

    /**
     * If a neighbor is detected alive (heart beat of this neighbor is received),
     * add it to the DVR table
     * @param neighbor
     */
    protected void addAliveNeighborToDvrTable(NeighborNode neighbor)
    {
        synchronized (this)
        {
            Globals.debugPrintln("addAliveNeighborToDvrTable: " + neighbor.getName());
            this.m_table.addAliveNeighbor(neighbor);
        }
    }
    
    protected void doSomethingWhenDvrTableConverges()
    {
        //TODO: The Poisoned Reverse or the extended version will do something here
    }

    /**
     * Read configuration file including ports and costs to all neighbors.
     * @param file_name
     * @throws IOException
     */
    protected void readConfig(String file_name) throws IOException
    {
        ArrayList<String[]> neighbors = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(file_name)))
        {
            String line = null;

            line = br.readLine();
            int nei_number = Integer.parseInt(line);
            Globals.debugPrintln(line);

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
            int port = Integer.parseInt(nei[2]);
            NeighborNode neighbor = new NeighborNode(nei[0], new Cost(cost), null, port);
            // this.m_table.addNeighbor(neighbor);
            this.m_neighbors.put(neighbor.getName(), neighbor);
        }
    }

    /**
     * Any node offline will no longer send heart beat signals to its neighbors.
     * <p>
     * If one neighbor has stayed quite for more than 5 seconds, this neighbor
     * will be considered as an offline node and will be removed from the DVR table.
     * <p>
     * If cost needed to a certain node exceeds 5000, it will be considered as a dead
     * node too. Sometimes the large cost is caused by "Count to Infinity" issues.
     * 
     * @param last_time_check_offline_nodes
     * @return
     */
    protected long checkOfflineNodes(long last_time_check_offline_nodes)
    {
        synchronized (this)
        {
            long current_time = System.currentTimeMillis();
            if (current_time - last_time_check_offline_nodes >= 1000)
            {
                // Check offline neighbors
                for (String nei_name : this.m_neighbors.keySet())
                {
                    NeighborNode node = this.m_neighbors.get(nei_name);
                    long silent_period = current_time - node.getLastMsgTime();

                    if (silent_period > 5000 && !node.isDead())
                    {
                        node.die();
                        Globals.println("--------------- " + node.getName() + " is thought to be dead. -----------");
                        Globals.debugPrintln("Silent time: " + silent_period);
                        this.m_table.removeNode(node.getName());
                        this.m_table.print();
                        last_time_table_change = current_time;
                        path_printed = false;
                    }
                }

                // Check offline nodes that are not neighbors
                Collection<String> node_names = this.m_table.getAllNodes();
                ArrayList<String> nn_list = new ArrayList<String>();

                // Copy all node names to an array list to avoid access
                // violation problem
                for (String node_name : node_names)
                {
                    nn_list.add(node_name);
                }

                for (String node_name : nn_list)
                {
                    double cost = this.m_table.getShortestPathToNode(node_name);
                    if (cost > 5000)
                    {
                        Globals.println("--------------- " + node_name + " is thought to be dead. -----------");
                        this.m_table.removeNode(node_name);
                        this.m_table.print();
                        last_time_table_change = current_time;
                        path_printed = false;
                    }
                }

                return current_time;
            }

            return last_time_check_offline_nodes;
        }
    }

    /**
     * This is the main loop of this program receiving heart beats and DVR tables from neighbor nodes.
     * 
     * @throws Exception
     */
    public void start() throws Exception
    {
        Globals.debugPrintln("--------------------- Original Table -------------------");
        // this.printDvrTable();
        // Create a datagram socket for receiving and sending UDP packets
        // through the port specified on the command line.
        DatagramSocket socket = new DatagramSocket(this.m_port);
        byte[] buf = new byte[1024];
        try
        {
            // Processing loop.
            while (true)
            {
                last_time_check_offline_neighor = checkOfflineNodes(last_time_check_offline_neighor);

                // Create a datagram packet to hold incomming UDP packet.
                DatagramPacket packet = new DatagramPacket(buf, 1024);

                // Block until the host receives a UDP packet.
                socket.receive(packet);

                byte[] data = packet.getData();
                String heart_beat = new String(data, 0, 10);
                if (heart_beat.startsWith("Alive:"))
                {
                    heart_beat = heart_beat.replaceFirst("Alive: ", "");
                    String nei_name = (heart_beat.split("\r\n"))[0];
                    // System.out.println("Hear beat from " + nei_name);
                    long time_a = System.currentTimeMillis();
                    NeighborNode node = this.m_neighbors.get(nei_name);
                    if (null != node)
                    {
                        node.setLastMsgTime(time_a);
                        if (node.isDead())
                        {
                            node.live();
                            this.addAliveNeighborToDvrTable(node);
                            Globals.println("--------------------- " + nei_name + " is now alive -------------------");
                            this.printDvrTable();
                            last_time_table_change = time_a;
                            path_printed = false;
                        }
                    }
                }
                else if (heart_beat.startsWith("Scn DVR"))
                {
                    DvrTable table_from_neighbor = new DvrTable(data);

                    long time_a = System.currentTimeMillis();
                    NeighborNode node = this.m_neighbors.get(table_from_neighbor.getHostName());
                    if (null != node)
                    {
                        node.setLastMsgTime(time_a);
                        if (node.isDead())
                        {
                            node.live();
                            this.addAliveNeighborToDvrTable(node);
                        }

                        this.updateDvrTable(table_from_neighbor);                        
                        Globals.debugPrintln("--------------------- Table updated -------------------");
                        
                        // If the table does not change
                        if (this.dvrTableEqualsOldOne())
                        {
                            long duration = time_a - last_time_table_change;
                            // If the table has stayed unchanged for 20 seconds
                            // Print shortest paths from this node to other nodes
                            if (duration > 20000 && !path_printed)
                            {
                                this.printPaths();
                                path_printed = true;
                                
                                this.doSomethingWhenDvrTableConverges();
                            }
                        }
                        // If the table changes
                        else
                        {
                            Globals.println("------------- Distance Vector Routing Table changes ------------");
                            this.printDvrTable();
                            last_time_table_change = time_a;
                            path_printed = false;
                        }                              
                    }
                }
            }
        }
        finally
        {
            socket.close();
        }
    }

    /**
     * This is another thread which sends heart beats and DVR table to the neighbors periodically.
     */
    @Override
    public void run()
    {
        // Create a client datagram socket
        DatagramSocket socket;
        try
        {
            socket = new DatagramSocket();
        }
        catch (SocketException e1)
        {
            e1.printStackTrace();
            return;
        }

        try
        {
            int index = 0;
            while (!this.sendingThreadShouldStop())
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                for (String nei_name : this.m_neighbors.keySet())
                {
                    NeighborNode neighbor = this.m_neighbors.get(nei_name);

                    InetAddress address = InetAddress.getByName("127.0.0.1");
                    int port = neighbor.getPort();

                    String heart_beat = "Alive: " + this.m_host_id + "\r\n";
                    byte[] heart_beat_data = heart_beat.getBytes();
                    DatagramPacket hb_request = new DatagramPacket(heart_beat_data, heart_beat_data.length, address,
                            port);
                    socket.send(hb_request);

                    if (0 == index)
                    {
                        String dvr_table_str = this.getStringModeOfDvrTable();
                        byte[] data_to_send = dvr_table_str.getBytes();

                        try
                        {
                            DatagramPacket dv_request = new DatagramPacket(data_to_send, data_to_send.length, address,
                                    port);
                            socket.send(dv_request);
                            // System.out.println(
                            //         "-------------Table Sent to neighbor: " + neighbor.getName() + "---------------");
                            // System.out.println(dvr_table_str);
                        }
                        catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }

                index = (index + 1) % 5;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            socket.close();
        }
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            Globals.println("Insufficient arguments!");
            Globals.println("Example 1: DvrPr A 3000 config.txt");
            return;
        }

        String host_id = args[0];
        int port = Integer.parseInt(args[1]);
        String file_name = args[2];

        DvrBase dvr = new DvrBase(host_id, port, file_name);
        // dvr.printDvrTable();
        // dvr.printPaths();

        (new Thread(dvr)).start();
        dvr.start();
    }
}
