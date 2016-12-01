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
import lyra.model.Post;
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * A Servlet that gets the last 10 posts by the user and the ones he follows to display in the Newsfeed(Home) view
 */
@SuppressWarnings("deprecation")
public class NewsFeedReadServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsFeedReadServlet()
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
	
			// Perform query
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_10_POSTS_BY_AUTHOR_POST_STMT);
			stmt.setMaxRows(10);
			stmt.setInt(1, currUser.getId());
			stmt.setInt(2, currUser.getId());
			ResultSet rs = stmt.executeQuery();
			
			Collection<Post> recentPosts = new ArrayList<Post>();
			
			while (rs.next())
			{
				// Create a post object and add it into a list
				Post currPost = new Post(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getInt(6));
				recentPosts.add(currPost);				
			}
			
			// Close DB objects
			rs.close();
			stmt.close();					
    		conn.close();
        	
        	Gson gson = new Gson();
        	// Convert from posts collection to json
        	String postJsonResult = gson.toJson(recentPosts, AppConstants.POST_COLLECTION);

        	// Send the result to the client
        	PrintWriter writer = response.getWriter();
        	writer.println(postJsonResult);
        	writer.close();
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
		
	}

}
