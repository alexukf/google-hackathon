
var coordinates = new Array();
coordinates[0] = new Array();
coordinates[1] = new Array();
coordinates[0]['msg'] = "mesaj1";
coordinates[0]['lat'] = -25.363882;
coordinates[0]['lng'] = 131.044922;
coordinates[1]['msg'] = "mesaj2";
coordinates[1]['lat'] = -24.363882;
coordinates[1]['lng'] = 132.044922;



function initialize(coord) {
	var lat = coord[0]['lat'];
	var lng = coord[0]['lng'];
	var myLatlng = new google.maps.LatLng(lat, lng);
	var mapOptions = {
	  zoom: 4,
		center: myLatlng
	}

	var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

	for (var i = 0; i < coord.length; i++) {
		var msg = coord[i]['msg'];
		var lat = coord[i]['lat'];
		var lng = coord[i]['lng'];
		var myLatlng = new google.maps.LatLng(lat, lng);

		var marker = new google.maps.Marker({
			position: myLatlng,
			map: map,
			title: msg
		});
	}
}


google.maps.event.addDomListener(window, 'load', initialize(coordinates));
