package lyra;

import java.util.Comparator;
import java.util.Map;

import lyra.model.User;

/**
 * Compare two users according to the popularity formula: log(2 + userFollowers)
 */
public class UserCompartor implements Comparator<User>
{
	Map<Integer, Integer> userPopularity; // Popularity map counting user followers
	
	/**
	 * @param userPopularity A popularity map
	 */
	public UserCompartor(Map<Integer, Integer> userPopularity)
	{
		this.userPopularity = userPopularity;
	}

	@Override
	public int compare(User u1, User u2)
	{
		Integer user1Pop = userPopularity.get(u1.getId());
		user1Pop = (user1Pop == null) ? 0 : user1Pop;
		
		Integer user2Pop = userPopularity.get(u2.getId());
		user2Pop = (user2Pop == null) ? 0 : user2Pop;
		
		// Apply formula
		double u1Pop = (Math.log(2 + user1Pop));
		double u2Pop = (Math.log(2 + user2Pop));
		
		// Compare
		if (u1Pop > u2Pop)
		{
			return -1;
		}
		else if (u2Pop > u1Pop)
		{
			return 1;			
		}
		else
		{
			return 0;
		}
	}
}
