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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * All nested classes of ServerRequest
 * define application level packet structures
 * sent from the server to the client
 * 
 * @author Chunnan Sheng
 *
 */
public class PacketFromServer
{
    
    public static ServerMsg generateServerRequest(BufferedReader in) throws Exception
    {
            String line_0 = in.readLine();
            
            if (line_0 == null)
            {
                throw new Exception("Socket stream terminated while receiving messages from the server.");
            }
            
            if (!line_0.equals(Globals.server_request_header))
            {
                throw new Exception("Invalid Message Header!");
            }
            
            String line_1 = in.readLine();
            
            if (line_1.equals("Login"))
            {
                return (new PacketFromServer()).new LoginResponse(in);
            }
            else if (line_1.equals("Message"))
            {
                return (new PacketFromServer()).new ClientPeerResponse(in);
            }
            else if (line_1.equals("Acknowledge"))
            {
                return (new PacketFromServer()).new AckResponse(in);
            }
            else if (line_1.equals("Presence"))
            {
                return (new PacketFromServer()).new PresenceNotification(in);
            }
            else if (line_1.equals("Users"))
            {
                return (new PacketFromServer()).new UserListResponse(in);
            }
            else if (line_1.equals("Private"))
            {
                return (new PacketFromServer()).new PrivateResponse(in);
            }
            else
            {
                throw new Exception("Message from server not supported!");
            }
    }
    
    public abstract class ServerMsg
    {   
        protected abstract void decode(BufferedReader in) throws Exception;       
        public abstract String toString();    
    }
    
    
    /* Login Granted Example
     * 
     * Server ScnChat 1.0
     * Login
     * Status: Successful
     * 
     */
    
    /* Login failure Example
     * 
     * Server ScnChat 1.0
     * Login
     * Status: Failed
     * 
     */
    
    /* Login Blocked Example
     * 
     * Server ScnChat 1.0
     * Login
     * Status: Blocked
     * 
     */
    
    public class LoginResponse extends ServerMsg
    {
        protected LoginStatus m_status;        
        
        public LoginResponse(LoginStatus status)
        {
            this.m_status = status;
        }
        
        private LoginResponse(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();
            

            if (line_2.startsWith("Status: "))
            {
                String status = line_2.replaceAll("^Status: ", "");
                
                switch (status)
                {
                case "Successful":
                    this.m_status = LoginStatus.Successful;
                    break;
                case "Failed":
                    this.m_status = LoginStatus.Failed;
                    break;
                case "Blocked":
                    this.m_status = LoginStatus.Blocked;
                    break;
                case "StillBlocked":
                    this.m_status = LoginStatus.StillBlocked;
                    break;
                default:
                    throw new Exception("Undefined login status!");
                }
            }
            else
            {
                throw new Exception("Invalid login response format!");
            }
        }
        
        public void setStatus(LoginStatus status)
        {
            this.m_status = status;
        }
        
        public LoginStatus getStatus()
        {
            return this.m_status;
        }

        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            sb.append("Login\r\n");
            sb.append("Status: ");
            sb.append(this.m_status + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /* Acknowledge Response Example
     * 
     * Server ScnChat 1.0
     * Acknowledge
     * Content Length: 6
     * Hello!
     * 
     */
    
    public class AckResponse extends ServerMsg
    {
        protected String m_content;
        
        public AckResponse(String content)
        {
            this.m_content = content;
        }
        
        public AckResponse()
        {
            this.m_content = "";
        }
        
        private AckResponse(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();

            if (line_2.startsWith("Content Length: "))
            {

                int content_length = Integer.parseInt(line_2.replaceAll("^Content Length: ", ""));

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < content_length; i++)
                {
                    int ch = in.read();

                    if (ch == -1)
                    {
                        throw new Exception("Content length mismatch in message!");
                    }

                    sb.append((char) ch);
                }

                this.m_content = sb.toString();
            }
            else
            {
                throw new Exception("Invalid error message format!");
            }
        }
        
        public String getContent()
        {
            return this.m_content;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            
            sb.append("Acknowledge\r\n");
            
            sb.append("Content Length: ");
            sb.append(this.m_content.length() + "\r\n");
            
            sb.append(this.m_content);
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }        
    }
    
    /*
     * Presence Notification
     * 
     * Server ScnChat 1.0
     * Presence
     * Type: Login
     * User: Yoda
     * Time: 20170325 15:00:23
     * 
     */
    
    public class PresenceNotification extends ServerMsg
    {   
        private boolean m_login;
        private String m_user;
        long    m_time;
        
        public PresenceNotification(String user, boolean login, long time)
        {
            this.m_user = user;
            this.m_login = login;
            this.m_time = time;
        }
        
        private PresenceNotification(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();
            String line_3 = in.readLine();
            String line_4 = in.readLine();
            
            if (line_2.startsWith("Type: ") && line_3.startsWith("User: ") && line_4.startsWith("Time: "))
            {
                String login_type = line_2.replaceAll("^Type: ", "");
                
                if (login_type.equals("Login"))
                {
                    this.m_login = true;
                }
                else
                {
                    this.m_login = false;
                }
                
                this.m_user = line_3.replaceAll("^User: ", "");
                Date the_date = Globals.date_format.parse(line_4.replaceAll("^Time: ", ""));
                this.m_time = the_date.getTime();
            }
            else
            {
                throw new Exception("Invalid user presence notification format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        public boolean getLoginType()
        {
            return this.m_login;
        }
        
        public long getTime()
        {
            return this.m_time;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            sb.append("Presence\r\n");
            
            if (this.m_login)
            {
                sb.append("Type: Login\r\n");
            }
            else
            {
                sb.append("Type: Logout\r\n");
            }
            
            sb.append("User: " + this.m_user + "\r\n");
            
            sb.append("Time: ");            
            Date time = new Date(this.m_time);
            
            sb.append(Globals.date_format.format(time) + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /* Message Example
     * 
     * Server ScnChat 1.0
     * Message
     * Host: 192.120.0.123
     * Sender: Lily
     * Receiver: Mike
     * Sent Time: 20170325 15:00:23
     * Content Length: 6
     * Hello!
     * 
     */
    public class ClientPeerResponse extends ServerMsg
    {
        protected String m_host;
        protected String m_sender;
        protected String m_receiver;
        protected long m_sent_time;
        protected String m_content;
        
        public ClientPeerResponse(String host, String sender, String receiver, long sent_time,
                String content)
        {
            this.m_host = host;
            this.m_sender = sender;
            this.m_receiver = receiver;
            this.m_sent_time = sent_time;
            this.m_content = content;
        }
        
        private ClientPeerResponse(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();
            String line_3 = in.readLine();
            String line_4 = in.readLine();
            String line_5 = in.readLine();
            String line_6 = in.readLine();

            if (line_2.startsWith("Host: ") && line_3.startsWith("Sender: ") && line_4.startsWith("Receiver: ")
                    && line_5.startsWith("Sent Time: ") && line_6.startsWith("Content Length: "))
            {
                this.m_host = line_2.replaceAll("^Host: ", "");
                this.m_sender = line_3.replaceAll("^Sender: ", "");
                this.m_receiver = line_4.replaceAll("^Receiver: ", "");

                String str_sent_time = line_5.replaceAll("^Sent Time: ", "");
                Date the_date = Globals.date_format.parse(str_sent_time);
                this.m_sent_time = the_date.getTime();

                int content_length = Integer.parseInt(line_6.replaceAll("^Content Length: ", ""));

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < content_length; i++)
                {
                    int ch = in.read();

                    if (ch == -1)
                    {
                        throw new Exception("Content length mismatch in message!");
                    }

                    sb.append((char) ch);
                }

                this.m_content = sb.toString();
            }
            else
            {
                throw new Exception("Invalid server message request format!");
            }
        }
        
        public String getHost()
        {
            return this.m_host;
        }
        
        public String getSender()
        {
            return this.m_sender;
        }
        
        public String getReceiver()
        {
            return this.m_receiver;
        }
        
        public long getSentTime()
        {
            return this.m_sent_time;
        }
        
        public int getContentLength()
        {
            return this.m_content.length();
        }
        
        public String getContent()
        {
            return this.m_content;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            
            sb.append("Message\r\n");
            
            sb.append("Host: ");
            sb.append(this.m_host + "\r\n");
            
            sb.append("Sender: ");
            sb.append(this.m_sender + "\r\n");
            
            sb.append("Receiver: ");
            sb.append(this.m_receiver + "\r\n");
            
            sb.append("Sent Time: ");            
            Date sent_time = new Date(this.m_sent_time);
            
            sb.append(Globals.date_format.format(sent_time) + "\r\n");
            
            sb.append("Content Length: ");
            sb.append(this.m_content.length() + "\r\n");
            
            sb.append(this.m_content);
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        } 
    }
    
    /* User List Example
     * 
     * Server ScnChat 1.0
     * Users
     * List Size: 4
     * chunnan
     * yoda
     * luke
     * vader
     * 
     */
    public class UserListResponse extends ServerMsg
    {
        private Collection <String> m_users; 
        
        public UserListResponse(Collection <String> users)
        {
            this.m_users = users;
        }
        
        private UserListResponse(BufferedReader in) throws Exception
        {
            this.m_users = new ArrayList <String> ();
            this.decode(in);
        }        
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();

            if (line_2.startsWith("List Size: "))
            {
                String str_size = line_2.replaceAll("^List Size: ", "");
                int size = Integer.parseInt(str_size);
                for (int i = 0; i < size; i++)
                {
                    this.m_users.add(in.readLine());
                }
            }
            else
            {
                throw new Exception("Invalid user list format!");
            }
        }
        
        public Collection <String> getAllUsers()
        {
            return this.m_users;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            
            sb.append("Users\r\n");
            
            sb.append("List Size: ");
            sb.append(this.m_users.size() + "\r\n");
            
            for (String user : this.m_users)
            {
                sb.append(user + "\r\n");
            }
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }       
    }
    
    /*
     * Private connection info response
     * 
     * Server ScnChat 1.0
     * Private
     * User: Yoda
     * IP: 12.34.56.78
     * P2PPort: 9331
     * 
     */
    
    public class PrivateResponse extends ServerMsg
    {   
        private String m_user;
        private String m_ip;
        private int m_p2p_port;
        
        public PrivateResponse(String user, String ip, int p2p_port)
        {
            this.m_user = user;
            this.m_ip = ip;
            this.m_p2p_port = p2p_port;
        }
        
        private PrivateResponse(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();
            String line_3 = in.readLine();
            String line_4 = in.readLine();
            
            if (line_2.startsWith("User: ") && line_3.startsWith("IP: "))
            {
                this.m_user = line_2.replaceAll("^User: ", "");
                this.m_ip = line_3.replaceAll("^IP: ", "");
                this.m_p2p_port = Integer.parseInt(line_4.replaceAll("^P2PPort: ", ""));
            }
            else
            {
                throw new Exception("Invalid user presence notification format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        public String getIPAddress()
        {
            return this.m_ip;
        }
        
        public int getP2PPort()
        {
            return this.m_p2p_port;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.server_request_header + "\r\n");
            sb.append("Private\r\n");
            
            sb.append("User: " + this.m_user + "\r\n");
            
            sb.append("IP: " + this.m_ip + "\r\n");
            
            sb.append("P2PPort: " + this.m_p2p_port + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
}
