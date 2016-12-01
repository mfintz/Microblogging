package lyra.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
 * Servlet for user registration process
 */
@SuppressWarnings("deprecation")
public class UserRegServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRegServlet()
    {
        super();
    } 
   
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		String username = request.getParameter("username");
		String password = request.getParameter("pwd");
		String nickname = request.getParameter("nickname");
		String description = request.getParameter("description");
		String photoURL = request.getParameter("photourl");
		try
		{
			// Obtain DB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(AppConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection();	
			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_NEW_USER, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			pstmt.setString(3, nickname);
			pstmt.setString(4, description);
			//default photo if none was provided
			if (photoURL.isEmpty())
			{
				photoURL = AppConstants.DEF_PHOTO;
			}
			pstmt.setString(5, photoURL);
			pstmt.executeUpdate();
			// Commit update
			conn.commit();
			ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			int newUsertId = rs.getInt(1);			
			// Close ResultSet and Statement
			rs.close();
			pstmt.close();
			//update as the current user
			User currUser = new User(newUsertId, username, password, nickname, description, photoURL);
			HttpSession session = request.getSession();
			session.setAttribute("currUser", currUser);			
		}
		catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500); // Internal server error
		}
	}
}
