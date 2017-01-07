package myproject.model;

import model.FileDescriptor;

import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 18.12.2016
 * Time: 22:52
 */
public class FileHelper implements Serializable
{
    public static long MAX_BYTE_LENGTH = 1000;
    public static FileDescriptor file;
    public static long fileSize;
    public static Map<Long, Long> fileBytes = new HashMap<Long, Long>();
    public static Map<Long, Long> remainingBytes = new HashMap<Long, Long>();
    public static RandomAccessFile randomAccessFile;

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
                fileBytes.put(i, (i + MAX_BYTE_LENGTH));
            }
            else
            {
                fileBytes.put(i, fileSize);
            }
            i += MAX_BYTE_LENGTH;
        }
    }

    public static synchronized int[] getBytesToDownload()
    {
        if (fileBytes != null && fileBytes.size() > 0)
        {
            Long startByteValue = fileBytes.keySet().iterator().next();
            Long endByteValue = fileBytes.get(startByteValue);
            int[] byteArray = new int[]{startByteValue.intValue(), endByteValue.intValue()};
            fileBytes.remove(Long.parseLong(String.valueOf(startByteValue)));
            return byteArray;
        }
        return null;
    }
}
