package lyra;

import java.lang.reflect.Type;
import java.util.Collection;

import lyra.model.Post;
import lyra.model.User;

import com.google.gson.reflect.TypeToken;

/**
 * Global application constants
 */
public interface AppConstants
{
	// General constants
	public final String NAME = "name";
	public final String DISCOVER_ONLY_FOLLOW = "onlyfollow";
	public final String SHOW_FOLLOWERS = "followers";
	public final String NEWSFEED_REPUB = "repub";
	public final Type POST_COLLECTION = new TypeToken<Collection<Post>>() {}.getType();
	public final Type USER_COLLECTION = new TypeToken<Collection<User>>() {}.getType();
	public final String DEF_PHOTO="http://i1142.photobucket.com/albums/n617/matanfintz/user-default_zpssfn6y8r5.jpg";
	
	// HTML constants
	public final String HYPERTEXT_WRAP_MENTION_OPEN = "<a class=\"mention-link\" href=\"javascript:showProfile('";
	public final String HYPERTEXT_WRAP_TOPIC_OPEN = "<a class=\"topic-link\" href=\"javascript:showTopic('";
	public final String HYPERTEXT_WRAP_MIDDLE = "')\">";
	public final String HYPERTEXT_WRAP_CLOSE = "</a>";
	
	// Derby constants
	public final String DB_NAME = "LyraDB";
	public final String DB_DATASOURCE = "java:comp/env/jdbc/LyraDatasource";
	public final String PROTOCOL = "jdbc:derby:"; 
	
	// SQL statements
	public final String CREATE_ACCOUNT_TABLE = "CREATE TABLE ACCOUNT(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
																  + "USERNAME VARCHAR(10) NOT NULL,"
																  + "PASSWORD VARCHAR(8) NOT NULL,"
																  + "NICKNAME VARCHAR(20) NOT NULL,"
																  + "DESCR VARCHAR(500),"
																  + "PHOTO VARCHAR(256),"
																  + "PRIMARY KEY (ID),"
																  + "UNIQUE (USERNAME, NICKNAME))";
	
	public final String CREATE_POST_TABLE = "CREATE TABLE POST(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
															+ "AUTHOR_ID INTEGER NOT NULL, "
														    + "POST_TEXT VARCHAR(500), "
														 	+ "POST_TIME BIGINT NOT NULL, "
															+ "REPUBS INTEGER, "
															+ "PRIMARY KEY (ID), "
															+ "FOREIGN KEY (AUTHOR_ID) REFERENCES ACCOUNT (ID))";
	
	public final String CREATE_TOPIC_TABLE = "CREATE TABLE TOPIC(ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
															  + "TOPIC_TEXT VARCHAR(140) NOT NULL, "
															  + "POST_ID INTEGER NOT NULL, "
															  + "PRIMARY KEY (ID), "
															  + "FOREIGN KEY (POST_ID) REFERENCES POST (ID))";
	
	public final String CREATE_FOLLOW_TABLE = "CREATE TABLE FOLLOW(FOLLOWING_ID INTEGER NOT NULL, "
																+ "FOLLOWED_ID INTEGER NOT NULL, "
																+ "PRIMARY KEY (FOLLOWING_ID, FOLLOWED_ID), "
																+ "FOREIGN KEY (FOLLOWING_ID) REFERENCES ACCOUNT (ID), "
																+ "FOREIGN KEY (FOLLOWED_ID) REFERENCES ACCOUNT (ID))";
	  
	public final String INSERT_NEW_USER = "INSERT INTO ACCOUNT VALUES(DEFAULT, ?, ?, ?, ?, ?)";
	public final String INSERT_POST = "INSERT INTO POST VALUES(DEFAULT, ?, ?, ?, 0)";
	public final String INSERT_TOPIC = "INSERT INTO TOPIC VALUES(DEFAULT, ?, ?)";
	public final String INSERT_FOLLOW = "INSERT INTO FOLLOW VALUES(?, ?)";	
	
	
	public final String SELECT_ALL_ACCOUNT_STMT = "SELECT * FROM ACCOUNT";
	public final String SELECT_PWD_BY_UN_ACCOUNT_STMT = "SELECT PASSWORD FROM ACCOUNT WHERE USERNAME = ?";
	public final String SELECT_ACCOUNT_BY_UN_ACCOUNT_STMT = "SELECT * FROM ACCOUNT WHERE USERNAME = ?";
	public final String SELECT_ACCOUNT_BY_NICK_ACCOUNT_STMT = "SELECT * FROM ACCOUNT WHERE NICKNAME = ?";
	
	public final String SELECT_POST_BY_ID_POST_STMT = "SELECT * FROM POST WHERE ID = ?";
	
	public final String SELECT_10_POSTS_BY_AUTHOR_POST_STMT = "SELECT p.ID, p.AUTHOR_ID, a.NICKNAME, p.POST_TEXT, p.POST_TIME, p.REPUBS " + 
															  "FROM POST p, ACCOUNT a " +
															  "WHERE (p.AUTHOR_ID = a.ID) AND ((p.AUTHOR_ID = ?) OR (p.AUTHOR_ID IN (SELECT FOLLOWED_ID " +
																																	"FROM FOLLOW " + 
																																	"WHERE FOLLOWING_ID = ?))) " +
															  "ORDER BY p.POST_TIME DESC";
	
	public final String SELECT_POSTS_BY_OTHER_AUTHORS_POST_STMT = "SELECT p.ID, p.AUTHOR_ID, a.NICKNAME, p.POST_TEXT, p.POST_TIME, p.REPUBS " + 
																  "FROM POST p, ACCOUNT a " +
																  "WHERE (p.AUTHOR_ID <> ?) AND (p.AUTHOR_ID = a.ID) " +
																  "ORDER BY p.POST_TIME DESC";
	
	public final String SELECT_POSTS_BY_OTHER_FOLLOWED_AUTHORS_POST_STMT = "SELECT p.ID, p.AUTHOR_ID, a.NICKNAME, p.POST_TEXT, p.POST_TIME, p.REPUBS " + 
																  "FROM POST p, ACCOUNT a " +
																  "WHERE (p.AUTHOR_ID = a.ID) AND (p.AUTHOR_ID IN (SELECT FOLLOWED_ID " +
																												  "FROM FOLLOW " + 
																												  "WHERE FOLLOWING_ID = ?)) " +
																  "ORDER BY p.POST_TIME DESC";
	
	public final String SELECT_AUTHOR_POPULARITY_FOLLOW_STMT = "SELECT f.FOLLOWED_ID, COUNT(f.FOLLOWING_ID) " +
															   "FROM FOLLOW f " +
															   "GROUP BY f.FOLLOWED_ID";
	
	public final String SELECT_FOLLOWED_AUTHOR_POPULARITY_FOLLOW_STMT = "SELECT f.FOLLOWED_ID, COUNT(f.FOLLOWING_ID) " +
																	    "FROM FOLLOW f " +
																	    "WHERE (f.FOLLOWED_ID IN (SELECT FOLLOWED_ID FROM FOLLOW WHERE FOLLOWING_ID = ?)) " +			
																	    "GROUP BY f.FOLLOWED_ID";
	
	public final String SELECT_FOLLOWERS_POPULARITY_FOLLOW_STMT = "SELECT f.FOLLOWED_ID, COUNT(f.FOLLOWING_ID) " +
																  "FROM FOLLOW f " +
																  "WHERE (f.FOLLOWED_ID IN (SELECT FOLLOWING_ID FROM FOLLOW WHERE FOLLOWED_ID = ?))" +			
																  "GROUP BY f.FOLLOWED_ID";
														
	public final String SELECT_10_POSTS_BY_SELECTED_USER_POST_STMT = "SELECT p.ID, p.AUTHOR_ID, a.NICKNAME, p.POST_TEXT, p.POST_TIME, p.REPUBS "+
																	"FROM POST p, ACCOUNT a " + 
																	"WHERE (p.AUTHOR_ID = ?) AND (p.AUTHOR_ID = a.ID) " +
																	"ORDER BY p.POST_TIME DESC";
	
	public final String SELECT_10_FOLLOWERS_USER_FOLLOW_STMT = "SELECT f1.FOLLOWED_ID, f2.POP "+
															   "FROM FOLLOW f1, (SELECT FOLLOWED_ID, COUNT(FOLLOWING_ID) as POP FROM FOLLOW GROUP BY FOLLOWED_ID) f2 " +
															   "WHERE (f1.FOLLOWING_ID = ?) AND (f2.FOLLOWED_ID = f1.FOLLOWING_ID)";

	public final String SELECT_ALL_USER_FOLLOWING_FOLLOW_STMT = "SELECT  a.ID, a.USERNAME , a.PASSWORD , a.NICKNAME , a.DESCR , a.PHOTO " +
															    "FROM  ACCOUNT a, (SELECT FOLLOWED_ID, COUNT(FOLLOWING_ID) as POP "+
																"FROM FOLLOW " +
																"WHERE FOLLOWED_ID IN (SELECT FOLLOWED_ID FROM FOLLOW WHERE FOLLOWING_ID = ?) "+
																"GROUP BY FOLLOWED_ID " +
																"ORDER BY POP DESC) f "+
																"WHERE (f.FOLLOWED_ID = a.ID)";
	
	public final String SELECT_ALL_USER_FOLLOWERS_FOLLOW_STMT = "SELECT a.ID, a.USERNAME, a.PASSWORD, a.NICKNAME, a.DESCR, a.PHOTO " +
																"FROM FOLLOW f, ACCOUNT a " +
																"WHERE (f.FOLLOWED_ID = ?) AND (f.FOLLOWING_ID = a.ID)";
	
	public final String SELECT_10_LATEST_POSTS_BY_TOPIC_POST_STMT = "SELECT p.ID, p.AUTHOR_ID, a.NICKNAME, p.POST_TEXT, p.POST_TIME, p.REPUBS " +
																	"FROM TOPIC t, POST p, ACCOUNT a " +
																	"WHERE (t.TOPIC_TEXT = ?) AND (t.POST_ID = p.ID) AND (p.AUTHOR_ID = a.ID) " +
																	"ORDER BY p.POST_TIME DESC";	
	
	public final String SELECT_NUUMBER_OF_FOLLOWERS_FOLLOW_STMT= "SELECT DISTINCT COUNT(FOLLOWING_ID) "+
																 "FROM FOLLOW " +
																 "WHERE (FOLLOWED_ID = ?) "+
																 "GROUP BY FOLLOWED_ID";
			
	public final String SELECT_NUUMBER_OF_FOLLOWING_FOLLOW_STMT= "SELECT COUNT(FOLLOWED_ID) "+
																 "FROM FOLLOW " +
																 "WHERE (FOLLOWING_ID = ?) "+
																 "GROUP BY FOLLOWING_ID";
															
		
	public final String UPDATE_INC_POST_REPUBS_POST_STMT = "UPDATE POST SET REPUBS = REPUBS + 1 WHERE ID = ?";		
}
