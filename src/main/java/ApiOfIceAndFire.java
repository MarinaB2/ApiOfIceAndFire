/*@author Marina
* .This program allow the user to look up a character by a number
* .It give the user an option to display the names of all sowrn members of the characters house
* .It looks up all pov characters in the books published by "Bamtam Books" */


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ApiOfIceAndFire {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a number of the character: ");
        String number = scanner.nextLine().trim();

        displayCharacters(number); // Displays a specific character name and the sworn members names
        // " if " the user likes to

        displayPOVCharacters(); // Displays the Pov characters and their books

    }
    // Making a method that returns a Json object, because I want to use it in many places
    public static JSONObject getJSONObject(String stringUrl) {
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                InputStreamReader isr = new InputStreamReader(con.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String inputLine;
                StringBuffer content = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    content.append(inputLine);
                }
                br.close();
                return new JSONObject((content.toString()));

            } else {
                System.out.println("Error");
                System.out.println("Server responded with: " + con.getResponseCode());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    //Get the character by the specific number of it and return a json object witch is used then in another method
    public static JSONObject getCharacter(String number) {
        String characterURL = "https://anapioficeandfire.com/api/characters/" + number;
        return getJSONObject(characterURL);

    }
//Get the house of the character that the user looked up previously
    public static String getCharactersHouseUrl(JSONObject character) {
        String url = character.get("allegiances").toString();
        url = url.substring(2, url.length() - 2);              // Removes the extra "[ and ]" from the beginning and the end of the result
        return url;
    }
//Get the sworn members names and display them
    public static void getSwornMembers(JSONObject membersHouse) {
        ArrayList<String> swornMembersNames = new ArrayList<>();

        String swornMembersURLs = membersHouse.get("swornMembers").toString();
        JSONArray swornMembersArray = new JSONArray(swornMembersURLs);

        for (int i = 0; i < swornMembersArray.length(); i++) {
            String swornMemberUrl = swornMembersArray.getString(i);               // Gets the url as a string from the array
            JSONObject member = getJSONObject(swornMemberUrl);
            assert member != null;
            String name = member.get("name").toString();
            swornMembersNames.add(name);

        }
        displaySwornMembers(swornMembersNames); // Display the members names

    }

    public static void displaySwornMembers(ArrayList<String> swornMembers) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("If you would like to print the sworn members of this characters house type Yes: ");
        String userInput = scanner.next();

        if (userInput.equals("Yes") || userInput.equals("yes")) {

            for (int i = 0; i < swornMembers.size(); i++) {
                System.out.println((i + 1) + ". " + swornMembers.get(i));
            }
        } else if (userInput.equals("No") || userInput.equals("no")) {
            System.out.println("As you want! ");

        } else {
            System.out.println("If not yes then it means no ");
        }
    }
// Display the character that the user wish to, and if the user wish so can the user display the names of the sworn members of the house of the character
    public static void displayCharacters(String number) {

        JSONObject character = getCharacter(number);

        System.out.println("Character name: " + character.get("name").toString());
        System.out.println("Born : " + character.get("born").toString());
        System.out.println("Title : " + character.get("titles").toString());
        System.out.println("Character aliases: " + character.get("aliases").toString());
        System.out.println("Allegiances: " + character.get("allegiances").toString());
        System.out.println("Gender: " + character.get("gender").toString());
        System.out.println("Played By: " + character.get("playedBy").toString());
        System.out.println("Culture: " + character.get("culture").toString());
        System.out.println("Waiting.......");

        String houseURL = getCharactersHouseUrl(character);
        JSONObject house = getJSONObject(houseURL);

        getSwornMembers(house);


    }

    //Gets the books witch published by "Bantam Books"
    public static JSONArray getBooks() {
        try {
            URL url = new URL("https://www.anapioficeandfire.com/api/books");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //  Gets all the books

            JSONArray booksPublishedBy = new JSONArray();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                InputStreamReader isr = new InputStreamReader(con.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String inputLine;
                StringBuffer content = new StringBuffer();

                while ((inputLine = br.readLine()) != null) {
                    content.append(inputLine);
                }
                br.close();
                JSONArray books = new JSONArray((content.toString()));

                for (int i = 0; i < books.length(); i++) {
                    JSONObject book = books.getJSONObject(i);

                    if (book.get("publisher").toString().equals("Bantam Books"))
                        booksPublishedBy.put(book);
                }
                return booksPublishedBy;

            } else {
                System.out.println("Error");
                System.out.println("Server responded with: " + con.getResponseCode());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
// Get the pov characters of the books that is published by "Bantam Books"
    public static Map<String, String[]> getPOVCharacters() {
        JSONArray books = getBooks();

        Map<String, String[]> bookPovListPair = new HashMap<>();

        // Stores all the povCharacter arrays in an array
        for (int i = 0; i < books.length(); i++) {
            JSONObject book = books.getJSONObject(i);

            JSONArray povCharacters = new JSONArray(book.get("povCharacters").toString());
            String[] characters = new String[povCharacters.length()];

            for (int j = 0; j < povCharacters.length(); j++) {
                String characterUrl = povCharacters.getString(j);
                JSONObject character = getJSONObject(characterUrl);
                assert character != null;
                String characterName = character.get("name").toString();
                characters[j] = characterName;
            }

            bookPovListPair.put(book.get("name").toString(), characters);   //  Stores books and povCharacters as a pair
        }

        return bookPovListPair;
    }

    //Display the pov characters and their books
    public static void displayPOVCharacters() {
        System.out.println("All this books are published by Bantam Books : ");
        System.out.println("Waiting.......");
        for (Map.Entry<String, String[]> pair : getPOVCharacters().entrySet()) {

            System.out.println();
            System.out.println("Book: \"" + pair.getKey() + "\"");
            System.out.print("POV character: ");

            for (int i = 0; i < pair.getValue().length; i++) {
                if (i < pair.getValue().length - 1) {
                    System.out.print(pair.getValue()[i] + ", ");
                } else {
                    System.out.print(pair.getValue()[i]);
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}
