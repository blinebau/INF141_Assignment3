package app;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import indexer.Indexer;

public class Driver {
	
	/*
	 * 
	 * 	Content Tags
	 * 	Headers: h1, h2, h3...
	 * 	Content Tags: p, body, title, article, section, span
	 */
	
	//Download/Input Corpus
	//Parse Corpus - HTML Content Sections for Terms
	//Generate Pairs - (Term, DocID, etc...)
	//Populate Pairings with Pairs and Updating necessary Stats for each term
	//Append Pairs to existing Pairings in Index
		//If not exist, 
	
	/*
	 * ==== Block sort-based Indexing ====
	 * Alg:
	 * 	BSBIndexConstr()
	 * 		n = 0
	 * 		while(collection not indexed)
	 * 			do n++
	 *				block = ParseNextBlock() ...Define block size and cut-off for best in-memory usage
	 *				BSBIInvert(block) ...Sort Pairs of (TermID, DocID) and Merge Pairs into a Postings List
	 *				WriteBlockToDisk(block, fn) ...Write block of postings to a temporary file (fileN.txt)
	 *		MergeBlocks(f1...fn; return fmerged) ...Merge all temp postings file into single index
	 *
	 *	Merging Step
	 *		Open all block files simultaneously and maintain small read buffers
	 *		Single write buffer for the merged file
	 *		For each iteration select lowest 
	 *		
	 *	
	 *				
	 */
	public static void main(String [] args) {
		
		//Move work to separate methods
		//Assume static collection
		//final String doc_id = "1";
		String coll_path = "webpages_raw";
		Path dir;
		Indexer indexer;
		long start_time = System.currentTimeMillis();
		long end_time;
		try {
			
			indexer = new Indexer();
			
			indexer.bookkeeping();
			DirectoryStream<Path> coll_dir = Files.newDirectoryStream(FileSystems.getDefault().getPath(coll_path), new DirectoryStream.Filter<Path>() {
				public boolean accept(Path file) throws IOException {
					
					return !file.getFileName().toString().matches(".*json|.*tsv");
				}
			});
			indexer.indexDocs(coll_dir);		
			//Stats
			//IndexStatistics stats = new IndexStatistics();
			//System.out.println(stats.getUniqueTermCount());
			//stats.outputIndex();
			//stats.getTermFreq().entrySet().stream().forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
 			/*
			System.out.println("=== Index of DocId: " + doc_id + " ===");
			System.out.println("Document Count: " + stats.getDocCount());
			System.out.println("Term Count: " + stats.getTermCount());
			System.out.println("Unique Term Count: " + stats.getUniqueTermCount());
			System.out.println("Index " + doc_id + " Disk Size: " + (stats.getIndexMemSize()) + "kb");*/
			
		} catch (Exception e) {
			System.err.println(e);
		}
		
		end_time = System.currentTimeMillis() - start_time;
		System.out.println(end_time / 1000);
	}
	
	private static void index() {
		//TODO
	}
	
	private static void indexStatistics() {
		//TODO
	}
}
