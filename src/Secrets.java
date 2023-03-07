import java.net.URL;

//this class stores my secrets from the secret file

import java.io.*;
import java.util.Scanner;
public class Secrets {

    private String url;
    private String apikey;
    private String hash;
    private String dbUrl;
    private String user;
    private String pass;



    //constructor
    /**
    public Secrets(String url,String apikey, String hash){
        url = url;
        apikey = apikey;
        hash = hash;

    }
     */
    //default
    public Secrets(){

        try {
            Scanner secretsFile = new Scanner(new File("secrets.txt"));

            //sets the values from the secrets file
            this.url = secretsFile.nextLine();
            this.apikey = secretsFile.nextLine();
            this.hash = secretsFile.nextLine();
            this.dbUrl = secretsFile.nextLine();
            this.user = secretsFile.nextLine();
            this.pass = secretsFile.nextLine();

            secretsFile.close();
        } catch (FileNotFoundException e){
            System.out.println("File Not Found");
        }

    }

    public String getUrl() {
        return url;
    }
    public String getHash() {
        return hash;
    }
    public String getapikey(){
        return apikey;
    }
    public String getDbUrl(){
        return dbUrl;
    }
    public String getUser(){
        return user;
    }
    public String getPass(){
        return pass;
    }
}
