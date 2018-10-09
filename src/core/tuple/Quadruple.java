package core.tuple;

public class Quadruple<T, U, V, W>
{
    private final T first;
    private final U second;
    private final V third;
    private final W fourth;
    
    public Quadruple(T first, U second, V third, W fourth)
    {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
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

    public W getFourth()
    {
        return fourth;
    }

    @Override
    public String toString()
    {
        return "Quadruple [first=" + first + ", second=" + second + ", third=" + third + ", fourth=" + fourth + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((fourth == null) ? 0 : fourth.hashCode());
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
        Quadruple other = (Quadruple) obj;
        if (first == null)
        {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (fourth == null)
        {
            if (other.fourth != null)
                return false;
        } else if (!fourth.equals(other.fourth))
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
