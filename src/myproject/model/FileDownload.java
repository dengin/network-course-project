package myproject.model;

import client.loggerManager;
import com.google.common.collect.Lists;
import model.FileDataResponseType;
import model.RequestType;
import model.ResponseType;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 07.01.2017
 * Time: 15:52
 */
public class FileDownload implements Serializable, Runnable
{
    private String clientName;
    private MyServer myServer;
    List<int[]> byteArrayListToDownload = Lists.newArrayList();


    public FileDownload(String clientName, MyServer myServer)
    {
        this.clientName = clientName;
        this.myServer = myServer;
    }

    @Override
    public void run()
    {
        int counter = 0;

        while (true)
        {
            int[] byteArrayToDownload = FileHelper.getBytesToDownload();
            byteArrayListToDownload.add(byteArrayToDownload);
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
        System.out.println(clientName + " - " + counter + " defa çalıştı.");
    }

    private void startDownload(int[] byteArrayToDownload)
    {
        InetAddress IPAddress = null;
        try
        {
            IPAddress = InetAddress.getByName(myServer.getIp());

            int startByte = byteArrayToDownload[0];
            int endByte = byteArrayToDownload[1];
            RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, FileHelper.file.getFile_id(), startByte + 1, endByte, null);
            byte[] sendData = req.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, myServer.getPortNumber());
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
                else
                {
                    int offset = (int) response.getStart_byte();
                    int len = (int) (response.getEnd_byte() - response.getStart_byte()) + 1;
                    FileHelper.randomAccessFile.seek(offset - 1);
                    FileHelper.randomAccessFile.write(response.getData(), 0, len);
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
