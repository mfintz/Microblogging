package lyra.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lyra.AppConstants;
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * A servlet which returns a list of users followed by the current logged-in user
 */
@SuppressWarnings("deprecation")
public class CurrUserFollowingServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CurrUserFollowingServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		
		User currUser= (User)(session.getAttribute("currUser"));
		
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		

			// Perform the query
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_ALL_USER_FOLLOWING_FOLLOW_STMT);
			stmt.setInt(1, currUser.getId());
			
			ResultSet rs = stmt.executeQuery();
			
			Collection<User> followingUsers = new ArrayList<User>();
			
			while (rs.next())
			{		
				User currFollow = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)); 
				followingUsers.add(currFollow);			
			}
			
			// Close DB objects
			rs.close();
			stmt.close();
    		conn.close();
    		
    		// Add the current user (because everyone automatically follows himself, though not reflected in the table)
    		followingUsers.add(currUser);
			
    		// Send the result to the client as JSON 
			Gson gson = new Gson();
        	String followJsonResult = gson.toJson(followingUsers, AppConstants.USER_COLLECTION);

        	PrintWriter writer = response.getWriter();
        	writer.println(followJsonResult);
        	writer.close();    		
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
	}

}
