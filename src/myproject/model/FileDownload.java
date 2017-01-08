package myproject.model;

import client.loggerManager;
import com.google.common.collect.Lists;
import model.FileDataResponseType;
import model.RequestType;
import model.ResponseType;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 07.01.2017
 * Time: 15:52
 */
public class FileDownload implements Serializable, Runnable
{
    private final static Logger logger = loggerManager.getInstance(FileDownload.class);

    private String clientName;
    private MyServer myServer;
    private List<StartEndByte> downloadedStartEndBytes = Lists.newArrayList();
    private Long totalBytesDownloaded = 0L;


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
            List<StartEndByte> startEndByteList = FileHelper.getBytesToDownload(myServer.getBitRate());
            if (startEndByteList != null && startEndByteList.size() > 0)
            {
                long startTime = new Date().getTime();
                startDownload(startEndByteList);
                long elapsedTime = new Date().getTime() - startTime;
                prepareRemainingBytes(startEndByteList, downloadedStartEndBytes);
                downloadedStartEndBytes.clear();
                counter++;
            }
            else
            {
                break;
            }
        }
        logger.info(clientName + " - " + counter + " defa çalıştı.");
        logger.info(clientName + " - " + totalBytesDownloaded + " byte indirildi.");
    }

    private synchronized void startDownload(List<StartEndByte> byteArrayListToDownload)
    {
        InetAddress IPAddress = null;
        try
        {
            IPAddress = InetAddress.getByName(myServer.getIp());

            for (StartEndByte startEndByte : byteArrayListToDownload)
            {
                int startByte = (int) startEndByte.getStart();
                int endByte = (int) startEndByte.getEnd();
                RequestType req = new RequestType(RequestType.REQUEST_TYPES.GET_FILE_DATA, FileHelper.file.getFile_id(), startByte + 1, endByte, null);
                byte[] sendData = req.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, myServer.getPortNumber());
                DatagramSocket dsocket = new DatagramSocket();
                dsocket.send(sendPacket);

                dsocket.setSoTimeout(myServer.getTimeout());

                receivePackets(endByte, dsocket);
//                logger.debug("Gelenler: Client - " + clientName + " - " + startByte + "##" + endByte);
            }
        }
        catch (SocketException e)
        {
            logger.error("Hata: " + e.getMessage());
        }
        catch (UnknownHostException e)
        {
            logger.error("Hata: " + e.getMessage());
        }
        catch (SocketTimeoutException e)
        {
            logger.error("Timeout oluştu. Client: " + clientName + ", " + myServer.getInfo());
        }
        catch (IOException e)
        {
            logger.error("Hata: " + e.getMessage());
        }
    }

    private synchronized void receivePackets(int endByte, DatagramSocket dsocket) throws IOException
    {
        byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
        long maxReceivedByte = -1;
        RandomAccessFile randomAccessFile = new RandomAccessFile(new File("out/" + FileHelper.file.getFile_name()), "rw");
        randomAccessFile.setLength(FileHelper.fileSize);
        while (maxReceivedByte < endByte)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            dsocket.receive(receivePacket);
            FileDataResponseType response = new FileDataResponseType(receivePacket.getData());
            if (response.getResponseType() != ResponseType.RESPONSE_TYPES.GET_FILE_DATA_SUCCESS)
            {
                break;
            }
            else
            {
                downloadedStartEndBytes.add(new StartEndByte(response.getStart_byte(), response.getEnd_byte(), response.getData()));
                int offset = (int) response.getStart_byte();
                int len = (int) (response.getEnd_byte() - response.getStart_byte()) + 1;
                totalBytesDownloaded += len;
                randomAccessFile.seek(offset - 1);
                randomAccessFile.write(response.getData(), 0, len);
            }
            if (response.getEnd_byte() > maxReceivedByte)
            {
                maxReceivedByte = response.getEnd_byte();
            }
        }
        randomAccessFile.close();
    }

    private synchronized void prepareRemainingBytes(List<StartEndByte> startEndByteList, List<StartEndByte> downloadedStartEndBytes)
    {
        List<StartEndByte> remainingBytes = Lists.newArrayList();
        for (StartEndByte startEndByte : startEndByteList)
        {
            long startByte = startEndByte.getStart();
            long endByte = startEndByte.getEnd();
            prepareRemaininBytesBySmallestByte(downloadedStartEndBytes, startByte, endByte, remainingBytes);
            long biggestByteInDownloadedBytes = findBiggestByte(downloadedStartEndBytes);
            if (endByte > biggestByteInDownloadedBytes)
            {
                long index = biggestByteInDownloadedBytes + ((endByte - biggestByteInDownloadedBytes) / 2);
                remainingBytes.add(new StartEndByte(biggestByteInDownloadedBytes, index));
                remainingBytes.add(new StartEndByte(index, endByte));
            }
        }
        if (remainingBytes.size() > 0)
        {
            FileHelper.remainingStartEndBytes.addAll(remainingBytes);
            if (myServer.getBitRate() > 1)
            {
                myServer.setBitRate(myServer.getBitRate() / 2);
                myServer.setTimeout(2000);
//                logger.info("bitrate düşürüldü: " + myServer.getBitRate());
            }
        }
        else
        {
            myServer.setBitRate(myServer.getBitRate() + 1);
            myServer.setTimeout(myServer.getTimeout() + 1000);
//            logger.info("bitrate arttırıldı: " + myServer.getBitRate());
        }
    }

    private void prepareRemaininBytesBySmallestByte(List<StartEndByte> downloadedStartEndBytes, long startByte, long endByte, List<StartEndByte> remainingBytes)
    {
        StartEndByte smallestStartByteInDownloadedBytes = findSmallestByte(downloadedStartEndBytes);
        if (smallestStartByteInDownloadedBytes != null)
        {
            smallestStartByteInDownloadedBytes.setChecked(true);
            if (startByte + 1 < smallestStartByteInDownloadedBytes.getStart())
            {
                long index = startByte + ((smallestStartByteInDownloadedBytes.getStart() - startByte) / 2);
                remainingBytes.add(new StartEndByte(startByte, index));
                remainingBytes.add(new StartEndByte(index, smallestStartByteInDownloadedBytes.getStart()));
            }
            startByte = smallestStartByteInDownloadedBytes.getEnd();
            if (startByte < endByte)
            {
                prepareRemaininBytesBySmallestByte(downloadedStartEndBytes, startByte, endByte, remainingBytes);
            }
        }
    }

    private long findBiggestByte(List<StartEndByte> downloadedStartEndBytes)
    {
        return downloadedStartEndBytes.get(downloadedStartEndBytes.size() - 1).getEnd();
    }

    private StartEndByte findSmallestByte(List<StartEndByte> downloadedStartEndBytes)
    {
        StartEndByte smallestStartEndByte = null;
        long smallestByte = Long.MAX_VALUE;
        for (Iterator<StartEndByte> iterator = downloadedStartEndBytes.iterator(); iterator.hasNext(); )
        {
            StartEndByte startEndByte = iterator.next();
            if (!startEndByte.isChecked() && startEndByte.getStart() < smallestByte)
            {
                smallestByte = startEndByte.getStart();
                smallestStartEndByte = startEndByte;
            }
        }
        return smallestStartEndByte;
    }
}
