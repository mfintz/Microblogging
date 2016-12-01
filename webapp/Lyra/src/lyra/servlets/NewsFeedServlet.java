package lyra.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lyra.model.User;

import com.google.gson.Gson;

/**
 * A Servlet the gets the current user from the session and sends it to the client 
 */
@SuppressWarnings("deprecation")
public class NewsFeedServlet extends HttpServlet implements SingleThreadModel
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsFeedServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		
		// Get the current user from the session
		User currUser= (User)(session.getAttribute("currUser"));

		// Send the result to the client as JSON
    	Gson gson = new Gson();
    	String userJson = gson.toJson(currUser);

    	PrintWriter writer = response.getWriter();
    	writer.println(userJson);
    	writer.close();
	}

}
