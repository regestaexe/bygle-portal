package net.bygle.portal.utils;

import java.util.Map;

public class Misc extends org.dvcama.lodview.utils.Misc {

	public static String parseFilters(Map<String, String[]> filtersRequest, Map<String, String> filters, String query) {

		// filter template: ${FILTER:(filtername) ... ${FILTERVALUE}}
		for (String filter : filters.keySet()) {
			System.out.println("------filter: " + filter);
			try {
				String queryPart = filters.get(filter).replaceAll("\\$\\{FILTERVALUE\\}", filtersRequest.get(filter)[0]);
				query = query.replaceAll("\\$\\{FILTER:" + filter + "}", queryPart);
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}

		query = query.replaceAll("\\$\\{FILTER:[^}]+\\}", "");
		return query;
	}
}
