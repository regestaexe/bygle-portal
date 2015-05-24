<%@page session="true"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%>
<script>
	$(function() {
		$(window).on('resize', function() {
		});
		$(window).on('load', function() {
		});
		bygleportal.loadMap();
		bygleportal.addMarkers((document.location.search + "&IRI=${results.getMainIRI()}").replace(/^&/, '?'));
		bygleportal.initPaginators();
	});

	var bygleportal = {

		initPaginators : function() {
			$('.bygpaginator').each(function() {
				var anchor = $(this);
				var count = parseInt(anchor.attr('data-tot'), 10);
				var queryId = anchor.attr("data-queryId");

				if (count > 10) {
					var prev = $('<a href="#prev" class="prevArrow sp"></a>');
					var next = $('<a href="#next" class="nextArrow sp"></a>');
					prev.css({
						'opacity' : '0.3',
						'cursor' : 'default'
					});
					next.click(function() {
						return bygleportal.paginating('next', $(this), 0, queryId, count);
					});
					anchor.after(next);
					anchor.after(prev);
				}

			});
		},
		paginating : function(direction, anchor, start, queryId, count) {
			if (direction == 'next') {
				start = start + 10;
			} else if (start > 0) {
				start = start - 10;
			}
			var contInverse = anchor.parent();
			if (callingPage) {
				callingPage.abort();
			}
			if (callingPageTitles) {
				callingPageTitles.abort();
			}
			callingPage = $.ajax({
				url : "${conf.getPublicUrlPrefix()}byg.paginator",
				method : 'GET',
				data : {
					"offset" : start,
					"IRI" : "${results.getMainIRI()}",
					"queryId" : queryId,
					"tot" : count
				},
				beforeSend : function() {
					contInverse.find('.toOneLine').addClass('toRemove').css({
						'opacity' : 0.2
					});
					contInverse.find('.prevArrow,.nextArrow').remove();
					contInverse.find('.lloadingb').show();
					var prev = $('<a href="#prev" class="prevArrow sp"></a>');
					var next = $('<a href="#next" class="nextArrow sp"></a>');
					if (start + 10 > count) {
						next.css({
							'opacity' : '0.3',
							'cursor' : 'default'
						});
					} else {
						next.click(function() {
							return bygleportal.paginating('next', $(this), start, queryId, count);
						});
					}
					if (start > 0) {
						prev.click(function() {
							return bygleportal.paginating('prev', $(this), start, queryId, count);
						});
					} else {
						prev.css({
							'opacity' : '0.3',
							'cursor' : 'default'
						});
					}
					contInverse.find('a:first').after(next);
					contInverse.find('a:first').after(prev);
				},
				success : function(data) {

					$.each(data, function(k1, v1) {
						$.each(v1, function(k, v) { 
							var ele = $('<div class="toOneLine" style="display:none"><a href="'+v.url+'">' + v.value + '</a></div>');
							contInverse.append(ele)
						});
					});

					contInverse.find('.toOneLine.toRemove').remove();
					$('.toOneLine', contInverse).show();
					contInverse.find('.lloadingb').hide();
					anchor.unbind('click');
				},
				complete : function() {
				},
				error : function() {
					contInverse.find('.toOneLine').remove();
					contInverse.append($("<div class='toOneLine' >sorry, an error occurred</div>"));
				}
			});
			return false;
		},
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
				url : '${conf.getPublicUrlPrefix()}byg.map/data' + datasearch,
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
					if (markersList.length > 0) {
						bygleportal.map.addLayer(markers);
						bygleportal.map.fitBounds(bounds);
					} else {
						$('#bygmap').slideUp('slow');
					}
				}
			});

		}

	};
</script>
