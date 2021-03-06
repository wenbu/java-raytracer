package core.tuple;

public class Triple<T, U, V>
{
    private final T first;
    private final U second;
    private final V third;
    
    public Triple(T first, U second, V third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst()
    {
        return first;
    }

    public U getSecond()
    {
        return second;
    }

    public V getThird()
    {
        return third;
    }

    @Override
    public String toString()
    {
        return "Triple [first=" + first + ", second=" + second + ", third=" + third + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        result = prime * result + ((third == null) ? 0 : third.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Triple other = (Triple) obj;
        if (first == null)
        {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null)
        {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        if (third == null)
        {
            if (other.third != null)
                return false;
        } else if (!third.equals(other.third))
            return false;
        return true;
    }
}
