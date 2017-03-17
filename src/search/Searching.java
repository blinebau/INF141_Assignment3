package search;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class Searching {
	
	private final int numResults = 10;
	private final String indexDir = "indexMulti";
	private final String termField = "content";
	private IndexReader reader;
	private IndexSearcher searcher;
	private QueryParser parser;
	
	public Searching() throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
		searcher = new IndexSearcher(reader);
		parser = new QueryParser(termField, new StandardAnalyzer());
	}
	
	public SearchResult handleQuery(String queryRaw) throws IOException {
		boolean phrase;
		String queryText = "";
		phrase = queryRaw.matches("\\w+\\s+\\w+") ? true : false;
		queryText = queryRaw.toLowerCase().trim();
		Query query = processSearch(queryText, phrase);
		SearchResult resultWrapper = searchIndex(query, queryText);
		return resultWrapper;
	} 
	
	private Query processSearch(String queryNormal, boolean phrase) {
		Query query = null;
		try {
			if(phrase) {
				PhraseQuery.Builder queryp = new PhraseQuery.Builder();
				String[] terms = queryNormal.split("\\W+");
				for(int i = 0; i < terms.length; i++) {
					queryp.add(new Term("content", terms[i]), i);
				}
				query = queryp.build();
			} else {
				query = parser.parse(queryNormal);
			}
			//outputSearchResult(searchIndex(query, queryNormal));
		} catch(ParseException e) {
			System.err.println(e);
		}
		return query;
	}
	
	private SearchResult searchIndex(Query query, String queryRaw) throws IOException {
		TopDocs doc = searcher.search(query, numResults);
		SearchResult result = new SearchResult(doc, queryRaw);
		result.setResultDocs(collectResults(result));
		return result;
	}
	
	private List<String> collectResults(SearchResult result) throws IOException {
		List<String> results = new ArrayList<>();
		ScoreDoc[] docs = result.getTopHits();
		for(ScoreDoc hit : docs) {
			results.addAll(Arrays.asList(searcher.doc(hit.doc).getValues("docs")));	
		}
		return results;
	}
	
	private void outputSearchResult(SearchResult result) throws IOException {
		System.out.println("\n====== Search Results: \"" + result.getQueryText() + "\" ======");
		ScoreDoc[] docs = result.getTopHits();
		for(ScoreDoc hit : docs) {
			Document doc = searcher.doc(hit.doc);
			String[] doc_urls = doc.getValues("docs");
			for(int i = 0; i < doc_urls.length; i++) {
				System.out.println(doc_urls[i]+ "\n");
			}
		}
	}

}
