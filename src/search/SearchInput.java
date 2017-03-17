package search;
/*package app.search;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SearchInput {
	
	public static void main(String [] args) {
		try {
			Searching search = new Searching();
			handleInput(search);
		} catch(IOException e) {
			System.err.println(e);
		}
	}
	
	private static void handleInput(Searching search) throws IOException {
		Scanner s;
		String query = "";
		boolean phrase;
		s = new Scanner(System.in);
		do {
			System.out.print("Please enter a query, or type _EXIT to close the program: ");
			query = s.nextLine();
			if(query.equalsIgnoreCase("_EXIT")) {
				break;
			}
			query = normalizeQuery(query);
			phrase = query.matches("\\w+\\s+\\w+") ? true : false;
			search.processSearch(query, phrase);
		} while(true);
		s.close();
	}
	
	private static String normalizeQuery(String queryRaw) {
		String normalQuery = queryRaw.toLowerCase().trim();
		return normalQuery;
	}

}
*/