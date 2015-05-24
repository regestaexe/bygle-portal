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
import net.bygle.portal.builder.ResourceBuilder;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;

import net.bygle.portal.conf.ConfigurationBean;
import net.bygle.portal.utils.Misc;

import org.dvcama.lodview.controllers.ErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Controller
@RequestMapping(value = "/byg.paginator")
public class PaginatorController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	org.dvcama.lodview.conf.ConfigurationBean conf;

	@Autowired
	ConfigurationBean confBygle;

	@Autowired
	OntologyBean ontoBean;

	@ResponseBody
	@RequestMapping(value = "", produces = "application/json;charset=UTF-8")
	public Object pedigreeDataController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "queryId") String queryId, @RequestParam(value = "tot") String tot, @RequestParam(value = "offset", defaultValue = "0") String offset, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return mapdata(conf, model, req, res, locale, IRI, queryId, tot, offset, colorPair);
	}

	public Object mapdata(org.dvcama.lodview.conf.ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String IRI, String queryId, String tot, String offset, String colorPair) throws UnsupportedEncodingException {

		Map<String, String[]> filters = req.getParameterMap();

		Map<String, String> filtersAppender = new HashMap<String, String>();
		filtersAppender.put("main", Misc.urlMinusFilter(filters, confBygle.getFilters(), ""));

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + " in byg.paginator ################# ");

		try {

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
				String id = "";
				{
					LinkedHashMap<PropertyBean, List<TripleBean>> result = new LinkedHashMap<PropertyBean, List<TripleBean>>();
					LinkedHashMap<PropertyBean, TripleBean> resultCount = new LinkedHashMap<PropertyBean, TripleBean>();
					NodeIterator iter = m.listObjectsOfProperty(m.createResource(IRI), m.createProperty(m.getNsPrefixURI("conf"), "searchBox"));
					while (iter.hasNext()) {
						id += "^";
						try {
							RDFNode node = iter.next();
							if (id.equals(queryId)) {

								RDFNode mainClasses = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainClasses")).next();
								NodeIterator mainQueryIter = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainQuery"));
								String mainQuery = mainQueryIter != null && mainQueryIter.hasNext() ? mainQueryIter.next().toString() : defaultMainQuery;

								String rdfclass = mainClasses.toString();

								PropertyBean p = Misc.generatePropertyBean(rdfclass, locale.getLanguage(), ontoBean, conf);

								// finding the page link for every classes

								for (String rdfpageclass : confBygle.getMultiConfValue(IRI, "mainClasses")) {
									if (rdfpageclass.equals(rdfclass)) {
										p.setPropertyUrl(Misc.toBrowsableUrl(IRI, conf));
									}

								}

								result.put(p, builder.buildHtmlMainClassSearch(Misc.parseFilters(filters, confBygle.getFilters(), mainQuery, locale.getLanguage()), rdfclass, Integer.parseInt(offset), locale, conf, confBygle, ontoBean));

								TripleBean t = new TripleBean();
								t.setValue(tot);
								resultCount.put(p, t);
								ObjectMapper objectMapper = new ObjectMapper();
								return objectMapper.writeValueAsString(result);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				}

			}
			return "{\"result\":\"empty\"}";

		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ErrorController(conf).error404(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			} else {
				return new ErrorController(conf).error500(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			}
		}

	}
}
