package myproject.model;

import client.loggerManager;
import com.google.common.collect.Lists;
import model.FileDescriptor;
import model.FileSizeResponseType;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 18.12.2016
 * Time: 22:52
 */
public class FileHelper implements Serializable
{
    private final static Logger logger = loggerManager.getInstance(FileHelper.class);

    public static long MAX_BYTE_LENGTH = 5000;
    public static FileDescriptor file;
    public static long fileSize;
    public static List<StartEndByte> startEndBytes = Lists.newArrayList();
    public static List<StartEndByte> remainingStartEndBytes = Lists.newArrayList();

    public FileHelper()
    {
    }

    public static void prepareFileBytesMap()
    {
        long i = 0;
        while (i <= fileSize)
        {
            if (fileSize - i >= MAX_BYTE_LENGTH)
            {
                startEndBytes.add(new StartEndByte(i, (i + MAX_BYTE_LENGTH)));
            }
            else
            {
                startEndBytes.add(new StartEndByte(i, fileSize));
            }
            i += MAX_BYTE_LENGTH;
        }
    }

    public synchronized static List<StartEndByte> getBytesToDownload(int bitRate)
    {
        List<StartEndByte> byteArrayListToDownload = Lists.newArrayList();
        if (startEndBytes != null && startEndBytes.size() > 0)
        {
            if (startEndBytes.size() < bitRate)
            {
                bitRate = startEndBytes.size();
            }
            Long startByteValue = startEndBytes.get(0).getStart();
            Long endByteValue = startEndBytes.get(bitRate - 1).getEnd();
            startEndBytes = startEndBytes.subList(bitRate, startEndBytes.size());
            byteArrayListToDownload.add(new StartEndByte(startByteValue, endByteValue));
        }
        else if (remainingStartEndBytes != null && remainingStartEndBytes.size() > 0)
        {
            if (remainingStartEndBytes.size() < bitRate)
            {
                bitRate = remainingStartEndBytes.size();
            }
            byteArrayListToDownload.addAll(remainingStartEndBytes.subList(0, bitRate));
            remainingStartEndBytes = remainingStartEndBytes.subList(bitRate, remainingStartEndBytes.size());
        }
        return byteArrayListToDownload;
    }

    public static void setFileSizeAndFileStartByteSize(FileSizeResponseType response)
    {
        fileSize = response.getFileSize();
        logger.info("Seçilen dosyanın boyutu: " + fileSize);
        long startByteSize = fileSize / 100;
        if (startByteSize > 50000L)
        {
            startByteSize = 50000L;
        }
        else if (startByteSize < 1000L)
        {
            startByteSize = 1000L;
        }
        MAX_BYTE_LENGTH = startByteSize;
        logger.info("Seçilen dosya için başlangıç byte değeri: " + MAX_BYTE_LENGTH);
    }
}
