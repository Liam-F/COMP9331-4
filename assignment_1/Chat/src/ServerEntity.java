/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerEntity
{
    public class AllData
    {   
        // All server connection sessions are stored here with user name as keys
        public Map<String, ServerConnectionSession> all_sessions;
        // All credentials and offline messages are stored in this map
        public Map<String, ServerUserProfile> all_credentials;
        
        public AllData()
        {
            this.all_sessions = new HashMap <String, ServerConnectionSession>();
            this.all_credentials = new HashMap <String, ServerUserProfile>();
        }
    }
    
    /**
     * Static members
     */

    // The server program has only a singleton server instance
    private static ServerEntity server_instance = null;
    
    // All data should deal with concurrency of multiple threads
    private static AllData all_data = null;

    public static ServerEntity getServerInstance()
    {
         return server_instance;
    }

    public static ServerEntity createServerInstance(int port, long block_duration, long timeout) throws IOException
    {   
        if (null == server_instance)
        {
            server_instance = new ServerEntity(port, block_duration, timeout);
            all_data = server_instance.new AllData();
        }

        return server_instance;
    }

    public static void insertConnectionSession(String user, ServerConnectionSession session)
    {
        synchronized (all_data)
        {
            all_data.all_sessions.put(user, session);
        }
    }

    public static ServerConnectionSession getConnectionSession(String user)
    {
        synchronized (all_data)
        {
            return all_data.all_sessions.get(user);
        }
    }

    public static ServerConnectionSession removeConnectionSession(String user)
    {
        synchronized (all_data)
        {
            return all_data.all_sessions.remove(user);
        }
    }
    
    public static boolean inCredential(String user)
    {
        synchronized (all_data)
        {
            return all_data.all_credentials.containsKey(user);
        }
    }
    
    public static String getCredentialPassword(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if  (null == profile)
            {
                return null;
            }
            
            return profile.getPassword();
        }
    }
    
    public static void addLoginFailureToCredential(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.plusLoginFailure();
            }
        }
    }
    
    public static void addLoginToCredential(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.plusLogin();
            }
        }
    }
    
    public static void addLogoutToCredential(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.plusLogout();
            }
        }
    }
    
    public static void addIPAddressToCredential(String user, String ip)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.setIPAddress(ip);
            }
        }
    }
    
    public static void addP2PPortToCredential(String user, int port)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.setP2PPort(port);;
            }
        }
    }
    
    public static String getCredentialIPAddress(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if  (null == profile)
            {
                return null;
            }
            
            return profile.getIPAddress();
        }
    }
    
    public static int getCredentialP2PPport(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if  (null == profile)
            {
                return -1;
            }
            
            return profile.getP2PPort();
        }
    }
    
    public static boolean isCredentialBlocked(String user)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null == profile)
            {
                return false;
            }
            
            return profile.isBlocked();
        }
    }
    
    public static void insertCredantial(String user, String password)
    {
        synchronized (all_data)
        {
            all_data.all_credentials.put(user, new ServerUserProfile(user, password));
        }
    }
    
    public static Collection <String> getAllUsers()
    {
        synchronized (all_data)
        {
            Collection<String> users = new ArrayList<String>();

            for (String user : all_data.all_credentials.keySet())
            {
                users.add(user);
            }
            
            return users;
        }
    }
    
    public static Collection <String> getAllOnlineUsers()
    {
        synchronized (all_data)
        {
            Collection<String> users = new ArrayList<String>();

            for (String user : all_data.all_sessions.keySet())
            {
                users.add(user);
            }
            
            return users;
        }
    }
    
    public static boolean inBlacklist(String user, String user_in)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null == profile)
            {
                return false;
            }
            
            return profile.inBlackList(user_in);
        }
    }
    
    public static void intoBlacklist(String user, String user_in)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.intoBlacklist(user_in);
            }
        }
    }
    
    public static void outBlacklist(String user, String user_in)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(user);
            
            if (null != profile)
            {
                profile.outBlacklist(user_in);
            }
        }
    }
    
    public static Collection <String> findWhoelse(String myself)
    {
        synchronized (all_data)
        {
            Collection<String> users = new ArrayList<String>();

            for (String user : all_data.all_sessions.keySet())
            {
                if (!user.equals(myself))
                {
                    users.add(user);
                }
            }
            
            return users;
        }
    }
    
    public static Collection <String> findWhoelseSince(String myself, int time)
    {  
        synchronized (all_data)
        {   
            Collection<String> users = new ArrayList<String>();
            // If this peer is offline but used to log out at the time between
            // start_time and now,
            // he/she is included as well
            long since = System.currentTimeMillis() - (long)time * 1000;
            for (ServerUserProfile profile : all_data.all_credentials.values())
            {
                if (!profile.getUser().equals(myself) && !all_data.all_sessions.containsKey(profile.getUser())
                        && since <= profile.getLogoutTime())
                {
                    users.add(profile.getUser());
                }
            }

            // If this peer is online, he/she is included in whoelsesince list
            for (String user : all_data.all_sessions.keySet())
            {
                if (!user.equals(myself))
                {
                    users.add(user);
                }
            }
            
            return users;
        }
    }
    
    public static void enqueueOfflinePeerMessage(String sender, String receiver, String content, String host, long sent_time)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(receiver);
            
            if (null != profile)
            {
                profile.enqueuePeerMessage(sender, content, host, sent_time);
            }
        }
    }
    
    public static InterSessionMessage dequeueOfflinePeerMessage(String receiver)
    {
        synchronized (all_data)
        {
            ServerUserProfile profile = all_data.all_credentials.get(receiver);
            
            if (null == profile)
            {
                return null;
            }
            
            return profile.dequeuePeerMessage();
        }
    }
    
    /**
     * Non-static members
     */

    ServerSocket m_listener;
    long m_block_duration;
    long m_timeout;

    /**
     * Constructor of the main server class with
     * port, block duration and timeout as arguments
     * 
     * @param port
     * @throws IOException
     */
    private ServerEntity(int port, long block_duration, long timeout) throws IOException
    {
        m_listener = new ServerSocket(port);
        this.m_block_duration = block_duration;
        this.m_timeout = timeout;
    }
    
    /**
     * Read user names and passwords from credential file
     * @throws Exception
     */
    private void readCredentials() throws Exception
    {
        try (BufferedReader br = new BufferedReader(new FileReader("credentials.txt")))
        {
            String line = null;

            while ((line = br.readLine()) != null)
            {
                String l_line = line.replaceAll("^[ \t]+", "");
                Globals.debugPrintln(l_line);
                String[] name_pass = l_line.split("[ \t]+");
                
                if (name_pass.length >= 2 && name_pass[0].length() > 0)
                {
                    insertCredantial(name_pass[0], name_pass[1]);
                }
                else if (name_pass.length == 1 && name_pass[0].length() > 0)
                {
                    insertCredantial(name_pass[0], "");
                }
            }
        }
    }


    public long getBlockDuration()
    {
        return this.m_block_duration;
    }

    public long getTimeout()
    {
        return this.m_timeout;
    }

    /**
     * Start listening and establish TCP connections for incoming connection requests.
     * @throws Exception 
     */
    public void start() throws Exception
    {
        if (null == m_listener)
        {
            return;
        }
        
        readCredentials();
        
        for (String key : all_data.all_credentials.keySet())
        {
            Globals.debugPrintln("User: " + key + "  Pass: " + all_data.all_credentials.get(key).getPassword());
        }

        // Main loop of this server
        while (true)
        {
            try
            {
                // Establish a new connection
                // This call will be blocked until a new connection request
                // comes.
                Socket new_socket = m_listener.accept();

                PrintWriter net_out = new PrintWriter(new_socket.getOutputStream(), true);
                BufferedReader net_in = new BufferedReader(new InputStreamReader(new_socket.getInputStream()));
                
                // Try to receive first connection request data from the client
                // Two sockets will be created for this client.
                // One socket is used to communicate between the client and server.
                // The other socket is used to push peer message from another client to this client.
                // The first request from this socket helps distinguish whether this socket is used for server-client
                // communication or push of peer message.
                PacketFromClient.ClientMsg cm = PacketFromClient.generateClientRequest(net_in);
                
                // The socket for server-client communication is called primary socket
                if (cm instanceof PacketFromClient.PrimarySocketRequest)
                {
                    Globals.println("New connection from: " + new_socket.getInetAddress());
                    new_socket.setSoTimeout((int) this.m_timeout);
                    ServerConnectionSession new_session = new ServerConnectionSession(new_socket, net_in, net_out);
                    (new Thread(new_session)).start();
                }
                // The socket for push of peer message is called secondary socket
                else if (cm instanceof PacketFromClient.SecondarySocketRequest)
                {
                    PacketFromClient.SecondarySocketRequest ssr = (PacketFromClient.SecondarySocketRequest)cm;
                    String user_name = ssr.getUser();                   
                    ServerConnectionSession session = ServerEntity.getConnectionSession(user_name);
                    
                    if (null == session)
                    {
                        Globals.println("Session of " + user_name + " does not exist. \r\nSecondary connection cannot be established.");
                        continue;
                    }
                    
                    session.startPeerReplySession(new_socket);
                }
                else
                {
                    new_socket.close();
                    Globals.println("Connection type not supported. \r\nOnly Primary or Secondary types is allowed.");
                    continue;
                }
            }
            catch (Exception ex)
            {
                Globals.println("Failure of new connection.");
                Globals.debugPrintStackTrace(ex);;
            }
        }
    }
}
