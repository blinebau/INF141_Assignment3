package search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class SearchResult {
	
	private TopDocs result;
	private ScoreDoc[] topHits;
	private int numHits;
	private String queryText;
	private List<String> resultDocs;
	
	public SearchResult() {
		resultDocs = new ArrayList<>();
	}
	
	public SearchResult(TopDocs result, String queryRaw) {
		this.result = result;
		topHits = result.scoreDocs;
		numHits = result.totalHits;
		queryText = queryRaw;
		resultDocs = new ArrayList<>();
	}
	
	public TopDocs getTopDocs() {
		return result;
	}
	
	public ScoreDoc[] getTopHits() {
		return topHits;
	}
	
	public int numHits() {
		return numHits;
	}
	
	public String getQueryText() {
		return queryText;
	}
	
	public void setResultDocs(List<String> results) {
		resultDocs = results;
	}
	
	public List<String> getResultDocs() {
		return resultDocs;
	}
}
