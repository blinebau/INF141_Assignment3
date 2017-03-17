package indexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;

public class IndexStatistics {
	
	private final String indexDir = "index";
	private IndexReader reader;
	
	public IndexStatistics () throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
	} 
	//Unique terms
	//Term Freqs
	//Doc Freqs
	//Size of Index
	//Tf-idf?
	
	public long getDocCount() throws IOException {
		TermsEnum docs = getTermEnumforDoc();
		long doc_count = 1;
		do {
			doc_count++;
		} while(docs.next() != null);
		return doc_count;
	}
	
	public long getTermCount() throws IOException {
		return reader.getDocCount("term");
	}
	
	public long getUniqueTermCount() throws IOException {
		Terms terms = MultiFields.getTerms(reader, "term");
		return terms.size();
	}
	
	private TermsEnum getTermEnumForTerm() throws IOException {
		return MultiFields.getTerms(reader, "term").iterator();
	}
	
	private TermsEnum getTermEnumforDoc() throws IOException {
		return MultiFields.getTerms(reader, "docs").iterator();
	}
	
/*	public Map<String, Long> getTermFreq() throws IOException {
		Map<String, Long> termFreq = new HashMap<>();
		TermsEnum te = getTermEnumForTerm();
		while(te.next() != null) {
			long total_freq = te.docFreq();
			termFreq.put(te.term().utf8ToString(), total_freq);
		}
		
		return termFreq;
	}*/
	
	public Map<String, Integer> getDocFreq() throws IOException {
		Map<String, Integer> docFreq = new HashMap<>();
		TermsEnum te = getTermEnumForTerm();
		while(te.next() != null) {
			int doc_freq = te.docFreq();
			docFreq.put(te.term().utf8ToString(), doc_freq);
		}
		
		return docFreq;
	}
	
	public long getIndexMemSize() {
		File dir = new File("index");
		return dir.length();
	}
	
	public long getIndexMemSize(int index_id) {
		File dir = new File("index/" + Integer.toString(index_id));
		return dir.length();
	}
	
	public IndexReader getReader() {
		return reader;
	}
	
	public void generateStats() {
		//generate a selected list of stats for a specific index or all indexes(?)
	}
	
	public void outputIndex() throws IOException {
		for(int i = 0; i < 100; i++) {
			Iterator<IndexableField> field = reader.document(i).getFields().iterator();
			while(field.hasNext()) {
				System.out.println(field.next().stringValue());
			}
		}
	}
}
