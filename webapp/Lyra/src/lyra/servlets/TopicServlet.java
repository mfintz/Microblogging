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

import lyra.AppConstants;
import lyra.model.Post;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * Servlet for returning the latest posts that match a topic
 */
@SuppressWarnings("deprecation")
public class TopicServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TopicServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String topic = request.getParameter("topicToView");
		try
		{
			// Obtain CustomerDB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_10_LATEST_POSTS_BY_TOPIC_POST_STMT);
			stmt.setMaxRows(10);
			stmt.setString(1, topic);
			ResultSet rs = stmt.executeQuery();
			Collection<Post> latestPosts = new ArrayList<Post>();
			//insert posts into the list
			while (rs.next())
			{
				// Create a post object and add it into a list
				Post currPost = new Post(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getInt(6));
				latestPosts.add(currPost);				
			}
			rs.close();
			stmt.close();					
    		// Close connection
    		conn.close();
        	Gson gson = new Gson();
        	// Convert from posts collection to json
        	String postJsonResult = gson.toJson(latestPosts, AppConstants.POST_COLLECTION);
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
