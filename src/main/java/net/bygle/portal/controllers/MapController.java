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
@RequestMapping(value = "/byg.map")
public class MapController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	org.dvcama.lodview.conf.ConfigurationBean conf;

	@Autowired
	ConfigurationBean confBygle;

	@Autowired
	OntologyBean ontoBean;

	@ResponseBody
	@RequestMapping(value = "/data", produces = "application/json;charset=UTF-8")
	public Object pedigreeDataController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "output", defaultValue = "") String output, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return mapdata(conf, model, req, res, locale, output, IRI, colorPair);
	}

	public Object mapdata(org.dvcama.lodview.conf.ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String IRI, String colorPair) throws UnsupportedEncodingException {
		model.addAttribute("conf", conf);
		model.addAttribute("Misc", new Misc());

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + " in map byg.portal ################# ");

		try {
			model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
			model.addAttribute("ontoBean", ontoBean);
			Model m = confBygle.getConfModel();
			ResourceBuilder builder = new ResourceBuilder(messageSource);

			Map<Object, Object> result = new HashMap<Object, Object>();

			Map<String, String[]> filters = req.getParameterMap();
			Map<String, String> filtersAppender = new HashMap<String, String>();
			filtersAppender.put("main", Misc.urlMinusFilter(filters, confBygle.getFilters(), ""));

			LinkedHashMap<PropertyBean, List<FacetBean>> facets = new LinkedHashMap<PropertyBean, List<FacetBean>>();
			NodeIterator iter = m.listObjectsOfProperty(m.createResource(IRI), m.createProperty(m.getNsPrefixURI("conf"), "placeFacet"));
			while (iter.hasNext()) {
				try {

					RDFNode node = iter.next();
					RDFNode mainQuery = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "mainQuery")).next();
					RDFNode alias = m.listObjectsOfProperty(node.asResource(), m.createProperty(m.getNsPrefixURI("conf"), "alias")).next();

					// finding the facets for every classes
					// TODO: merging facets
					System.out.println(" " + alias.toString());

					List<String> queries = new ArrayList<String>();
					// Misc.parseFilters(filters, confBygle.getFilters(),
					// mainQuery.toString(), locale.getLanguage()),
					// alias.toString(), locale, conf, confBygle, ontoBean)
					queries.add(Misc.parseFilters(filters, confBygle.getFilters(), mainQuery.toString(), locale.getLanguage()));

					ResultBean resultBean = builder.buildHtmlResource(IRI, locale, conf, ontoBean, false, queries);

					Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> placesMap = resultBean.getLiterals();

					filtersAppender.put(alias.toString(), Misc.urlMinusFilter(filters, confBygle.getFilters(), alias.toString()));

					for (String place : placesMap.keySet()) {
						Map<Object, Object> aplace = new HashMap<Object, Object>();
						LinkedHashMap<PropertyBean, List<TripleBean>> map = placesMap.get(place);
						for (PropertyBean resource : map.keySet()) {
							List<TripleBean> trip = map.get(resource);
							List<String> values = new ArrayList<String>();
							for (TripleBean tripleBean : trip) {
								values.add(tripleBean.getValue());
							}

							if (conf.getTitleProperties().contains(resource.getProperty())) {
								aplace.put("label", values);
							} else if (conf.getLatitudeProperties().contains(resource.getProperty())) {
								aplace.put("lat", values);
							} else if (conf.getLongitudeProperties().contains(resource.getProperty())) {
								aplace.put("long", values);
							}

						}
						Map<String, Object> inplace = new HashMap<String, Object>();
						inplace.put("data", aplace);
						inplace.put("url", Misc.toBrowsableUrl(place, conf));
						inplace.put("nsIRI", Misc.toNsResource(place, conf));
						inplace.put("filterurl", "?" + alias + "=" + java.net.URLEncoder.encode(place, "UTF-8") + "&amp;" + filtersAppender.get(alias.toString()));

						result.put(place, inplace);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			model.addAttribute("facets", facets);
			model.addAttribute("filtersAppender", filtersAppender);

			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(result);
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
