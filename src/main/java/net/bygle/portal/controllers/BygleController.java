package net.bygle.portal.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.bygle.portal.bean.FacetBean;
import net.bygle.portal.conf.ConfigurationBean;
import net.bygle.portal.utils.Misc;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.builder.ResourceBuilder;
import org.dvcama.lodview.controllers.ErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UrlPathHelper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Controller
@RequestMapping(value = "**/html")
public class BygleController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	org.dvcama.lodview.conf.ConfigurationBean conf;

	@Autowired
	ConfigurationBean confBygle;

	@Autowired
	OntologyBean ontoBean;

	@RequestMapping(value = "")
	public Object resourceController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "output", defaultValue = "") String output, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return resource(conf, model, req, res, locale, output, "", colorPair);
	}

	public Object resource(org.dvcama.lodview.conf.ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String forceIRI, String colorPair) throws UnsupportedEncodingException {
		model.addAttribute("conf", conf);
		model.addAttribute("confBygle", confBygle);
		model.addAttribute("Misc", new Misc());

		String IRIsuffix = new UrlPathHelper().getLookupPathForRequest(req).replaceAll("/lodview/", "/");
		model.addAttribute("path", new UrlPathHelper().getContextPath(req).replaceAll("/lodview/", "/"));

		String IRIprefix = conf.getIRInamespace().replaceAll("/$", "");
		model.addAttribute("locale", locale.getLanguage());
		IRIsuffix = IRIsuffix.replaceAll(conf.getHttpRedirectSuffix() + "$", "");
		IRIsuffix = IRIsuffix.replaceAll("^/", "");

		String IRI = IRIprefix + "/" + IRIsuffix.replaceAll(" ", "%20");
		if (forceIRI != null && !forceIRI.equals("")) {
			IRI = forceIRI;
		}
		Map<String, String[]> filters = req.getParameterMap();

		Map<String, String> filtersAppender = new HashMap<String, String>();
		filtersAppender.put("main", Misc.urlMinusFilter(filters, confBygle.getFilters(), ""));

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + " in byg.portal ################# ");

		if (locale.getLanguage().equals("it")) {
			model.addAttribute("lodliveUrl", "http://lodlive.it?" + IRI.replaceAll("#", "%23"));
		} else if (locale.getLanguage().equals("fr")) {
			model.addAttribute("lodliveUrl", "http://fr.lodlive.it?" + IRI.replaceAll("#", "%23"));
		} else {
			model.addAttribute("lodliveUrl", "http://en.lodlive.it?" + IRI.replaceAll("#", "%23"));
		}

		try {
			List<String> queries = new ArrayList<String>();
			model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
			ResultBean r = new ResourceBuilder(messageSource).buildHtmlResource(IRI, locale, conf, ontoBean);
			model.addAttribute("results", Misc.guessClass(r, conf, ontoBean));
			model.addAttribute("ontoBean", ontoBean);
			enrichResponse(r, req, res);
			model.addAttribute("colorPair", Misc.guessColor(colorPair, r, conf));

			if (confBygle.getMainIRIs().contains(IRI)) {

				Model m = confBygle.getConfModel();
				net.bygle.portal.builder.ResourceBuilder builder = new net.bygle.portal.builder.ResourceBuilder(messageSource);

				/*************** ******** ***************/
				/*************** defaults ***************/
				/*************** ******** ***************/
				NodeIterator defaultIter = m.listObjectsOfProperty(m.createProperty(m.getNsPrefixURI("conf"), "defaultSearchBox"));
				String defaultMainQuery = null;
				String defaultMainCountQuery = null;
				while (defaultIter.hasNext()) {
					try {
						RDFNode node = defaultIter.next();

						NodeIterator mainQueryIter = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainQuery"));
						defaultMainQuery = mainQueryIter != null && mainQueryIter.hasNext() ? mainQueryIter.next().toString() : defaultMainQuery;

						NodeIterator mainCountQueryIter = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainCountQuery"));
						defaultMainCountQuery = mainCountQueryIter != null && mainCountQueryIter.hasNext() ? mainCountQueryIter.next().toString() : defaultMainCountQuery;

					} catch (Exception e) {
					}
				}
				/*************** ******** ***************/
				/*************** ******** ***************/

				/*************** ********* ***************/
				/*************** resources ***************/
				/*************** ********* ***************/

				{
					LinkedHashMap<PropertyBean, List<TripleBean>> result = new LinkedHashMap<PropertyBean, List<TripleBean>>();
					LinkedHashMap<PropertyBean, TripleBean> resultCount = new LinkedHashMap<PropertyBean, TripleBean>();
					NodeIterator iter = m.listObjectsOfProperty(m.createResource(IRI), m.createProperty(m.getNsPrefixURI("conf"), "searchBox"));
					while (iter.hasNext()) {
						try {

							RDFNode node = iter.next();
							RDFNode mainClasses = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainClasses")).next();
							NodeIterator mainQueryIter = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainQuery"));
							String mainQuery = mainQueryIter != null && mainQueryIter.hasNext() ? mainQueryIter.next().toString() : defaultMainQuery;

							NodeIterator mainCountQueryIter = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainCountQuery"));
							String mainCountQuery = mainCountQueryIter != null && mainCountQueryIter.hasNext() ? mainCountQueryIter.next().toString() : defaultMainCountQuery;
							String rdfclass = mainClasses.toString();

							PropertyBean p = Misc.generatePropertyBean(rdfclass, locale.getLanguage(), ontoBean, conf);

							// finding the page link for every classes

							for (String rdfpageclass : confBygle.getMultiConfValue(IRI, "mainClasses")) {
								if (rdfpageclass.equals(rdfclass)) {
									p.setPropertyUrl(Misc.toBrowsableUrl(IRI, conf));
								}

							}

							result.put(p, builder.buildHtmlMainClassSearch(Misc.parseFilters(filters, confBygle.getFilters(), mainQuery, locale.getLanguage()), rdfclass, -1, locale, conf, confBygle, ontoBean));
							resultCount.put(p, builder.buildHtmlMainClassCount(Misc.parseFilters(filters, confBygle.getFilters(), mainCountQuery, locale.getLanguage()), rdfclass, locale, conf, confBygle, ontoBean).get(0));

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					model.addAttribute("result", result);
					model.addAttribute("resultCount", resultCount);
				}

				/*************** ********* ***************/
				/*************** ********* ***************/

				/*************** ****** ***************/
				/*************** facets ***************/
				/*************** ****** ***************/
				{
					LinkedHashMap<PropertyBean, List<FacetBean>> facets = new LinkedHashMap<PropertyBean, List<FacetBean>>();
					NodeIterator iter = m.listObjectsOfProperty(m.createResource(IRI), m.createProperty(m.getNsPrefixURI("conf"), "facet"));
					while (iter.hasNext()) {
						try {

							RDFNode node = iter.next();
							RDFNode facetProperty = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "facetProperty")).next();
							RDFNode mainQuery = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainQuery")).next();
							RDFNode alias = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "alias")).next();
							PropertyBean p = Misc.generatePropertyBean(facetProperty.toString(), locale.getLanguage(), ontoBean, conf);

							// finding the facets for every classes
							// TODO: merging facets
							System.out.println(" "+alias.toString());
							
							facets.put(p, builder.buildHtmlFacets(Misc.parseFilters(filters, confBygle.getFilters(), mainQuery.toString(), locale.getLanguage()), alias.toString(), locale, conf, confBygle, ontoBean));
							filtersAppender.put(alias.toString(), Misc.urlMinusFilter(filters, confBygle.getFilters(), alias.toString()));

						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					model.addAttribute("facets", facets);
					model.addAttribute("filtersAppender", filtersAppender);
				}
				/*************** ****** ***************/
				/*************** ****** ***************/

				return "bygle-resource";
			} else {
				return "resource";
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ErrorController(conf).error404(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			} else {
				return new ErrorController(conf).error500(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			}
		}

	}

	private void enrichResponse(ResultBean r, HttpServletRequest req, HttpServletResponse res) {

		String publicUrl = r.getMainIRI();
		res.addHeader("Link", "<" + publicUrl + ">; rel=\"about\"");
		if (req.getParameter("IRI") != null) {
			publicUrl = req.getRequestURL().toString() + "?" + req.getQueryString() + "&";
		} else {
			publicUrl += "?";
		}
		res.addHeader("Link", "<" + publicUrl + "output=application%2Frdf%2Bxml>; rel=\"alternate\"; type=\"application/rdf+xml\"; title=\"Structured Descriptor Document (xml)\"");
		res.addHeader("Link", "<" + publicUrl + "output=text%2Fplain>; rel=\"alternate\"; type=\"text/plain\"; title=\"Structured Descriptor Document (ntriples)\"");
		res.addHeader("Link", "<" + publicUrl + "output=text%2Fturtle>; rel=\"alternate\"; type=\"text/turtle\"; title=\"Structured Descriptor Document (turtle)\"");
		res.addHeader("Link", "<" + publicUrl + "output=application%2Fld%2Bjson>; rel=\"alternate\"; type=\"application/ld+json\"; title=\"Structured Descriptor Document (ld+json)\"");
		try {
			for (TripleBean t : r.getResources(r.getMainIRI()).get(r.getTypeProperty())) {
				res.addHeader("Link", "<" + t.getProperty().getProperty() + ">; rel=\"type\"");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
