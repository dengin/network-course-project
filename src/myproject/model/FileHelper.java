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

//    public FileHelper(model.FileDescriptor file)
//    {
//        this.file = file;
//    }

//    public FileDescriptor getFile()
//    {
//        return file;
//    }

//    public void setFile(FileDescriptor file)
//    {
//        this.file = file;
//    }
//
//    public long getFileSize()
//    {
//        return fileSize;
//    }
//
//    public void setFileSize(long fileSize)
//    {
//        this.fileSize = fileSize;
//    }
//
    public static Map<Long, Long> getFileBytes()
    {
        return fileBytes;
    }
//
//    public void setFileBytes(Map<Long, Long> fileBytes)
//    {
//        this.fileBytes = fileBytes;
//    }
//
//    public Map<Long, Long> getRemainingBytes()
//    {
//        return remainingBytes;
//    }
//
//    public void setRemainingBytes(Map<Long, Long> remainingBytes)
//    {
//        this.remainingBytes = remainingBytes;
//    }
//
//    public RandomAccessFile getRandomAccessFile()
//    {
//        return randomAccessFile;
//    }
//
//    public void setRandomAccessFile(RandomAccessFile randomAccessFile)
//    {
//        this.randomAccessFile = randomAccessFile;
//    }

    public static void prepareFileBytesMap()
    {
        long i = 0;
        while (i <= fileSize)
        {
            if (fileSize - i >= MAX_BYTE_LENGTH)
            {
                fileBytes.put(i, MAX_BYTE_LENGTH);
            }
            else
            {
                fileBytes.put(i, fileSize - i);
            }
            i += MAX_BYTE_LENGTH;
        }
    }

    public static synchronized Long getAKey()
    {
        if (fileBytes != null && fileBytes.size() > 0)
        {
            Long l = fileBytes.keySet().iterator().next();
            fileBytes.remove(l);
            return l;
        }
        return null;
    }
}
