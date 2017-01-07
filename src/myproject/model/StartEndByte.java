package myproject.model;

import java.io.Serializable;

/**
 * User: TTEDEMIRCIOGLU
 * Date: 07.01.2017
 * Time: 21:52
 */
public class StartEndByte implements Serializable
{
    private long start;
    private long end;
    private byte[] data;
    private boolean checked;

    public StartEndByte(long start, long end)
    {
        this.start = start;
        this.end = end;
    }

    public StartEndByte(long start, long end, byte[] data)
    {
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public long getStart()
    {
        return start;
    }

    public void setStart(long start)
    {
        this.start = start;
    }

    public long getEnd()
    {
        return end;
    }

    public void setEnd(long end)
    {
        this.end = end;
    }

    public boolean isChecked()
    {
        return checked;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }
}
