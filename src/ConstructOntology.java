import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ConstructOntology {
    
    public static void main(String[] args) throws Exception {
        OutputStream os = new FileOutputStream(new File("cinergi_metpetdb.owl"));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        OWLOntology ont = manager.createOntology(IRI.create("http://hydro10.sdsc.edu/cinergi_ontology/cinergi_metpetdb.owl"));
                
        String[] metPetDB = {
            "Element",
            "Image",
            "MetamorphicGrade",
            "Methods",
            "Mineral",
            "Oxide",
            "Region",
            "RockType",
            "Subsample"
        };  
        
        try{
            addTerms(ont, metPetDB, manager, df);
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        manager.saveOntology(ont, new OWLXMLOntologyFormat(), os);
        os.close();              
    }

    private static void addTerms(OWLOntology ont, String[] metPetDB, OWLOntologyManager manager, OWLDataFactory df) throws IOException {
        BufferedReader br = null;
        for (String category : metPetDB) {
            
            OWLClass topLevel = df.getOWLClass(IRI.create("http://wiki.cs.rpi.edu/trac/metpetdb/wiki/"+category));
            OWLAxiom subclassAxiom = df.getOWLSubClassOfAxiom(topLevel, df.getOWLThing());
            
            OWLAnnotation labelAnnotation = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(createLabel(category)));
            OWLAxiom labelAxiom = df.getOWLAnnotationAssertionAxiom(topLevel.getIRI(), labelAnnotation);
            
            List<AddAxiom> addAxioms = new ArrayList<AddAxiom>();
            addAxioms.add(new AddAxiom(ont, subclassAxiom));
            addAxioms.add(new AddAxiom(ont, labelAxiom));
            
            manager.applyChanges(addAxioms);  
            
            if (br != null) {
                br.close();
            }
            
            br = new BufferedReader(new FileReader(category + ".txt"));
            String str;
            while ((str = br.readLine()) != null) {
                OWLClass sub = df.getOWLClass(IRI.create("http://wiki.cs.rpi.edu/trac/metpetdb/wiki/"+category+"#"+str.replace(" ", "_")));
                subclassAxiom = df.getOWLSubClassOfAxiom(sub, topLevel);
                labelAnnotation = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(createLabel(str)));  
                
                String label = createLabel(str);
                
                labelAxiom = df.getOWLAnnotationAssertionAxiom(sub.getIRI(), labelAnnotation);
                
                addAxioms = new ArrayList<AddAxiom>();
                addAxioms.add(new AddAxiom(ont, subclassAxiom));
                addAxioms.add(new AddAxiom(ont, labelAxiom));
                manager.applyChanges(addAxioms);  
            }
        }
        br.close();
    }

    private static String createLabel(String str) {
        String label = "";        
        /*
        label += Character.toUpperCase(str.charAt(0));
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) == ' ' || str.charAt(i) == '-') {
                label += (str.charAt(i) + Character.toUpperCase(str.charAt(i+1)));
                i++;
            } else {
                label += Character.toLowerCase(str.charAt(i));
            }
        }
                
        return label;
        */        
        return str;
    }
    
}
