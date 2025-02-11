import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import java.io.*;

public class CSVtoRDF {

    // Préfixes pour RDF
    private static final String BASE_NS = "http://localhost:3333/";
    private static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

    public static void main(String[] args) {
        String csvFile = "tomatoFilm.csv"; // Fichier CSV en entrée
        String ttlFile = "data.ttl";       // Fichier de sortie au format Turtle

        // Création du modèle RDF
        Model model = ModelFactory.createDefaultModel();

        // Définir les préfixes
        model.setNsPrefix("", BASE_NS);
        model.setNsPrefix("foaf", FOAF_NS);
        model.setNsPrefix("rdf", RDF.uri);
        model.setNsPrefix("rdfs", RDFS.uri);
        model.setNsPrefix("xsd", XSD.getURI());

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Ignorer la première ligne (en-têtes)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Séparer les colonnes du CSV
                String[] columns = line.split(",", -1); // "-1" pour conserver les colonnes vides

                // Assigner les colonnes du CSV à des variables
                String id = columns[0].trim();
                String title = columns[1].trim();
                String releaseDateStreaming = columns[7].trim();
                String runtimeMinutes = columns[8].trim();
                String genres = columns[9].trim();
                String language = columns[10].trim();
                String directors = columns[11].trim();

                // Créer une ressource RDF pour le film
                Resource movie = model.createResource(BASE_NS + id)
                        .addProperty(RDF.type, RDFS.Resource) // Utilisation de rdfs:Resource pour le type
                        .addProperty(model.createProperty(BASE_NS, "title"), title)
                        .addProperty(model.createProperty(BASE_NS, "inLanguage"), language);

                // Ajouter la durée si disponible
                if (!runtimeMinutes.isEmpty()) {
                    movie.addProperty(model.createProperty(BASE_NS, "duration"),
                            model.createTypedLiteral(Double.parseDouble(runtimeMinutes)));
                }

                // Ajouter les genres si disponibles
                if (!genres.isEmpty()) {
                    String[] genreArray = genres.split(",");
                    for (String genre : genreArray) {
                        movie.addProperty(model.createProperty(BASE_NS, "genre"), genre.trim());
                    }
                }

                // Ajouter la date de streaming
                if (!releaseDateStreaming.isEmpty()) {
                    movie.addProperty(model.createProperty(BASE_NS, "releasedStreamingDate"),
                            model.createTypedLiteral(releaseDateStreaming, XSD.date.getURI()));
                }

                // Ajouter les réalisateurs
                if (!directors.isEmpty()) {
                    String[] directorArray = directors.split(",");
                    for (String director : directorArray) {
                        Resource directorResource = model.createResource(BASE_NS + director.trim().replaceAll(" ", "_"))
                                .addProperty(RDF.type, RDFS.Person)
                                .addProperty(FOAF.name, director.trim());
                        movie.addProperty(model.createProperty(BASE_NS, "director"), directorResource);
                    }
                }
            }

            // Écrire le modèle RDF au format Turtle
            try (FileWriter out = new FileWriter(ttlFile)) {
                model.write(out, "TURTLE");
                System.out.println("Conversion terminée avec succès. Fichier Turtle généré : " + ttlFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
