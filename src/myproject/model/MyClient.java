package myproject.model;

import java.io.Serializable;
import java.net.Inet4Address;

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

    public MyClient(Integer id, String clientName, Inet4Address ipAddress, int portNumber)
    {
        this.id = id;
        this.clientName = clientName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
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

    @Override
    public void run()
    {
        int counter = 0;

        while (true)
        {
            Long l = FileHelper.getAKey();
            if (l != null)
            {
                System.out.println("Benim adım: " + clientName + " ve " + l + " çıkarttım");
                counter ++;
            }
            else
            {
                break;
            }
        }
        System.out.println(clientName + " - " + counter + " tane çıkarttı.");
    }
}

