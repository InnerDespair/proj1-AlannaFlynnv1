

import com.fasterxml.jackson.dataformat.yaml.*;
import com.fasterxml.jackson.databind.*;
import org.json.simple.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Base64;

public class Main {



    public static void main(String[] args) throws IOException {


        JSONObject obj = new JSONObject();

        /**ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File key = new File(classLoader.getResource("Secrets.yaml").getFile());


         * //this is supposed to be reading from the secrets yaml file, but I couldn't find a solution
         * //so out of frustration i just put the info in the class for now and made that gitignore
         *
        ObjectMapper om = new YAMLMapper();
        YAMLFactory factory = new YAMLFactory();

        Secrets secrets = om.readValue(key, Secrets.class);
        */

        //makes the secrets method
        Secrets secrets = new Secrets();

        //declares the url
        URL url = urlMaker(secrets.getUrl() + "forms/" + secrets.getHash() + "/entries.json?sort=EntryId&sortDirection=DESC");

        String encoding = Base64.getEncoder().encodeToString((secrets.getapikey()+":"+secrets.getHash()).getBytes("UTF-8"));

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput (true);
        connection.setRequestProperty  ("Authorization", "Basic " + encoding);
        InputStream urlStream = connection.getInputStream();
        BufferedReader in   = new BufferedReader (new InputStreamReader (urlStream));

        StringBuilder json = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            json.append((char) c);
        }
        //System.out.println(json.toString());

        //for (String line; (line = in.readLine()) != null;) {
          //  System.out.println(line);
        //}

        FileWriter printFile = new FileWriter("example.txt");

        printFile.write(json.toString());
        printFile.close();

    }

    //sets the url address as the string sent
    public static URL urlMaker(String urlString) throws MalformedURLException{
        URL url = new URL(urlString);
        return(url);
    }


}