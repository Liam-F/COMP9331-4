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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The client entity possesses a hash table of all P2P sessions,
 * handle login procedure and other client commands
 * @author Chunnan Sheng
 *
 */
public class ClientEntity
{
    private static Map <String, ClientP2PSession> all_p2p_sessions = new HashMap <String, ClientP2PSession>();
    
    public static ClientP2PSession getP2PSession(String user)
    {
        synchronized (all_p2p_sessions)
        {
            return all_p2p_sessions.get(user);
        }
    }
    
    public static void addP2PSession(String user, ClientP2PSession session)
    {
        synchronized (all_p2p_sessions)
        {
            all_p2p_sessions.put(user, session);
        }
    }
    
    public static void removeP2PSession(String user)
    {
        synchronized (all_p2p_sessions)
        {
            all_p2p_sessions.remove(user);
        }
    }
    
    public static Collection <String> findP2PWhoelse(String myself)
    {
        synchronized (all_p2p_sessions)
        {
            ArrayList <String> user_names = new ArrayList <> ();
            for (String name : all_p2p_sessions.keySet())
            {
                if (name.equals(myself))
                {
                    continue;
                }
                user_names.add(name);
            }
            
            return user_names;
        }
    }
    
    public static void closeAllP2PSessions()
    {
        synchronized (all_p2p_sessions)
        {
            List <ClientP2PSession> s_list = new ArrayList <ClientP2PSession>();
            // Get all sessions from hash map
            for (ClientP2PSession session : all_p2p_sessions.values())
            {
                s_list.add(session);
            }
            // And then kill them all
            for (ClientP2PSession s_item : s_list)
            {
                s_item.stop();
            }
        }
    }
    
    // A client process can only allow one client instance to run
    private static ClientEntity client_instance = null;

    /**
     * The client entity can only be created once
     * @param server_ip
     * @param server_port
     * @return
     */
    public static ClientEntity createClientInstance(String server_ip, int server_port)
    {
        if (null == client_instance)
        {
            client_instance = new ClientEntity(server_ip, server_port);
        }

        return client_instance;
    }

    public static ClientEntity getClientInstance()
    {
        return client_instance;
    }

    // IP address of server
    private String m_server_ip;
    // Port of server
    private int m_server_port;
    // client socket connecting to the server
    private Socket m_socket;
    // Net input from server
    private BufferedReader m_net_in;
    // Net output to server
    private PrintWriter m_net_out;
    // Std in from keyboard input
    private BufferedReader m_std_in;
    // The login user of this client
    private String m_user;
    // This session receives message from other clients via the server
    private ClientPeerReplySession m_peer_reply_session;
    // The listening socket for incoming P2P connections
    private ClientP2PListener m_p2p_listener;
    // Main thread of this client
    private Thread m_current_thread;

    public String getUser()
    {
        synchronized (this)
        {
            return this.m_user;
        }
    }

    /**
     * The login procedure
     * @return
     * @throws Exception
     */
    private LoginStatus login() throws Exception
    {
        System.out.print(Globals.login_user_prompt);
        this.m_user = Globals.readLine(this.m_std_in);
        System.out.print(Globals.login_pass_prompt);
        String password = Globals.readLine(this.m_std_in);

        PacketFromClient.LoginRequest request = (new PacketFromClient()).new LoginRequest(this.m_user, password);
        this.m_net_out.print(request.toString());
        this.m_net_out.flush();

        PacketFromServer.ServerMsg sm = PacketFromServer.generateServerRequest(this.m_net_in);

        if (!(sm instanceof PacketFromServer.LoginResponse))
        {
            throw new Exception("Unrecognized response from server.");
        }

        PacketFromServer.LoginResponse login_res = (PacketFromServer.LoginResponse) sm;

        LoginStatus status = login_res.getStatus();

        switch (status)
        {
        case Successful:
            Globals.println(Globals.login_welcome_msg);
            break;
        case Failed:
            Globals.println(Globals.login_failure);
            break;
        case Blocked:
            Globals.println(Globals.login_block);
            break;
        case StillBlocked:
            Globals.println(Globals.login_account_blocked);
            break;
        }

        return status;
    }

    /**
     * Constructor
     * Server IP and port as mandatory arguments
     * @param server_ip
     * @param server_port
     */
    private ClientEntity(String server_ip, int server_port)
    {
        this.m_server_ip = server_ip;
        this.m_server_port = server_port;
        this.m_std_in = null;
        this.m_net_in = null;
        this.m_net_out = null;
        this.m_socket = null;
        this.m_peer_reply_session = null;
        this.m_p2p_listener = null;
    }
    
    public String getServerIP()
    {
        synchronized (this)
        {
            return this.m_server_ip;
        }
    }

    public int getServerPort()
    {
        synchronized (this)
        {
            return this.m_server_port;
        }
    }
    
    public Thread getMainThread()
    {
        synchronized (this)
        {
            return this.m_current_thread;
        }
    }

    /**
     * The start procedure can only be executed by the main thread
     * What should we do in this procedure:
     * 1. Connect to the server (Command sending and server response receiving)
     * 2. login
     * 3. Connect to the server (Message receiving socket)
     * 4. Start P2P listening and send P2P port to the server
     * 5. Start terminal for command input
     */
    public void start()
    {
        LoginStatus status = LoginStatus.Failed;
        this.m_current_thread = Thread.currentThread();

        try
        {
            do
            {
                if (null != this.m_socket)
                {
                    this.m_socket.close();
                }

                this.m_socket = new Socket(this.m_server_ip, this.m_server_port);

                this.m_net_out = new PrintWriter(m_socket.getOutputStream(), true);
                this.m_net_in = new BufferedReader(new InputStreamReader(this.m_socket.getInputStream()));
                this.m_std_in = new BufferedReader(new InputStreamReader(System.in));
                
                
                PacketFromClient.PrimarySocketRequest p_req = (new PacketFromClient()).new PrimarySocketRequest();
                this.m_net_out.print(p_req.toString());
                this.m_net_out.flush();
                
                //this.setSessionStatus(SessionStatus.Connected);

                status = this.login();
            }
            while (LoginStatus.Failed == status);
            
            
            if (status == LoginStatus.Successful)
            {   
                //this.setSessionStatus(SessionStatus.Login);
                
                // Start peer reply session here
                Socket secondary = new Socket(this.m_server_ip, this.m_server_port);
                this.m_peer_reply_session = new ClientPeerReplySession(secondary, this);
                (new Thread(m_peer_reply_session)).start();
                
                // Start P2P listening here
                this.m_p2p_listener = new ClientP2PListener();
                (new Thread(m_p2p_listener)).start();
                
                // Tell the server the P2P port
                PacketFromClient.P2PPort p2p_port = (new PacketFromClient()).new P2PPort(this.m_p2p_listener.getPort());
                this.m_net_out.print(p2p_port.toString());
                this.m_net_out.flush();          
                
                // 
                new ClientConsole(this.m_user, this.m_net_out, this.m_net_in, this.m_std_in).consoleInputLoop();
            }
        }
        catch (Exception ex)
        {
            Globals.debugPrintStackTrace(ex);
            Globals.println("Timeout or other reasons cause the program to exit.");
        }
        finally
        {
            //this.setSessionStatus(SessionStatus.Disconnected);       
            
            // TODO: Kill the listener and all P2P sessions
            ClientEntity.closeAllP2PSessions();
            if (null != this.m_p2p_listener)
            {
                this.m_p2p_listener.stop();
            }
            
            if (null != this.m_socket)
            {
                try
                {
                    this.m_socket.close();
                }
                catch (IOException e)
                {
                    Globals.debugPrintStackTrace(e);
                }
            }
        }

    }
    
    /**
     * This procedure tries to establish p2p connection to a remote user
     * Both sending socket and receiving socket are created here
     * @param remote_user
     * @param remote_ip
     * @throws Exception
     */
    public void establishP2P(String remote_user, String remote_ip, int remote_port) throws Exception
    {
        Socket s_socket = null;
        Socket r_socket = null;
        
        try
        {
            s_socket = new Socket(remote_ip, remote_port);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            Globals.debugPrintStackTrace(e);
            Globals.println("Connection to " + remote_user + " failed");
            return;
        }
        
        try
        {
            r_socket = new Socket(remote_ip, remote_port);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            Globals.debugPrintStackTrace(e);
            Globals.println("Connection to " + remote_user + " failed");
            s_socket.close();
            
            return;
        }
        
        PrintWriter s_net_out = new PrintWriter(s_socket.getOutputStream(), true);
        
        PacketFromP2P.SendingSocketRequest ssr = (new PacketFromP2P()).new SendingSocketRequest(this.m_user);
        s_net_out.print(ssr.toString());
        s_net_out.flush();
        
        ClientP2PSendingSession s_session = new ClientP2PSendingSession(s_socket, s_net_out, remote_user);            
        
        PrintWriter r_net_out = new PrintWriter(r_socket.getOutputStream(), true);
        BufferedReader r_net_in = new BufferedReader(new InputStreamReader(r_socket.getInputStream()));
        
        PacketFromP2P.ReceivingSocketRequest rsr = (new PacketFromP2P()).new ReceivingSocketRequest(this.m_user);
        r_net_out.print(rsr.toString());
        r_net_out.flush();
        
        ClientP2PReceivingSession r_session = new ClientP2PReceivingSession(r_socket, r_net_in);
        
        ClientP2PSession session = new ClientP2PSession(remote_user);
        session.setSendingSession(s_session);
        session.setReceivingSession(r_session);
        
        ClientEntity.addP2PSession(remote_user, session);
        (new Thread(s_session)).start();
        (new Thread(r_session)).start();
        
        Globals.println("Start private messaging with " + remote_user);
    }
       
}