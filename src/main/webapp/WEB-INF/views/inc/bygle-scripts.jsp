<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%>
<script>
	$(function() {

		$(window).on('resize', function() {

		});

		$(window).on('load', function() {
		});
		bygleportal.loadMap();
	});

	var bygleportal = {

		loadMap : function() {
			var map = new L.Map('bygmap', {
				center : latlng = new L.LatLng(0, 0),
				scrollWheelZoom : false,
				zoom : 3
			});
			L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
				attribution : '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(map);
			this.addMarkers(map);
		},
		addMarkers : function(map) {

			/* map *//* 	L.DomUtil.get('populate').onclick = function() {
			 var bounds = map.getBounds(), southWest = bounds.getSouthWest(), northEast = bounds.getNorthEast(), lngSpan = northEast.lng - southWest.lng, latSpan = northEast.lat - southWest.lat;
			 var m = new L.Marker(new L.LatLng(southWest.lat + latSpan * 0.5, southWest.lng + lngSpan * 0.5));
			 markersList.push(m);
			 markers.addLayer(m);
			 };
			 L.DomUtil.get('remove').onclick = function() {
			 markers.removeLayer(markersList.pop());
			 }; */
			var markers = new L.MarkerClusterGroup();
			var markersList = [];

			function populateRandomVector(map) {
				for (var i = 0, latlngs = [], len = 20; i < len; i++) {
					latlngs.push(getRandomLatLng(map));
				}
				var path = new L.Polyline(latlngs);
				map.addLayer(path);
			}
			function getRandomLatLng(map) {
				var bounds = map.getBounds(), southWest = bounds.getSouthWest(), northEast = bounds.getNorthEast(), lngSpan = northEast.lng - southWest.lng, latSpan = northEast.lat - southWest.lat;

				return new L.LatLng(southWest.lat + latSpan * Math.random(), southWest.lng + lngSpan * Math.random());
			}

			markers.on('clusterclick', function(a) {
				alert('cluster ' + a.layer.getAllChildMarkers().length);
			});
			markers.on('click', function(a) {
				alert('marker ' + a.layer);
			});

			for (var i = 0; i < 100; i++) {
				var m = new L.Marker(getRandomLatLng(map));
				markersList.push(m);
				markers.addLayer(m);
			}
			map.addLayer(markers);

		}

	};
</script>
