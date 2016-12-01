package lyra.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

/**
 * A Servlet That writes a new post / republished post to the DB
 */
@SuppressWarnings("deprecation")
public class NewsFeedWriteServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsFeedWriteServlet()
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
		String postText = request.getParameter("textToPost");
		
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection();
			
			String hyperTextPost = postText;
			List<String> postTopics = new ArrayList<String>();
			String uri = request.getRequestURI();
			
			// Check whether the post is new or a republish
    		if (uri.indexOf(AppConstants.NEWSFEED_REPUB) != -1)
    		{
    			// We are republishing an existing post
    			
    			// Wrap the message with a republish pattern
    			int origPostId = Integer.parseInt(request.getParameter("postId"));
    			hyperTextPost = "RE: \"" + hyperTextPost + "\"";    			
    			
    			// Increase the republishes counter of the original post
    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.UPDATE_INC_POST_REPUBS_POST_STMT);
				pstmt.setInt(1, origPostId);
				pstmt.executeUpdate();
				
				conn.commit();
				pstmt.close();
    		}
    		else
    		{
    			// We are writing a new post
    			
    			// Parse posts text and look for mentions
    			int i = 0;
    			
    			while((i = hyperTextPost.indexOf('@', i)) != -1)
    			{
    				int hyperTextStart = i;
    				
    				i++;
    				
    				int nextSpace = hyperTextPost.indexOf(' ', i);
    				
    				// If there are no more spaces, set nextSpace to the last char
    				nextSpace = (nextSpace == -1) ? hyperTextPost.length() : nextSpace;
    				
    				String mention = hyperTextPost.substring(i, nextSpace);
    								
    				int hyperTextEnd = nextSpace;
    				
    				// Check if such a user exists
    				PreparedStatement pstmt = conn.prepareStatement(AppConstants.SELECT_ACCOUNT_BY_NICK_ACCOUNT_STMT);
    				pstmt.setString(1, mention);
    				ResultSet rs = pstmt.executeQuery();	

    				Boolean userExists = false;
    				
    				if (rs.next())
    				{
    					userExists = true;					
    				}
    				
    				pstmt.close();
    				
    				if (userExists)
    				{
    					// Wrap mention with hypertext
    					
    					// Add the closing tag
    					hyperTextPost = hyperTextPost.substring(0, hyperTextEnd) + 
    						AppConstants.HYPERTEXT_WRAP_CLOSE +
    						((hyperTextEnd == hyperTextPost.length()) ? ("") :
    						(hyperTextPost.substring(hyperTextEnd, hyperTextPost.length())));
    					
    					// Add the middle tag
    					hyperTextPost = hyperTextPost.substring(0, hyperTextStart) +
    							AppConstants.HYPERTEXT_WRAP_MIDDLE +
    							hyperTextPost.substring(hyperTextStart, hyperTextPost.length());
    					
    					// Add the opening tag
    					hyperTextPost = hyperTextPost.substring(0, hyperTextStart) +
    							AppConstants.HYPERTEXT_WRAP_MENTION_OPEN + mention +
    							hyperTextPost.substring(hyperTextStart, hyperTextPost.length());
    					
    					i += AppConstants.HYPERTEXT_WRAP_CLOSE.length() + AppConstants.HYPERTEXT_WRAP_MIDDLE.length() +
    						 AppConstants.HYPERTEXT_WRAP_MENTION_OPEN.length() + mention.length();
    				}
    				
    				i += mention.length();
    			}

    			
    			// Parse posts text and look for topics
    			i = 0;
    			
    			while((i = hyperTextPost.indexOf('#', i)) != -1)
    			{
    				int hyperTextStart = i;
    				
    				i++;
    				
    				int nextSpace = hyperTextPost.indexOf(' ', i);
    				
    				// If there are no more spaces, set nextSpace to the last char
    				nextSpace = (nextSpace == -1) ? hyperTextPost.length() : nextSpace;
    				
    				String topic = hyperTextPost.substring(i, nextSpace);
    								
    				int hyperTextEnd = nextSpace;
    				
					// Wrap topic with hypertext
					
					// Add the closing tag
					hyperTextPost = hyperTextPost.substring(0, hyperTextEnd) + 
						AppConstants.HYPERTEXT_WRAP_CLOSE +
						((hyperTextEnd == hyperTextPost.length()) ? ("") :
						(hyperTextPost.substring(hyperTextEnd, hyperTextPost.length())));
					
					// Add the middle tag
					hyperTextPost = hyperTextPost.substring(0, hyperTextStart) +
							AppConstants.HYPERTEXT_WRAP_MIDDLE +
							hyperTextPost.substring(hyperTextStart, hyperTextPost.length());
					
					// Add the opening tag
					hyperTextPost = hyperTextPost.substring(0, hyperTextStart) +
							AppConstants.HYPERTEXT_WRAP_TOPIC_OPEN + topic +
							hyperTextPost.substring(hyperTextStart, hyperTextPost.length());
					
					i += AppConstants.HYPERTEXT_WRAP_CLOSE.length() + AppConstants.HYPERTEXT_WRAP_MIDDLE.length() +
						 AppConstants.HYPERTEXT_WRAP_TOPIC_OPEN.length() + (2 * topic.length());
					
					
					// Save the topic for later insertion into the DB
					postTopics.add(topic);				
    			}
    		}


    		// Insert the new post into the Post table	
			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_POST, Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, currUser.getId());
			pstmt.setString(2, hyperTextPost);
			pstmt.setLong(3, (new Date()).getTime());

			pstmt.executeUpdate();
			conn.commit();
			
			ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			
			int newPostId = rs.getInt(1);			
			
			rs.close();
			pstmt.close();
			
			// Insert all the topics into the Topic table
			pstmt = conn.prepareStatement(AppConstants.INSERT_TOPIC);
			
			// Won't happen for a republished post because the list will be empty
			for (String currTopic : postTopics)
			{
				pstmt.setString(1, currTopic);
				pstmt.setInt(2, newPostId);
				pstmt.executeUpdate();			
			}
			
			conn.commit();
			pstmt.close();
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}	
	}

}
