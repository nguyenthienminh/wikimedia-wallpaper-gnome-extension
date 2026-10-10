import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import org.apache.commons.io.*;
import org.jsoup.*;
public class Converter {
	public static void main(String[] args) {
		while (true) {
			downloadJSONdataAndConvertIt();
			try {
				Thread.sleep(600000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void downloadJSONdataAndConvertIt() {
		File createdImageJSONFile = new File("/var/www/html/wikimediaCommons.json");
		File downloadedWikimediaJSONFile = new File("wikimedia.json");
		try {
			// Downloads the current json data from Wikimedia Commons' Picture of the Day
			LocalDate temp = LocalDate.now();
			URI uri = new URI("https://commons.wikimedia.org/w/api.php?action=expandtemplates&text=%7B%7BPotd/" + temp.getYear() +"-" + String.format("%02d", temp.getMonthValue()) + "-" + String.format("%02d", temp.getDayOfMonth()) + "%7D%7D&format=json");
			URLConnection conn = uri.toURL().openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), downloadedWikimediaJSONFile);
			Scanner scanner = new Scanner(downloadedWikimediaJSONFile);
			String jsonFile = new String();
			scanner.useDelimiter("\r?\n");
			while (scanner.hasNextLine()) {
				jsonFile = scanner.nextLine();
			}
			jsonFile = jsonFile.replace("*", "asterisk");
			scanner.close();
			
			// write the "fixed" json output back to the file, overwriting the older version (thank you asterisk!)
			PrintStream printstream = new PrintStream(new File("wikimedia.json"));
			printstream.print(jsonFile);
			printstream.close(); 
			
			JsonObject object = JsonParser.parseString(jsonFile).getAsJsonObject();
			JsonObject expandTemplates = object.get("expandtemplates").getAsJsonObject();
			String actualImageFileName = expandTemplates.get("asterisk").getAsString();
			
			// read the Wikimedia image-link data in as a string
			File imageLinkData = new File("wikimediaImageLinkData.json");
			uri = new URI("https://commons.wikimedia.org/w/api.php?titles=Image:" + URLEncoder.encode(actualImageFileName, "UTF-8") + "&action=query&prop=imageinfo&iiprop=url&format=json");
			conn = uri.toURL().openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), imageLinkData);
			String downloadedLinkData = new String();
			Scanner imageLinkReader = new Scanner(imageLinkData);
			while (imageLinkReader.hasNextLine()) {
				downloadedLinkData = imageLinkReader.nextLine();
			}
			imageLinkReader.close();
			JsonObject linkJsonObject = JsonParser.parseString(downloadedLinkData).getAsJsonObject();
			JsonObject query = linkJsonObject.get("query").getAsJsonObject();
			JsonObject pages = query.get("pages").getAsJsonObject();
			String randomNumberAsAKey = new String();
			// code to get the first element from a JSON object is copied from https://stackoverflow.com/questions/41382181/gson-help-trying-to-get-the-first-value-key-of-json-using-gson.
			for (Map.Entry<String, JsonElement> e : pages.entrySet()) {
    			randomNumberAsAKey = e.getKey();
			}		
			JsonObject randomNumber = pages.get(randomNumberAsAKey).getAsJsonObject();
			JsonArray imageInfo = randomNumber.get("imageinfo").getAsJsonArray();
			String url = imageInfo.get(0).getAsJsonObject().get("url").getAsString();
			
			// get the image artists
			File imageArtistData = new File("wikimediaImageArtistData.json");
			uri = new URI("https://commons.wikimedia.org/w/api.php?action=query&titles=Image:" + URLEncoder.encode(actualImageFileName, "UTF-8") + "&prop=imageinfo&iiprop=extmetadata&format=json");
			conn = uri.toURL().openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), imageArtistData);
			String downloadedArtistData = new String();
			Scanner imageArtistReader = new Scanner(imageArtistData);
			while (imageArtistReader.hasNextLine()) {
				downloadedArtistData = imageArtistReader.nextLine();
			}
			imageArtistReader.close();
			JsonObject descriptionJsonObject = JsonParser.parseString(downloadedArtistData).getAsJsonObject();
			query = descriptionJsonObject.get("query").getAsJsonObject();
			pages = query.get("pages").getAsJsonObject();
			randomNumberAsAKey = new String();
			// code to get the first element from a JSON object is copied from https://stackoverflow.com/questions/41382181/gson-help-trying-to-get-the-first-value-key-of-json-using-gson.
			for (Map.Entry<String, JsonElement> e : pages.entrySet()) {
    			randomNumberAsAKey = e.getKey();
			}		
			randomNumber = pages.get(randomNumberAsAKey).getAsJsonObject();
			imageInfo = randomNumber.get("imageinfo").getAsJsonArray();
			JsonObject imageInfoChild = imageInfo.get(0).getAsJsonObject();
			JsonObject extMetadata = imageInfoChild.get("extmetadata").getAsJsonObject();
			String imageArtist = Jsoup.parse(extMetadata.get("Artist").getAsJsonObject().get("value").getAsString()).text();

			// get the images' description
			File imageDescriptionData = new File("wikimediaImageDescData.json");
			uri = new URI("https://commons.wikimedia.org/w/api.php?action=expandtemplates&text=%7B%7BPotd/" + temp.getYear() +"-" + String.format("%02d", temp.getMonthValue()) + "-" + String.format("%02d", temp.getDayOfMonth()) + "%20%28en%29%7D%7D&format=json&action=parse");
			conn = uri.toURL().openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), imageDescriptionData);
			scanner = new Scanner(imageDescriptionData);
			jsonFile = new String();
			scanner.useDelimiter("\r?\n");
			while (scanner.hasNextLine()) {
				jsonFile = scanner.nextLine();
			}
			jsonFile = jsonFile.replace("*", "asterisk");
			scanner.close();
			
			// write the "fixed" json output back to the file, overwriting the older version (thank you asterisk!)
			printstream = new PrintStream(new File("wikimediaImageDescData.json"));
			printstream.print(jsonFile);
			printstream.close(); 
			
			JsonObject parse = JsonParser.parseString(jsonFile).getAsJsonObject().get("parse").getAsJsonObject();
			JsonObject text = parse.get("text").getAsJsonObject();
			String imageDescription = Jsoup.parse(text.get("asterisk").getAsString()).text();
			
			// get the image hash
			File wikimediaImageHash = new File("wikimediaImageHash.json");
			uri = new URI("https://commons.wikimedia.org/w/api.php?action=query&titles=Image:" + URLEncoder.encode(actualImageFileName, "UTF-8") + "&prop=imageinfo&iiprop=sha1&format=json");
			conn = uri.toURL().openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), wikimediaImageHash);
			String downloadedHashData = new String();
			Scanner imageHashReader = new Scanner(wikimediaImageHash);
			while (imageHashReader.hasNextLine()) {
				downloadedHashData = imageHashReader.nextLine();
			}
			JsonObject hashJsonObject = JsonParser.parseString(downloadedHashData).getAsJsonObject();
			query = hashJsonObject.get("query").getAsJsonObject();
			pages = query.get("pages").getAsJsonObject();
			randomNumberAsAKey = new String();
			// code to get the first element from a JSON object is copied from https://stackoverflow.com/questions/41382181/gson-help-trying-to-get-the-first-value-key-of-json-using-gson.
			for (Map.Entry<String, JsonElement> e : pages.entrySet()) {
    			randomNumberAsAKey = e.getKey();
			}		
			randomNumber = pages.get(randomNumberAsAKey).getAsJsonObject();
			imageInfo = randomNumber.get("imageinfo").getAsJsonArray();
			String hash = imageInfo.get(0).getAsJsonObject().get("sha1").getAsString();
			
System.out.println("Done!");

			// put the pieces of info that was extracted from the Wikimedia API calls and re-encode them in Bing-style JSON format
			String startdate = Integer.toString(temp.getYear());
			startdate = startdate.concat(String.format("%02d", temp.getMonthValue()));
			startdate = startdate.concat(String.format("%02d", temp.getDayOfMonth()));

			String enddate = Integer.toString(temp.getYear());
			enddate = enddate.concat(String.format("%02d", temp.getMonthValue()));
			enddate = enddate.concat(String.format("%02d", temp.getDayOfMonth() + 1));
			
			imageDescription = imageDescription.replace("(", "[");
			imageDescription = imageDescription.replace(")", "]");
			imageDescription = imageDescription.replace("\"",  "\\\"");
			
			imageArtist = imageArtist.replace("(", "[");
			imageArtist = imageArtist.replace(")", "]");
			imageArtist = imageArtist.replace("\"",  "\\\"");
			
			actualImageFileName = actualImageFileName.replace("\"", "\\\"");

			// actual piece of code that actually does the parsing & inserting (also yes, I didn't even bother using a parser for this one!)
			String finalJSONFile = new String();
			finalJSONFile = finalJSONFile.concat("{\"images\":[{");
			finalJSONFile = finalJSONFile.concat("\"startdate\": \"" + startdate + "\",");
			finalJSONFile = finalJSONFile.concat("\"fullstartdate\": \"" + startdate + "1700\",");
			finalJSONFile = finalJSONFile.concat("\"enddate\": \"" + enddate + "\",");
			finalJSONFile = finalJSONFile.concat("\"url\": \"" + url + "\",");
			finalJSONFile = finalJSONFile.concat("\"urlbase\": \"" + url + "?\",");
			finalJSONFile = finalJSONFile.concat("\"copyright\": \"" + imageDescription + " (© " + imageArtist + "/Wikimedia Commons)\",");
			finalJSONFile = finalJSONFile.concat("\"copyrightlink\": \"https://commons.wikimedia.org/wiki/File:" + actualImageFileName + "\",");
			finalJSONFile = finalJSONFile.concat("\"title\": \"Wikimedia Commons' picture of the day for " + temp.getYear() + "-" + temp.getMonthValue() + "-" + temp.getDayOfMonth() + "\",");
			finalJSONFile = finalJSONFile.concat("\"quiz\": \"https://www.nytimes.com/games/wordle/index.html\",");
			finalJSONFile = finalJSONFile.concat("\"wp\": true,");
			finalJSONFile = finalJSONFile.concat("\"hsh\": \"" + hash + "\",");
			finalJSONFile = finalJSONFile.concat("\"drk\": 1,");
			finalJSONFile = finalJSONFile.concat("\"top\": 1,");
			finalJSONFile = finalJSONFile.concat("\"bot\": 1,");
			finalJSONFile = finalJSONFile.concat("\"hs\": []");
			finalJSONFile = finalJSONFile.concat("}],\"tooltips\":{\"loading\":\"Loading...\",\"previous\":\"Previous image\",\"next\":\"Next image\",\"walle\":\"This image is not available to download as wallpaper.\",\"walls\":\"Download this image. Use of this image is restricted to wallpaper only.\"}}");
			
			// writes the final string to the json file at the server directory
			PrintStream newPrintStream = new PrintStream(createdImageJSONFile);
			newPrintStream.print(finalJSONFile);
			newPrintStream.close();
		}
		catch (Exception e) {
				System.out.println("Something went wrong. Please try running this again.");
				e.printStackTrace();
		}
	}
}
