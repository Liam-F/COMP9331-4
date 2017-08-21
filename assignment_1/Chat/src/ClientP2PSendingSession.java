/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class ClientP2PSendingSession implements Runnable
{
    private Socket m_socket;
    private boolean m_stop;
    private String m_remote_user;
    private PrintWriter m_net_out;
    private ClientP2PSession m_p_session;

    private Queue<InterSessionMessage> m_message_queue;

    public ClientP2PSendingSession(Socket socket, PrintWriter net_out, String remote_user)
    {
        this.m_socket = socket;
        this.m_stop = false;
        this.m_message_queue = new LinkedList<InterSessionMessage>();
        this.m_net_out = net_out;
        this.m_p_session = null;
        this.m_remote_user = remote_user;
    }
    
    public void setParent(ClientP2PSession session)
    {
        this.m_p_session = session;
    }

    /**
     * This should be multiple thread safe
     * @param sender
     * @param content
     * @param host
     * @param sent_time
     */
    public void enqueueSendingMessage(String sender, String content, String host, long sent_time)
    {
        InterSessionMessage msg = new InterSessionMessage(sender, content, host, sent_time);

        synchronized (this)
        {
            this.m_message_queue.add(msg);
        }
    }

    /**
     * it should be multiple thread safe
     * 
     * @return
     */
    private InterSessionMessage dequeueSendingMessage()
    {
        synchronized (this)
        {
            return this.m_message_queue.poll();
        }
    }

    private boolean hasPeerMessage()
    {
        synchronized (this)
        {
            return !m_message_queue.isEmpty();
        }
    }

    public void stop()
    {
        synchronized (this)
        {
            this.m_stop = true;
        }
    }
    
    private boolean shouldStop()
    {
        synchronized (this)
        {
            return this.m_stop;
        }
    }

    @Override
    public void run()
    {
        Globals.debugPrintln("============ P2P sending thread (" + this.m_p_session.getRemoteUser() + ") begins =============");
        try
        {
            InterSessionMessage msg;
            // The main loop of peer reply session should not exit until
            // the mother session is closed and the message queue is empty
            while (!this.shouldStop() || this.hasPeerMessage())
            {
                while ((msg = this.dequeueSendingMessage()) != null)
                {
                    PacketFromP2P.MessageRequest cr = (new PacketFromP2P()).new MessageRequest(msg.getHost(),
                            msg.getSender(), this.m_remote_user, msg.getSentTime(), msg.getContent());

                    this.m_net_out.print(cr.toString());
                    this.m_net_out.flush();
                }

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    Globals.debugPrintStackTrace(e);
                }
            }
        }
        catch (Exception e1)
        {
            Globals.debugPrintStackTrace(e1);
        }
        finally
        {
            this.m_p_session.stop();
            // Close the connection
            try
            {
                this.m_socket.close();
            }
            catch (IOException e)
            {
                Globals.debugPrintStackTrace(e);
            }

            Globals.debugPrintln("============ P2P sending thread (" + this.m_p_session.getRemoteUser() + ") exits =============");
        }
    }
}
