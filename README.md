Copyright 2015
Themistoklis Mavridis <themis.mavridis@issel.ee.auth.gr>.

<http://thesmartweb.eu>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


### SWebRank (former LSHrank).
***
SWebRank is a Topic modeling mechanism (formerly entitled LSHrank - Latent Semantic Headless rank) that employs Content Semantic Analysis through Latent Dirichlet Allocation (LDA) to generate optimal content analyzing web documents out of search engine result pages. It also evaluates the influence of webpage and semantic characteristics (such as Semantic Entities and Categories, namespaces, semantic triples) on the ranking mechanisms of major search engines always considering the domain of analysis.

##Technical details & Important Library File
***
* The project is a build with Maven and every dependency is in the pom.xml except the following.
* DBpedia Spotlight jar is not in maven repository.
* Download it from here: <http://bit.ly/DBpediaSpotlight>

##More details are coming soon....for now check the Javadoc
***
<https://github.com/thmavri/SWebRank/blob/master/SWebRank_Javadoc.zip?raw=true>

##Necessary databases
***
* MySQL database using the statements in the configuration files (you could possibly need to create the tables without the foreign keys first and then add them to work)
* Elasticsearch to store the various content. The name used for the cluster in the project is "lshrankldacluster". You can use whatever you want as long as you change it in the project in the necessary files. For Elasticsearch all you have to do is to install it and set the cluster name in the Elasticsearch config files. The indexes are built automatically through the source code.
* Elasticsearch guide : <http://www.elastic.co/guide/en/elasticsearch/reference/current/index.html>

##Sample Input and Configuration
***
* Sample files can be find in the respective folders
* The files should be .txt for the API credentials and the input.
* The SQL commands are in order to setup the MySQL database. You may want to configure the tables without the foreign keys first.

##Important Library File
***
DBpedia Spotlight jar is not in maven repository.
Download it from here: <http://bit.ly/DBpediaSpotlight>

###Publications
***
* Exploring the influence of semantic data on the ranking mechanisms of search engines. T. Mavridis, L. Aroyo, A.L. Symeonidis. ACM Transactions on the Web (submitted Feb 15).
* Identifying valid search engine ranking factors in a Web 2.0 and Web 3.0 context for building efficient SEO mechanisms. T. Mavridis,
* A.L. Symeonidis. Engineering Applications of Artificial Intelligence, Elsevier, Vol. 41, May 15, pp.75-91.
* Semantic analysis of web documents for the generation of optimal content. T. Mavridis, Andreas L. Symeonidis. Engineering Applications of Artificial Intelligence, Elsevier, Vol.35, Oct. 14, pp. 114-130.
* Identifying webpage semantics for search engine optimization. T. Mavridis, Andreas L. Symeonidis. 8th International Conference on Web Information Systems and Technologies (WEBIST), Apr. 12, pp. 272-275.

It is created by Themis Mavridis (my personal webpage: <http://thesmartweb.eu>) and is under the Apache License 2.0.


