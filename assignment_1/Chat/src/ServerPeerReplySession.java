
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

/**
 * 
 * @author Chunnan Sheng
 *
 */
public class ServerPeerReplySession implements Runnable
{
    private Socket m_socket;
    private boolean m_stop;
    private String m_user;
    
    private Queue<InterSessionMessage> m_message_queue;

    public ServerPeerReplySession(Socket socket, String user)
    {
        this.m_socket = socket;
        this.m_user = user;
        this.m_message_queue = new LinkedList<InterSessionMessage>();
        this.m_stop = false;
    }
    
    
    /**
     * This procedure may be executed by another connection sessions
     * so, it should be multiple thread safe
     * 
     * @param user
     * @param message
     */
    public void enqueuePeerMessage(String sender, String content, String host, long sent_time)
    {
        InterSessionMessage msg = new InterSessionMessage(sender, content, host, sent_time);

        synchronized (this)
        {
            this.m_message_queue.add(msg);
        }
    }
    
    /**
     * This procedure may be executed by another connection sessions
     * so, it should be multiple thread safe
     * 
     * @param user
     * @param message
     */
    public void enqueuePeerMessage(String sender, boolean login, String host, long sent_time)
    {
        InterSessionMessage msg = new InterSessionMessage(sender, login, host, sent_time);

        synchronized (this)
        {
            this.m_message_queue.add(msg);
        }
    }
    
    /**
     * This procedure is executed by PeerReplySession
     * so, it should be multiple thread safe
     * 
     * @return
     */
    private InterSessionMessage dequeuePeerMessage()
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

    /**
     * 
     */
    @Override
    public void run()
    {
        Globals.debugPrintln("==================== Peer reply thread begins =======================");
        PrintWriter netout;
        try
        {
            netout = new PrintWriter(this.m_socket.getOutputStream(), true);

            InterSessionMessage msg;

            while ((msg = ServerEntity.dequeueOfflinePeerMessage(this.m_user)) != null)
            {
                PacketFromServer.ClientPeerResponse cr = (new PacketFromServer()).new ClientPeerResponse(msg.getHost(),
                        msg.getSender(), this.m_user, msg.getSentTime(), msg.getContent());

                netout.print(cr.toString());
            }

            netout.flush();

            // The main loop of peer reply session should not exit until
            // the mother session is closed and the message queue is empty
            while (!this.m_stop || this.hasPeerMessage())
            {
                while ((msg = this.dequeuePeerMessage()) != null)
                {
                    if (null == msg.getContent())
                    {
                        PacketFromServer.PresenceNotification pn = (new PacketFromServer()).new PresenceNotification(
                                msg.getSender(), msg.getPresenceType(), msg.getSentTime());

                        netout.print(pn.toString());
                        netout.flush();
                    }
                    else
                    {
                        PacketFromServer.ClientPeerResponse cr = (new PacketFromServer()).new ClientPeerResponse(
                                msg.getHost(), msg.getSender(), this.m_user, msg.getSentTime(),
                                msg.getContent());

                        netout.print(cr.toString());
                        netout.flush();
                    }
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
        catch (IOException e1)
        {
            Globals.debugPrintStackTrace(e1);
            //this.m_mother.setSessionStatus(SessionStatus.Disconnected);
        }
        finally
        {
            // Close the connection
            try
            {
                this.m_socket.close();
            }
            catch (IOException e)
            {
                Globals.debugPrintStackTrace(e);
            }

            Globals.debugPrintln("==================== Peer reply thread exits =======================");
        }
    }
}