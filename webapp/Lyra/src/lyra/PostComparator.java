package lyra;

import java.util.Comparator;
import java.util.Map;

import lyra.model.Post;

/**
 * Compare two posts according to the popularity formula: log(2 + authorFollowers)*log(2 + repubs)
 *
 */
public class PostComparator implements Comparator<Post>
{
	Map<Integer, Integer> authorPopularity; // Popularity map counting user followers

	
	/**
	 * @param authorPopularity A popularity map
	 */
	public PostComparator(Map<Integer, Integer> authorPopularity)
	{
		this.authorPopularity = authorPopularity;
	}

	@Override
	public int compare(Post p1, Post p2)
	{
		Integer author1Pop = authorPopularity.get(p1.getAuthorId());
		author1Pop = (author1Pop == null) ? 0 : author1Pop;
		
		Integer author2Pop = authorPopularity.get(p2.getAuthorId());
		author2Pop = (author2Pop == null) ? 0 : author2Pop;
	
		// Apply formula
		double p1Pop = (Math.log(2 + author1Pop) * Math.log(2 + p1.getRepubs()));
		double p2Pop = (Math.log(2 + author2Pop) * Math.log(2 + p2.getRepubs()));
		
		// Compare
		if (p1Pop > p2Pop)
		{
			return -1;
		}
		else if (p2Pop > p1Pop)
		{
			return 1;			
		}
		else
		{
			return 0;
		}
	}
}
