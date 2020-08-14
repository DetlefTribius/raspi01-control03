package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Das SwingWindow ist ein JFrame (GUI-Swing-Klasse).
 * Es gestaltet die GUI mit einer Liste und den 
 * entsprechenden Buttons.
 * </p>
 * <p>
 * Die Datenhaltung erfolgt im Model.
 * </p> 
 * <p>
 * Vgl.: https://dbs.cs.uni-duesseldorf.de/lehre/docs/java/javabuch/html/k100242.html<br>
 * Auch: http://www.willemer.de/informatik/java/guimodel.htm<br>
 * </p>
 * <p>
 * Radio-Button: http://www.fredosaurus.com/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
 * </p>
 * @author Detlef Tribius
 *
 */
public class SwingWindow extends JFrame implements View   
{
    /**
     * serialVersionUID = 1L - durch Eclipse generiert...
     */
    private static final long serialVersionUID = 1L;

    /**
     * logger - Instanz zur Protokollierung...
     */
    private final static Logger logger = LoggerFactory.getLogger(SwingWindow.class);      

    /**
     * textComponentMap - nimmt die Controls zur Darstellung der Daten (hier JTextField) auf...
     */
    private final java.util.Map<String, JTextComponent> textComponentMap = new java.util.TreeMap<>();
    
    private final java.util.Map<String, JComboBox<BigDecimal>> comboBoxMap = new java.util.TreeMap<>();
    
    private final java.util.Map<String, JCheckBox> checkBoxMap = new java.util.TreeMap<>();
  
    private static final String TEXT_FIELD = JTextField.class.getCanonicalName();
    
    private static final String FORMATTED_TEXT_FIELD = JFormattedTextField.class.getCanonicalName();
    
    private static final String CHECK_BOX = JCheckBox.class.getCanonicalName();
    
    private static final String COMBO_BOX = JComboBox.class.getCanonicalName();
    
    /**
     * controlData - Beschreibungsdaten der Oberflaechenelemente...
     */
    private final static String[][] controlData = new String[][]
    {
        {TEXT_FIELD,            Data.COUNTER_KEY,                       "Lfd. Nr." },
        {FORMATTED_TEXT_FIELD,  Model.DATA_DESTINATION_KEY,             "Sollwert Lage" },
        {TEXT_FIELD,            Data.PHI_KEY,                           "Impulse" },
        {TEXT_FIELD,            Data.ROTATION_KEY,                      "Umdrehungen" },
        {TEXT_FIELD,            Data.RPM_KEY,                           "Drehzahl" },
        {TEXT_FIELD,            Data.CYCLE_TIME_KEY,                    "Taktzeit" },
        {TEXT_FIELD,            Data.DRV_SET_POINT_KEY,                 "Sollwert DRV8830" },
        {COMBO_BOX,             Model.DATA_ENHANCEMENT_KEY,             "P-Reglerverstärkung" },
        {COMBO_BOX,             Model.DATA_INTEGRAL_ENHANCEMENT_KEY,    "I-Reglerverstärkung" },
        {CHECK_BOX,             Model.DATA_ANTI_WINDUP_KEY,             "Anti-Windup" }
    };
    
    
    /**
     * 
     */
    private ActionListener actionListener = null; 
    
    /**
     * Reset-Button...
     */
    private final JButton resetButton = new JButton("Reset");
    
    /**
     * Start-Button...
     */
    private final JButton startButton = new JButton("Start");
    
    /**
     * Stop-Button...
     */
    private final JButton stopButton = new JButton("Stop");
    
    /**
     * Ende-Button... beendet die Anwendung
     */
    private final JButton endButton = new JButton("Ende");
    
    /**
     * 
     */
    private final JButton buttons[] = new JButton[] 
    { 
        resetButton,
        startButton,
        stopButton,
        endButton
    };
    
    /**
     * jContentPane - Referenz auf das Haupt-JPanel 
     */
    private JPanel jContentPane = null;

    /**
     * This is the default constructor
     */
    public SwingWindow(Model model)
    {
        super();
        initialize();
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                logger.debug("windowClosing(WindowEvent)...");
                model.shutdown();
                System.exit(0);
            }
        });
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(450, 250);
        this.setContentPane(getJContentPane());
        this.setTitle( "GPIO-Lageregelung 3" );
        this.resetButton.setName(Model.NAME_RESET_BUTTON);
        this.startButton.setName(Model.NAME_START_BUTTON);
        this.stopButton.setName(Model.NAME_STOP_BUTTON);
        this.endButton.setName(Model.NAME_END_BUTTON);
    }

    /**
     * This method initializes jContentPane
     * 
     * getJContentPane() - Methode baut das SwingWindow-Fenster auf.
     * Es werden alle sichtbaren Komponenten instanziiert.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            // BorderLayout hat die Bereiche
            // BorderLayou.NORTH
            // BorderLayout.CENTER
            // BorderLayout.SOUTH
            jContentPane.setLayout(new BorderLayout(10, 10));
            
            {   // NORTH
                JPanel northPanel = new JPanel();
                northPanel.setLayout(new BoxLayout(northPanel, javax.swing.BoxLayout.Y_AXIS));
                
                // northPanel wird in den Bereich NORTH eingefuegt.
                jContentPane.add(northPanel, BorderLayout.NORTH);
            }
            
            { // WEST
                // leeres Panel (Platzhalter)...
                jContentPane.add(new JPanel(), BorderLayout.WEST);
            }
            
            { // EAST
                // leeres Panel (Platzhalter)...
                jContentPane.add(new JPanel(), BorderLayout.EAST);
            }
            
            {   // CENTER
                // Struktur: centerPanel als BoxLayout, Ausrichtung von oben nach unten.
                // Jede Zelle erneut als BoxLayout von links nach rechts.
                JPanel centerPanel = new JPanel();
                centerPanel.setLayout(new BoxLayout(centerPanel, javax.swing.BoxLayout.Y_AXIS));
                
                for(String[] controlParam: SwingWindow.controlData)
                {
                    final String controlType = controlParam[0];
                    final String controlId = controlParam[1];
                    final String labelText = controlParam[2];
                    {
                        JPanel controlPanel = new JPanel();
                        controlPanel.setLayout(new BoxLayout(controlPanel, javax.swing.BoxLayout.X_AXIS));
                        controlPanel.add(Box.createHorizontalGlue());
                        controlPanel.add(new JLabel(labelText));
                        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                    
                        if (TEXT_FIELD.equals(controlType))
                        {
                            JTextField controlTextField = new JTextField(10);
                            controlTextField.setMaximumSize(new Dimension(100, controlTextField.getMinimumSize().height));
                            this.textComponentMap.put(controlId, controlTextField);
                            controlTextField.setEditable(false);
                            controlPanel.add(controlTextField);
                            controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                            centerPanel.add(controlPanel);
                        }
                        else if (FORMATTED_TEXT_FIELD.equals(controlType))
                        {
                            // Es handelt sich um ein JFormattedTextField...
                            final String pattern = (controlParam.length > 3)? controlParam[3] : Model.FORMATTED_TEXT_FIELD_PATTERN;
                            final NumberFormatter numberFormatter = new NumberFormatter(new DecimalFormat(pattern)); 
                            
                            final String value = new DecimalFormat(pattern).format(0.0);
                            
                            JFormattedTextField controlTextField = new JFormattedTextField(numberFormatter);
                            controlTextField.setInputVerifier(new FormattedTextFieldVerifier());
                            controlTextField.setColumns(10);
                            controlTextField.setText(value);
                            controlTextField.setHorizontalAlignment(JTextField.RIGHT);
                            controlTextField.setMaximumSize(new Dimension(100, controlTextField.getMinimumSize().height));
                            this.textComponentMap.put(controlId, controlTextField);
                            controlTextField.setEditable(true);
                            controlPanel.add(controlTextField);
                            controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                            centerPanel.add(controlPanel);
                            
                            controlTextField.setName(controlId);
                            
                            controlTextField.addPropertyChangeListener(new PropertyChangeListener() 
                            {
                                @Override
                                public void propertyChange(PropertyChangeEvent event)
                                {
                                    JFormattedTextField source = (JFormattedTextField) event.getSource();
                                    
                                    final Object newValue = event.getNewValue();

                                    logger.info(source.getName() + ": " + event.getNewValue());
                                    
                                    if (newValue instanceof Boolean)
                                    {
                                        final String value = new DecimalFormat(Model.FORMATTED_TEXT_FIELD_PATTERN).format(0.0);
                                        
                                        controlTextField.setText(value);
                                        return;
                                    }
                                    // Weiterreichen...
                                    propertyChangeDelegate(event);
                                }
                            });  
                        }
                        else if (COMBO_BOX.equals(controlType) && Model.DATA_ENHANCEMENT_KEY.equals(controlId))
                        {
                            // 1.) Es handelt sich um eine JComboBox ...und... 
                            // 2.) der zweite Eintrag in der controlData-Tabelle lautet DATA_ENHANCEMENT_KEY
                            // => also die Combobox mit der Regelverstaerkung fuer den P-Anteils...
                            JComboBox<BigDecimal> enhancementsComboBox = new JComboBox<>(Model.ENHANCEMENTS);
                            enhancementsComboBox.setName(controlId);
                            enhancementsComboBox.setMaximumSize(new Dimension(100, enhancementsComboBox.getMinimumSize().height));
                            this.comboBoxMap.put(controlId, enhancementsComboBox); 
                            controlPanel.add(enhancementsComboBox);
                            controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                            centerPanel.add(controlPanel);
                            
                            // Selektion des Eintrages mit BigDecimal.ZERO...
                            enhancementsComboBox.setSelectedIndex(Model.SELECTED_ENHANCEMENTS_INDEX);
                            
                            enhancementsComboBox.addActionListener(new ActionListener() 
                            {

                                @Override
                                @SuppressWarnings("unchecked")
                                public void actionPerformed(ActionEvent event)
                                {
                                    JComboBox<BigDecimal> source = (JComboBox<BigDecimal>)event.getSource();   
                                    logger.info(source.getName() + ": " + event.getActionCommand());   
                                    
                                    actionCommandDelegate(event);
                                }
                            });
                        }
                        else if (COMBO_BOX.equals(controlType) && Model.DATA_INTEGRAL_ENHANCEMENT_KEY.equals(controlId))
                        {
                            // 1.) Es handelt sich um eine JComboBox ...und... 
                            // 2.) der zweite Eintrag in der controlData-Tabelle lautet DATA_INTEGRAL_ENHANCEMENT_KEY
                            // => also die Combobox mit der Regelverstaerkung fuer den I-Anteils...
                            JComboBox<BigDecimal> integralEnhancementsComboBox = new JComboBox<>(Model.INTEGRAL_ENHANCEMENTS);
                            integralEnhancementsComboBox.setName(controlId);
                            integralEnhancementsComboBox.setMaximumSize(new Dimension(100, integralEnhancementsComboBox.getMinimumSize().height));
                            this.comboBoxMap.put(controlId, integralEnhancementsComboBox); 
                            controlPanel.add(integralEnhancementsComboBox);
                            controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                            centerPanel.add(controlPanel);
                            
                            // Selektion des Eintrages mit BigDecimal.ZERO...
                            integralEnhancementsComboBox.setSelectedIndex(Model.SELECTED_INTEGRAL_ENHANCEMENTS_INDEX);
                            
                            integralEnhancementsComboBox.addActionListener(new ActionListener() 
                            {

                                @Override
                                @SuppressWarnings("unchecked")
                                public void actionPerformed(ActionEvent event)
                                {
                                    JComboBox<BigDecimal> source = (JComboBox<BigDecimal>)event.getSource();   
                                    logger.info(source.getName() + ": " + event.getActionCommand());   
                                    
                                    actionCommandDelegate(event);
                                }
                            });
                        }
                        else if (CHECK_BOX.equals(controlType) && Model.DATA_ANTI_WINDUP_KEY.equals(controlId))
                        {
                            // 1.)
                            // 2.)
                            JCheckBox antiWindupCheckBox = new JCheckBox();
                            antiWindupCheckBox.setName(controlId);
                            this.checkBoxMap.put(controlId, antiWindupCheckBox);
                            controlPanel.add(antiWindupCheckBox);
                            controlPanel.add(Box.createRigidArea(new Dimension(4, 0)));
                            centerPanel.add(controlPanel);
                            
                            antiWindupCheckBox.addItemListener(new ItemListener() 
                            {

                                @Override
                                public void itemStateChanged(ItemEvent event)
                                {
                                    JCheckBox source = (JCheckBox) event.getSource();
                                    
                                    final boolean isDeSelected = (event.getStateChange() == ItemEvent.DESELECTED);
                                    final boolean isSelected = (event.getStateChange() == ItemEvent.SELECTED);
                                    
                                    final String eventMsg = (isDeSelected? "deselected" : "")
                                                          + (isSelected? "selected" : "")
                                                          + ((!isDeSelected && !isSelected)? "?" : "");
                                    logger.info(source.getName() + ": " + eventMsg); 
                                    
                                    itemStateChangedDelegate(event);                                    
                                }
                            });
                        }
                    }

                    {
                        // Leerzeile...
                        JPanel emptyPanel = new JPanel();
                        emptyPanel.setLayout(new BoxLayout(emptyPanel, javax.swing.BoxLayout.Y_AXIS));
                        emptyPanel.add(Box.createRigidArea(new Dimension(0, 4)));
                        centerPanel.add(emptyPanel);
                    }
                }
                
                jContentPane.add(centerPanel, BorderLayout.CENTER);
            }
            
            {   // SOUTH...
                // buttonPanel beinhaltet die Button...
                JPanel buttonPanel = new JPanel();
                FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
                flowLayout.setAlignment(FlowLayout.RIGHT);
            
                for(JButton button: buttons)
                {
                    button.setHorizontalAlignment(SwingConstants.RIGHT);
                    button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent event)
                        {
                            final JButton source = (JButton)event.getSource();
                            logger.debug(source.getName());
                            //
                            actionCommandDelegate(event);
                        }
                    });
                    //
                    buttonPanel.add(button);
                }
                
                jContentPane.add(buttonPanel, BorderLayout.SOUTH);
            }
        }
        return jContentPane;
    }

    @Override
    public void addActionListener(ActionListener listener)
    {
        logger.debug("Controller hinzugefuegt (ActionListener)...");
        this.actionListener = listener;
    }

    /**
     * propertyChange(PropertyChangeEvent event) - wird vom Model her beaufragt
     * und muss die View evtl. nachziehen...  
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        final String propertyName = event.getPropertyName();
        final Object newValue = event.getNewValue();

        if (Model.DATA_KEY.equals(propertyName))
        {
            // propertyChange vom Model her mit DATA_KEY...
            if (newValue instanceof Data)
            {
                final Data newData = (Data) newValue;
                for(String key: newData.getKeys())
                {
                    if (this.textComponentMap.containsKey(key))
                    {
                        final JTextComponent textComponent = this.textComponentMap.get(key);
                        textComponent.setText(newData.getValue(key));
                        continue;
                    }
                }
            }
        }
        
        if (Model.DATA_DESTINATION_KEY.equals(propertyName))
        {
            // propertyChange vom Model her mit DATA_DESTINATION_KEY...
            if (this.textComponentMap.containsKey(propertyName))
            {
                final JTextComponent textComponent = this.textComponentMap.get(propertyName);
                textComponent.setText((String)newValue);
                logger.debug("Sollwert Lage: " + newValue);
            }
        }
        
        if (Model.DATA_ENHANCEMENT_KEY.equals(propertyName))
        {
            // propertyChange vom Model her mit DATA_ENHANCEMENT_KEY...
            final BigDecimal newData = (BigDecimal) newValue;
            
            if (this.comboBoxMap.containsKey(propertyName))
            {
                JComboBox<BigDecimal> enhancementComboBox = this.comboBoxMap.get(propertyName);
                enhancementComboBox.setSelectedItem(newData);
                logger.debug("P-Reglerverstärkung: " + newValue);
            }
        }
        
        if (Model.DATA_INTEGRAL_ENHANCEMENT_KEY.equals(propertyName))
        {
            // propertyChange vom Model her mit DATA_ENHANCEMENT_KEY...
            final BigDecimal newData = (BigDecimal) newValue;
            
            if (this.comboBoxMap.containsKey(propertyName))
            {
                JComboBox<BigDecimal> integralEnhancementComboBox = this.comboBoxMap.get(propertyName);
                integralEnhancementComboBox.setSelectedItem(newData);
                logger.debug("I-Reglerverstärkung: " + newValue);
            }
        }

        if (Model.DATA_ANTI_WINDUP_KEY.equals(propertyName))
        {
            final boolean isSelected = Boolean.TRUE.equals(newValue);
            
            if (this.checkBoxMap.containsKey(propertyName))
            {
                JCheckBox checkBox = this.checkBoxMap.get(propertyName);
                checkBox.setSelected(isSelected);
                logger.debug(propertyName + ": " + (isSelected? "selected" : "deselected"));
            }
            
        }
        
        // Kontrollausgabe im Debuglevel...
        logger.debug(event.toString());
    }

    /**
     * 
     * @param event
     */
    private void actionCommandDelegate(java.awt.event.ActionEvent event) 
    {                                       
        if (this.actionListener != null) 
        {
            this.actionListener.actionPerformed(event);
        }
    }
    
    /**
     * 
     * @param event
     */
    private void propertyChangeDelegate(PropertyChangeEvent event)
    {
        if (this.actionListener != null) 
        {
            JFormattedTextField source = (JFormattedTextField) event.getSource();
            // event.getPropertyName() - Propertyname des geaenderten Attributes,
            // Es handelts sich um das Attribus "value"
            // d.i. Last valid value in JFormattedTextField, dort private Object value...
            if ("value".equals(event.getPropertyName()))
            {
                // Als command wird Data.DESTINATION_KEY mitgegeben...
                this.actionListener.actionPerformed(new ActionEvent(source,
                                                                    ActionEvent.ACTION_PERFORMED,
                                                                    Model.DATA_DESTINATION_KEY));
            }
        }
    }
    
    /**
     * 
     * @param event
     */
    private void itemStateChangedDelegate(ItemEvent event)
    {
        if (this.actionListener != null)
        {
            JCheckBox source = (JCheckBox) event.getSource();
            
            final String name = source.getName();
            
            this.actionListener.actionPerformed(new ActionEvent(source,
                                                                ActionEvent.ACTION_PERFORMED,
                                                                name));
        }
        
    }
}

