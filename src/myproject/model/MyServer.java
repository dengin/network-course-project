package myproject.model;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 07.01.2017
 * Time: 15:11
 */
public class MyServer implements Serializable
{
    private Integer id;
    private String ip;
    private int portNumber;
    private int bitRate;
    private int timeout;

    public MyServer(int id, String serverIpPort)
    {
        String[] ipPort = serverIpPort.split(":");
        checkArgument((ipPort != null && ipPort.length == 2), "Server Ip ve port bilgisi ip:port formatında parametre olarak gönderilmelidir");
        this.id = id;
        this.ip = ipPort[0];
        try
        {
            this.portNumber = Integer.valueOf(ipPort[1]);
        }
        catch (Exception e)
        {
            checkArgument(false, "Port numara tipinde olmalıdır");
        }
        this.bitRate = 1;
        this.timeout = 5000;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
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

    public String getInfo()
    {
        String info = "";
        if (this.id != null)
        {
            info += "Sunucu Id: " + this.id.toString();
        }
        if (this.ip != null)
        {
            info += " IP: " + this.ip;
        }
        if (this.portNumber != 0)
        {
            info += " Port: " + this.portNumber;
        }
        return info;
    }
}
