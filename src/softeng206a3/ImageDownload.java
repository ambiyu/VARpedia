package softeng206a3;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

public class ImageDownload {

	public ImageDownload() {
		
	}
	public static String getAPIKey(String key) throws Exception {

		String config = System.getProperty("user.dir") 
				+ System.getProperty("file.separator")+ "resources/flickr-api-keys.txt"; 

		//		String config = System.getProperty("user.home")
		//				+ System.getProperty("file.separator")+ "bin" 
		//				+ System.getProperty("file.separator")+ "flickr-api-keys.txt"; 

		File file = new File(config); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 

		String line;
		while ( (line = br.readLine()) != null ) {
			if (line.trim().startsWith(key)) {
				br.close();
				return line.substring(line.indexOf("=")+1).trim();
			}
		}
		br.close();
		throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
	}

	public int downloadImages(String searchTerm, int numOfImages) {
		int numOfImagesDwn = 0;
		
		try {
			String apiKey = "8fbe78a021bff89c660d39ec9876a749"; //getAPIKey("apiKey");
			String sharedSecret = "084afd66ee43505b";//getAPIKey("sharedSecret");

			Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

			//String query = "bicycle";
			//int resultsPerPage = 5;
			int page = 0;
			
			File dir = new File(".temp/downloads");
			dir.mkdir();

			PhotosInterface photos = flickr.getPhotosInterface();
			SearchParameters params = new SearchParameters();
			params.setSort(SearchParameters.RELEVANCE);
			params.setMedia("photos"); 
			params.setText(searchTerm);

			PhotoList<Photo> results = photos.search(params, numOfImages, page);
			

			for (Photo photo: results) {
				try {
					BufferedImage image = photos.getImage(photo,Size.LARGE);
					String filename = searchTerm.trim().replace(' ', '-')+"-"+System.currentTimeMillis()+"-"+photo.getId()+".jpg";
					File outputfile = new File(".temp/downloads",filename);
					ImageIO.write(image, "jpg", outputfile);
					
					numOfImagesDwn++;
				} catch (FlickrException fe) {
					System.err.println("Ignoring image " +photo.getId() +": "+ fe.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\nDone");
		return numOfImagesDwn;

	}
}