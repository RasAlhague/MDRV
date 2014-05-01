package com.rasalhague.mdrv.wirelessadapter;

/**
 * The type Round var.
 */
public class RoundVar
{
    private int currentValue;
    private int minValue;
    private int maxValue;

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

        this.currentValue = minValue;
    }

    /**
     * Next value.
     *
     * @return the int
     */
    public synchronized int nextValue()
    {
        return currentValue > maxValue ? currentValue = minValue : currentValue++;
    }
}
