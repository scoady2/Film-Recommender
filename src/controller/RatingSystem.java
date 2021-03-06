package controller;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import java.io.FileNotFoundException;
import java.net.URL;

import model.Film;
import model.Member;
import model.Rating;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class which performs all tasks needed by the view controller. 
 * 
 * @author Stephen
 *
 */

public class RatingSystem
{
	private ArrayList<Member> members = new ArrayList<Member>();
	private ArrayList<Film> films = new ArrayList<Film>();
	private HashMap<Integer, Double> ratings = new HashMap<Integer, Double>();
	private Member loggedIn;
	private String loadMembersLocation = "src/files/members.json";
	private String loadFilmsLocation = "src/files/films.json";
	private String loadBackupMembersLocation = "src/backup/members.json";
	private String loadBackupFilmsLocation = "src/backup/films.json";
	private String saveMembersLocation = "src/files/members.json";
	private String saveFilmsLocation = "src/files/films.json";
	private HashMap<Integer, Integer> similarMembers = new HashMap<Integer, Integer>();
	private ArrayList<Film> recommendedFilms = new ArrayList<Film>();
	private ArrayList<Film> betterRecommendedFilms = new ArrayList<Film>();
	private static final Comparator<Film> BY_TITLE   = new ByTitle();
	private static final Comparator<Film> BY_YEAR  = new ByYear();
	private ArrayList<Film> sortedByTitle = new ArrayList<Film>();
	private ArrayList<Film> sortedByYear = new ArrayList<Film>();


	/**
	 * 
	 * a method to load all members from a JSON file. 
	 * takes in a jsonObject and splits it up to add each individual component.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void loadMembers()
	{
		JSONParser parser = new JSONParser();
		try {

			Object obj = parser.parse(new FileReader(loadMembersLocation));
			JSONObject jsonObject = (JSONObject) obj;
			for (int i = 0; i < jsonObject.size(); i++)
			{
				ArrayList<?> newArray = (ArrayList<?>) jsonObject.get(Integer.toString(i));
				String obj2  = (String) newArray.get(0); // username
				String obj3  = (String) newArray.get(1); // firstname
				String obj4  = (String) newArray.get(2); // secondname
				String obj5  = (String) newArray.get(3); // password
				String obj6  = (String) newArray.get(4); // genre preference


				Long obj7  = (Long) newArray.get(5);     // year preference
				String stringPref = Long.toString(obj7);
				int pref;
				if(stringPref.equals(null))
				{
					pref = 0;
				}
				else
				{
					pref = Integer.valueOf(stringPref);
				}
				Member newMember = new Member(obj3, obj4, obj2, obj5, obj6, pref);
				members.add(newMember);

				//deals with the member's ratings
				Object keyArray = newArray.get(6);
				HashMap<Integer, Rating> map = (HashMap<Integer, Rating>) newArray.get(6);
				newMember.setRatings(map);
				JSONObject jsonObject2 = (JSONObject) keyArray;
				for(int j = 0; j<films.size();j++)
				{
					if(jsonObject2.get(Integer.toString(j))!=null)
					{
						String string = jsonObject2.get(Integer.toString(j)).toString();
						int rating = Integer.parseInt(string);
						initialNewRating(members.indexOf(newMember), films.get(j), rating);
					}
				}
			}
		} catch (FileNotFoundException e) {
			this.loadMembersLocation = "src/backup/members.json";
			this.loadBackupMembersLocation = "src/files/members.json";
			errorLog(e.getMessage());
			loadMembers();
		} catch (IOException e) {
			this.loadMembersLocation = "src/backup/members.json";
			this.loadBackupMembersLocation = "src/files/members.json";
			errorLog(e.getMessage());
			loadMembers();
		} catch (ParseException e) {
			this.loadMembersLocation = "src/backup/members.json";
			this.loadBackupMembersLocation = "src/files/members.json";
			errorLog(e.getMessage());
			loadMembers();
		}
		backupMembers();
		
	}

	/**
	 * 
	 * saves all members in members arrayList to a JSON file.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void saveMembers() throws IOException
	{
		JSONObject obj = new JSONObject();

		for(int i = 0; i <members.size(); i++)
		{
			Member member = members.get(i);
			JSONArray newMember = new JSONArray();
			newMember.add(member.getAccountName());
			newMember.add(member.getFirstName());
			newMember.add(member.getSecondName());
			newMember.add(member.getPassword());
			newMember.add(member.getGenrePreference());
			newMember.add(member.getYearPreference());

			Map <Integer, Integer> hm = new HashMap<Integer, Integer>();
			for (int j = 0; j < films.size(); j++)
			{
				if(member.getRatings().containsKey(j))
				{
					int rate = member.getRatings().get(j).getRating();
					hm.put(j, rate);
				}
			}
			newMember.add(hm);
			obj.put(i, newMember);
		}

		FileWriter file = new FileWriter(saveMembersLocation);
		try {
			file.write(obj.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
			errorLog(e.getMessage());
		} finally {
			file.flush();
			file.close();
		}
	}

	/**
	 * 
	 * loads films from JSON file. breaks the JSON object 
	 * into individual pieces to load each film
	 * 
	 */
	public void loadFilms()
	{
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(new FileReader(loadFilmsLocation));
			JSONObject jsonObject = (JSONObject) obj;
			for (int i = 0; i < jsonObject.size(); i++)
			{
				if(jsonObject.get(Integer.toString(i)) != null)
				{
					ArrayList<?> newArray = (ArrayList<?>) jsonObject.get(Integer.toString(i));
					Object obj2  = newArray.get(0);
					String ID2 = obj2.toString();
					int ID = Integer.parseInt(ID2);
					String title = (String) newArray.get(1);

					Long longYear = (Long) newArray.get(2);
					String stringYear = Long.toString(longYear);
					int year = Integer.valueOf(stringYear);

					String genre = (String) newArray.get(3);
					String imageLocation = (String) newArray.get(4);

					if(imageLocation.equals("src/images/no_image_available.jpg"))
					{
						imageLocation = getFilmImage(title);
						imageLocation = saveImage(imageLocation, title);
					}

					Film film = new Film(ID, title, year, genre);
					film.setFilmImage(imageLocation);
					films.add(film);
				}
			}
		}catch (FileNotFoundException e) {
			this.loadFilmsLocation = "src/backup/films.json";
			this.loadBackupFilmsLocation = "src/files/films.json";
			errorLog(e.getMessage());
			loadFilms();
		} catch (IOException e) {
			this.loadFilmsLocation = "src/backup/films.json";
			this.loadBackupFilmsLocation = "src/files/films.json";
			errorLog(e.getMessage());
			loadFilms();
		} catch (ParseException e) {
			this.loadFilmsLocation = "src/backup/films.json";
			this.loadBackupFilmsLocation = "src/files/films.json";
			errorLog(e.getMessage());
			loadFilms();
		}
		backUpFilms();
	}

	/**
	 * 
	 * saves all films in films arrayList to a JSON file.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void saveFilms() throws IOException
	{
		JSONObject obj = new JSONObject();

		for(int i = 0; i <films.size(); i++)
		{
			JSONArray newFilm = new JSONArray();
			newFilm.add(films.get(i).getID());
			newFilm.add(films.get(i).getTitle());
			newFilm.add(films.get(i).getYear());
			newFilm.add(films.get(i).getGenre());
			newFilm.add(films.get(i).getFilmImage());

			obj.put(films.get(i).getID(), newFilm);
		}
		FileWriter file = new FileWriter(saveFilmsLocation);
		try {
			file.write(obj.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
			errorLog(e.getMessage());
		} finally {
			file.flush();
			file.close();
		}
	}

	/**
	 * 
	 * 
	 * creates a new member with properties
	 * 
	 * @param firstName
	 * @param secondName
	 * @param accountName
	 * @param password
	 * @param genrePref
	 * @param yearPref
	 * @return the member created
	 * 
	 * 
	 */
	public Member newMember(String firstName, String secondName, String accountName, String password, String genrePref, int yearPref)
	{
		Member newMember = new Member(firstName, secondName, accountName, password, genrePref, yearPref);
		members.add(newMember);
		return newMember;
	}

	/**
	 * 
	 * creates a new film in the system
	 * 
	 * @param title
	 * @param year
	 * @param genre
	 * @throws IOException if the film image cannot be downloaded
	 */
	public void newFilm(String title, int year, String genre) throws IOException
	{
		Film newFilm = new Film(films.size(), title, year, genre);
		films.add(newFilm);
		String url = getFilmImage(title);
		saveImage(url, title);
		newFilm.setFilmImage("src/images/" + title + ".gif");
	}

	public void deleteFilm(Film film)
	{
		int index = films.indexOf(film);
		for(int i = 0; i<members.size(); i++)
		{
			members.get(i).getRatings().remove(index);
		}
		films.remove(film);
		File file = new File("src/images/" + film.getTitle() + ".gif");
		file.delete();
	}

	/**
	 * a method to make a rating when the system loads a new member
	 * @param userID
	 * @param film
	 * @param rating
	 * @throws IOException
	 */
	public void initialNewRating(int userID, Film film, int rating) throws IOException
	{
		Member member = members.get(userID);
		int ID = film.getID();
		film.addRating(rating);
		ratings.put(ID, film.getSumOfRatings());
		Rating newRating = new Rating(rating, film, member);
		member.getRatings().put(ID, newRating);
	}

	/**
	 * a method to allow a member to rate a film.
	 * if the member has already rated a film then 
	 * the original rating is overwritten by the new rating
	 * 
	 * @param member
	 * @param film
	 * @param rating
	 */
	public void newRating(Member member, Film film, int rating)
	{
		int ID = film.getID();
		if(!member.getRatings().containsKey(ID))
		{
			film.addRating(rating);
			ratings.put(ID, film.getSumOfRatings());
			Rating newRating = new Rating(rating, film, member);
			member.getRatings().put(ID, newRating);
		}
		//if the member has already rated this film, their previous rating 
		//will not be reflected in that films total ratings
		else
		{
			film.subtractRating(member.getRatings().get(ID).getRating());
			film.addRating(rating);
			ratings.put(ID, film.getSumOfRatings());
			Rating newRating = new Rating(rating, film, member);
			member.getRatings().put(ID, newRating);
		}
	}

	public void logIn(String userName, String password)
	{
		for(int i = 0; i<members.size();i++)
		{
			if (members.get(i).getAccountName().equals(userName) 
					&& members.get(i).getPassword().equals(password) )
			{
				loggedIn = members.get(i);
				members.get(i).setLoggedIn(true);
				break;
			}
		}
	}

	/**
	 * saves the image for the downloaded string to a file
	 * @param urlString
	 * @param filmName
	 * @return
	 */
	public String saveImage(String urlString, String filmName)
	{
		Image image = null;
		File newFile = null;
		try {
			URL url = new URL(urlString);
			image = ImageIO.read(url);
			BufferedImage image2 = (BufferedImage) image;
			newFile = new File("src/images/"+ filmName+ ".gif");
			ImageIO.write(image2, "gif", newFile);
		} catch (IOException e) {
			errorLog(e.getMessage());
		}
		return "src/images/" + filmName + ".gif";
	}

	/**
	 * 
	 * uses the google ajax API to download an image for a film
	 * 
	 * @param filmName
	 * @return
	 * @throws IOException
	 */
	public String getFilmImage(String filmName) throws IOException
	{
		String search = filmName.replaceAll("\\s+","+");
		URL googleSearch = new URL("http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + search + "+film+small+image");
		BufferedReader in = new BufferedReader(
				new InputStreamReader(googleSearch.openStream()));

		String line;
		line = in.readLine();
		in.close();

		Object obj=JSONValue.parse(line);
		JSONObject object = (JSONObject)obj;

		JSONObject newObj = (JSONObject) object.get("responseData");
		JSONArray newerObj = (JSONArray) newObj.get("results");
		JSONObject evenNewerObj = (JSONObject) newerObj.get(0);
		String imageString = (String) evenNewerObj.get("unescapedUrl");

		return imageString;
	}

	/**
	 * 
	 * compiles a list of members with similar ratings to the logged in member.
	 */
	public void getSimilarMembers()
	{
		for(int i = 0; i < members.size();i++)
		{
			int dotProduct = 0;
			if(loggedIn!=members.get(i))
			{
				for(int j = 0;j<films.size();j++)
				{
					if(loggedIn.getRatings().get(j) != null && members.get(i).getRatings().get(j) != null)
					{
						Integer memberRating = loggedIn.getRatings().get(j).getRating();
						Integer checkRating = members.get(i).getRatings().get(j).getRating();
						dotProduct += (memberRating*checkRating);
					}
				}
				if(dotProduct > 0)
					similarMembers.put(i, dotProduct);
			}
		}
	}

	public HashMap<Integer, Integer> getListOfSimilarMembers()
	{
		return similarMembers;
	}

	/**
	 * 
	 * gets a list of films to recommend to a 
	 * user based on the list of similar members. 
	 * adds the films of the four most similar members.
	 * 
	 */
	public void getFilmRecommendations()
	{
		int largestNum = 0;
		int largest = 0;
		int secondLargest = 0;
		int thirdLargest = 0;
		int fourthLargest = 0;
		for(int i = 0; i < members.size(); i++)
		{
			if(similarMembers.get(i)!=null)
			{
				if(similarMembers.get(i) > largestNum)
				{
					largestNum = similarMembers.get(i);
					fourthLargest = thirdLargest;
					thirdLargest = secondLargest;
					secondLargest = largest;
					largest = i;
				}
			}
		}


		Member mostSimilar = members.get(largest);
		Member secondMostSimilar = members.get(secondLargest);
		Member thirdMostSimilar = members.get(thirdLargest);
		Member fourthMostSimilar = members.get(fourthLargest);

		for(int i = 0; i<films.size();i++)
		{
			if(!loggedIn.getRatings().containsKey(i) && !recommendedFilms.contains(films.get(i)))
			{
				if(mostSimilar.getRatings().get(i)!=null &&mostSimilar.getRatings().get(i).getRating()>=3)
					recommendedFilms.add(mostSimilar.getRatings().get(i).getFilm());
			}
			if(!loggedIn.getRatings().containsKey(i) && !recommendedFilms.contains(films.get(i)))
			{
				if(secondMostSimilar.getRatings().get(i)!=null &&secondMostSimilar.getRatings().get(i).getRating()>=3)
					recommendedFilms.add(secondMostSimilar.getRatings().get(i).getFilm());
			}
			if(!loggedIn.getRatings().containsKey(i) && !recommendedFilms.contains(films.get(i)))
			{
				if(thirdMostSimilar.getRatings().get(i)!=null &&thirdMostSimilar.getRatings().get(i).getRating()>=3)
					recommendedFilms.add(thirdMostSimilar.getRatings().get(i).getFilm());
			}
			if(!loggedIn.getRatings().containsKey(i) && !recommendedFilms.contains(films.get(i)))
			{
				if(fourthMostSimilar.getRatings().get(i)!=null &&fourthMostSimilar.getRatings().get(i).getRating()>=3)
					recommendedFilms.add(fourthMostSimilar.getRatings().get(i).getFilm());
			}
		}
	}

	/**
	 * 
	 * takes the recommended films and narrows down the list. this is done by using the user's
	 * preferences for genre and decade
	 * 
	 */
	public void getBetterRecommendations()
	{
		String userGenrePreference = loggedIn.getGenrePreference();
		int userYearPreference = loggedIn.getYearPreference();
		int decade = userYearPreference+9; 

		if(userGenrePreference !=null && userYearPreference!=0)
		{
			for(int i = 0; i<recommendedFilms.size();i++)
			{
				if(recommendedFilms.get(i).getGenre().equalsIgnoreCase(userGenrePreference)
						|| (recommendedFilms.get(i).getYear()>(userYearPreference)
								&&recommendedFilms.get(i).getYear()<(decade)))
				{
					betterRecommendedFilms.add(recommendedFilms.get(i));
				}
			}
		}

	}
	
	/**
	 * 
	 * sorts the films using a comparator and the films titles.
	 * 
	 */
	public void sortByTitle()
	{
		Collections.sort(films, BY_TITLE);
		for (int i = 0; i < films.size(); i++)
			sortedByTitle.add(films.get(i));
	}

	public ArrayList<Film> getSortedByTitle() {
		return sortedByTitle;
	}

	public void setSortedByTitle(ArrayList<Film> sortedByTitle) {
		this.sortedByTitle = sortedByTitle;
	}

	public ArrayList<Film> getSortedByYear() {
		return sortedByYear;
	}

	public void setSortedByYear(ArrayList<Film> sortedByYear) {
		this.sortedByYear = sortedByYear;
	}

	// comparator to sort by title
	private static class ByTitle implements Comparator<Film> {
		public int compare(Film a, Film b) {
			return a.getTitle().compareTo(b.getTitle());
		}
	}
	/**
	 * 
	 * sorts the films using a comparator and the films year.
	 * 
	 */
	public void sortByYear()
	{
		Collections.sort(films, BY_YEAR);
		for (int i = 0; i < films.size(); i++)
			sortedByYear.add(films.get(i));
	}

	// comparator to sort by year
	private static class ByYear implements Comparator<Film> {
		public int compare(Film a, Film b) {
			return b.getYear() - a.getYear();
		}
	}

	private void backUpFilms()
	{
		File source = new File(loadFilmsLocation);
		File dest = new File(loadBackupFilmsLocation);
		try {
			FileUtils.copyFile(source, dest);
		} catch (IOException e) {
			errorLog(e.getMessage());
		}
	}

	public void setLoadBackupMembersLocation(String loadBackupMembersLocation) {
		this.loadBackupMembersLocation = loadBackupMembersLocation;
	}

	public void setLoadBackupFilmsLocation(String loadBackupFilmsLocation) {
		this.loadBackupFilmsLocation = loadBackupFilmsLocation;
	}


	/**
	 * 
	 * generates an error message and saves it to the error log file in the backup folder
	 * @param errorDetails - the details of the error that occured
	 */
	private void errorLog(String errorDetails)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		FileWriter writer = null;
		try {
			writer = new FileWriter("src/backup/errorLog.txt", true);
		} catch (IOException e1) {
		}
		try {
			writer.write("-------------------------------------------------------");
			writer.write("\n" + "Logged on: " + dateFormat.format(date) + "\n" + "error: " +  errorDetails +"\n" + "\n");
			writer.write("-------------------------------------------------------");
		} catch (IOException e) {
		}
		try {
			writer.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * writes the contents of the members file to the backup file.
	 * if an error occurs in loading then the files will be switched and so a complete and 
	 * working backup has been made
	 *
	 */

	private void backupMembers()
	{
		File membersSource = new File(loadMembersLocation);
		File membersDest = new File(loadBackupMembersLocation);
		try {
			FileUtils.copyFile(membersSource, membersDest);
		} catch (IOException e) {
			errorLog(e.getMessage());
		}
	}

	public void deleteAllRatings()
	{
		for(Member member: members)
		{
			ratings.clear();
			member.getRatings().clear();
		}
	}

	public Member getLoggedIn() {
		return loggedIn;
	}


	public ArrayList<Member> getMembers() {
		return members;
	}

	public ArrayList<Film> getFilms() {
		return films;
	}


	public ArrayList<Film> getRecommendedFilms() {
		return recommendedFilms;
	}


	public ArrayList<Film> getBetterRecommendedFilms() {
		return betterRecommendedFilms;
	}


	public void setLoggedIn(Member loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setLoadMembersLocation(String loadMembersLocation) {
		this.loadMembersLocation = loadMembersLocation;
	}

	public void setLoadFilmsLocation(String loadFilmsLocation) {
		this.loadFilmsLocation = loadFilmsLocation;
	}


	public HashMap<Integer, Double> getRatings() {
		return ratings;
	}

	public void setSaveMembersLocation(String saveMembersLocation) {
		this.saveMembersLocation = saveMembersLocation;
	}
	
	public void setSaveFilmsLocation(String saveFilmsLocation) {
		this.saveFilmsLocation = saveFilmsLocation;
	}
}