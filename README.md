# INF141_Assignment3
INF141 Assignment 3: Search Engine


# Search Engine for (some of) the ICS Network 
## UCI - INF141 Project
Made using:
[Lucene](https://lucene.apache.org/core/)
[Jsoup](https://jsoup.org/)
[Spring](https://spring.io)
[Javax.Json](https://docs.oracle.com/javaee/7/api/javax/json/package-summary.html)


Program is broken into two key components:
1. Indexing (Indexer package)
2. Searching (Search package)

The two components occur as separate executions for the program. Indexing is peformed separately beforehand because the collection being used is static. After indexing is completed a separate Spring Boot application (web interface) is ran for the actual search functionality.

## Indexing

The indexing process of the program relies heavily on the Lucene library for building an inverted index (term -> [docs]) and Jsoup for the parsing of the HTML content. The following is a high-level explanation of the process in order:

* Provided a collection of **assumed** HTML files in a nested directory structure: webpages_raw/[0-74]/[0-499]
* Indexing begins through the **Driver** class (app package)
* Uses an Indexer instance to perform a crucial step in the indexing process: **Bookkeeping**
* We were provided a JSON file containing information that mapped the HTML file documents to their respective hosted URLs
* JSON Format -> "Directory/Filename" : "URL"
* Within the **Indexer.bookkeeping()** method, the JSON file is parsed using the **javax.JSON** API. Which will basically scan through the JSON file reacting to 'events' and extracting the key (dir/fname) and value (url) from each entry
* Each extracted (k,v) pair is stored into the **Indexer** objects HashMap for future reference while indexing
* After bookkeeping is completed, the indexing process can now start. Firstly, an iterable **DirectoryStream** is passed to **Indexer.indexDocs()** to scan the outer directory (webpages_raw) of collection folders (/[0-74])
* For each folder in the 'webpages_raw' directory the Path (in the filesystem) of each folder is used to create a secondary **DirectoryStream** which will scan the files within that directory
* ie. DirectoryStream(webpages_raw) - for each folder in webpages_raw -> DirectoryStream(webpages_raw/[0-74])
* Using the DirectoryStream we can now iterate over each 'HTML' file in the folder, allowing us to index the content of each one
* For each 'HTML' **File** in the DirectoryStream we pass it to **Indexer.indexDoc()** which is only concerned with building a list of **Lucene Documents** (more on this later) to be added using the Lucene's **IndexWriter**

* **Note: From here on, when I say Document(s) I mean the built Lucene Document object in the Index, unless I specify otherwise**
* Aside: The **IndexWriter** is the main tool for building the index (using add(Document)). Using the writer we buffer the **Documents** we are indexing until the maximum amount of buffered Documents is reached and then the writer compiles the buffered into an **Index Segment** which is then written to disk in the specified **Index Directory**. In the case of this program is a local directory 'indexMulti'

* Each 'HTML' file is passed to **Indexer.createDocuments()** to be parsed by the Jsoup API in various ways
* **Key Points:** Ignore files with line # > 10000 (avoids garbage), File's path is used as a key for accessing the bookkeeping HashMap
* The JSOUP library is used to parse the text of **Body**, **Title** and **b** (bolded) tags in the HTML
* Text of those are stored in separate Strings and with the Bookkeeping **key** are passed to **Indexer.createDocument()**
* **Indexer.createDocument()** is where the Lucene Document object is created, fields added, field boosting occurs :

``` java
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
```

* **!!!Important!! This process originally generated a List of Documents for each HTML file, but the program now treats the parsed Strings as bodies of Text. Allowing for better quering techniques later. In essence, the lists are unnecessary, but were not refactored due to time**

* Each of the Documents are added by the IndexWriter which eventually is added to the existing Index being generated

## Searching

Ignoring the web interface components of the search engine the searching/querying proces all occurs in the **Seacrch** package.
The searching process relies of lucene to read (**IndexReader**) and search (**IndexSearcher**) an existing Lucene Index Directory.The following is a high-level explanation of the process in order:

* Searching process begins after receiving a query and passing the query text and a boolean representing whether the query is multi-term to **Searching.handleQuery()** method
* If the query is multi-term handleQuery() will build a **PhraseQuery()**:
```java
                PhraseQuery.Builder queryp = new PhraseQuery.Builder();
                String[] terms = queryNormal.split("\\W+");
                for(int i = 0; i < terms.length; i++) {
                  queryp.add(new Term("content", terms[i]), i);
                }
                query = queryp.build();
```
* PhraseQuery considers positional data stored in the Index when searching for terms matching the query in the index. For a valid match, PhraseQuery requires all terms to match and their positions in the query. If the query is a single term Lucene's provided **QueryParser** parses the query normally.
* The built query and the queryText is then passed to **Searching.searchIndex** which uses a Lucene **IndexSearcher** to search the index using the query for the top n matching docs (based on scoring, notes later), returning a Lucene **TopDocs** object representing the result
* The program wraps the result in a **Search.SearchResult** object to be used for further processing
* **Searching.collectResults** uses the SearchResult object to extract **ACTUAL** top documents are collected from it's Lucene TopDocs attribute:

```java
		List<String> results = new ArrayList<>();
		ScoreDoc[] docs = result.getTopHits();
		for(ScoreDoc hit : docs) {
			results.addAll(Arrays.asList(searcher.doc(hit.doc).getValues("docs")));	
		}
		return results;
```
* In this code, ```getValues("docs") ``` is returning the values for the Index Document's 'docs' field (**Remember** when we added 'docs' field for the URLs when we were indexing). So, it is really returning the URL associated with the Index Document to be displayed in the search result to User.
* The results this is set to the SearchResult's resultDocs attribute and then the SearchResult object is returned to be sent as a response to the web interface's query request. The web interface extract the returned URLs 

## Scoring / Ranking

Not much in terms of ranking/scoring was added in addition to what Lucene does behind the scenes. Basically, the only addition is fields associated with the different content of the HTML: 'Body', 'Title' and 'b' tags. The text of each is its own field in an Index Document.
* 'Title' Text boosted: 2
* 'b' Text boosted: 1.5
* 'Body' defaulted to 1

As far as I can tell Lucene performs the following for ranking/scoring:
* Vector-Space Model: Cosine Similarity
* Boolean Model: Does the Document contain the Query Terms?
* Boolean Model Approves Documents for VSM scoring
* VSM is used to derive the Lucene Conceptual Scoring -> Practical Scoring function
* The practical scoring funcation consider some of the following: tf-idf, boosted term and field values, document precision relative to query terms (how many terms for the query are in the doc)



