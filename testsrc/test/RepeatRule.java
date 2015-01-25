package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/*
 * from http://www.codeaffine.com/2013/04/10/running-junit-tests-repeatedly-without-loops/
 */
public class RepeatRule implements TestRule
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Repeat
    {
        public abstract int times();
    }
    
    private static class RepeatStatement extends Statement
    {
        private final int times;
        private final Statement statement;
        
        private RepeatStatement(int times, Statement statement)
        {
            this.times = times;
            this.statement = statement;
        }
        
        @Override
        public void evaluate() throws Throwable
        {
            for (int i = 0; i < times; i++)
                statement.evaluate();
        }
    }
    
    @Override
    public Statement apply(Statement base, Description description)
    {
        Statement result = base;
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat != null)
        {
            int times = repeat.times();
            result = new RepeatStatement(times, base);
        }
        return result;
    }

}
