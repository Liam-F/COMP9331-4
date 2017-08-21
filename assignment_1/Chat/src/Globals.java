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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Globals
{
    public static boolean debug = false;
    
    public static final DateFormat date_format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    public static final String client_request_header = "Client ScnChat 1.0";

    public static final String server_request_header = "Server ScnChat 1.0";
    
    public static final String client_p2p_request_header = "Client ScnChat 1.0 P2P";

    public static final String error_invalid_command = "Error. Invalid Command";

    public static final String error_invalid_user = "Error. Invalid User";

    public static final String error_cannot_block_self = "Error. Cannot block self";
    
    public static final String error_cannot_unblock_self = "Error. Cannot unblock self";
    
    public static final String error_blocked_by_user = "Your message could not be delivered as the recipient has blocked you";
    
    public static final String error_blocked_by_users = "Your message could not be delivered to some recipients";
    
    public static final String error_user_offline = "The user you trying to connect to is offline";
    
    public static final String command_whoelse = "whoelse";
    
    public static final String command_message = "message";
    
    public static final String command_broadcast = "broadcast";
    
    public static final String command_whoelsesince = "whoelsesince";
    
    public static final String command_block = "block";
    
    public static final String command_unblock = "unblock";
    
    public static final String command_startprivate = "startprivate";
    
    public static final String command_stopprivate = "stopprivate";
    
    public static final String command_private = "private";
    
    public static final String command_private_whoelse = "privatewhoelse";
    
    public static final String command_logout = "logout";

    // public static final int p2p_port = 9331;

    public static final String login_user_prompt = "Username: ";

    public static final String login_pass_prompt = "Password: ";

    public static final String login_failure = "Invalid User or Password. Please try again";

    public static final String login_welcome_msg = "Welcome to the greatest messaging application ever!";

    public static final String login_block = "Invalid User or Password. Your account has been blocked. Please try again later";

    public static final String login_account_blocked = "Your account is blocked due to multiple login failures. Please try again later";
    
    /**
     * This asynchronous procedure replaces the original System.in readLine procedure
     * to solve the problem that System.in readLine cannot be interrupted
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String readLine(BufferedReader in) throws IOException, InterruptedException
    {
        boolean interrupted = false;

        StringBuilder result = new StringBuilder();
        int chr = -1;
        while (true)
        {
            if (in.ready())
            {
                chr = in.read();
            }
            else
            {
                Thread.sleep(100);
            }
            
            if (chr > -1)
            {
                result.append((char) chr);
            }
            
            interrupted = Thread.interrupted(); // resets flag, call only once
            
            if (result.toString().endsWith("\n") || interrupted)
            {
                break;
            }
        }
        
        if (interrupted)
        {
            throw new InterruptedException();
        }
        
        return result.toString().replaceAll("[\r]*\n$", "");
    }

    
    public static void debugPrintln(String str)
    {
        if (debug)
        {
            System.out.println(str);
        }
    }

    public static void debugPrint(String str)
    {
        if (debug)
        {
            System.out.print(str);
        }
    }
    
    public static void debugPrintStackTrace(Exception e)
    {
        if (debug)
        {
            System.out.println(e.toString());
            StackTraceElement [] traces = e.getStackTrace();
            for (StackTraceElement trace : traces)
            {
                System.out.println(trace);
            }
        }
    }
    
    public static void println(String str)
    {
        System.out.println(str);
    }
    
    public static void print(String str)
    {
        System.out.print(str);
    }

}
