package indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

public class Indexer {
	
	private final String indexDir = "indexMulti";
	private IndexWriter writer;
	private JsonParser parser;
	private Map<String, String> book;
	
	public Indexer() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()).setOpenMode(OpenMode.CREATE));
		parser = Json.createParser(new FileInputStream(new File("webpages_raw/bookkeeping.json")));
		book = new HashMap<>();
	}
	
	public void bookkeeping() {
		try {
			JsonParser parser = Json.createParser(new FileInputStream("webpages_raw/bookkeeping.json"));
			String dir_key = "";
			while(parser.hasNext()) {
				JsonParser.Event event = parser.next();
				switch(event) {
					case KEY_NAME:
						dir_key = "webpages_raw\\" + parser.getString().replace('/', '\\');
						break;
					case VALUE_STRING:
						book.put(dir_key, parser.getString());
						break;
					default:
						continue;
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void indexDocs(DirectoryStream<Path> coll_dir) {
		try {
			for(Path entry : coll_dir) {
				DirectoryStream<Path> docs_stream = Files.newDirectoryStream(entry);
				for(Path doc : docs_stream) {
					File input = doc.toFile();
					System.out.println(input.getPath());
					indexDoc(input);
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			if(writer.isOpen() && writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		}
	}
	
	private void indexDoc(File file) throws IOException {
		List<Document> docs = createDocuments(file);
		for(Document doc : docs) {
			writer.addDocument(doc);
		}
	}
	
	private List<Document> createDocuments(File file) throws IOException {
		List<Document> documents = new ArrayList<Document>();
		String doc_key = file.getPath();
		try {
			if(file.length() < 10000) {
				String body = "", title = "", bolded = "";
				org.jsoup.nodes.Document doc = Jsoup.parse(file, "UTF-8");
				if(doc.body() != null && !doc.body().equals("")) {
					if(doc.select("b").text() != null && !doc.select("b").text().equals("")) {
						bolded = doc.select("b").text();
					}
					doc.select("b").remove();
					body = doc.body().text();
				}
				if(doc.title() != null & !doc.title().equals("")) {
					title = doc.title();
				}
				documents.add(createDocument(body, title, bolded, book.get(doc_key)));
			}
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
		return documents;
	}
	
	//Re-index; new field 'title'
	private Document createDocument(String body, String title, String bolded, String doc_path) throws IOException {
		Document document = new Document();
		
		Field boldField = new TextField("bolded", bolded, Store.YES);
		Field titleField = new TextField("title", title, Store.YES);
		Field bodyField = new TextField("content", body, Store.YES);
		Field docField = new StringField("docs", doc_path, Store.YES);
		boldField.setBoost(1.5f);
		titleField.setBoost(2.0f);
		document.add(boldField);
		document.add(titleField);
		document.add(bodyField);
		document.add(docField);

		return document;
	}

	public IndexWriter getWriter() {
		return writer;
	}
	
	public Map<String, String> getBook() {
		return book;
	}
}
