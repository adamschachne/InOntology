import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.gson.Gson;

import info.debatty.java.stringsimilarity.Jaccard;


@SuppressWarnings("deprecation")
public class InOntology {
    
    static String SERVER_URL = "http://ec-scigraph.sdsc.edu:9000";
    static Gson gson = new Gson();
    static final int NUM_SHINGLES = 2;
    static final double THRESHOLD_SIMILARITY = 0.6;
    static Jaccard jaccard = new Jaccard(NUM_SHINGLES);
    
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter in_ontology = new PrintWriter("MetPetDB_in_ontology.tsv", "UTF-8");
        PrintWriter not_in_ontology = new PrintWriter("MetPetDB_unique.tsv", "UTF-8");
        in_ontology.println("MetPetDB term\tsimilarity\tcinergi term");
        not_in_ontology.println("MetPetDB term\tsimilarity\tclosest cinergi term");
        /*
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLDataFactory df = manager.getOWLDataFactory();
        */
        String[] MetPetDB = {
            "Analytical methods",
            "Elements",
            "Image Types",
            "Metamorphic Grades",
            "Minerals",
            "Oxides",
            "Region Names",
            "Rock Types",
            "Subsample"
        };      
        
        BufferedReader br = null;
        for (String category : MetPetDB) {
            try {
                if (br != null) 
                    br.close();
                br = new BufferedReader(new FileReader(category + ".txt"));
                String line;
                while ((line = br.readLine()) != null) {
                    
                    Vocab vocab = vocabTerm(line, false);
                    
                    if (vocab == null) {                        
                        vocab = vocabTerm(line, true);
                        if (vocab == null) {
                            not_in_ontology.println(line);
                            continue;
                        }
                        double max_similarity = 0.0;
                        String closest_label = "";
                        
                        for (Concept c : vocab.concepts) {
                            for (String label : c.labels) {
                                double temp = jaccard.similarity(line.toLowerCase(), label.toLowerCase());
                                //System.out.println(line + " " + label + "\t\t similarity=" + temp);
                                if (temp > max_similarity) {
                                    closest_label = label;
                                    max_similarity = temp;
                                }
                            }
                        }
                        if (max_similarity < THRESHOLD_SIMILARITY) {
                            not_in_ontology.println(line+"\t"+max_similarity+"\t"+closest_label);
                            continue;
                        }
                        in_ontology.println(line + "\t" + max_similarity + "\t" + closest_label);
                    }
                    else {
                        // 100% match                    
                        in_ontology.println(line + "\t" + "1.0");
                    }                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }     
        try {
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        in_ontology.close();
        not_in_ontology.close();
    }
    
    public static String readURL(String urlString) throws IOException {
        String jsonStr = null;
        HttpClient client = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(urlString);

        httpGet.addHeader("Content-Type", "application/json;charset=utf-8");
        try {
            HttpResponse response = client.execute(httpGet);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                jsonStr = EntityUtils.toString(entity);

            }
            if (response.getStatusLine().getStatusCode() == 404
                    || response.getStatusLine().getStatusCode() == 406) {
                jsonStr = null;
            }
            //System.out.println(jsonStr);

        } finally {
            httpGet.releaseConnection();
        }
        return jsonStr;
    }
    
    public static Vocab vocabTerm(String input, boolean search) throws UnsupportedEncodingException {
        if (input == null) {
            return null;
        }
        
        String prefix = SERVER_URL + "/scigraph/vocabulary/" + ((search == true) ? "search/" : "term/");
        String suffix = "?limit=10&searchSynonyms=true&searchAbbreviations=false&searchAcronyms=false";
        String urlInput = URLEncoder.encode(input, StandardCharsets.UTF_8.name()).replace("+", "%20");

        String urlOut = null;

        System.out.println(prefix + urlInput + suffix);
        try {
            urlOut = readURL(prefix + urlInput + suffix);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (urlOut == null) {
            return null;
        }
        
        //System.out.println(urlOut);
        Concept[] concepts = gson.fromJson(urlOut, Concept[].class);
        ArrayList<Concept> conceptList = new ArrayList<Concept>(Arrays.asList(concepts));       

        return new Vocab(conceptList);
    }
    
}
