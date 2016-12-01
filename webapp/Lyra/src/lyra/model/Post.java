package lyra.model;

/**
 * Entity representing post messages 
 *
 */
public class Post
{
	int id;
	int authorId;
	String authorName;
	String postText;
	long postTime;
	int repubs;
	
	// C-tor
	
	/**
	 * @param id the post's id
	 * @param authorId the user ID
	 * @param authorName 	the name of the post's author
	 * @param postText	the text inside the post
	 * @param postTime	the time of the post
	 * @param repubs	number of time the post was republished
	 */
	public Post(int id, int authorId, String authorName, String postText, long postTime, int repubs)
	{
		this.id = id;
		this.authorId = authorId;
		this.authorName = authorName;
		this.postText = postText;
		this.postTime = postTime;
		this.repubs = repubs;
	}

	// Getters & Setters
	
	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id set
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return author's id
	 */
	public int getAuthorId()
	{
		return authorId;
	}

	/**
	 * @param authorId set
	 */
	public void setAuthorId(int authorId)
	{
		this.authorId = authorId;
	}

	/**
	 * @return the post's author name
	 */
	public String getAuthorName()
	{
		return authorName;
	}

	/**
	 * @param authorName set
	 */
	public void setAuthorName(String authorName)
	{
		this.authorName = authorName;
	}

	/**
	 * @return the post's text
	 */
	public String getPostText()
	{
		return postText;
	}

	/**
	 * @param postText set
	 */
	public void setPostText(String postText)
	{
		this.postText = postText;
	}

	/**
	 * @return post's time
	 */
	public long getPostTime()
	{
		return postTime;
	}

	/**
	 * @param postTime set
	 */
	public void setPostTime(long postTime)
	{
		this.postTime = postTime;
	}

	/**
	 * @return number of times the post was republished
	 */
	public int getRepubs()
	{
		return repubs;
	}

	/**
	 * @param repubs set
	 */
	public void setRepubs(int repubs)
	{
		this.repubs = repubs;
	}
}
