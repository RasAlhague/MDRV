package com.rasalhague.mdrv.wirelessadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * The type Round var.
 */
public class RoundVar
{
    private int currentValue;
    private int minValue;
    private int maxValue;

    private ArrayList<Integer> arrayToRound = new ArrayList<>();
    private Iterator<Integer> arrayToRoundIterator;

    /**
     * Gets current value.
     *
     * @return the current value
     */
    public int getCurrentValue()
    {
        return currentValue;
    }

    /**
     * Gets min value.
     *
     * @return the min value
     */
    public int getMinValue()
    {
        return minValue;
    }

    /**
     * Gets max value.
     *
     * @return the max value
     */
    public int getMaxValue()
    {
        return maxValue;
    }

    /**
     * Instantiates a new Round var.
     *
     * @param arrayToRound
     *         the array to round
     */
    public RoundVar(ArrayList<Integer> arrayToRound)
    {
        this.arrayToRound = arrayToRound;
        this.arrayToRoundIterator = this.arrayToRound.iterator();

        this.minValue = Collections.max(arrayToRound);
        this.maxValue = Collections.min(arrayToRound);
    }

    /**
     * Next value.
     * <p>
     * If arrayToRoundIterator has not Next() value then update iterator
     *
     * @return the int
     */
    public synchronized int nextValue()
    {
        currentValue = arrayToRoundIterator.next();

        if (!arrayToRoundIterator.hasNext())
        {
            arrayToRoundIterator = arrayToRound.iterator();
        }

        return currentValue;
    }
}
