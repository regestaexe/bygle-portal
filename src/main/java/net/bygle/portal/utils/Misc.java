package net.bygle.portal.utils;

import java.util.Map;

public class Misc {

	public static String parseFilters(Map<String, String[]> filters, String query) {

		// filter template: ${FILTER:(filtername) ... ${FILTERVALUE}}
		for (String filter : filters.keySet()) {
			System.out.println("------filter: " + filter);
			try {
				query = query.replaceAll("\\$\\{FILTER:" + filter + " ([^$]*)\\$\\{FILTERVALUE\\}([^}]*)\\}", "$1" + filters.get(filter)[0] + "$2");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		query = query.replaceAll("\\$\\{FILTER:[^$]*\\$\\{FILTERVALUE\\}[^}]*\\}", "");
		return query;
	}
}
