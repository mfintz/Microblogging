package lyra.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet checking if theres a user that logged into the system - otherwise it wont let you in (so outside users can't loging for url only)
 */
public class AuthFilter implements Filter
{

    /**
     * Default constructor. 
     */
    public AuthFilter()
    {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy()
	{
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		HttpSession session = req.getSession();
		Object currUser= (session.getAttribute("currUser"));
		if (currUser == null)
		{
			res.sendRedirect("index.html");
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException
	{
	}

}
