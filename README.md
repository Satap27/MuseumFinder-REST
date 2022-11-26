# MuseumFinder-REST
MuseumFinder-REST is a **RESTful API** that allows the management and retrieval of information relating to a large number of Italian museums.

## Motivations
This project context is my Bachelor research thesis about **migration methodologies from desktop architectures to lightweight web architectures in Java ecosystem**.  
The case study is a Java Desktop application, [Museum Finder](https://github.com/Satap27/MuseumFinder), that provides the same functionalities but with less flexibility.  
A detailed document about the implementation, the research and its results is available [here](thesis.pdf) (in italian).

## Functionalities
(from [Museum Finder](https://github.com/Satap27/MuseumFinder))

> All users who want to use the services offered by Museum Finder must log in with their credentials. The software is meant for three different types of users:
>
> * The **administrator** is responsible for keeping the data correct and up-to-date: he receives error reports and after verifying their validity, makes changes to the database. Also, he takes care of adding new museums and removing those that no longer adhere to the service.
>
> * Regular **users** have the ability to search among the museums in the database through a search bar, and then access the related information. The search function can be set according to various criteria, which determine the results: for example, it can be searched by keywords, by distance from a certain location or by positive reviews. In addition, users can also book visits to a museum or leave reviews.
> 
> * Finally, **museum owners** can request the addition (but also the removal) of their own museum. The owner of a museum that is present in the database can access an interface that allows him to perform various operations: add events (ticket price reductions, special exhibitions) or view a series of statistics such as the number of visits to the page or the number of reservations.
>
> The application also includes an error reporting mechanism; users can make reports if they notice inaccuracies or missing information for a given museum. These are reviewed by the respective museum owners who have the option to approve or disapprove them. The approved requests are then sent to the administrator, who proceeds with the modification. Any changes can also be requested directly from the owner of a museum and in this case obviously do not require approval.

## Technologies and frameworks
The REST interface implementation is built over some _lightweight_ JavaSE frameworks, and an external API:

* [**Java Spark**](https://sparkjava.com/), a simple and expressive web framework that allow to rapidly build REST API. 
* [**Ebean**](https://ebean.io/), provides a service of **Object-relational Mapping** (ORM), placing itself as an intermediary between the program logic and the Database. It represents an alternative to heavier platforms like Hibernate.
* [**Elasticsearch**](https://www.elastic.co/), an extremely powerful search engine, specialized in **full-text search** and integrable with _Ebean_ through the API that Elasticsearch exposes. It is implemented as a document oriented database, which indexes the supplied documents so that searching and retrieval are very fast.
