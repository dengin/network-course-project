package myproject.model;

import client.loggerManager;
import model.FileDataResponseType;
import model.RequestType;
import model.ResponseType;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
    private String serverIpAddress;
    private int serverPortNumber;
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

    public String getServerIpAddress()
    {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress)
    {
        this.serverIpAddress = serverIpAddress;
    }

    public int getServerPortNumber()
    {
        return serverPortNumber;
    }

    public void setServerPortNumber(int serverPortNumber)
    {
        this.serverPortNumber = serverPortNumber;
    }

    @Override
    public void run()
    {
        int counter = 0;

        while (true)
        {
            int[] byteArrayToDownload = FileHelper.getBytesToDownload();
            if (byteArrayToDownload != null)
            {
                startDownload(byteArrayToDownload);

                counter++;
            }
            else
            {
                break;
            }
        }
        System.out.println(clientName + " - " + counter + " tane çıkarttı.");
    }

    private void startDownload(int[] byteArrayToDownload)
    {
        InetAddress IPAddress = null;
        try
        {
            IPAddress = InetAddress.getByName(serverIpAddress);

            int startByte = byteArrayToDownload[0];
            int endByte = byteArrayToDownload[1];
            RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, FileHelper.file.getFile_id(), startByte, endByte, null);
            byte[] sendData = req.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPortNumber);
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(sendPacket);
            byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
            long maxReceivedByte = -1;
            while (maxReceivedByte < endByte)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                dsocket.receive(receivePacket);
                FileDataResponseType response = new FileDataResponseType(receivePacket.getData());
                loggerManager.getInstance(this.getClass()).debug(response.toString());
                if (response.getResponseType() != ResponseType.RESPONSE_TYPES.GET_FILE_DATA_SUCCESS)
                {
                    break;
                }
                if (response.getEnd_byte() > maxReceivedByte)
                {
                    maxReceivedByte = response.getEnd_byte();
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

