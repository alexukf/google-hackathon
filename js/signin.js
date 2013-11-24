/**
 * Method called after if the user is signed in
 *
 * @param {type} authResult
 * @returns {undefined}
 */
function signinCallback(authResult) {
	if (authResult['access_token']) {
		// Store the returned token
        gapi.auth.setToken(authResult);

		console.log("ACCESS TOKEN:");
		console.log(authResult['access_token']);
		// Update the app to reflect a signed in user
		welcomeUser();
		getDatastoreData(authResult['access_token']);
		// Hide the sign-in button now that the user is authorized, for example:
		toggleElement("signinButton");
		toggleElement("signOutButton");
	} else if (authResult['error']) {
		// Update the app to reflect a signed out user
		// Possible error values:
		//   "user_signed_out" - User is signed-out
		//   "access_denied" - User denied access to your app
		//   "immediate_failed" - Could not automatically log in the user
		console.log('Sign-in state: ' + authResult['error']);
	}
}

/**
 * Signs out and reloads the page
 *
 * @returns {undefined}
 */
function signOut() {
	gapi.auth.signOut();
	location.reload();
}

/*
 * Initiates the request to the userinfo endpoint to get the user's display
 * name. This function relies on the gapi.auth.setToken containing a valid
 * OAuth access token.
 *
 * When the request completes, the getEmailCallback is triggered and passed
 * the result of the request.
 */
function welcomeUser(){
	gapi.client.load('plus','v1', function(){
		var request = gapi.client.plus.people.get({
		  'userId': 'me'
		});

		request.execute(function(resp) {
			setDisplayName(resp.displayName);
		});
	});
}



function getDatastoreData(accessToken){
	var clientId = '357201289067-qc1is393arr830dpf2ndm6uj7kmndhps.apps.googleusercontent.com';
	var datasetId = 'stone-notch-404';
	var scopes = 'https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/datastore';
	var apiKey = "AIzaSyC5zVaJwyeC9k5H82Ka1JQEWfBJeKxGRyg";

	gapi.client.setApiKey(apiKey);
	gapi.auth.authorize(
		{client_id: clientId, scope: scopes, immediate: false},
		function(authResult) {
		  if (authResult && ! authResult.error) {
			gapi.client.load('datastore', 'v1beta1', function() {
			  gapi.client.datastore.datasets.runQuery({
				'datasetId': datasetId,
				gqlQuery: {
					queryString: 'SELECT * FROM Hackathon'
				}
			  }).execute(function(resp) {
				  console.log("getDatastoreData");
				  console.log(resp);
			  });
			});
		  }
		});
}

/**
 * Modifies the welcome message
 *
 * @param {type} obj
 * @returns {undefined} */
function setDisplayName(obj){
	var welcome = document.getElementById('welcomeMessage');
	if (obj != null) {
		welcome.innerHTML = "Welcome, " + obj;
	}
}

/**
 * Displays or hides an element on the page
 *
 * @param {type} id
 * @returns {undefined}
 */
function toggleElement(id) {
	var el = document.getElementById(id);
	if (el.getAttribute('class') == 'hide') {
		el.setAttribute('class', 'show');
	} else {
		el.setAttribute('class', 'hide');
	}
}
