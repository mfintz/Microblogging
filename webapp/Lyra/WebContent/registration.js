var app = angular.module('regApp', []);
app.controller('RegisterController', function($scope) {
	// Send registration request to the server
	$scope.register = function() {
		var checkDesc = 0;
		
		// Check number of words in the description field
		if ($scope.user.desc != undefined)
		{
			checkDesc = $scope.user.desc.trim().split(/\s+/).length;
		}
		
		if (checkDesc < 51)
		{
			// Send the request
			$.ajax({
				type: "POST",
				url: "/Lyra/register",
				data: $('#regForm').serialize(),
				success: function(res, status, xhr)
				{
					window.location = "/Lyra/mainUI.html";
				},
				error: function(responseText)
				{
					$('#errorMsg').text("Unexpected error in registration");
				}
			});
		}
		else
		{
			// Display description-related error message
			$('#descErr').text("too many words in the description");
			$('#desc').addClass("'has-error'")
		}
	}

});