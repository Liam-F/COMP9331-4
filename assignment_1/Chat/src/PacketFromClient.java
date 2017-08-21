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
import java.util.Date;

/**
 * All nested classes of ClientRequest
 * define application level packet structures
 * sent from the client to the server
 * 
 * @author dada
 *
 */
public class PacketFromClient
{

    
    public static ClientMsg generateClientRequest(BufferedReader in) throws Exception
    {
            String line_0 = in.readLine();
            
            if (line_0 == null)
            {
                throw new Exception("Socket stream terminated while receiving messages from the client.");
            }
            
            if (!line_0.equals(Globals.client_request_header))
            {
                throw new Exception("Invalid Message Header!");
            }
            
            String line_1 = in.readLine();
            
            if (line_1.equals("Login"))
            {
                return (new PacketFromClient()).new LoginRequest(in);
            }
            else if (line_1.equals("Primary"))
            {
                return (new PacketFromClient()).new PrimarySocketRequest(in);
            }
            else if (line_1.equals("Secondary"))
            {
                return (new PacketFromClient()).new SecondarySocketRequest(in);
            }
            else if (line_1.equals("Logout"))
            {
                return (new PacketFromClient()).new LogoutRequest(in);
            }
            else if (line_1.equals("Message"))
            {
                return (new PacketFromClient()).new MessageRequest(in);
            }
            else if (line_1.equals("Whoelse"))
            {
                return (new PacketFromClient()).new WhoelseRequest(in);
            }
            else if (line_1.equals("Whoelsesince"))
            {
                return (new PacketFromClient()).new WhoelsesinceRequest(in);
            }
            else if (line_1.equals("Broadcast"))
            {
                return (new PacketFromClient()).new BroadcastRequest(in);
            }
            else if (line_1.equals("Block"))
            {
                return (new PacketFromClient()).new BlockRequest(in);
            }
            else if (line_1.equals("StartP2P"))
            {
                return (new PacketFromClient()).new StartP2PRequest(in);
            }
            else if (line_1.equals("P2PPort"))
            {
                return (new PacketFromClient()).new P2PPort(in);
            }
            else if (line_1.equals("Default"))
            {
                return (new PacketFromClient()).new DefaultRequest(in);
            }
            else
            {
                throw new Exception("Message from client not supported!");
            }
    }
    
    public abstract class ClientMsg
    {   
        protected abstract void decode(BufferedReader in) throws Exception;      
        public abstract String toString();
    }


    /*
     * Primary Socket example
     * 
     * Client ScnChat 1.0
     * Primary
     * 
     */
    
    public class PrimarySocketRequest extends ClientMsg
    {   
        public PrimarySocketRequest()
        {
        }
        
        private PrimarySocketRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {          
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            sb.append("Primary\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /*
     * Secondary Socket example
     * 
     * Client ScnChat 1.0
     * Secondary
     * User: Mike
     * 
     */
    
    public class SecondarySocketRequest extends ClientMsg
    {
        protected String m_user;
        
        public SecondarySocketRequest(String user)
        {
            this.m_user = user;
        }
        
        private SecondarySocketRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {          
            String line_2 = in.readLine();

            if (line_2.startsWith("User: "))
            {
                this.m_user = line_2.replaceAll("^User: ", "");
            }
            else
            {
                throw new Exception("Invalid login response format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Secondary\r\n");
            
            sb.append("User: ");
            sb.append(this.m_user + "\r\n");                    
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    
    
    
    
    /* Login Request Example
     * 
     * Client ScnChat 1.0
     * Login
     * User: Mike
     * Password: 12345
     * 
     */
    
    public class LoginRequest extends ClientMsg
    {
        protected String m_user;
        protected String m_password;
        
        public LoginRequest(String user, String password)
        {
            this.m_user = user;
            this.m_password = password;
        }
        
        private LoginRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {          
            String line_2 = in.readLine();
            String line_3 = in.readLine();


            if (line_2.startsWith("User: ") && line_3.startsWith("Password: "))
            {
                this.m_user = line_2.replaceAll("^User: ", "");
                this.m_password = line_3.replaceAll("^Password: ", "");
            }
            else
            {
                throw new Exception("Invalid login request format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        public String getPassword()
        {
            return this.m_password;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Login\r\n");
            
            sb.append("User: ");
            sb.append(this.m_user + "\r\n");
            
            sb.append("Password: ");
            sb.append(this.m_password + "\r\n");
                       
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /* Logout Request Example
     * 
     * Client ScnChat 1.0
     * Logout
     * User: Mike
     * 
     */
    
    public class LogoutRequest extends ClientMsg
    {
        protected String m_user;
        
        public LogoutRequest(String user)
        {
            this.m_user = user;
        }
        
        private LogoutRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {             
            String line_2 = in.readLine();

            if (line_2.startsWith("User: "))
            {
                this.m_user = line_2.replaceAll("^User: ", "");
            }
            else
            {
                throw new Exception("Invalid logout request format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Logout\r\n");
            
            sb.append("User: ");
            sb.append(this.m_user + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /* Message Example
     * 
     * Client ScnChat 1.0
     * Message
     * Host: 192.120.0.125
     * Sender: Mike
     * Receiver: Lily
     * Sent Time: 20170325 15:00:23
     * Content Length: 3
     * Hi!
     * 
     */

    public class MessageRequest extends ClientMsg
    {
        protected String m_host;
        protected String m_sender;
        protected String m_receiver;
        protected long m_sent_time;
        protected String m_content;
        
        public MessageRequest(String host, String sender, String receiver, long sent_time,
                String content)
        {
            this.m_host = host;
            this.m_sender = sender;
            this.m_receiver = receiver;
            this.m_sent_time = sent_time;
            this.m_content = content;
        }
        
        private MessageRequest(BufferedReader in) throws Exception
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
            

            if (line_2.startsWith("Host: ")
                    && line_3.startsWith("Sender: ") && line_4.startsWith("Receiver: ")
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
                throw new Exception("Invalid client message request format!");
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
            sb.append(Globals.client_request_header + "\r\n");
            
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
    
    /* Default Request Example
     * 
     * Client ScnChat 1.0
     * Default
     * Content Length: 3
     * Hi!
     * 
     */

    public class DefaultRequest extends ClientMsg
    {
        protected String m_content;
        
        public DefaultRequest(String content)
        {
            this.m_content = content;
        }
        
        private DefaultRequest(BufferedReader in) throws Exception
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
                throw new Exception("Invalid client message request format!");
            }
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
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Default\r\n");
            
            sb.append("Content Length: ");
            sb.append(this.m_content.length() + "\r\n");
          
            sb.append(this.m_content);
            
            String str = sb.toString();
            Globals.debugPrintln(str);

            return str;
        }   
    }
    
    /*
     * Whoelse request example
     * 
     * Client ScnChat 1.0
     * Whoelse
     * 
     */
    
    public class WhoelseRequest extends ClientMsg
    {   
        public WhoelseRequest()
        {
        }
        
        private WhoelseRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {          
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            sb.append("Whoelse\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
        
    }
    
    /*
     * Whoelsesince request example
     * 
     * Client ScnChat 1.0
     * Whoelsesince
     * Time: 200
     * 
     */
    
    public class WhoelsesinceRequest extends ClientMsg
    {   
        private int m_time;
        public WhoelsesinceRequest(int time)
        {
            this.m_time = time;
        }
        
        private WhoelsesinceRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {
            String line_2 = in.readLine();
            
            if (line_2.startsWith("Time: "))
            {
                String str_time = line_2.replaceAll("^Time: ", "");
                this.m_time = Integer.parseInt(str_time);
            }
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            sb.append("Whoelsesince\r\n");

            sb.append("Time: ");
            sb.append(this.m_time + "\r\n");

            String str = sb.toString();
            Globals.debugPrintln(str);

            return str;
        }
        
        public int getTime()
        {
            return this.m_time;
        }
    }
    
    /* Broadcast Message Example
     * 
     * Client ScnChat 1.0
     * Broadcast
     * Host: 192.120.0.125
     * Sender: Mike
     * Sent Time: 20170325 15:00:23
     * Content Length: 3
     * Hi!
     * 
     */

    public class BroadcastRequest extends ClientMsg
    {
        protected String m_host;
        protected String m_sender;
        protected long m_sent_time;
        protected String m_content;
        
        public BroadcastRequest(String host, String sender, long sent_time, String content)
        {
            this.m_host = host;
            this.m_sender = sender;
            this.m_sent_time = sent_time;
            this.m_content = content;
        }
        
        private BroadcastRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
        
        
        protected void decode(BufferedReader in) throws Exception
        {   
            String line_2 = in.readLine();
            String line_3 = in.readLine();
            String line_4 = in.readLine();
            String line_5 = in.readLine();
            

            if (line_2.startsWith("Host: ")
                    && line_3.startsWith("Sender: ")
                    && line_4.startsWith("Sent Time: ") && line_5.startsWith("Content Length: "))
            {
                this.m_host = line_2.replaceAll("^Host: ", "");
                this.m_sender = line_3.replaceAll("^Sender: ", "");

                String str_sent_time = line_4.replaceAll("^Sent Time: ", "");
                Date the_date = Globals.date_format.parse(str_sent_time);
                this.m_sent_time = the_date.getTime();

                int content_length = Integer.parseInt(line_5.replaceAll("^Content Length: ", ""));

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
                throw new Exception("Invalid client message request format!");
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
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Broadcast\r\n");
            
            sb.append("Host: ");
            sb.append(this.m_host + "\r\n");
            
            sb.append("Sender: ");
            sb.append(this.m_sender + "\r\n");
            
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
    
    /* Block Request Example
     * 
     * Client ScnChat 1.0
     * Block
     * Type: True
     * User: Luke
     * 
     */
    
    public class BlockRequest extends ClientMsg
    {
        protected boolean m_type;
        protected String m_user;
        
        public BlockRequest(String user, boolean type)
        {
            this.m_user = user;
            this.m_type = type;
        }
        
        private BlockRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {             
            String line_2 = in.readLine();
            String line_3 = in.readLine();

            if (line_2.startsWith("Type: ") && line_3.startsWith("User: "))
            {
                String str_type = line_2.replaceAll("^Type: ", "");
                if (str_type.equals("True"))
                {
                    this.m_type = true;
                }
                else
                {
                    this.m_type = false;
                }
                
                this.m_user = line_3.replaceAll("^User: ", "");
            }
            else
            {
                throw new Exception("Invalid block request format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        public boolean getType()
        {
            return this.m_type;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");
            
            sb.append("Block\r\n");
            
            sb.append("Type: ");
            if (this.m_type)
            {
                sb.append("True\r\n");
            }
            else
            {
                sb.append("False\r\n");
            }
            
            sb.append("User: ");
            sb.append(this.m_user + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    
    /* Start Private Example
     * 
     * Client ScnChat 1.0
     * StartP2P
     * User: Luke
     * 
     */
    
    public class StartP2PRequest extends ClientMsg
    {
        protected String m_user;
        
        public StartP2PRequest(String user)
        {
            this.m_user = user;
        }
        
        private StartP2PRequest(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {             
            String line_2 = in.readLine();

            if (line_2.startsWith("User: "))
            {               
                this.m_user = line_2.replaceAll("^User: ", "");
            }
            else
            {
                throw new Exception("Invalid start P2P request format!");
            }
        }
        
        public String getUser()
        {
            return this.m_user;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");          
            sb.append("StartP2P\r\n");
            
            sb.append("User: ");
            sb.append(this.m_user + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }

    /* 
     * P2PPort will be sent to the server after
     * the client logs in successfully
     * 
     * Client ScnChat 1.0
     * P2PPort
     * Value: 9331
     * 
     */
    
    public class P2PPort extends ClientMsg
    {
        private int m_port;
        
        public P2PPort(int port)
        {
            this.m_port = port;
        }
        
        private P2PPort(BufferedReader in) throws Exception
        {
            this.decode(in);
        }
                
        protected void decode(BufferedReader in) throws Exception
        {             
            String line_2 = in.readLine();

            if (line_2.startsWith("Value: "))
            {
                String str_port = line_2.replaceAll("^Value: ", "");
                this.m_port = Integer.parseInt(str_port);
            }
            else
            {
                throw new Exception("Invalid P2P port format!");
            }
        }
        
        public int getPort()
        {
            return this.m_port;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Globals.client_request_header + "\r\n");          
            sb.append("P2PPort\r\n");
            
            sb.append("Value: ");
            sb.append(this.m_port + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
}
