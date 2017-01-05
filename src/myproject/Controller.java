package myproject;

import client.loggerManager;
import model.FileDescriptor;
import model.FileListResponseType;
import model.FileSizeResponseType;
import model.RequestType;
import model.ResponseType;
import myproject.model.FileHelper;
import myproject.model.MyClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
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
    private String serverIp;
    private int serverPort;
    private List<MyClient> myClients = new ArrayList<MyClient>();
    private MyClient selectedClient;

    public Controller() throws SocketException
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
                        MyClient myClient = new MyClient(startId++, netint.getName(), (Inet4Address) inetAddress, startPort++);
                        myClient.setServerIpAddress(serverIp);
                        myClient.setServerPortNumber(serverPort);
                        myClients.add(myClient);
                    }
                }
            }
        }
    }

    private void prepareServerParameters(String[] args)
    {
        checkArgument((args != null && args.length == 1), "Server Ip ve port bilgisi ip:port formatında parametre olarak gönderilmelidir");
        String[] ipPort = args[0].split(":");
        checkArgument((ipPort != null && ipPort.length == 2), "Server Ip ve port bilgisi ip:port formatında parametre olarak gönderilmelidir");
        this.serverIp = ipPort[0];
        try
        {
            this.serverPort = Integer.valueOf(ipPort[1]);
        }
        catch (Exception e)
        {
            checkArgument(false, "Port numara tipinde olmalıdır");
        }
    }

    private void prepareMainScreen()
    {
        checkArgument((this.myClients != null && this.myClients.size() > 0), "Ağ bulunamadı");
        for (MyClient myClient : this.myClients)
        {
            System.out.println(myClient.getInfo());
        }
        System.out.print("Ağ için id seçiniz: ");
        while (this.selectedClient == null)
        {
            Scanner in = new Scanner(System.in);
            String selectedClientId = in.nextLine();
            selectedClient = getSelectedClient(selectedClientId);
        }
        System.out.println("Seçilen ağ: " + this.selectedClient.getInfo());
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
        System.out.print("lütfen geçerli bir id giriniz: ");
        return null;
    }

    private DatagramPacket getResponse(int requestType, int file_id, long start_byte, long end_byte, byte[] data) throws IOException
    {
        InetAddress IPAddress = InetAddress.getByName(this.serverIp);
        RequestType req = new RequestType(requestType, file_id, start_byte, end_byte, data);
        byte[] sendData = req.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, this.serverPort);
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(sendPacket);
        byte[] receiveData = new byte[ResponseType.MAX_RESPONSE_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        dsocket.receive(receivePacket);
        return receivePacket;
    }

    private void prepareProcessSelectionScreen() throws IOException
    {
        DatagramPacket receivePacket = getResponse(RequestType.REQUEST_TYPES.GET_FILE_LIST, 0, 0, 0, null);
        FileListResponseType response = new FileListResponseType(receivePacket.getData());
        loggerManager.getInstance(this.getClass()).debug(response.toString());
        System.out.print("Dosya numarası seçiniz: ");
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
                System.out.print("Lütfen geçerli bir dosya id giriniz: ");
            }
        }
        System.out.println("Seçilen dosya id: " + FileHelper.file.getFile_id());
    }

    private void prepareFileSize() throws IOException
    {
        DatagramPacket receivePacket = getResponse(RequestType.REQUEST_TYPES.GET_FILE_SIZE, FileHelper.file.getFile_id(), 0, 0, null);
        FileSizeResponseType response = new FileSizeResponseType(receivePacket.getData());
        FileHelper.fileSize = response.getFileSize();
        System.out.println("Seçilen dosyanın boyutu: " + FileHelper.fileSize);
    }

    private void prepareFileFileByteMap()
    {
        try
        {
            File file = new File("lib/" + FileHelper.file.getFile_name());
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileHelper.prepareFileBytesMap();
            FileHelper.randomAccessFile = raf;
        }
        catch (FileNotFoundException exception)
        {
            System.out.println("Hata: " + exception.getMessage());
        }
    }

    private void startDownloading()
    {
        ExecutorService executorService = Executors.newFixedThreadPool(this.myClients.size());
        for (MyClient myClient : this.myClients)
        {
            executorService.execute(new Thread(myClient));
        }
        executorService.shutdown();
        while (!executorService.isTerminated())
        {
        }
    }

    public static void main(String[] args) throws IOException
    {
        try
        {
            Controller controller = new Controller();
            controller.prepareServerParameters(args);
            controller.prepareMainScreen();
            controller.prepareProcessSelectionScreen();
            controller.prepareFileSize();
            controller.prepareFileFileByteMap();
            controller.startDownloading();
        }
        catch (Exception exception)
        {
            System.out.println("Hata: " + exception.getMessage());
        }
    }
}
