package app;
import java.io.IOException;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

import search.SearchResult;
import search.Searching;

@Controller
public class SearchController {
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String landing() {
		return "index.html";
	}

	@RequestMapping(value = "/result", method = RequestMethod.POST)
	@ResponseBody
	public SearchResult querySubmit(@RequestBody String query) {
		SearchResult result = new SearchResult();
		try {
			Searching searcher = new Searching();
			result = searcher.handleQuery(query.substring(query.indexOf('=') + 1).replace('+', ' '));
		} catch(IOException e) {
			System.err.println(e);
		}
		return result;
	}
}
