package com.rasalhague.mdrv.wirelessadapter;

import java.util.Iterator;

/**
 * The type Round var.
 */
public class RoundVar implements Iterable<Integer>
{
    private int currentValue;
    private int minValue;
    private int maxValue;

    public int getCurrentValue()
    {
        return currentValue;
    }

    /**
     * Instantiates a new Round var.
     *
     * @param minValue
     *         the min value
     * @param maxValue
     *         the max value
     */
    public RoundVar(int minValue, int maxValue)
    {
        this.minValue = minValue;
        this.maxValue = maxValue;

        //-1 coz need fix first out coz ++currentValue instead of currentValue++
        this.currentValue = minValue - 1;
    }

    /**
     * Next value.
     *
     * @return the int
     */
    public synchronized int nextValue()
    {
        return currentValue >= maxValue ? currentValue = minValue : ++currentValue;
    }

    @Override
    public Iterator<Integer> iterator()
    {
        return new Iterator<Integer>()
        {
            @Override
            public boolean hasNext()
            {
                return true;
            }

            @Override
            public Integer next()
            {
                return nextValue();
            }
        };
    }
}
