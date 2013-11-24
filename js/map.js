
var coordinates = new Array();
coordinates[0] = new Array();
coordinates[1] = new Array();
coordinates[0]['msg'] = "";
coordinates[0]['lat'] = 44.4461438;
coordinates[0]['lng'] = 26.0977535;
coordinates[1]['msg'] = "";
coordinates[1]['lat'] = 44.4461428;
coordinates[1]['lng'] = 26.0977525;

function initialize(coord) {
	var lat = coord[0]['lat'];
	var lng = coord[0]['lng'];
	var myLatlng = new google.maps.LatLng(lat, lng);
	var mapOptions = {
	  zoom: 15,
		center: myLatlng
	}

	map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

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
		markersArray.push(marker);
		google.maps.event.addListener(marker,"click",function(){});
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
}


google.maps.event.addDomListener(window, 'load', initialize(coordinates));
