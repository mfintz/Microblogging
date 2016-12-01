package lyra.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import com.google.gson.Gson;

/**
 * Servlet for retrieving number of followers/following for a specific user
 */
@SuppressWarnings("deprecation")
public class FollowNumberServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FollowNumberServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int number = Integer.parseInt(request.getParameter("userID"));
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		
    		PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_NUUMBER_OF_FOLLOWING_FOLLOW_STMT);
    		stmt.setInt(1, number);
			ResultSet rs = stmt.executeQuery();
			int numOfFollowing = 0;
			if(rs.next()) // the list isn't empty
			{
			numOfFollowing = rs.getInt(1);
			}
    		stmt = conn.prepareStatement(AppConstants.SELECT_NUUMBER_OF_FOLLOWERS_FOLLOW_STMT);
    		stmt.setInt(1, number);
			rs = stmt.executeQuery();			
			int numOfFollowers = 0;
			if (rs.next()) // the list isn't empty
			{
				numOfFollowers = rs.getInt(1);
			}
			stmt.close();
			rs.close();
    		// Close connection
    		conn.close();
        	int[] result = {numOfFollowing, numOfFollowers};
    		//generate the json obj
        	Gson gson = new Gson();
        	String followJsonResult = gson.toJson(result);
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
