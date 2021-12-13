package de.nomagic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class addJob
{

    // configuration:
    private String ServerURL = "127.0.0.1";
    private String ClientId = null;
    private int ServerPort = 4321;
    private String additionalParameters = "";

    private boolean isConnected = false;
    private Socket clientSocket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private boolean printErrorMessages = false;
    private int result = 99;

    public addJob()
    {
    }

    private void printHelpText()
    {
        System.err.println("Parameters:");
        System.err.println("===========");
        System.err.println("-h");
        System.err.println("     : This text");
        System.err.println("-host hostname");
        System.err.println("     : connect to the remote server on the host 'hostname'");
        System.err.println("- port 1234");
        System.err.println("     : use the given port instead of the default port " + ServerPort);
        System.err.println("-requestId clientName");
        System.err.println("     : send 'clientName' to the server as identification.");
        System.err.println("-errors");
        System.err.println("     : Print Status and error messages.");
        System.err.println("-param type=analyse:data=12345:");
        System.err.println("     : defines Job parameters that will be send to the server.");


    }

    public void getConfigFromCommandLine(String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].startsWith("-"))
            {
                if(true == "-host".equals(args[i]))
                {
                    i++;
                    ServerURL = args[i];
                }
                else if(true == "-port".equals(args[i]))
                {
                    i++;
                    ServerPort = Integer.parseInt(args[i]);
                }
                else if(true == "-errors".equals(args[i]))
                {
                    printErrorMessages = true;
                }
                else if(true == "-requestId".equals(args[i]))
                {
                    i++;
                    ClientId = args[i];
                }
                else if(true == "-param".equals(args[i]))
                {
                    i++;
                    additionalParameters = args[i];
                    additionalParameters = additionalParameters.trim();
                    if(true == additionalParameters.startsWith(":"))
                    {
                        additionalParameters = additionalParameters.substring(1);
                    }
                    if(false == additionalParameters.endsWith(":"))
                    {
                        additionalParameters = additionalParameters + ":";
                    }
                }
                else if(true == "-h".equals(args[i]))
                {
                    printHelpText();
                    System.exit(2);
                }
                else
                {
                    System.err.println(" ! ! ! Invalid Parameter : " + args[i]);
                    printHelpText();
                    System.exit(1);
                }
            }
        }
    }

    private void doTheWork()
    {
        String JobInfo = "";
        // Connect to server
        connectTo(ServerURL, ServerPort);
        if(false == isConnected)
        {
            if(true == printErrorMessages)
            {
                System.err.println("Could not connect to Server !");
            }
            System.exit(3);
        }

        JobInfo =  getJobInformation();

        disconnect();
        if(null != JobInfo)
        {
            JobInfo = JobInfo.trim();
            if(0 < JobInfo.length())
            {
                System.out.println("Job:" + JobInfo);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        addJob m = new addJob();
        m.getConfigFromCommandLine(args);
        m.doTheWork();
        System.exit(m.getResult());
    }

    private int getResult()
    {
        return result;
    }

    public void disconnect()
    {
        try
        {
            clientSocket.close();
        }
        catch (IOException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        isConnected = false;
    }

    public String getJobInformation()
    {
        try
        {
            do
            {
                if(null == ClientId)
                {
                    outToServer.writeBytes("2:add:" + additionalParameters + "\n");
                }
                else
                {
                    outToServer.writeBytes("2:add:name=" + ClientId + ":" + additionalParameters + "\n");
                }
                outToServer.flush();
                String rep;
                rep = inFromServer.readLine();
                if(rep.startsWith("2:0:"))
                {
                    // OK
                    result = 0;
                    return rep;
                }
                else if(rep.startsWith("2:3:"))
                {
                    if(true == printErrorMessages)
                    {
                        System.out.println("Server rejected the Job!");
                    }
                    result = 4;
                    return "";
                }
                else if(rep.startsWith("2:2:"))
                {
                    Thread.sleep(1000);
                }
            }while(true);
        }
        catch (IOException e)
        {
            isConnected = false;
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
            result = 5;
        }
        catch (InterruptedException e)
        {
            isConnected = false;
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
            result = 6;
        }
        result = 7;
        return "";
    }

    public void connectTo(String serverURL, int serverPort)
    {
        try
        {
            clientSocket = new Socket(serverURL, serverPort);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            isConnected = true;
        }
        catch (UnknownHostException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            if(true == printErrorMessages)
            {
                e.printStackTrace();
            }
        }
    }

}
