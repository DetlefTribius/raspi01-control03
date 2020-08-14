/**
 * 
 */
package gui;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Detlef Tribius
 * <p>
 * 
 * 
 * </p>
 */
public class PositionController
{
    
    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(PositionController.class);
            
    /**
     * Darstellung der Taktzeit...
     */
    public static int SCALE_CYCLE_TIME = 3;
    
    /**
     * Zeitpunkt der aktuellen Beauftragung...
     */
    private Instant now = null;
    
    /**
     * Zeitpunkt der letzten Beauftragung...
     */
    private Instant past = null;
    
    /**
     * 
     */
    private BigDecimal cycleTimeDecimal = BigDecimal.ZERO;    
    
    /**
     * Begrenzung der Stellgroesse, als final eingefuehrt!!
     */
    private final int maxDrvSetPoint;
    
    /**
     * enhancement - Reglerverstaerkung...
     * <p>
     * Die Reglerverstaerkung wird durch die Oberflaeche geaendert.
     * </p>
     */
    private BigDecimal enhancement;
    
    /**
     * integralEnhancement - Verstaerkung zum Integral-Anteil
     * <p>
     * Die Verstaerkung wird durch die Oberflaeche geaendert.
     * </p>
     */
    private BigDecimal integralEnhancement;
    
    /**
     * isAntiWindUp - Schalter fuer die Modifikation des Reglerverhaltens...
     */
    private boolean isAntiWindUp = true;    
    
    /**
     * Summation (Integration) der Regelabweichungen... 
     */
    private long integralDelta;
    
    /**
     * SIZE_MEMORY - Umfang des Gedaechtnisses (Groesses des Array delta[])
     */
    public static int SIZE_MEMORY = 3;  
    
    /**
     * Array mit den Regelabweichungen...
     */
    private long delta[] = new long[SIZE_MEMORY];
    
    /**
     * outPut - Ergebnis der Regelberechnung, wird als Stellgroesse ausgegeben...
     */
    private long outPut = 0;
    
    /**
     * 
     * @param maxDrvSetPoint
     */
    PositionController(int maxDrvSetPoint)
    {
        this(maxDrvSetPoint, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    /**
     * 
     * @param maxDrvSetPoint
     * @param enhancement
     */
    PositionController(int maxDrvSetPoint, BigDecimal enhancement)
    {
        this(maxDrvSetPoint, enhancement, BigDecimal.ZERO);
    }
    
    /**
     * 
     * @param maxDrvSetPoint
     * 
     */
    PositionController(int maxDrvSetPoint, BigDecimal enhancement, BigDecimal integralEnhancement)
    {
        this.enhancement = (enhancement != null)? enhancement : BigDecimal.ZERO;
        this.integralEnhancement = (integralEnhancement != null)? integralEnhancement : BigDecimal.ZERO;
        // I-Anteil (ausgedrueckt in Impulse)...
        this.integralDelta = 0L;
        this.maxDrvSetPoint = (maxDrvSetPoint > 0)? maxDrvSetPoint : 0;
        for (int index = 0; index < this.delta.length; index++)
        {
            this.delta[index] = 0L;
        }
    }
    
    /**
     * 
     * @param enhancement
     */
    public void setEnhancement(BigDecimal enhancement)
    {
        this.enhancement = (enhancement != null)? enhancement : BigDecimal.ZERO;
    }
    
    /**
     * 
     * @return
     */
    public BigDecimal getEnhancement()
    {
        return this.enhancement;
    }

    /**
     * 
     * @param integralEnhancement
     */
    public void setIntegralEnhancement(BigDecimal integralEnhancement)
    {
        this.integralEnhancement = (integralEnhancement != null)? integralEnhancement : BigDecimal.ZERO;
    }
    
    /**
     * 
     * @return
     */
    public BigDecimal getIntegralEnhancement()
    {
        return this.integralEnhancement;
    }

    /**
     * @return the isAntiWindUp
     */
    public final boolean isAntiWindUp()
    {
        return this.isAntiWindUp;
    }

    /**
     * @param isAntiWindUp the isAntiWindUp to set
     */
    public final void setAntiWindUp(boolean isAntiWindUp)
    {
        this.isAntiWindUp = isAntiWindUp;
    }

    /**
     * getValue()...
     * @param cycleTimeDecimal
     * @param phiSetPoint - Lage Sollwert
     * @param phi - Lage Istwert
     * @return
     */
    public int getValue(BigDecimal cycleTimeDecimal, 
                        long phiSetPoint, 
                        long phi)
    {
        
        logger.info( "getValue() Takt [s]: " + cycleTimeDecimal + ", Lage: " + phiSetPoint + " (Soll) " + phi + " (Ist)");
        
        // Differenz (Soll-Ist) bestimmen... 
        final long delta = phiSetPoint - phi;
        
        for (int index = 1; index < this.delta.length; index++)
        {
            this.delta[this.delta.length-index] = this.delta[this.delta.length-index-1];
        }
        this.delta[0] = delta;
        // Jetzt im Array this.delta die Regeldifferenzen der letzten Beauftragungen...
        
        // Den Zeitpunkt der letzten Beauftragung merken...
        this.past = now;
        // Den aktuellen Zeitpunkt merken...
        // now zur zeitlichen Einordnung des Ereignisses.
        this.now = Instant.now();
        
        // cycleTimeDecimal - momentane Taktzeit (cycleTime) in Sekunden,
        // wird 'ausserhalb' bestimmt und hier mitgegeben...
        this.cycleTimeDecimal = (cycleTimeDecimal != null)? cycleTimeDecimal : BigDecimal.ZERO;
      
        //////////////////////////////////////////////////////////////////////////////////////////
        // Zwei Moeglichkeiten:
        // Verstaerkungen: 
        // - this.enhancement: Gesamtverstaerkung Verstaerkung fuer den P-Anteil (nur delta + Integralanteil)
        // - this.integralEnhancement: Verstaerkung nur fuer den I-Anteil (Summation ueber delta) 
        //
        // 1.) 
        // 2.) 
        
        
        // delta ist die Regelabweichung delta = (Lage-Soll)-(Lage-Ist)...
        // Jetzt Berechnung nur Integralanteil als BigDecimal aus integralEnhancement * (delta + this.integralDelta)...
        final BigDecimal bigDecimalIntegral = this.integralEnhancement.multiply(BigDecimal.valueOf(delta + this.integralDelta));
        // => bigDecimalIntegral beinhaltet den Integralanteil aus (delta + Summe(delta)) * this.integralEnhancement.
        // Es fehlt fuer den naechsten Aufruf noch die Summation delta zu Summe(delta).

        // Output aus dem P-Anteil (delta)... 
        final BigDecimal bigDecimalProportional = this.enhancement.multiply(BigDecimal.valueOf(delta));

        label:
        if (isAntiWindUp())
        {
            {
                // Achtung, lokale Var. outPut: Moeglicher Verlust an relevanten Stellen...
                final long outPut = bigDecimalProportional.longValue();
                final int sign = (outPut >= 0)? 1 : -1;
                if (Math.abs(outPut) > Math.abs(maxDrvSetPoint))
                {
                    logger.info("isAntiWindUp: Begrenzung, bigDecimalOutput (nur P-Anteil) = " + outPut); 
                    // maxDrvSetPoint wurde nur mit dem P-Anteil ueberschritten... 
                    this.outPut = sign * Math.abs(maxDrvSetPoint);
                    // Ausgabe in der Begrenzung, dann auch keine Integration der Lageabweichung...
                    break label;
                }
            }
            {
                // Achtung, lokale Var. outPut: Moeglicher Verlust an relevanten Stellen...
                final long outPut = bigDecimalProportional.longValue()              // P-Anteil
                                  + bigDecimalIntegral.longValue();                 // I-Anteil
                final int sign = (outPut >= 0)? 1 : -1;
                if (Math.abs(outPut) > Math.abs(maxDrvSetPoint))
                {
                    logger.info("isAntiWindUp: Begrenzung, bigDecimalOutput (PI-Anteil) = " + outPut); 
                    // maxDrvSetPoint wurde nur mit dem P-Anteil ueberschritten... 
                    this.outPut = sign * Math.abs(maxDrvSetPoint);
                    // Ausgabe in der Begrenzung, dann auch keine Integration...
                    break label;
                }
                logger.info("isAntiWindUp: Keine Begrenzung, bigDecimalOutput (PI-Anteil) = " + outPut); 
                this.integralDelta += delta;
                this.outPut = outPut;
                break label;
            }
        } // end() isAntiWindUp().
        else
        {
            // KEIN AntiWindUp()...
            final long outPut = bigDecimalProportional.longValue()              // P-Anteil
                              + bigDecimalIntegral.longValue();                 // I-Anteil
             
            final int sign = (outPut >= 0)? 1 : -1;

            // Begrenzung der Ausgabe?
            if (Math.abs(outPut) > Math.abs(maxDrvSetPoint))
            {
                // maxDrvSetPoint wurde ueberschritten, 
                this.outPut = sign * Math.abs(maxDrvSetPoint);
            }
            else
            {
                this.outPut = outPut;
            }
            // Aber immer Integration der Lageabweichung, da KEIN AntiWindUp()!
            this.integralDelta += delta;
            
            logger.info("Kein AntiWindUp: bigDecimalOutput (PI-Anteil) = " + outPut); 
            
            break label;
        } // end() KEIN AntiWindUp().
        
        return (int) this.outPut;
    }
    
    /**
     * resetData() - Zuruecksetzen des Gedaechtnisses...
     */
    public void resetData()
    {
        logger.info( "resetData()...");
        for (int index = 0; index < this.delta.length; index++)
        {
            this.delta[index] = 0;
        }
        this.integralDelta = 0;
        this.outPut = 0;
    }

}
