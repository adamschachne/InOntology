import java.io.*;
import java.util.*;
import java.net.*;
//import org.json.*;

import com.google.gson.Gson;

public class ExistInOntology {
	public static void main(String[] args) throws Exception {
		inOntology("coral");
	}

	private static double inOntology(String term) throws Exception {
		boolean get = sendGet(term, 20, false, false, false);
		System.out.println(get);

		if(get == false)
			return 0;
		return 1;
	}

	private static boolean sendGet(String term, int limit, boolean synon, boolean abbrev, 
		boolean acron) throws Exception {
		//http://ec-scigraph.sdsc.edu:9000/scigraph/vocabulary/term/coral?limit=20&searchSynonyms=true&searchAbbreviations=true&searchAcronyms=true
		String baseURL = "http://ec-scigraph.sdsc.edu:9000/scigraph/vocabulary/term/" + term + "?";
		String limitParam = "limit=" + limit;
		String synonymParam = "searchSynonyms=" + synon;
		String abrreviationParam = "searchAbbreviations=" + abbrev;
		String acronymParam = "searchAcronyms=" + acron;

		String url = baseURL + limitParam + "&" + synonymParam + "&" + 
			abrreviationParam + "&" + acronymParam;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();

		if(responseCode == 404)
			return false;

		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());

//		Gson gson; 
//		Concept[] cpts = gson.fromJson(response.toString(), Concept[].class);	
//		List<String> labels = cpts.labels;
//		//JSONArray labels = json.getJSONArray("labels");
//		for(int i=0; i<labels.length(); i++) {
//			System.out.println(labels.getString(i));
//		}
//		return true;
		return false;

	}
}