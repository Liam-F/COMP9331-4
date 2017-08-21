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

public class PacketFromP2P
{
    public static ClientMsg generateClientP2PRequest(BufferedReader in) throws Exception
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
            
            if (line_1.equals("P2PMessage"))
            {
                return (new PacketFromP2P()).new MessageRequest(in);
            }
            else if (line_1.equals("P2PSend"))
            {
                return (new PacketFromP2P()).new SendingSocketRequest(in);
            }
            else if (line_1.equals("P2PReceive"))
            {
                return (new PacketFromP2P()).new ReceivingSocketRequest(in);
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
    
    /* Private Message Example
     * 
     * Client ScnChat 1.0
     * P2PMessage
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
            
            sb.append("P2PMessage\r\n");
            
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
    
    /*
     * P2P Sending Socket example
     * 
     * Client ScnChat 1.0
     * P2PSend
     * User: chunnan
     * 
     */
    
    public class SendingSocketRequest extends ClientMsg
    {   
        private String m_user;
        public SendingSocketRequest(String user)
        {
            this.m_user = user;
        }
        
        private SendingSocketRequest(BufferedReader in) throws Exception
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
                throw new Exception("Invalid P2P request format!");
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
            sb.append("P2PSend\r\n");
            
            sb.append("User: " + this.m_user + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }
    
    /*
     * P2P Sending Socket example
     * 
     * Client ScnChat 1.0
     * P2PReceive
     * User: Chunnan
     * 
     */
    
    public class ReceivingSocketRequest extends ClientMsg
    {
        private String m_user;
        public ReceivingSocketRequest(String user)
        {
            this.m_user = user;
        }
        
        private ReceivingSocketRequest(BufferedReader in) throws Exception
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
                throw new Exception("Invalid P2P request format!");
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
            sb.append("P2PReceive\r\n");
            
            sb.append("User: " + this.m_user + "\r\n");
            
            String str = sb.toString();
            Globals.debugPrintln(str);
            
            return str;
        }
    }

}
