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
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The telnet command line is used to test and debug of the Server
 * @author Chunnan Sheng
 *
 */
public class ClientTelnet
{
    public static void commandLine(BufferedReader in, PrintWriter out) throws IOException
    {
        Reader the_reader = new InputStreamReader(System.in);

        try (BufferedReader br = new BufferedReader(the_reader))
        {
            while (true)
            {
                String msg = br.readLine();
                out.print(msg + "\r\n");
                out.flush();
            }
        }
    }

    public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException
    {
        try (Socket socket = new Socket(args[0], Integer.parseInt(args[1])))
        {

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            commandLine(in, out);
        }
    }
}
