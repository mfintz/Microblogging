var app = angular.module('loginApp', []);
app.controller('LoginController', function($scope) {
	// Send login request to the server
	$scope.authenticate = function() {
		$.ajax({
			type: "POST",
			url: "/Lyra/login",
			data: $('#loginForm').serialize(),
			success: function(res, status, xhr)
			{
				window.location = "/Lyra/mainUI.html";
			},
			error: function(responseText)
			{
				$('#errorMsg').text("Wrong username or password");
			}
		});
	}
});


