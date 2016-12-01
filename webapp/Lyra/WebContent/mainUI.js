(function() {
	var app = angular.module('mainApp', []);
	
	// A custom filter for displaying time in a "time ago" format
	app.filter('timeAgo', function() {
	    return function(input) {       		
	    		date = (new Date(input));
	    		var dateDiff = (new Date()) - input;
	
	        	// Time vars
	        	var oneSecond = 1000;
	         	var oneMinute = 1000*60;
	        	var oneHour = 1000*60*60;
	         	var oneDay = 1000*60*60*24;
	        	var fullDays = Math.floor(dateDiff/oneDay);
	        	var fullHours = Math.floor( (dateDiff - fullDays*1000*60*60*24)/(oneHour) );
	        	 	
	         	var fullMinutes =Math.floor((dateDiff - fullDays*1000*60*60*24 - fullHours*1000*60*60)/oneMinute);     	
	        	var fullSeconds = Math.floor( (dateDiff - fullDays*1000*60*60*24 - fullHours*1000*60*60 - fullMinutes*1000*60) / oneSecond);
	        	var minFix= ( (date.getMinutes() < 10) ? '0' : '' );
	
	        	// Define the right format of the time
	        	if (fullDays > 0)
	    		{
	        		return (date.getDate() +  '/' +(date.getMonth() + 1) +'/'+date.getFullYear()+' at ' +date.getHours()+ ':' + minFix+date.getMinutes());		
	    		}	
	        	else if (fullHours > 0)
	    		{
	    			return (fullHours + ' hours ago');
	    		}
	    		else if (fullMinutes > 0)
	    		{
	    			return (fullMinutes + ' minutes ago');	
	    		}
	    		else if (fullSeconds > 0)
	    		{
	    			return (fullSeconds + ' seconds ago');	
	    		}
	    		else
	    		{
	    			return 'Just now';
	    		}	 		        	
	  	}
	});

	
	// The main controller responsible for mainUI
	app.controller('mainController', function($scope, $http, $sce, $interval) {
		
		// Load current user info and load posts
		$(document).ready(function ()
		{
			$(window).load(function ()
			{			
				$.ajax({
					type: "POST",
					url: "/Lyra/NewsFeedServlet",
					success: function(res, status, xhr)
					{
						var userObj = JSON.parse(res);
						$scope.user = userObj;
	
						$scope.getCurrUserFollowing();
						$scope.readPosts();
						
						// Start the update timer
						$scope.updatePosts();
					}
				});
			})
		});
		
		
		var postUpdatePromise;
		
		// Update posts every 30 secs
		$scope.updatePosts = function() {
			postUpdatePromise = $interval(function() {
	        	$scope.readPosts();
	        	$scope.discoverPosts();
	        }, 30000);
	    };
		
	    
		var topicUpdatePromise;
		
		// Check topics update every 30 secs
		$scope.checkUpdateTopic = function() {
			topicUpdatePromise = $interval(function() {
				// Request the last 10 posts for the wanted topic
	        	$.ajax({
	    			type: "POST",
	    			url: "/Lyra/TopicServlet",
	    			data: {topicToView : $scope.viewTopic},
	    			success: function(res, status, xhr)
	    			{
	    				var newPosts = false;
						var postsObj = JSON.parse(res);
						
						// Check for new posts - since they are sorted, a difference in length of in the first
						// post means an update is available
						if (postsObj.length != $scope.topicTenPosts.length)
						{
							newPosts = true;
						}
						else if (postsObj[0].id != $scope.topicTenPosts[0].id)
						{
							newPosts = true;
						}
						
						if (newPosts)
						{
							// HTML Sanitation
							for (var i = 0; i < postsObj.length; i++)
						    {
								postsObj[i].postText = $sce.trustAsHtml(postsObj[i].postText);	
						    }
							
							$scope.waitingTopicTenPosts = postsObj;
							
							// Show pop-up
							$("#topicInfo").removeClass('hidden');
						}					
	    			},
	    		});
	        }, 30000);
	    };
	      
	    
	    // Apply the waiting update to topic view
	    updateTopic = function()
	  	{
	    	$("#topicInfo").addClass('hidden');
	    	$(window).scrollTop(0);
	    	
		    $scope.$apply(function(){
		    	$scope.topicTenPosts = $scope.waitingTopicTenPosts;
			});  	
	  	}
	    
	    // Show the last 10 posts for a selected topic
	    showTopic = function(topic)
		{
			$("#topicInfo").addClass('hidden');
			
			// Switch to the 'Topic view' tab
			$scope.$apply(function(){
				$scope.setTab(5);			  
			});
				  
			// Request the last 10 posts
		    $.ajax({
				  type: "POST",
				  url: "/Lyra/TopicServlet",
				  data: {topicToView : topic},
				  success: function(res, status, xhr)
				  {	
					  var postsObj = JSON.parse(res);
						
					  // HTML Sanitation
					  for (var i = 0; i < postsObj.length; i++)
					  {
						  postsObj[i].postText = $sce.trustAsHtml(postsObj[i].postText);	
					  }
	
					  // Define topic and load its posts
					  $scope.$apply(function(){
						  $scope.viewTopic = topic;
						  $scope.topicTenPosts = postsObj;
					  });
					    
					  $scope.checkUpdateTopic();
				  }
			  });  
		}
	
	
		// General tabs functionality
		$scope.tab = 1;
		
		$scope.isSet = function(pTab) {
		  return $scope.tab === pTab;
		};
		
		$scope.setTab = function(pTab) {
			$scope.tab = pTab;	
		};
		
		
		// Profile tabs functionality
		$scope.profTab = 1;	
	    
	    $scope.isProfTabSet = function(pTab) {
	      return $scope.profTab === pTab;
	    };	
	    
	    $scope.setProfTab = function(pTab) {
	    	$scope.profTab = pTab;    	
	    };
		
	    
	    // 'Only follower posts' checkbox in Discovery view
		$scope.checkboxModel = {
			       value : false
		};
			
		// Load the data to the republish modal 
		$('#repubModal').on('show.bs.modal', function (event) {
			
			var button = $(event.relatedTarget); 
			var postIndex = button.data('whatever');
  
			// Take the posts from the correct list according to current tab
		  	if ($scope.isSet(1))
			{
				$scope.repubList = $scope.recentPosts;
			}
			else if ($scope.isSet(2))
			{
				$scope.repubList = $scope.topPosts;
			}
			else if ($scope.isSet(3) || $scope.isSet(4))
			{
				$scope.repubList = $scope.userTenPosts;
			}
			else if ($scope.isSet(5))
			{
				$scope.repubList = $scope.topicTenPosts;
			}
			  
		  	// Load the original message to the modal
			$scope.$apply(function(){
				$scope.rePost = $scope.repubList[postIndex];
			});  
		});
	
		
		// Post a republish post
		$scope.writeRepublish = function()
		{
			var pText = ($scope.rePost.postText).toString();
			var pId = ($scope.rePost.id).toString();
			
			// Write the post to the server
			$.ajax({
				type: "POST",
				url: "/Lyra/NewsFeedWriteServlet/repub",
				data: {postId : pId, textToPost : pText},
				success: function(res, status, xhr)
				{
					// Update views
					$scope.readPosts();
					$scope.discoverPosts();
					$scope.readUserPosts();
				},
			});
		}
	
	
		// Post a new comment
		$scope.postNewComment = function ()
		{	
			var inputText = $("#taComment").val();
			
			// Write the post to the server
			$.ajax({
				type: "POST",
				url: "/Lyra/NewsFeedWriteServlet",
				data: {textToPost : inputText},
				success: function(res, status, xhr)
				{
					// Update view
					$scope.readPosts();
				},
			});
			
			// Clean the textArea
			$("#taComment").val('');
		}
		
		
		// Get last 10 posts of self and followed users for the Newsfeeds(Home) view
		$scope.readPosts = function ()
		{	
			$http.post("/Lyra/NewsFeedReadServlet")
			.success(function(response)
			{
				// HTML Sanitation
			    for (var i = 0; i < response.length; i++)
			    {
			    	response[i].postText = $sce.trustAsHtml(response[i].postText);
			    }
		
			    $scope.recentPosts = response;	    
			});	
		}
		
		
		// Get last 10 posts of the user which profile is being watched
		$scope.readUserPosts = function ()
		{	
			$.ajax({
				type: "POST",
				url: "/Lyra/UserLastPostsServlet",
				data: {authorId : $scope.profUser.id},
				success: function(res, status, xhr)
				{
					var postsObj = JSON.parse(res);
					
					// HTML Sanitation
					for (var i = 0; i < postsObj.length; i++)
				    {
						postsObj[i].postText = $sce.trustAsHtml(postsObj[i].postText);	
				    }
	
				    $scope.$apply(function(){
				    	$scope.userTenPosts = postsObj;
					});
				}
			});
		}
	
		// Load 10 top followers/users who are followed by the profile user
		$scope.showFollow = function ()
		{	
			var srvUrl = "/Lyra/UserFollowersServlet";
			
			// Choose followers/users who are followed according to the open profile tab
			if ($scope.isProfTabSet(3))
			{
				srvUrl += "/followers";
			}
			
			// Get top 10 of them
			$.ajax({
				type: "POST",
				url: srvUrl,
				data: {userID : $scope.profUser.id},
				success: function(res, status, xhr)
				{
					var userObj = JSON.parse(res);
				    
				    $scope.$apply(function(){
				    	  $scope.userTenFollowers = null;
					      $scope.userTenFollowers = userObj;	 
				    });
				}
			});
		}
		
		
		// Check whether the user which id is provided is already being followed by the current user
		$scope.isFollowing = function(uid)
		{
			var found = false;
			
			// Look of the given id in the list of followed users
			for (var i = 0; i < $scope.userAllFollowing.length; i++)
			{
				if ($scope.userAllFollowing[i].id == uid)
				{
					found = true;
					break;
				}			
			}
			
			return found;		
		}
		
		
		// Follow the user with the given id
		$scope.follow = function(uid)
		{		
			// Write a followership relation to the server
			$.ajax({
				type: "POST",
				url: "/Lyra/FollowersServlet",
				data: {followerId : $scope.user.id, followedId : uid},
				success: function(res, status, xhr)
				{
					// Update view
					$scope.getCurrUserFollowing();
					$scope.showFollow();
				}
			});		
		}
		
		
		// Get the list of users followed by the current user
		$scope.getCurrUserFollowing = function()
		{
			$.ajax({
				type: "POST",
				url: "/Lyra/CurrUserFollowingServlet",
				success: function(res, status, xhr)
				{
					var usersObj = JSON.parse(res);
					$scope.userAllFollowing = usersObj;
				}
			});	
		}
	
	
		// Load top 10 posts for Discovery view according to requested mode
		$scope.discoverPosts = function ()
		{	
			var srvUrl = "/Lyra/DiscoverReadServlet";
			
			// Choose 'all'/'followed users only' mode
			if ($scope.checkboxModel.value)
			{
				srvUrl += "/onlyfollow";
			}
			
			// Get the posts
			$http.post(srvUrl)
			.success(function(response)
			{
				var i;
				   
				// HTML Sanitation
			    for (i = 0; i < response.length; i++)
			    {
			    	response[i].postText = $sce.trustAsHtml(response[i].postText);
			    }
				
			    $scope.topPosts = response;
			});
		}
	
		// Start a reply to a given post
		$scope.writeReply = function(post)
		{
			$(window).scrollTop(0);
			$("#taComment").focus();
			$("#taComment").val('@' + post.authorName + ' ');	
		}
	
		// Load the profile information of a given user
		showProfile = function(nickname)
		{
			// Switch to the 'User profile' tab
			$scope.setProfTab(1);
			$scope.setTab(4);
	
			// Get the information
		    $.ajax({
				  type: "POST",
				  url: "/Lyra/ProfileServlet",
				  data: {viewProfile : nickname},
				  success: function(res, status, xhr)
				  {
					  var userObj = JSON.parse(res);
					  
					  $scope.$apply(function(){
						  $scope.profUser = userObj;
						  $(window).scrollTop(0);
					  });
					  
					  // Load user posts
					  $scope.readUserPosts();
					  
					  // Get following and followers count
					  var srvUrl = "/Lyra/FollowNumberServlet";
						$.ajax({
							type: "POST",
							url: srvUrl,
							data: {userID : $scope.profUser.id},
							success: function(res, status, xhr)
							{
								var followIsh = JSON.parse(res);
							    $scope.$apply(function(){
								      $scope.numOfFollowing = followIsh[0];
								      $scope.numOfFollowers = followIsh[1];
							    });
							}
						});
				  }
			  });  
		}
		
		// A reference to the 'showProfile' function, made accessible from within the controller's scope
		$scope.showProfileTunnel = function(nickname)
		{
			showProfile(nickname);
		}
		
		
		// Load current user profile information
		$scope.showCurrUserProfile = function()
		{
			
			$scope.profUser = $scope.user;	
			  
			// Get following and followers count
			var srvUrl = "/Lyra/FollowNumberServlet";
			$.ajax({
				type: "POST",
				url: srvUrl,
				data: {userID : $scope.profUser.id},
				success: function(res, status, xhr)
				{
				 var followIsh = JSON.parse(res);
				    $scope.$apply(function(){
				       $scope.numOfFollowing = followIsh[0];
				       $scope.numOfFollowers = followIsh[1];
				    });
				}
			});
		}
	
	});
})();