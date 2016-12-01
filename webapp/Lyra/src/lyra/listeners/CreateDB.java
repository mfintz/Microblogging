package lyra.listeners;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lyra.AppConstants;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * Creating all the system's DB tables 
 *
 */
public class CreateDB implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public CreateDB() {
    }


	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)
    { 
    	ServletContext cntx = event.getServletContext();

		try
		{
			// Obtain CustomerDB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection();
			
			DatabaseMetaData metaData = conn.getMetaData();	
			ResultSet rs;
			
			// Check if ACCOUNT table exists
			rs = metaData.getTables(null, null, "ACCOUNT", null);
						
			if (!rs.next())
			{
			    // Table doesn't exist - create it
				Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_ACCOUNT_TABLE);
    			
    			// Commit update
        		conn.commit();
        		stmt.close();
			}
			
			// Check if POST table exists
			rs = metaData.getTables(null, null, "POST", null);
						
			if (!rs.next())
			{
			    // Table doesn't exist - create it
				Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_POST_TABLE);
    			
    			// Commit update
        		conn.commit();
        		stmt.close();
			}
			
			// Check if TOPIC table exists
			rs = metaData.getTables(null, null, "TOPIC", null);
						
			if (!rs.next())
			{
			    // Table doesn't exist - create it
				Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_TOPIC_TABLE);
    			
    			// Commit update
        		conn.commit();
        		stmt.close();
			}
			
			// Check if FOLLOW table exists
			rs = metaData.getTables(null, null, "FOLLOW", null);
						
			if (!rs.next())
			{
			    // Table doesn't exist - create it
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(AppConstants.CREATE_FOLLOW_TABLE);
				
				// Commit update
	    		conn.commit();
	    		stmt.close();
			}
			
			// Close connection
    		conn.close();			
		}
		catch (NamingException | SQLException e)
		{
			// Log error 
    		cntx.log("Error during database initialization", e);
		}	      
    }
    
    
    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)
    { 
    	// Shut down database
	   	try
	   	{
			DriverManager.getConnection(AppConstants.PROTOCOL + AppConstants.DB_NAME + ";shutdown=true");
		} 
	   	catch (SQLException e)
	   	{
				ServletContext cntx = event.getServletContext();
				cntx.log("Error shutting down database", e);
		}
    }
}
