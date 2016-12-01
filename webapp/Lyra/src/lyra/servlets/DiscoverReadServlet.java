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
import java.util.Hashtable;
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
import javax.servlet.http.HttpSession;

import lyra.AppConstants;
import lyra.PostComparator;
import lyra.model.Post;
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * A Servlet that gets the top 10 posts for the discovery view (all/followed users only)
 */
@SuppressWarnings("deprecation")
public class DiscoverReadServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DiscoverReadServlet()
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
	
			String postsQuery = "", popularityQuery = "";
			Boolean onlyFollow = false;
						
			String uri = request.getRequestURI();
			
			// Check working mode and define queries accordingly
    		if (uri.indexOf(AppConstants.DISCOVER_ONLY_FOLLOW) != -1)
    		{
    			// Show only followed users
    			postsQuery = AppConstants.SELECT_POSTS_BY_OTHER_FOLLOWED_AUTHORS_POST_STMT;
    			popularityQuery = AppConstants.SELECT_FOLLOWED_AUTHOR_POPULARITY_FOLLOW_STMT;
    			onlyFollow = true;
    		}
    		else
    		{
    			// Show all users
    			postsQuery = AppConstants.SELECT_POSTS_BY_OTHER_AUTHORS_POST_STMT;
    			popularityQuery = AppConstants.SELECT_AUTHOR_POPULARITY_FOLLOW_STMT;    			
    		}
    		
    		// Perform the queries
			PreparedStatement stmt = conn.prepareStatement(postsQuery);
			stmt.setInt(1, currUser.getId());
			ResultSet rs = stmt.executeQuery();
			
			List<Post> allPosts = new ArrayList<Post>();
			
			while (rs.next())
			{
				// Create a post object and add it into a list
				Post currPost = new Post(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getInt(6));
				allPosts.add(currPost);				
			}
			
			stmt.close();
						
			
			stmt = conn.prepareStatement(popularityQuery);
			
			if (onlyFollow)
			{
				stmt.setInt(1, currUser.getId());
			}
			
			rs = stmt.executeQuery();
			Map<Integer, Integer> authorPopularity = new Hashtable<Integer, Integer>();
			
			while (rs.next())
			{
				authorPopularity.put(rs.getInt(1), rs.getInt(2));				
			}
			
			// Sort the posts by popularity using a comparator
			Comparator<Post> comp = new PostComparator(authorPopularity);
			Collections.sort(allPosts, comp);
			
			// Close DB objects
			stmt.close();
			rs.close();
    		conn.close();
        	
        	Gson gson = new Gson();
        	
        	// Convert the collection of top 10 posts to json
        	String postJsonResult = gson.toJson(allPosts.subList(0, ((allPosts.size() >= 10) ? 10 : allPosts.size())), AppConstants.POST_COLLECTION);

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
