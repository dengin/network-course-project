package myproject;

import com.google.common.collect.Lists;
import log.loggerManager;
import model.FileDescriptor;
import model.FileListResponseType;
import model.FileSizeResponseType;
import model.RequestType;
import model.ResponseType;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 18.12.2016
 * Time: 02:51
 */
public class Controller implements Serializable
{
    private final static Logger logger = loggerManager.getInstance(Controller.class);

    private List<MyClient> myClients = new ArrayList<MyClient>();
    private MyClient selectedClient;
    private MyServer selectedServer;

    public Controller(String[] args) throws SocketException
    {
        List<MyServer> myServers = prepareServerParameters(args);
        prepareClientParameters(myServers);
    }

    private List<MyServer> prepareServerParameters(String[] args)
    {
        checkArgument((args != null && args.length > 0), "Sunucu ip ve port bilgisi ip:port formatinda parametre olarak gonderilmelidir");
        List<MyServer> myServers = Lists.newArrayList();
        int startId = 1;
        for (int i = 0; i < args.length; i++)
        {
            myServers.add(new MyServer(startId++, args[i]));
        }
        return myServers;
    }

    private void prepareClientParameters(List<MyServer> myServers) throws SocketException
    {
        Enumeration<NetworkInterface> nets = null;
        int startPort = 5000;
        int startId = 1;
        nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
        {
            if (netint.isUp())
            {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses))
                {
                    if (inetAddress instanceof Inet4Address)
                    {
                        myClients.add(new MyClient(startId++, netint.getName(), (Inet4Address) inetAddress, startPort++, myServers));
                    }
                }
            }
        }
    }

    private void prepareMainScreen()
    {
        checkArgument((this.myClients != null && this.myClients.size() > 0), "Istemci bulunamadı");
        for (MyClient myClient : this.myClients)
        {
            System.out.println(myClient.getInfo());
        }
        logger.info("Istemci icin id seciniz: ");
        while (this.selectedClient == null)
        {
            Scanner in = new Scanner(System.in);
            String selectedClientId = in.nextLine();
            selectedClient = getSelectedClient(selectedClientId);
        }
        checkArgument((this.selectedClient.getMyServers() != null && this.selectedClient.getMyServers().size() > 0), "Sunucu bulunamadi");
        logger.info("Secilen istemci: " + this.selectedClient.getInfo());
        this.selectedServer = this.selectedClient.getMyServers().get(0);
        logger.info("Secilen sunucu: " + this.selectedServer.getInfo());
    }

    private MyClient getSelectedClient(String selectedClientId)
    {
        if (this.myClients != null)
        {
            for (MyClient myClient : this.myClients)
            {
                if (selectedClientId.equals(myClient.getId().toString()))
                {
                    return myClient;
                }
            }
        }
        logger.error("Lutfen gecerli bir id giriniz: ");
        return null;
    }

    private DatagramPacket getResponse(int requestType, int file_id, long start_byte, long end_byte, byte[] data, String serverIp, int serverPort) throws IOException
    {
        InetAddress IPAddress = InetAddress.getByName(serverIp);
        RequestType req = new RequestType(requestType, file_id, start_byte, end_byte, data);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        dsocket.receive(receivePacket);
        return receivePacket;
    }

    private void prepareProcessSelectionScreen() throws IOException
    {
        DatagramPacket receivePacket = getResponse(RequestType.REQUEST_TYPES.GET_FILE_LIST, 0, 0, 0, null, selectedServer.getIp(), selectedServer.getPortNumber());
        FileListResponseType response = new FileListResponseType(receivePacket.getData());
        logger.info(response.toString());
        logger.info("Dosya numarasi seciniz: ");
        while (FileHelper.file == null)
        {
            Scanner in = new Scanner(System.in);
            String fileId = in.nextLine();
            for (int i = 0; i < response.getFileDescriptors().length; i++)
            {
                FileDescriptor fileDescriptor = response.getFileDescriptors()[i];
                if (fileDescriptor.getFile_id() == Integer.valueOf(fileId))
                {
                    FileHelper.file = fileDescriptor;
                    break;
                }
            }
            if (FileHelper.file == null)
            {
                logger.error("Lutfen gecerli bir dosya id giriniz: ");
            }
        }
        logger.info("Secilen dosya id: " + FileHelper.file.getFile_id());
    }

    private void prepareFileSize() throws IOException
    {
        DatagramPacket receivePacket = getResponse(RequestType.REQUEST_TYPES.GET_FILE_SIZE, FileHelper.file.getFile_id(), 0, 0, null, selectedServer.getIp(), selectedServer.getPortNumber());
        FileSizeResponseType response = new FileSizeResponseType(receivePacket.getData());
        FileHelper.setFileSizeAndFileStartByteSize(response);
    }

    private void prepareFileFileByteMap()
    {
        File file = new File("./" + FileHelper.file.getFile_name());
        FileHelper.prepareFileBytesMap();
    }

    private void startDownloading()
    {
        ExecutorService executorService = Executors.newFixedThreadPool(this.myClients.size());
        long startTime = new Date().getTime();
        for (MyClient myClient : this.myClients)
        {
            executorService.execute(myClient);
        }
        executorService.shutdown();
        while (!executorService.isTerminated())
        {
        }
        prepareResultLog(startTime);
    }

    private void prepareResultLog(long startTime)
    {
        long elapsedTime = new Date().getTime() - startTime;
        logger.info("Toplam sure: " + elapsedTime + " ms.");
        long totalBytesDownloaded = FileHelper.totalBytesDownloaded;
        if (totalBytesDownloaded > 0)
        {
            logger.info("Toplam indirilen byte: " + totalBytesDownloaded + " B.");

            double elapsedTimeAsSecond = ((double) elapsedTime) / 1000;
            double meanRate = (totalBytesDownloaded * 8) / elapsedTimeAsSecond;
            DecimalFormat df = new DecimalFormat("#0.0");
            logger.info("Ortalama indirme hizi: " + df.format(meanRate) + " bps.");
        }
        logger.info("md5: " + Util.getMd5(new File("./" + FileHelper.file.getFile_name())));
    }

    public static void main(String[] args) throws IOException
    {
        try
        {
            Controller controller = new Controller(args);
            controller.prepareMainScreen();
            controller.prepareProcessSelectionScreen();
            controller.prepareFileSize();
            controller.prepareFileFileByteMap();
            controller.startDownloading();
        }
        catch (Exception exception)
        {
            logger.error(exception.getMessage());
        }
    }
}
