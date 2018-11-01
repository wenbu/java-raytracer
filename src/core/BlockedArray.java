package core;

import java.lang.reflect.Array;

public class BlockedArray<T>
{
    private static final int DEFAULT_LOG_BLOCK_SIZE = 2;
    
    private final int uRes;
    private final int vRes;
    private final int uBlocks;
    private final T[] data;
    private final int logBlockSize;
    
    public BlockedArray(int uRes, int vRes, Class<T> clazz)
    {
        this(uRes, vRes, DEFAULT_LOG_BLOCK_SIZE, clazz);
    }
    
    public BlockedArray(int uRes, int vRes, int logBlockSize, Class<T> clazz)
    {
        this(uRes, vRes, logBlockSize, null, clazz);
    }
    
    public BlockedArray(int uRes, int vRes, T[] existingValues, Class<T> clazz)
    {
        this(uRes, vRes, DEFAULT_LOG_BLOCK_SIZE, existingValues, clazz);
    }
    
    public BlockedArray(int uRes, int vRes, int logBlockSize, T[] existingValues, Class<T> clazz)
    {
        this.uRes = uRes;
        this.vRes = vRes;
        this.uBlocks = roundUp(uRes) >> logBlockSize;
        this.logBlockSize = logBlockSize;
        int size = roundUp(uRes) * roundUp(vRes);
        data = (T[]) Array.newInstance(clazz, size);
        
        if (existingValues != null)
        {
            for (int v = 0; v < vRes; v++)
            {
                for (int u = 0; u < uRes; u++)
                {
                    set(u, v, existingValues[v * uRes + u]);
                }
            }
        }
    }
    
    public T get(int u, int v)
    {
        try
        {
            return data[index(u, v)];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println(":( u, v = " + u + ", " + v);
            throw e;
        }
    }
    
    public void set(int u, int v, T value)
    {
        data[index(u, v)] = value;
    }
    
    private int index(int u, int v)
    {
        int bu = block(u);
        int bv = block(v);
        int ou = offset(u);
        int ov = offset(v);
        int offset = blockSize() * blockSize() * (uBlocks * bv + bu);
        offset += blockSize() * ov + ou;
        return offset;
    }
    
    public int block(int a)
    {
        return a >> logBlockSize;
    }
    
    public int offset(int a)
    {
        return a & (blockSize() - 1);
    }
    
    public int uSize()
    {
        return uRes;
    }
    
    public int vSize()
    {
        return vRes;
    }
    
    T[] getData()
    {
        return data;
    }
    
    final int blockSize()
    {
        return 1 << logBlockSize;
    }
    
    private int roundUp(int x)
    {
        return (x + blockSize() - 1) & ~(blockSize() - 1);
    }
}
