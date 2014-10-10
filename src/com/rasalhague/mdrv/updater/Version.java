package com.rasalhague.mdrv.updater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version
{
    private Integer major;
    private Integer minor;

    public Version(String version)
    {
        parseVersion(version);
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public void parseVersion(String version)
    {
        Matcher matcher = Pattern.compile("(?<Major>\\d*)\\.*(?<Minor>\\d*)")
                                 .matcher(version);

        if (matcher.find())
        {
            major = Integer.parseInt(matcher.group("Major"));
            minor = Integer.parseInt(matcher.group("Minor"));
        }
    }

    /**
     * Compare versions.
     *
     * @param version
     *         the version
     *
     * @return 1 - bigger; 0 - equals; -1 - lesser
     */
    public int compare(Version version)
    {
        if (major >= version.getMajor() && minor > version.getMinor()) return 1;

        if (major == version.getMajor() && minor == version.getMinor()) return 0;

        return -1;
    }

    @Override
    public String toString()
    {
        return "Version{" +
                "major=" + major +
                ", minor=" + minor +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (!major.equals(version.major)) return false;
        if (!minor.equals(version.minor)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        return result;
    }
}
