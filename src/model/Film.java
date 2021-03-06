package model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * model for a film in the rating system
 * @author Stephen
 *
 */

public class Film 
{
	private int ID;
	private String title;
	private int year;
	private String genre;
	private String imagePath;
	private double sumOfRatings = 0;
	private double numberOfRatings = 0;
	

	public Film(int ID, String title, int year, String genre)
	{
		this.ID = ID;
		this.title = title;
		this.year = year;
		this.genre = genre;
		this.imagePath = "src/images/no_image_available.jpg";
	}

	public void addRating(int rating)
	{
		this.sumOfRatings += rating;
		this.numberOfRatings++;
	}
	
	public void subtractRating(int rating)
	{
		this.sumOfRatings -= rating;
		this.numberOfRatings--;
	}
	
	public double getSumOfRatings()
	{
		return sumOfRatings;
	}
	
	public double getNumberOfRatings() {
		return numberOfRatings;
	}
	
	public double getAverageRating()
	{
		return (double) (sumOfRatings/numberOfRatings);
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public BufferedImage selectInitialImage() throws IOException
	{
		BufferedImage firstImage = ImageIO.read(new File("src/images/no_image_available.jpg"));
		return firstImage;
	}

	@Override
	public String toString() {
		return "Film [ID=" + ID + ", title=" + title + ", year=" + year
				+ ", genre=" + genre + "]";
	}
	
	public String getFilmImage()
	{
		return imagePath;
	}
	
	public void setFilmImage(String imagePath) 
	{
		this.imagePath = imagePath;
	}
	
}
