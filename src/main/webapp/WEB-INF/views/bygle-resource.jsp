<%@page session="true"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><html version="XHTML+RDFa 1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/">
<head data-color="${colorPair}" profile="http://www.w3.org/1999/xhtml/vocab">
<title>${results.getTitle()}&mdash;byg.portal</title>
<%-- asking lodview to load map scripts --%>${results.setLatitude("0") }
<jsp:include page="inc/header.jsp"></jsp:include>
<link href="${conf.getStaticResourceURL()}css/bygle-portal.css" rel="stylesheet" type="text/css" />
<!-- managing maps  -->
<script src="${conf.getStaticResourceURL()}leaflet/leaflet.markercluster.js"></script>
</head>
<body id="top">
	<article>
		<div id="logoBanner">
			<div id="logo">
				<!-- placeholder for logo -->
			</div>
		</div>
		<header>
			<hgroup>
				<h1>
					<span>${results.getTitle()}</span>
				</h1>
				<h2>
					<a class="iri" href="${results.getMainIRI()}">${results.getMainIRI()}</a> <span class="istance"> <c:forEach end="0" items='${results.getResources(results.getMainIRI()).get(results.getTypeProperty())}' var="el">
							<a title="&lt;${el.getValue()}&gt;" href="${el.getUrl()}" <c:if test="${!el.isLocal()}">target="_blank" </c:if>> <c:choose>
									<c:when test='${el.getNsValue().startsWith("null:")}'>&lt;${el.getValue().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;
					</c:when>
					<c:otherwise>
						<span class="istanceOf"><sp:message code='label.anEntityOfType' text='an entity of type' />:</span> ${el.getNsValue().replaceAll(".+:","<span>")}</span>
					</c:otherwise>
					</c:choose>
					</a>
					</c:forEach>
					</span>
					<div id="seeOnLodlive" class="sp">
						<a title="view resource on lodlive" target="_blank" href="${lodliveUrl }"></a>
					</div>
				</h2>
			</hgroup>
			<c:choose>
				<c:when test="${results.getDescriptionProperty() != null}">
					<div id="abstract">
						<label class="c1"> <a data-label="${results.getDescriptionProperty().getLabel()}" data-comment="${results.getDescriptionProperty().getComment()}" href="${results.getDescriptionProperty().getPropertyUrl()}"> <c:choose>
									<c:when test='${results.getDescriptionProperty().getNsProperty().startsWith("null:")}'>&lt;${results.getDescriptionProperty().getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
									<c:otherwise>${results.getDescriptionProperty().getNsProperty().replaceAll(":",":<span>")}</span>
									</c:otherwise>
								</c:choose>
						</a>
						</label>
						<div class="c2 value">
							<c:forEach items='${results.getLiterals(results.getMainIRI()).get(results.getDescriptionProperty())}' var="el">
								<div class="lang ${el.getLang()}" data-lang="${el.getLang()}">
									${el.getValue()}
									<c:if test='${el.getDataType()!=null && !el.getDataType().equals("")}'>
										<span class="dType">${el.getNsDataType()}</span>
									</c:if>
								</div>
							</c:forEach>
						</div>
					</div>
				</c:when>
				<c:otherwise>
					<div id="abstract" class="empty"></div>
				</c:otherwise>
			</c:choose>
		</header>

		<aside class="empty"></aside>
		<div id="directs" class="bygportal">
			<div class="c1">
				<h3>
					<sp:message code='title.resources' text='resources' />
				</h3>
			</div>
			<div class="c2">
				<c:forEach items='${result.keySet()}' var="prop">
					<label class="c3"> <a data-label="${prop.getLabel()}" href="${prop.getPropertyUrl()}" data-comment="${prop.getComment()}"><c:choose>
								<c:when test='${prop.getNsProperty().startsWith("null:")}'>&lt;${prop.getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
								<c:otherwise>${prop.getNsProperty().replaceAll(":",":<span>")}</span>
								</c:otherwise>
							</c:choose></a>
					</label>
					<div class="c4 value isOpened">
						<a href="${prop.getPropertyUrl()}" class="bygpaginator" data-property="${prop.getNsProperty()}">${resultCount.get(prop).getValue() } <sp:message code='label.resources' text='resources' /></a><span class="lloadingb" style="display: none"></span>
						<c:forEach items='${result.get(prop)}' var="iel">
							<div class="toOneLine">
								<a href="${Misc.toBrowsableUrl(iel.getIRI(), conf)}">${iel.getValue()}</a>
							</div>
						</c:forEach>
					</div>
				</c:forEach>
			</div>
		</div>
		<div id="bygmapcnt">
			<div id="bygmap"></div>
		</div>
		<div id="directs" class="bygportal">
			<div class="c1">
				<h3>
					<sp:message code='title.facets' text='facets' />
				</h3>
			</div>
			<div class="c2">
				<c:forEach items='${facets.keySet()}' var="prop">
					<label class="c3"> <a data-label="${prop.getLabel()}"  href="${prop.getPropertyUrl()}"  data-comment="${prop.getComment()}"><c:choose>
								<c:when test='${prop.getNsProperty().startsWith("null:")}'>&lt;${prop.getProperty().replaceAll("([#/])([^#/]+)$","$1<span>$2")}</span>&gt;</c:when>
								<c:otherwise>${prop.getNsProperty().replaceAll(":",":<span>")}</span>
								</c:otherwise>
							</c:choose></a>
					</label>
					<div class="c4 value isOpened">
						<c:forEach items='${facets.get(prop)}' var="facet">
							<div class="toNobreakLine">
								<a href="?${facet.getAlias()}=${facet.getValue()}">${facet.getLabel()} <span>${facet.getCount() }</span></a>
							</div>
						</c:forEach>
					</div>
				</c:forEach>
			</div>
		</div>
		<jsp:include page="inc/custom_footer.jsp"></jsp:include>
	</article>
	<jsp:include page="inc/footer.jsp"></jsp:include>
	<c:import url="inc/scripts.jsp"></c:import>
	<c:import url="inc/bygle-scripts.jsp"></c:import>
	<div id="loadPanel">
		<p id="lmessage">
			<span class="lloading"></span><span class="content">&nbsp;</span>
		</p>
	</div>
	<div id="navigator">
		<div class="up sp"></div>
		<div class="top sp"></div>
		<div class="down sp"></div>
	</div>
</body>
</html>