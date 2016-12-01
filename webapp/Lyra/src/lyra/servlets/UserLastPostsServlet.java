package lyra.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;




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
 * Servlet for returning the user's latest posts
 */
@SuppressWarnings("deprecation")
public class UserLastPostsServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserLastPostsServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		int authorID = Integer.parseInt(request.getParameter("authorId")); //get parameter of the request
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_10_POSTS_BY_SELECTED_USER_POST_STMT);
			stmt.setMaxRows(10);
			stmt.setInt(1, authorID);
			ResultSet rs = stmt.executeQuery();
			List<Post> allPosts = new ArrayList<Post>();
			//insert posts into the list
			while (rs.next())
			{
				// Create a post object and add it into a list
				Post currPost = new Post(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getLong(5), rs.getInt(6));
				allPosts.add(currPost);				
			}
			stmt.close();
			rs.close();
    		// Close connection
    		conn.close();
    		//generate the json obj
        	Gson gson = new Gson();
        	String postJsonResult = gson.toJson(allPosts, AppConstants.POST_COLLECTION);
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
