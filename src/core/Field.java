package core;

// XXX Is this a proper use of the term 'field'?
public interface Field<T extends Field<T>>
{
    T plus(T addend);
    T minus(T subtrahend);
    T times(T multiplicand);
    T times(double scalar);
    T divideBy(T divisor);
    default T divideBy(double scalar) { return times(1.0/scalar); }
    T negative();
}
