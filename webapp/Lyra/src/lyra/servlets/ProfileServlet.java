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
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

/**
 * Servlet for presenting the user profile with all his details
 */
@SuppressWarnings("deprecation")
public class ProfileServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProfileServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String profNickname = request.getParameter("viewProfile");
		
		try
		{
			// Obtain CustomerDB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_ACCOUNT_BY_NICK_ACCOUNT_STMT);
			stmt.setString(1, profNickname);
			ResultSet rs = stmt.executeQuery();
			//insert users into the list
			if (rs.next())
			{
				User profUser = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)); 
				rs.close();
				stmt.close();
	    		conn.close();
	    		//generate the json obj
	        	Gson gson = new Gson();
	        	String userJson = gson.toJson(profUser);
	        	PrintWriter writer = response.getWriter();
	        	writer.println(userJson);
	        	writer.close();
			}
			else
			{
				// Account doesn't exist - shouldn't happen				
				rs.close();
				stmt.close();
	    		conn.close();
	    		response.setStatus(404);			
			}
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
	}
}
