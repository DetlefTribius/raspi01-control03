/**
 * 
 */
package gui;

import java.math.BigDecimal;

/**
 * @author Detlef Tribius
 *
 * Data ist eine Hilfsklasse zur Zusammenfassung
 * aller GUI-relevanten Daten...
 *
 */
public class Data implements Comparable<Data>
{

    /**
     * COUNTER_KEY = "counterKey" - Key zum Zugriff auf die Nummer/den Zaehler...
     */
    public final static String COUNTER_KEY = "counterKey";

    /**
     * PHI_KEY = "phiKey" - Key zum Zugriff auf die Lage, den Winkel (Impulszahl)...
     */
    public static final String PHI_KEY = "phiKey";
    
    /**
     * ROTATION_KEY = "rotationKey" - Key zum Zugriff auf Lage, gemessen in Umdrehungen
     */
    public static final String ROTATION_KEY = "rotationKey";
    
    /**
     * RPM_KEY = "rmpKey" - Key zum Zugriff auf die Drehzahl...
     */
    public static final String RPM_KEY = "rmpKey";
    
    /**
     * CYCLE_TIME_KEY = "cycleTimeKey" - Key zum Zugriff auf die Taktzeit...
     */
    public final static String CYCLE_TIME_KEY = "cycleTimeKey";

    /**
     * 
     */
    public final static String DRV_SET_POINT_KEY = "drvSetPointKey";
    
    /**
     * counter - Zaehler fuer die aktuelle Taktung k...
     */
    private final Long counter;

    /**
     * phi - Lageinformation in Impulse, Mass fuer den Winkel phi
     */
    private final Long phi;
    
    /**
     * rotation - Lageinformation gemessen in Umdrehungen...
     */
    private final BigDecimal rotation;

    /**
     * rpm - BigDecimal Drehzahlangabe, berechnet aus Aenderung phi/Taktzeit.
     */
    private final BigDecimal rpm;
    
    /**
     * cycleTime - aktuelle Taktzeit der Taktung...
     */
    private final BigDecimal cycleTime;
    
    /**
     * drvSetPoint - Sollwert fuer den DRV8830, wird durch den Regler berechnet...
     */
    private final Integer drvSetPoint;
    
    /**
     * Data() - Defaultkonstruktor...
     */
    public Data()
    {
        this(Long.valueOf(0L), Long.valueOf(0L), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Integer.valueOf(0));
    }

    /**
     * Konstruktor Data(...)
     * @param counter
     * @param phi
     * @param rotation
     * @param rpm
     * @param cycleTime
     */
    public Data(Long counter, Long phi, BigDecimal rotation, BigDecimal rpm, BigDecimal cycleTime)
    {
        this.counter = (counter != null)? counter : Long.valueOf(0L);
        this.phi = (phi != null)? phi : Long.valueOf(0L);
        this.rotation = (rotation != null)? rotation : BigDecimal.ZERO;
        this.rpm = (rpm != null)? rpm : BigDecimal.ZERO;
        this.cycleTime = (cycleTime != null)? cycleTime : BigDecimal.ZERO;
        this.drvSetPoint = Integer.valueOf(0);
    }

    
    /**
     * Konstruktor Data(...)
     * @param counter
     * @param phi
     * @param rotation
     * @param rpm
     * @param cycleTime
     */
    public Data(Long counter, Long phi, BigDecimal rotation, BigDecimal rpm, BigDecimal cycleTime, Integer drvSetPoint)
    {
        this.counter = (counter != null)? counter : Long.valueOf(0L);
        this.phi = (phi != null)? phi : Long.valueOf(0L);
        this.rotation = (rotation != null)? rotation : BigDecimal.ZERO;
        this.rpm = (rpm != null)? rpm : BigDecimal.ZERO;
        this.cycleTime = (cycleTime != null)? cycleTime : BigDecimal.ZERO;
        this.drvSetPoint = (drvSetPoint != null)? drvSetPoint : Integer.valueOf(0);
    }

    /**
     * compareTo(Data another) - Vergleich auf Basis der lfd. Nummer (counter)
     */
    @Override
    public int compareTo(Data another)
    {
        return this.counter.compareTo(another.counter);
    }

    /**
     * hashCode() - auf Basis von counter...
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((counter == null) ? 0 : counter.hashCode());
        return result;
    }

    /**
     * equals(Object obj) - auf Basis von counter...
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Data other = (Data) obj;
        if (counter == null)
        {
            if (other.counter != null)
                return false;
        } else if (!counter.equals(other.counter))
            return false;
        return true;
    }
    
    /**
     * @return the counter
     */
    public final Long getCounter()
    {
        return this.counter;
    }

    /**
     * @return the phi
     */
    public final Long getPhi()
    {
        return this.phi;
    }
    
    /**
     * @return the rpm
     */
    public final BigDecimal getRpm()
    {
        return this.rpm;
    }

    /**
     * @return the cycleTime
     */
    public final BigDecimal getCycleTime()
    {
        return this.cycleTime;
    }

    /**
     * 
     * @return the drvSetPoint
     */
    public final Integer getDrvSetPoint()
    {
        return this.drvSetPoint;
    }
    
   /**
     * 
     * @return String[]
     */
    public String[] getKeys()
    {
        return new String[] {COUNTER_KEY, PHI_KEY, ROTATION_KEY, RPM_KEY, CYCLE_TIME_KEY, DRV_SET_POINT_KEY};
    }

    /**
     * getValue(String key) - Bereitstellung der Anzeige...
     * @param key
     * @return string-Anzeige
     */
    public final String getValue(String key)
    {
        if (Data.COUNTER_KEY.equals(key))
        {
            return (this.counter != null)? this.counter.toString() : null;  
        }
        if (Data.PHI_KEY.equals(key))
        {
            return (this.phi != null)? this.phi.toString() : null;  
        }
        if (Data.ROTATION_KEY.equals(key))
        {
            return (this.rotation != null)? this.rotation.toString() : null;
        }
        if (Data.RPM_KEY.equals(key))
        {
            return (this.rpm != null)? this.rpm.toString() : null;
        }
        if (Data.CYCLE_TIME_KEY.equals(key))
        {
            return (this.cycleTime != null)? this.cycleTime.toString() : null;   
        }
        if (Data.DRV_SET_POINT_KEY.equals(key))
        {
            return (this.drvSetPoint != null)? this.drvSetPoint.toString() : null;
        }
        return null;
    }    
    
    
    
    /**
     * toString() - zu Protokollzwecken...
     */
    @Override
    public String toString()
    {
        return new StringBuilder().append("[")
                                  .append(this.counter)
                                  .append(" ")
                                  .append(this.phi)
                                  .append(" ")
                                  .append(this.rpm)
                                  .append(" ")
                                  .append(this.cycleTime)
                                  .append(" ")
                                  .append(this.drvSetPoint)
                                  .append("]")
                                  .toString();
    }

    
}
