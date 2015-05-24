<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%>
<script>
	$(function() {
		$(window).on('resize', function() {
		});
		$(window).on('load', function() {
		});
		bygleportal.loadMap();
		bygleportal.addMarkers((document.location.search + "&IRI=${results.getMainIRI()}").replace(/^&/, '?'));
	});

	var bygleportal = {

		loadMap : function(dataUrl) {
			var map = new L.Map('bygmap', {
				center : latlng = new L.LatLng(0, 0),
				scrollWheelZoom : false,
				zoom : 3
			});
			L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
				attribution : '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
			}).addTo(map);
			this.map = map;
		},
		addMarkers : function(datasearch) {

			$.ajax({
				url : '/bygle-portal/byg.map/data' + datasearch,
				success : function(data) {
					console.info(data);

					var markers = new L.MarkerClusterGroup();
					var markersList = [];
					var bounds = [];

					/* 			markers.on('clusterclick', function(a) {
									alert('cluster ' + a.layer.getAllChildMarkers().length);
								});
								markers.on('click', function(a) {
									alert('marker ' + a.layer);
								});
					 */
					$.each(data, function(k, v) {
						// a place
						try {
							var mdata = {};
							var m = new L.Marker(new L.LatLng(v.data['lat'][0], v.data['long'][0]), mdata);
							bounds.push([ v.data['lat'][0], v.data['long'][0] ]);

							var popContent = v.data['label'].join(', ') + '<br><br>' + '<a href="'+v.filterurl+'">use as filter</a> <br> or <a href="'+v.url+'">open resource: ' + (v.nsIRI.indexOf('null') == -1 ? v.nsIRI : k) + '</a>' + '<br>';
							m.bindPopup(popContent);
							markersList.push(m);
							markers.addLayer(m);
						} catch (e) {

						}
					});
					if (markers.length > 0) {
						bygleportal.map.addLayer(markers);
						bygleportal.map.fitBounds(bounds);
					}else{
						$('#bygmap').slideUp('slow');
					}
				}
			});

		}

	};
</script>
