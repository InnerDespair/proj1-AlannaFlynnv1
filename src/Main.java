

import com.fasterxml.jackson.dataformat.yaml.*;
import com.fasterxml.jackson.databind.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.sql.*;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Base64;

public class Main {
    public static Secrets secrets;
    public static URL url;
    public static JSONArray dataArr;
    public static Statement stmt;
    public static String[] columns;
    public static String[] fields;
    public static ResultSet rs;
    public static Connection dbCon;
    public static String dbName;
    public static String tableName;

    public static void main(String[] args) throws IOException {
        // comment to test workflow


        JSONObject obj = new JSONObject();

        //declares a secrets method so we can use it
        secrets = new Secrets();

        //there are 16 columns
        columns = new String[]{"EntryID", "Prefix", "FirstName", "LastName",
                "Title", "Company", "Email", "Website", "Phone", "EventTypes", "Dates",
                "Permission", "DateCreated", "CreatedBy", "DateUpdated", "UpdatedBy"};

        //26 fields (9-15 are event type)(16-20 date range)
        fields = new String[]{"EntryId", "Field5", "Field1", "Field2", "Field3", "Field6", "Field7", "Field8", "Field9", "Field10",
                "Field11", "Field12", "Field13", "Field14", "Field15", "Field16", "Field110", "Field111",
                "Field112", "Field113", "Field114", "Field210", "DateCreated", "CreatedBy", "DateUpdated", "UpdatedBy"};

        dbName = "cubesprodb";
        tableName = "entrytable";
        int dbStatus = 1;

        String dataStr = getStringFromUrl();
        //System.out.println(url);

        try {
            //Create JSON array from string for use
            dataArr = stringToJsonArray(dataStr);
            //testJSONArray(dataArr);

            try{
                String driver = "com.mysql.cj.jdbc.Driver";
                Class.forName(driver);

                dbCon = DriverManager.getConnection(secrets.getDbUrl(), secrets.getUser(), secrets.getPass());

                stmt = dbCon.createStatement();

                dbStatus = createDBIfNotExists(dbName, dbCon, stmt);


                stmt = dbCon.createStatement();

                //create table or open it if it exists
                createTableIfNotExists();

                stmt = dbCon.createStatement();

                //this gets all values from the table as (column = value), then to print them out it would be while(rs.next()){print values needed from each row}

                rs = stmt.executeQuery("select * from "+ tableName);



                int rows = 0;

                while(rs.next()){
                    rows++;
                    //System.out.println(rs.getString(columns[0]));
                }

                //System.out.println(rows);

                //update the rows that are already in the database (if any)
               updateTable(rows);

                //insert the new values into the table (if any)
                insertIntoTable(rows);

                //printRows();

                System.out.println("DB Actions successful");

            } catch(Exception e){
                System.out.println( "DB Actions failed");
                System.out.println(e);
            }

        } catch (ParseException e){
            System.out.println(e);
        }

        FileWriter printFile = new FileWriter("example.txt");

        printFile.write(dataStr);
        printFile.close();


    }

    public static void updateTable(int rows){
        try {
            JSONObject obj;
            int counter = 0;
            for (int i = 0; i < (rows); i++) {

                counter = 0;
                int currObjNum = dataArr.size() - 1 - i;
                obj = (JSONObject) dataArr.get(currObjNum);

                for (int k = 0; k < columns.length; k++) {

                    String query = "update " + tableName + " set ";

                    if (k == 9) {
                        query = query + columns[k] + " = '" + obj.get(fields[9]) + ", " + obj.get(fields[10]) + ", " + obj.get(fields[11]) + ", " +
                                obj.get(fields[12]) + ", " + obj.get(fields[13]) + ", " + obj.get(fields[14]) + ", " + obj.get(fields[15]) + "' ";
                        counter = 15;
                    } else if (k == 10) {
                        query = query + columns[k] + " = '" + obj.get(fields[16]) + ", " + obj.get(fields[17]) + ", " + obj.get(fields[18]) + ", " +
                                obj.get(fields[19]) + ", " + obj.get(fields[20]) + "' ";
                        counter = 20;
                    } else if (k == 0) {
                        query = query + columns[k] + " = " + obj.get(fields[counter]) + " ";
                    } else {
                        query = query + columns[k] + " = '" + obj.get(fields[counter]) + "' ";
                    }

                    query = query + "where " + columns[0] + " = " + obj.get(fields[0]) + ";";
                    stmt = dbCon.createStatement();

                    stmt.executeUpdate(query);

                    counter++;
                }

            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    public static void insertIntoTable(int rows){
        try {
            JSONObject currObj;
            String newTableString = "";

            for (int i = 0; i < (dataArr.size() - rows); i++) {

                currObj = (JSONObject) dataArr.get(i);
                stmt = dbCon.createStatement();

                newTableString = "INSERT INTO " + tableName + " VALUES (";
                for (int j = 0; j < fields.length; j++) {

                    if (j != 0) {
                        newTableString = newTableString + ", ";
                    }

                    if (j == 9) {

                        newTableString = newTableString + "'" + currObj.get(fields[9]) + ", " + currObj.get(fields[10]) + ", " + currObj.get(fields[11]) + ", " +
                            currObj.get(fields[12]) + ", " + currObj.get(fields[13]) + ", " + currObj.get(fields[14]) + ", " + currObj.get(fields[15]) + "'";
                        j = 15;

                    } else if (j == 16) {

                        newTableString = newTableString + "'" + currObj.get(fields[16]) + ", " + currObj.get(fields[17]) + ", " + currObj.get(fields[18]) + ", " +
                            currObj.get(fields[19]) + ", " + currObj.get(fields[20]) + "'";

                        j = 20;

                    } else if (j == 0) {
                        newTableString = newTableString + currObj.get(fields[0]);
                    } else {

                        newTableString = newTableString + "'" + currObj.get(fields[j]) + "'";

                    }

                }
                newTableString = newTableString + ")";

                stmt.executeUpdate(newTableString);
            }


        } catch(Exception e){

        }
    }
    public static void createTableIfNotExists(){
        //create table or open it if it exists
            try {
                stmt = dbCon.createStatement();

                String newTableString = ("CREATE TABLE " + tableName + " (" + columns[0] + " int");

                for (int i = 1; i < columns.length; i++) {
                    newTableString = (newTableString + ", " + columns[i] + " varchar(255)");
                }
                newTableString = newTableString + ")";

                try {
                    stmt.executeUpdate(newTableString);

                    System.out.println("Table Created");

                } catch (SQLException s) {
                    //you go here if table exists
                }
            } catch (Exception e){
            System.out.println(e);
            }
    }
    //create database if it does not exist, or opens it if it does
    public static int createDBIfNotExists(String dbName, Connection con, Statement stmt){
        ResultSet rs;
        int status = 1;
        Boolean dbExists = false;
        try {

            //checks if database exists
            if (con != null) {
                rs = con.getMetaData().getCatalogs();

                while (rs.next()) {
                    String catalogs = rs.getString(1);
                    //works

                    if (dbName.equals(catalogs)) {

                        dbExists = true;
                        status = stmt.executeUpdate("USE " + dbName);

                        System.out.println("Opened");
                    }
                }
            }

            //if it does not exist then create the database
            if (dbExists == false) {
                status = stmt.executeUpdate("CREATE DATABASE " + dbName);
                System.out.println("Created");
            }
        } catch(Exception e){
            System.out.println(e);
        }

        return(status);
    }

    //uses JSON to make the string an object
    public static JSONArray stringToJsonArray(String str) throws ParseException{
        JSONParser parser = new JSONParser();


        JSONObject dataObj = (JSONObject)parser.parse(str);

        JSONArray dataArr = (JSONArray)dataObj.get("Entries");

        return(dataArr);

    }

    public static String getStringFromUrl() throws IOException{

        url = urlMaker(secrets.getUrl() + "forms/" + secrets.getHash() + "/entries.json?sort=EntryId&sortDirection=DESC");

        //uses Base64 object and uses encoder to make string (dont know what it does exactly), basically stores the "username:password" for input into the popup
        String encoding = Base64.getEncoder().encodeToString((secrets.getapikey()+":"+secrets.getHash()).getBytes("UTF-8"));

        HttpURLConnection connection = (HttpURLConnection)url.openConnection(); //opens connection to url

        //this retrieves data from the url and retrieves the json
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput (true);
        connection.setRequestProperty  ("Authorization", "Basic " + encoding);
        InputStream urlStream = connection.getInputStream();
        BufferedReader in  = new BufferedReader (new InputStreamReader (urlStream));

        StringBuilder json = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            json.append((char) c);
        }
        String dataStr= json.toString();

        return(dataStr);
    }

    public static void printRows(){
        try {
            rs = stmt.executeQuery("select * from " + tableName);
            String printRow;
            while (rs.next()) {
                printRow = "";
                for (int i = 0; i < columns.length; i++) {
                    printRow = printRow + columns[i] + ": " + rs.getString(columns[i]);
                    if (i < columns.length - 1) {
                        printRow = printRow + ", ";
                    }
                }
                System.out.println(printRow);
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }

    //sets the url address as the string sent
    public static URL urlMaker(String urlString) throws MalformedURLException{
        URL url = new URL(urlString);
        return(url);
    }

    //method to get certain values
    public static String getField (JSONObject obj, String str){

        String newStr = obj.get(str).toString();

        return(newStr);
    }

    //returns the planned time of event
    public static String getEventTime(JSONObject obj){

        //for mine in specific, fields110-113 are the times
        String str = "";
        String counter = "";
        for(int i=0; i<4; i++){
            counter = "Field11" + i;
            str = str + obj.get(counter);
        }
        if(str == ""){
            str = "No Data";
        }

        return (str);
    }

    //method to test the JSONArray
    public static void testJSONArray(JSONArray arr){

        for(int i=0; i<arr.size();i++){
            System.out.println(arr.get(i));
            System.out.println(getEventTime((JSONObject) arr.get(i)));
        }

        //JSONObject currentObj = (JSONObject)dataArr.get(0);
        //System.out.println(currentObj.get("DateCreated"));


    }




}