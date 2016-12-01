package lyra.servlets;

import java.io.IOException;
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
import javax.servlet.http.HttpSession;

import lyra.AppConstants;
import lyra.model.User;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * An authentication servlet for login requests 
 */
@SuppressWarnings("deprecation")
public class LoginServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet()
    {
        super();
    }

    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
				
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); 		

			// Perform query
			PreparedStatement stmt = conn.prepareStatement(AppConstants.SELECT_ACCOUNT_BY_UN_ACCOUNT_STMT);
			stmt.setString(1, username);
			
			ResultSet rs = stmt.executeQuery();
			
			if (!rs.next())
			{
				// No such account
				
				response.setStatus(404);
			}
			else
			{ 
				String resultPassword = rs.getString(3);
				
				// Compare passwords
				if (!resultPassword.equals(password))
				{
					// Wrong password
					
					response.setStatus(404);
				}
				else
				{	
					// Successful authentication
					
					// Create and store the current user object on the session
					User currUser = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)); 
					session.setAttribute("currUser", currUser);				
				}
			}
			
			// Close DB objects
			rs.close();
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
