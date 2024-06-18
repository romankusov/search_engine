# Search engine
## Description
Back-end of web application of educational project which provides search Russian text on given sites which parsed saved and indexed to database 
## Configuration
Sites for search are defined in application.yaml as follows: 
```
indexing-settings:
  sites:
    - url: https://url_1.com/
      name: Sitename_1
    - url: http://url_2.ru/
      name: Sitename_2
```

Application is configured on localhost, port:8080; database MySQL on port 3306
## API
### GET /api/statistics
This GET request without parameters provides statistics and other service information about search indexes and engine as it.
### GET /api/startIndexing
This GET request without parameters provides start of indexation of given sites in multithreading mode. Data from each page (content, status code etc.) saved to DB. Russian words from page content convert to lemmas (normal form of word) by means of library Apache Lucene Morphology, then lemmas are saved to database.
### POST /api/indexPage
This POST request with one parameter (page url) provides an indexation of the one page of one of the sites.
### GET /api/stopIndex
This GET request without parameters provides stop of indexation of sites
### GET /api/search
GET request with 4 parameters: search query (required), site for search (not required, by default search is going on all sites in scope), offset for pagination (not reqiured, by default offset = 0), limit for search results (not required, by default limit = 20).
Request provides search of Russian words under the query on pages saved to database
## Stack
Java 17, Spring Boot, Spring Data Hibernate, Maven, Lombok, JSOUP, Slf4j, MySQL, Morphology Library, JUnit
