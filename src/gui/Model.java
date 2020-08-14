package gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import raspi.hardware.i2c.DRV8830;

// Vgl. https://www.baeldung.com/java-observer-pattern
// auch https://wiki.swechsler.de/doku.php?id=java:allgemein:mvc-beispiel
// http://www.nullpointer.at/2011/02/06/howto-gui-mit-swing-teil-4-interaktion-mit-der-gui/
// http://www.javaquizplayer.com/blogposts/java-propertychangelistener-as-observer-19.html
// TableModel...
// Vgl.: https://examples.javacodegeeks.com/core-java/java-swing-mvc-example/
/**
 * 
 * Das Model haelt die Zustandsgroessen..
 *
 * 
 * 
 */
public class Model 
{
    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(Model.class);

    /**
     * Status status
     */
    private Status status = Status.Reset;
    
    /**
     * Kennung isRaspi kennzeichnet, der Lauf erfolgt auf dem RasberryPi.
     * Die Kennung wird zur Laufzeit aus den Systemvariablen fuer das
     * Betriebssystem und die Architektur ermittelt. Mit dieser Kennung kann
     * die Beauftragung von Raspi-internen Programmen gesteuert werden.
     */
    private final boolean isRaspi;
    /**
     * OS_NAME_RASPI = "linux" - Kennung fuer Linux.
     * <p>
     * ...wird verwendet, um einen Raspi zu erkennen...
     * </p>
     */
    public final static String OS_NAME_RASPI = "linux";
    /**
     * OS_ARCH_RASPI = "arm" - Kennung fuer die ARM-Architektur.
     * <p>
     * ...wird verwendet, um einen Raspi zu erkennen...
     * </p>
     */
    public final static String OS_ARCH_RASPI = "arm";
    
    /**
     * Referenz auf den GPIO-controller...
     * <p>
     * Der GPIO-Controller bedient die GPIO-Schnittstelle des Raspi.
     * </p>
     * <p>
     * Der GPIO-Controller wird im Konstruktor instanziiert...
     * </p>
     */
    private final GpioController gpioController;

    /**
     * ADDRESS - Bus-Adresse des I2C-Bausteins, festgelegt durch
     * Verdrahtung auf dem Baustein...
     * <p> 
     * Der DRV8830-Baustein ist im Standard auf 0x60 adressiert.
     * </p>
     */
    public final static int ADDRESS = 0x60; 

    /**
     * drv8830 - Referenz auf den DRV8830-Baustein unter der Adresse ADDRESS
     */
    private DRV8830 drv8830 = null;
    
    /**
     * drvSetPoint - Sollwert fuer den DRV8830 (int)
     * <p>
     * Der Sollwert drvSetPoint wird direkt auf den DRV8830 gegeben. Die Drehzahl
     * ist in etwa proportional zum Sollwert. Der Bereich liegt von 0...63, dabei
     * gibt es eine 'Totzone' von 0...5, also erst ab drvSetPoint = 6 beginnt sich
     * der Motor zu drehen.  
     * </p>
     */
    private int drvSetPoint = 0;

    /**
     * Pull-Up/Pull-Down-Einstellung...
     * <p>
     * Hier Voreinstellung auf PinPullResistance.OFF, da Pull-Down-Widerstaende 
     * durch die Hardware bereitgestellt werden...
     * </p>
     * <p>
     * Hier Einstellung: Kein Pull-Down/Pull-Up durch den Raspi (daher PinPullResistance.OFF)...
     * </p>
     */
    private final static PinPullResistance PIN_PULL_RESISTANCE = PinPullResistance.OFF;
    
    /**
     * ...der folgenden Pin wird über den Takt des Ne555 angesprochen und gibt damit den Takt
     * fuer die Regelung und Anzeige vor...
     * <p>
     * Der Name des Pins wird als Key fuer die Ablage der damit im Zusammenhang
     * stehenden Daten verwendet (Key: GPIO_PIN_NE555_NAME)
     * </p>
     * <p>
     * Der am Pin auftretende Impuls wird durch das Interface GpioPinListenerDigital 
     * verarbeitet.
     * </p>
     */
    private final static Pin GPIO_NE555_PIN = RaspiPin.GPIO_00;    // GPIO 17, Board-Nr. = 11
    
    /**
     * Impulsfolge A..., wird durch einen entsprechenden Interrupt bedient.
     * <p>
     * Die Verarbeitung der Impulsfolge erfolgt durch das Interface
     * GpioPinListenerDigital.
     * </p>
     */
    private final static Pin GPIO_INC_A_PIN = RaspiPin.GPIO_02;     // GPIO 27, Board-Nr. = 13
    
    /**
     * Impulsfolge B..., wird aber mit dem Impuls A in der Interrupt-Routine abgefragt...
     * <p>
     * Die Verarbeitung der Impulsfolge GPIO_INC_B_PIN erfolgt durch Abfrage des 
     * Zustandes ueber GpioPinDigitalInput.
     * </p>
     */
    private final static Pin GPIO_INC_B_PIN = RaspiPin.GPIO_03;     // GPIO 22, Board-Nr. = 15
    
    /**
     * gpio_Inc_B_Pin - Referenz auf den Zustand des Pin GPIO_INC_B_PIN
     */
    private final GpioPinDigitalInput gpio_Inc_B_Pin; 
    
    /**
     * GPIO_NE555_PIN_NAME - String-Name des Takt-Pins an dem der Ne555
     * angeschlossen ist.
     */
    public final static String GPIO_NE555_PIN_NAME = GPIO_NE555_PIN.getName();
    
    /**
     * GPIO_INC_A_PIN_NAME - String-Name des Pin A...
     */
    public final static String GPIO_INC_A_PIN_NAME = GPIO_INC_A_PIN.getName();
    
    /**
     * GPIO_INC_B_PIN_NAME - String-Name des Pin B...
     */
    public final static String GPIO_INC_B_PIN_NAME = GPIO_INC_B_PIN.getName();
    
    /**
     * NAME_RESET_BUTTON = "resetButton"
     */
    public static final String NAME_RESET_BUTTON = "resetButton";
    
    /**
     * NAME_START_BUTTON = "startButton"
     */
    public static final String NAME_START_BUTTON = "startButton";

    /**
     * NAME_STOP_BUTTON = "stopButton"
     */
    public static final String NAME_STOP_BUTTON = "stopButton";
    
    /**
     * NAME_END_BUTTON = "endButton"
     * <p>
     * Der "endButton" fuehr zur Beendigung der Applikation.
     * </p>
     */
    public static final String NAME_END_BUTTON = "endButton";
    
    /**
     * dataMap - nimmt die Eingaben der GUI auf...
     * <p>
     * Ablage key => Eingabe-Object
     * </p>
     */
    private final java.util.TreeMap<String, Object>  dataMap = new java.util.TreeMap<>();

    /**
     * Unter dem DATA_KEY werden Anzeigewerte fuer die Oberflaeche zusammengefasst.
     * Mit jedem Takt werden diese Anzeigewerte fuer die GUI bereitgestellt.
     */
    public final static String DATA_KEY = "dataKey"; 
    
    /**
     * DATA_DESTINATION_KEY = "dataDestinationKey - Key zum Zugriff auf den Sollwert der Zielgroesse (Lage)
     * <p>
     * Die Zielgroesse fuer die Lage wird an der Oberflaeche als Anzahl Umdrehungen angegeben.
     * Die Eingabewerte werden in das Model uebertragen und finden sich 
     * </p>
     */
    public final static String DATA_DESTINATION_KEY = "dataDestinationKey";
    
    /**
     * DATA_ENHANCEMENT_KEY - Key zum Zugriff auf den Wert der Regelverstarkung
     * (P-Anteil der Lageregelung).
     * <p>
     * DATA_ENHANCEMENT_KEY => 
     * </p>
     */
    public final static String DATA_ENHANCEMENT_KEY = "dataEnhancementKey";
    
    /**
     * DATA_INTEGRAL_ENHANCEMENT_KEY - Key zum Zugriff auf den Wert der Verstaerkung des
     * Integralanteils (I-Anteil der Lageregelung).
     * <p>
     * DATA_INTEGRAL_ENHANCEMENT_KEY => 
     * </p>
     */
    public final static String DATA_INTEGRAL_ENHANCEMENT_KEY = "dataIntegralEnhancementKey";
    
    /**
     * 
     */
    public final static String DATA_ANTI_WINDUP_KEY = "dataAntiWindupKey";
    
    /**
     * DATA_KEYS[] - Array mit den Keys zur Ablage in der dataMap...
     * <p>
     * DATA_KEYS muss alle Keys aufnehmen, auf die reagiert werden soll.
     * Falls ein Key fehlt, wird eine entsprechende Aenderung ignoriert. 
     * </p>
     */
    private final static String[] DATA_KEYS = 
    {
        DATA_KEY,
        DATA_DESTINATION_KEY,
        DATA_ENHANCEMENT_KEY,
        DATA_INTEGRAL_ENHANCEMENT_KEY,
        DATA_ANTI_WINDUP_KEY
    };

    /**
     * MAX_VALUE - max. Sollwert des PWM-Schaltkreises (hier nur der Betrag!)...
     * <p>
     * Auswaehlbar sind Werte von (-MAX_VALUE... 0 ...+MAX_VALUE)
     * </p>
     */
    private final static int MAX_VALUE = 15;     
    
    /**
     * SET_POINT_SCALE = 5
     */
    public final static int SET_POINT_SCALE = 5;
    
    /**
     * Genauigkeit (Anzahl der Nachkommastellen) in der Verstaerkungsangabe
     */
    public final static int  ENHANCEMENT_SCALE = 4;
    
    /**
     * ENHANCEMENTS - Array mit den Verstaerkungswerten des Reglers (P-Anteil) 
     * zur Auswahl in der Combobox...
     */
    public final static BigDecimal[] ENHANCEMENTS = new BigDecimal[]
    {
        BigDecimal.valueOf(0.0).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.01).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.05).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.1).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.2).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.3).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.5).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(1.0).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(1.0).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP)
    };
    
    /**
     * Index zur Auswahl der Selektion...
     */
    public final static int SELECTED_ENHANCEMENTS_INDEX = 0;
    
    /**
     * INTEGRAL_ENHANCEMENTS - Array mit den Verstaerkungswerten des Reglers (I-Anteil) 
     * zur Auswahl in der Combobox...
     */
    public final static BigDecimal[] INTEGRAL_ENHANCEMENTS = new BigDecimal[]
    {
        BigDecimal.valueOf(0.0000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0001).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0002).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0005).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0008).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0010).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0020).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0050).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0080).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0100).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0200).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0500).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.0800).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.1000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.2000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.5000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(0.8000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(1.0000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(2.0000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
        BigDecimal.valueOf(5.0000).setScale(ENHANCEMENT_SCALE, BigDecimal.ROUND_HALF_UP),
    };
    
    /**
     * Index zur Auswahl der Selektion...
     */
    public final static int SELECTED_INTEGRAL_ENHANCEMENTS_INDEX = 0;

    /**
     * FORMATTED_TEXT_FIELD_PATTERN = "#0.00" - Formatstring fuer das JFormattedTextField...
     */
    public final static String FORMATTED_TEXT_FIELD_PATTERN = "#0.000";
    
    /**
     * support - Referenz auf den PropertyChangeSupport...
     */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    /**
     * counter - Taktzaehler (keine weitere funktionale Bedeutung)
     */
    private long counter = 0L;
    
    /**
     * phi - Lageinformation (Istwert) in Impulse, 
     * Mass fuer den Winkel phi, gemessen in Anzahl der Impule...
     * <p>
     * <code>phi</code> ist der Istwert der Lage.
     * </p>
     */
    private long phi = 0L;

    /**
     * phiSetPoint - Sollwert fuer die Lage (Sollwert) in Impulse
     * <p>
     * <code>phiSetPoint</code> ergibt sich aus
     * phiSetPoint = CIRCUMFERENCE * destination
     * </p>
     */
    private long phiSetPoint = 0L;
    /**
     * 
     */
    private long[] position = new long[] {0L, 0L};
    
    /**
     * Instant past - letzter Zeitstempel...
     * <p>
     * Der Takt wird durch den Ne555 vorgegeben. 
     * Hier wird der letzte Zeitstempel abgelegt zur Bestimmung
     * der Taktdauer T. Die Taktdauer wird in cycleTime abgelegt.
     * </p>
     * <p>
     * Der Anfangswert muss null sein, um die Erstbeauftragung zu erkennen,
     * da erst bei Zweitbeauftragung die Taktdauer bestimmbar ist.
     * </p>
     */
    private Instant past = null;
    
    /**
     * cycleTime - aktuell ermittelte Taktzeit aus (now - past)...
     */
    private Duration cycleTime = Duration.ZERO;

    /**
     * 
     */
    public final static int SCALE_INTERN = 6;
    
    /**
     * 
     */
    public final static int SCALE_RPM = 3;
    
    
    /**
     * Darstellung der Taktzeit...
     */
    public static int SCALE_CYCLE_TIME = 3;
    
    /**
     * CIRCUMFERENCE - Anzahl der Impulse pro Umdrehung
     * 
     * Aus der Anzahl der Impulse I pro Zeiteinheit T ergibt sich die
     * Umdrehungszahl U pro Minute zu:
     * 
     *   U = I * 1/CIRCUMFERENCE * 60/T
     *   U = (I/T) * (60/CIRCUMFERENCE) 
     */
    public final static BigDecimal CIRCUMFERENCE = BigDecimal.valueOf(400L);
    
    /**
     * CONST
     */
    public final static BigDecimal CONST = BigDecimal.valueOf(60L).divide(CIRCUMFERENCE, SCALE_INTERN, BigDecimal.ROUND_HALF_UP);

    
    /**
     * 
     */
    private BigDecimal cycleTimeDecimal = BigDecimal.ZERO;    
    
    /**
     * enhancement - Reglerverstaerung fuer den P-Anteil...
     */
    private BigDecimal enhancement = BigDecimal.ZERO;
    
    /**
     * integralEnhancement - Verstaerkung fuer den I-Anteil...
     */
    private BigDecimal integralEnhancement = BigDecimal.ZERO;   
    
    /**
     * 
     */
    private boolean isAntiWindUp = true;
    
    /**
     * destination - Sollwert der Lage, Angabe in Umdrehungen
     * <p>
     * Der Wert <code>destination</code> wird an der Oberflaeche eingegeben
     * und wird in Anzahl Umdrehungen bemessen. Aus dem Wert <code>destination</code>
     * wird die Sollzahl der Impulse ermittelt. Dazu gibt es die Konstante 
     * <code>CIRCUMFERENCE</code>  (Anzahl der Impulse pro Umdrehung). 
     * </p>
     * <p>
     * phiSetPoint = CIRCUMFERENCE * destination
     * </p>
     * <p>
     * Die Vergleichsgroesse zu destination (Sollwert) ist rotation (Istwert).
     * </p>
     */
    private BigDecimal destination = null;
    
    /**
     * 
     */
    private BigDecimal rotation = null;
    
    /**
     * 
     */
    private BigDecimal rpm = null;
    
    /**
     * lock - Object fuer das Synchronisieren...
     */
    final private Object lock = new Object(); 
    
    /**
     * 
     */
    final private PositionController positionController = new PositionController(MAX_VALUE, enhancement, integralEnhancement);
    
    /**
     * Default-Konstruktor 
     */
    public Model() 
    {
        // 1.) Wo erfolgt der Lauf, auf einem Raspi?
        final String os_name = System.getProperty("os.name").toLowerCase();
        final String os_arch = System.getProperty("os.arch").toLowerCase();
        logger.debug("Betriebssytem: " + os_name + " " + os_arch);
        // Kennung isRaspi setzen...
        this.isRaspi = OS_NAME_RASPI.equals(os_name) && OS_ARCH_RASPI.equals(os_arch);
        
        // *** Befuellen der dataMap... ***
        // Die dataMap muss mit allen Key-Eintraegen befuellt werden, sonst 
        // ist setProperty(String key, Object newValue) unwirksam!
        for (String key: Model.DATA_KEYS)
        {
            this.dataMap.put(key, null);
        }
        
        // ...den gpioController anlegen...
        this.gpioController = isRaspi? GpioFactory.getInstance() : null;

        this.gpio_Inc_B_Pin = (this.gpioController != null)? this.gpioController.provisionDigitalInputPin(GPIO_INC_B_PIN, PIN_PULL_RESISTANCE) : null;

        ///////////////////////////////////////////////////////////////////////////////////////////
        // Alles weitere nur, wenn der Lauf auf dem Raspi erfolgt...
        if (this.isRaspi)
        {
            ///////////////////////////////////////////////////////////////////////////////////////
            // Den Listener anlegen...
            final GpioPinListenerDigital listener  = new GpioPinListenerDigital() 
            {
                /**
                 * handleGpioPinDigitalStateChangeEvent() - Reaktion auf
                 * 
                 */
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
                {
                    final GpioPin gpioPin = event.getPin();
                    final String pinName = gpioPin.getName();
                    final PinEdge pinEdge = event.getEdge();
                    // Die steigende Flanke A wird jeweils zur Messung ausgewertet.
                    // Die Drehrichtung ergibt sich dabei daras, ob B bereits High
                    // ist oder noch Low.
                    // Dieses Vorgehen setzt voraus, dass bei Auftritt der Flanke
                    // der Zusatnd der anderen Impulsfolge bekannt ist, dazu dienen
                    // die Zustandsgroessen is_A_High bzw. is_B_High.
                    // Anm.: Die Impulsfolgen A und B sind auch vertauschbar.
                    pinLabel:
                    synchronized (lock)
                    {
                        // Wenn Flanke UND INC_A, dann...
                        if ((PinEdge.RISING == pinEdge) && GPIO_INC_A_PIN_NAME.equals(pinName))
                        {
                            // Zustand von Impuls B...
                            boolean is_B_High = (Model.this.gpio_Inc_B_Pin != null)? Model.this.gpio_Inc_B_Pin.isHigh() : true;
                            
                            //////////////////////////////////////////////////////////////
                            // Hier: Steigende Flanke und Impuls A...
                            //////////////////////////////////////////////////////////////
                            // Hier erfolgt das Zaehlen der Impulse in Model.this.phi 
                            // in Abhaengigkeit davon, welchen Zustand der Impuls B 
                            // angenommen hat...
                            Model.this.phi += (is_B_High)? -1L : +1L;
                            
                            break pinLabel;
                        }
                        
                        if ((PinEdge.RISING == pinEdge) && GPIO_NE555_PIN_NAME.equals(pinName))
                        {
                            ///////////////////////////////////////////////////////////////////
                            // Die Taktung hat einen Referenzpunkt erreicht.
                            ///////////////////////////////////////////////////////////////////
                            
                            // Den Status bestimmen: isStarted oder nicht...
                            final boolean isStarted = Model.this.status.equals(Status.Start);
                            
                            Model.this.counter++;
                            
                            // Reglerausgang bestimmen...
                            final int outPut = getPositioncontroller().getValue(Model.this.cycleTimeDecimal, 
                                                                                Model.this.phiSetPoint, 
                                                                                Model.this.phi);
                            
                            Model.this.drvSetPoint = isStarted? outPut : 0;
                            
                            // now zur zeitlichen Einordnung des Ereignisses...
                            // Jetzt werden die Kenngroesse der Taktung ermittelt:
                            // - now: der jetzige Zeitpunkt, die Zeitdauer ergibt sich
                            //        durch Differenzbildung zu Model.this.past...
                            // now wird im Verlauf im Zustand Model.this.past abgelegt. 
                            final Instant now = Instant.now();
                            // Model.this.past: Zeitpunkt der letzten Taktung...
                            if (Model.this.past == null)
                            {
                                // Erste Beauftragung: Model.this.past = null...
                                Model.this.past = now;
                            }
                            // Model.this.cycleTime: Taktzeit aus der Differenz now - past.
                            // Ablage der aktuell gemessenen Taktzeit in der Zustandsgroesse cycleTime...
                            Model.this.cycleTime = Duration.between(Model.this.past, now);
                          
                            // Bestimmung des Anzeigewertes von Model.this.cycleTime in Sekunden...
                            // cycleTimeDecimal - momentane Taktzeit (cycleTime) in Sekunden...
                            Model.this.cycleTimeDecimal = toBigDecimalSeconds(Model.this.cycleTime, SCALE_CYCLE_TIME);
                          
                            //////////////////////////////////////////////////////////////////////////
                            // ...und Ablage der aktuelle ermittelten Taktzeit...
                            Model.this.past = now;
                            //////////////////////////////////////////////////////////////////////////
                         
                            // Das Array position[] dient der Ermittlung des Zuwachses der Position
                            // waehrend der letzten Taktung: phi[k+1]-phi[k].
                            Model.this.position[1] = Model.this.position[0];
                            Model.this.position[0] = Model.this.phi;
                          
                            Model.this.rotation = BigDecimal.valueOf(Model.this.phi).divide(CIRCUMFERENCE, SCALE_RPM, BigDecimal.ROUND_HALF_UP);
                          
                            // increment: Zuwachs an Impulsen als BigDecimal
                            final BigDecimal increment = BigDecimal.valueOf(Model.this.position[0] - Model.this.position[1]);
                          
                            Model.this.rpm = (BigDecimal.ZERO.compareTo(cycleTimeDecimal) != 0)? (increment.divide(cycleTimeDecimal, SCALE_INTERN, BigDecimal.ROUND_HALF_UP).multiply(Model.CONST).setScale(SCALE_RPM, BigDecimal.ROUND_HALF_UP)) : BigDecimal.ZERO;
                          
                            Model.this.rpm = (Model.this.rpm.abs().compareTo(BigDecimal.ONE.movePointLeft(2)) < 0)? BigDecimal.ZERO : Model.this.rpm; 

                            
                            if (Model.this.dataMap.containsKey(DATA_KEY))
                            {
                                // Die dataMap haelt die Daten zur Anzeige in der View...
                                final Object oldValue = Model.this.dataMap.get(DATA_KEY);
                                final Data oldData = (oldValue instanceof Data)? (Data) oldValue : new Data();
                              
                                // Model.this.counter: fortlaufender Zaehler...
                                final Data newData = new Data(Long.valueOf(Model.this.counter),
                                                              Long.valueOf(Model.this.phi),
                                                              Model.this.rotation,
                                                              Model.this.rpm,
                                                              Model.this.cycleTimeDecimal,
                                                              Model.this.drvSetPoint);
                                setProperty(DATA_KEY, newData);
                            }
                          
                            logger.debug(now + ": Taktzeit=" + Model.this.cycleTime + ", phi=" + Model.this.phi);
                            
                            if (isStarted)
                            {
                                if (Model.this.drv8830 == null)
                                {
                                    logger.error("Zugriff auf DRV8830?");
                                    break pinLabel;
                                }
                                
                                try
                                {
                                    
                                    int fault = Model.this.drv8830.getFault(); 
                                    // Bei fault == 0 => Fehlerfrei, sonst Fehler!
                                    if (fault != 0)
                                    {
                                        final DRV8830.Fault error = DRV8830.Fault.getFault(fault);
                                        logger.error("DRV8830-Fehler: " + error.getReason());
                                    }
                                    Model.this.drv8830.drive(Model.this.drvSetPoint);
                                } 
                                catch (IOException exception)
                                {
                                    logger.error("drive():", exception);
                                }
                            } //
                        }
                    } // 
                }
        
                /**
                 * toBigDecimalSeconds(Duration duration) - liefert die Anzahl der Sekunden
                 * <p>
                 * Vgl. toBigDecimalSeconds() aus Duration in Java 11.
                 * </p>
                 * @param duration
                 * @return
                 */
                private BigDecimal toBigDecimalSeconds(Duration duration, int scale)
                {
                    Objects.requireNonNull(duration, "duration must not be null!");
                    final BigDecimal result = BigDecimal.valueOf(duration.getSeconds()).add(BigDecimal.valueOf(duration.getNano(), 9)).setScale(scale,  BigDecimal.ROUND_HALF_UP);
                    return (result.compareTo(BigDecimal.ONE.movePointLeft(scale)) < 0)? BigDecimal.ZERO : result;   
                }
            };
            
            
            GpioPinDigitalInput[] gpioPins = new GpioPinDigitalInput[]
            {
                this.gpioController.provisionDigitalInputPin(GPIO_NE555_PIN, GPIO_NE555_PIN_NAME, Model.PIN_PULL_RESISTANCE),                    
                this.gpioController.provisionDigitalInputPin(GPIO_INC_A_PIN, GPIO_INC_A_PIN_NAME, Model.PIN_PULL_RESISTANCE)
            };

            this.gpioController.addListener(listener, gpioPins);            
            
            ///////////////////////////////////////////////////////////////////////////////////////
            
            this.dataMap.put(DATA_KEY, new Data());
            logger.debug(DATA_KEY + " in dataMap aufgenommen.");
            
            ///////////////////////////////////////////////////////////////////////////////////////
            // Die I2C-Schnittstelle einrichten...
            try
            {
                final I2CBus i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
                this.drv8830 = new DRV8830(i2cBus.getDevice(ADDRESS));
                int fault = this.drv8830.getFault(); 
                logger.info("drv8830 liefert mit getFault() die Kennung: " + fault);                  
            } 
            catch (Throwable exception)
            {
                logger.error("I2CFactory.getInstance()", exception);
                System.exit(0);
            }
            ///////////////////////////////////////////////////////////////////////////////////////
            
        } // end if(this.isRaspi).
        else
        {
            this.dataMap.put(DATA_KEY, null);
            logger.debug(DATA_KEY + " in dataMap mit value=null aufgenommen.");
        }
    }
     
    /**
     * 
     * @return
     */
    public PositionController getPositioncontroller()
    {
        return this.positionController;
    }
    
    
    /**
     * 
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * 
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.support.removePropertyChangeListener(listener);
    }

    /**
     * setProperty(String key, Object newValue) - Die View wird informiert...
     * 
     * @param key
     * @param newValue
     */
    public void setProperty(String key, Object newValue)
    {
        if (this.dataMap.containsKey(key))
        {
            Object oldValue = this.dataMap.get(key);
            
            this.dataMap.put(key, newValue);
            
            if (Model.DATA_ENHANCEMENT_KEY.equals(key))
            {
                if (newValue instanceof BigDecimal)
                {
                    this.enhancement = (BigDecimal) newValue;
                    
                    getPositioncontroller().setEnhancement(this.enhancement);
                    
                    logger.debug("enhancement: " + getPositioncontroller().getEnhancement().toString());                    
                }
            }
            
            if (Model.DATA_INTEGRAL_ENHANCEMENT_KEY.equals(key))
            {
                if (newValue instanceof BigDecimal)
                {
                    this.integralEnhancement = (BigDecimal) newValue;
                    
                    getPositioncontroller().setIntegralEnhancement(this.integralEnhancement);
                    
                    logger.debug("enhancement: " + getPositioncontroller().getEnhancement().toString());                    
                }
            }
            
            if (Model.DATA_DESTINATION_KEY.equals(key))
            {
                if (newValue instanceof String)
                {
                    NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
                    try
                    {
                        Number number = format.parse(newValue.toString());
                        final BigDecimal newDestination = BigDecimal.valueOf(number.doubleValue());
                        // this.destination - Sollwert in Umdrehungen angegeben.
                        this.destination = newDestination;
                        
                        // Bestimmung des Sollwertes this.phiSetPoint in Impulsen...
                        this.phiSetPoint = Model.CIRCUMFERENCE.multiply(newDestination).longValue();
                        
                        logger.debug("setProperty() destination: " + newDestination + ", phiSetPoint: " + this.phiSetPoint);
                    } 
                    catch (ParseException exception)
                    {
                        logger.error("setProperty()", exception);
                    }
                }    
            }

            if (Model.DATA_ANTI_WINDUP_KEY.equals(key))
            {
                this.isAntiWindUp = Boolean.TRUE.equals(newValue);
                
                getPositioncontroller().setAntiWindUp(this.isAntiWindUp);                
                
                logger.debug("isAntiWindUp: " + this.isAntiWindUp);
            }
            
            if (oldValue == null || newValue == null || !oldValue.equals(newValue))
            {
                logger.debug(key + ": " + oldValue + " => " + newValue);
            }
            
            support.firePropertyChange(key, oldValue, newValue);
        }
    }
    
    /**
     * shutdown()...
     * <p>
     * Der gpioController wird auf dem Raspi heruntergefahren...
     * </p>
     */
    public void shutdown()
    {
       logger.debug("shutdown()..."); 
       if (isRaspi)
       {
           this.gpioController.shutdown();  
       }
    }
    
    /**
     * reset()...
     */
    public void reset()
    {
        logger.debug("reset()...");
        
        // Status auf Reset...
        this.status = Status.Reset;

        this.counter = 0L;
        
        // Sollwert auf 0...
        this.drvSetPoint = 0;
        
        this.phi = 0L;
        for (int index = 0; index < this.position.length; index++)
        {
            this.position[index] = 0L;                
        }
        if (this.positionController != null)
        {
            this.positionController.resetData();
        }
    }

    /**
     * start()...
     */
    public void start()
    {
        logger.debug("start()...");
        
        // Status auf Start...
        this.status = Status.Start;
    }
    
    /**
     * stop() 
     */
    public void stop()
    {
        logger.debug("stop()...");
        
        // Status auf Stop...
        this.status = Status.Stop;
        
        this.counter = 0L;
        
        // Sollwert auf 0...
        this.drvSetPoint = 0;
        
        if (isRaspi)
        {
            try
            {
                // Abbremsen...
                this.drv8830.brake();
            
                int fault = this.drv8830.getFault(); 
                // Bei fault == 0 => Fehlerfrei, sonst Fehler!
                if (fault != 0)
                {
                    DRV8830.Fault error = DRV8830.Fault.getFault(fault);
                    logger.error("stop(): Nach brake() " + error.getReason());
                }
            }
            catch (IOException exception)
            {
                logger.error("brake():", exception);
            }
        }
    }
    
    
    @Override
    public String toString()
    {
        return "gui.Model";
    }
}
