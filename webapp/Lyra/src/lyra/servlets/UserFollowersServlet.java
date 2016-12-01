package lyra.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lyra.AppConstants;
import lyra.UserCompartor;
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * A Servlet the gets the top 10 followers and users being followed by given user
 */
@SuppressWarnings("deprecation")
public class UserFollowersServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserFollowersServlet()
    {
        super();
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		int currUser = Integer.parseInt(request.getParameter("userID"));

		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		
			
			String uri = request.getRequestURI();
			String userQuery = "", popQuery = "";
								
			// Check the required mode and set queries accordingly
    		if (uri.indexOf(AppConstants.SHOW_FOLLOWERS) != -1)
    		{
    			userQuery = AppConstants.SELECT_ALL_USER_FOLLOWERS_FOLLOW_STMT;
    			popQuery = AppConstants.SELECT_FOLLOWERS_POPULARITY_FOLLOW_STMT;
    		}
    		else
    		{
    			userQuery = AppConstants.SELECT_ALL_USER_FOLLOWING_FOLLOW_STMT;
    			popQuery = AppConstants.SELECT_FOLLOWED_AUTHOR_POPULARITY_FOLLOW_STMT;
    		}			
			
    		// Perform users query
    		PreparedStatement stmt = conn.prepareStatement(userQuery);
			stmt.setMaxRows(10);
			stmt.setInt(1,currUser);
			ResultSet rs = stmt.executeQuery();
			
			List<User> allFollowers = new ArrayList<User>();
			
			while (rs.next())
			{
				// Create a user object and add it into a list
				User user = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
				allFollowers.add(user);				
			}
			
			Map<Integer,Integer> followPop = new HashMap<Integer, Integer>();
			
			// Perform popularity query
			stmt = conn.prepareStatement(popQuery);
			stmt.setInt(1, currUser);
			rs = stmt.executeQuery();
			
			while(rs.next())
			{
				followPop.put(rs.getInt(1), rs.getInt(2));
			}
			
			rs.close();
			stmt.close();
			
			// Add users without followers
			for (User cUser : allFollowers)
			{
				if (!followPop.containsKey(cUser.getId()))
				{
					followPop.put(cUser.getId(), 0);					
				}				
			}
			
			// Sort the users list by popularity with using a comparator
			Comparator<User> comp = new UserCompartor(followPop);
			Collections.sort(allFollowers, comp);					
			
    		// Close connection
    		conn.close();
        	
    		// Send the result to the client as JSON
        	Gson gson = new Gson();
        	String userJsonResult = gson.toJson(allFollowers, AppConstants.USER_COLLECTION);

        	PrintWriter writer = response.getWriter();
        	writer.println(userJsonResult);
        	writer.close();
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
	}
}
