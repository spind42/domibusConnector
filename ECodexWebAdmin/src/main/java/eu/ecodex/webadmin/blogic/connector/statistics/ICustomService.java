package eu.ecodex.webadmin.blogic.connector.statistics;

public interface ICustomService {

    /**
     * Statistics - Custom Report Given the selected Mask values, this method
     * conditions the customResultList List, which is displayed in the result
     * table
     * 
     * @return "/pages/main.xhtml" Refreshing the page
     */
    public String generateCustomReport();

}