package lyra.model;

/**
 * Entity representing a user/account in the system
 */
public class User
{
	private int id;
	private String username, password, nickname, description, photo; //the photo supposed to be just a link and the desc.  is up to 50 words

	/**
	 * All-fields c-tor
	 * @param pId User id
	 * @param pUsername Username
	 * @param pPassword User password
	 * @param pNickname User nickname
	 * @param pDesc User description
	 * @param pPhoto User photo url
	 */
	public User(int pId, String pUsername, String pPassword, String pNickname, String pDesc, String pPhoto)
	{
		id = pId;
		username = pUsername;
		password = pPassword;
		nickname = pNickname;
		description = pDesc;
		photo = pPhoto;
	}

	// Getter & Setters
	
	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param pId User id
	 */
	public void setId(int pId)
	{
		id = pId;
	}

	/**
	 * @return Username
	 */
	public String getUserName()
	{
		return username;
	}

	/**
	 * @param pUserName Username
	 */
	public void setUserName(String pUserName) 
	{
		username = pUserName;
	}	
	
	/**
	 * @return User password
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @param pPassword Password
	 */
	public void setPassword(String pPassword)
	{
		password = pPassword;
	}
	
	/**
	 * @return User nickname
	 */
	public String getNickname()
	{
		return nickname;
	}
	
	/**
	 * @param pNickname User nickname
	 */
	public void setNickname(String pNickname)
	{
		nickname = pNickname;
	}

	/**
	 * @return User description
	 */
	public String getDescription() 
	{
		return description;
	}

	/**
	 * @param pDescription User description
	 */
	public void setDescription(String pDescription)
	{
		description = pDescription;
	}

	/**
	 * @return User photo URL
	 */
	public String getPhoto()
	{
		return photo;
	}

	/**
	 * @param pPhoto User photo URL
	 */
	public void setPhoto(String pPhoto)
	{
		photo = pPhoto;
	}
}