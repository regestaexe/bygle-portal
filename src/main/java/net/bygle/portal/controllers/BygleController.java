package net.bygle.portal.controllers;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.builder.ResourceBuilder;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.controllers.ErrorController;
import org.dvcama.lodview.utils.Misc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UrlPathHelper;

@Controller
@RequestMapping(value = "**/html")
public class BygleController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	ConfigurationBean conf;

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

	public Object resource(ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String forceIRI, String colorPair) throws UnsupportedEncodingException {
		model.addAttribute("conf", conf);

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

			model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
			ResultBean r = new ResourceBuilder(messageSource).buildHtmlResource(IRI, locale, conf, ontoBean);
			model.addAttribute("results", Misc.guessClass(r, conf, ontoBean));
			model.addAttribute("ontoBean", ontoBean);
			enrichResponse(r, req, res);
			model.addAttribute("colorPair", Misc.guessColor(colorPair, r, conf));
			return "resource";
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
