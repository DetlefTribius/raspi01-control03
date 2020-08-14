/**
 * 
 */
package gui;

/**
 * @author Detlef Tribius
 *
 */
public enum Status
{
    /**
     * Reset("Reset")
     */
    Reset("Reset"),
    /**
     * Start("Start")
     */
    Start("Start"),
    /**
     * Stop("Stop")
     */
    Stop("Stop"),
    /**
     * Ende("Ende")
     */
    Ende("Ende");
    
    /**
     * String status - Kennung fuer den Status...   
     */
    private final String status;
    
    /**
     * private Status(String status) - Privater Konstruktor...
     * @param status
     */
    private Status(String status)
    {
        this.status = status;
    }
}
