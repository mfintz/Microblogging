package lyra.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lyra.AppConstants;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * A Servlet that writes a new followership connection between two users to the DB
 */
@SuppressWarnings("deprecation")
public class FollowersServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FollowersServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 
			
			int followerId = Integer.parseInt(request.getParameter("followerId"));
			int followedId = Integer.parseInt(request.getParameter("followedId"));
			
			// Insert followership connection
			PreparedStatement stmt = conn.prepareStatement(AppConstants.INSERT_FOLLOW);
			stmt.setInt(1, followerId);
			stmt.setInt(2, followedId);
			stmt.executeUpdate();
			
			// Close DB objects
			stmt.close();
			conn.close();			
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
	}
}
