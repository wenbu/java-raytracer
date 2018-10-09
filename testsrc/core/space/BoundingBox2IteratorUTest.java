package core.space;

import static org.junit.Assert.*;
import org.junit.Test;

import core.math.Point2;

public class BoundingBox2IteratorUTest
{
    // The number of points returned by the iterator should be equal to the value returned by
    // integerArea().
    @Test
    public void testIteratorBounds()
    {
       BoundingBox2 box = new BoundingBox2(new Point2(0, 0), new Point2(10, 10));
       int area = box.integerArea();
       assertEquals(100, area);
       
       int iterCounter = 0;
       for (Point2 p : box)
       {
           iterCounter++;
       }
       assertEquals(area, iterCounter);
    }
    
    @Test
    public void testIteratorBounds2()
    {
       BoundingBox2 box = new BoundingBox2(new Point2(-5, -5), new Point2(5, 5));
       int area = box.integerArea();
       assertEquals(100, area);
       
       int iterCounter = 0;
       for (Point2 p : box)
       {
           iterCounter++;
       }
       assertEquals(area, iterCounter);
    }
    
    @Test
    public void testIteratorBoundsNonInteger()
    {
        BoundingBox2 box = new BoundingBox2(new Point2(0, 0), new Point2(9.9, 9.9));
        int area = box.integerArea();
        assertEquals(100, area);
        
        int iterCounter = 0;
        for (Point2 p : box)
        {
            iterCounter++;
        }
        assertEquals(area, iterCounter);
    }
    
    @Test
    public void testIteratorBoundsNonInteger2()
    {
        BoundingBox2 box = new BoundingBox2(new Point2(0, 0), new Point2(10.01, 10.01));
        int area = box.integerArea();
        assertEquals(121, area);
        
        int iterCounter = 0;
        for (Point2 p : box)
        {
            iterCounter++;
        }
        assertEquals(area, iterCounter);
    }
}
