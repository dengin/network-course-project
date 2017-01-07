package myproject.model;

import com.google.common.collect.Lists;
import model.FileDescriptor;

import java.io.Serializable;
import java.util.List;

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

    public static synchronized StartEndByte getBytesToDownload(int bitRate)
    {
        if (startEndBytes != null && startEndBytes.size() > 0)
        {
            Long startByteValue = startEndBytes.get(0).getStart();
            Long endByteValue = startEndBytes.get(bitRate - 1).getEnd();
            for (int i = 0; i < bitRate; i++)
            {
                startEndBytes.remove(i);
            }
            return new StartEndByte(startByteValue, endByteValue);
        }
        return null;
    }
}
