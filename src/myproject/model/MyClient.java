package myproject.model;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 18.12.2016
 * Time: 02:36
 */
@SuppressWarnings("Since15")
public class MyClient implements Serializable, Runnable
{
    private Integer id;
    private String clientName;
    private Inet4Address ipAddress;
    private int portNumber;
    private int bitRate;
    private int timeout;
    private List<MyServer> myServers;

    public MyClient(Integer id, String clientName, Inet4Address ipAddress, int portNumber, List<MyServer> myServers)
    {
        this.id = id;
        this.clientName = clientName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.myServers = myServers;
    }

    public String getInfo()
    {
        String info = "";
        if (this.id != null)
        {
            info += "Id: " + this.id.toString();
        }
        if (this.clientName != null)
        {
            info += " Ağ: " + this.clientName;
        }
        if (this.ipAddress != null)
        {
            info += " IP: " + this.ipAddress.getHostAddress();
        }
        if (this.portNumber != 0)
        {
            info += " Port: " + this.portNumber;
        }
        return info;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getClientName()
    {
        return clientName;
    }

    public void setClientName(String clientName)
    {
        this.clientName = clientName;
    }

    public Inet4Address getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public void setPortNumber(int portNumber)
    {
        this.portNumber = portNumber;
    }

    public int getBitRate()
    {
        return bitRate;
    }

    public void setBitRate(int bitRate)
    {
        this.bitRate = bitRate;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public List<MyServer> getMyServers()
    {
        return myServers;
    }

    public void setMyServers(List<MyServer> myServers)
    {
        this.myServers = myServers;
    }

    @Override
    public void run()
    {
        ExecutorService executorService = Executors.newFixedThreadPool(this.myServers.size());
        for (MyServer myServer : this.myServers)
        {
            executorService.execute(new Thread(new FileDownload(clientName, myServer)));
        }
        executorService.shutdown();
        while (!executorService.isTerminated())
        {
        }
    }
}

