# h2o-model-provider
Provider of models of TAP [H2O](http://www.h2o.ai/) instances.

## Purposes
Idea behind h2o-model-provider is simple. It is intended to periodically ask all h2o servers on platform for models residing on them. Let’s name this point of flow the lookup phase. All models gathered in this phase need to be placed in [model-catalog](https://github.com/trustedanalytics/model-catalog), then. Similarly we will refer to this action as populate phase. Last operation needed consists in saving data in internal state. Let’s call it memorize phase.

## Architectural overview
It was determined that lookup phase shall be based on communication with [Catalog](https://github.com/trustedanalytics/tap-catalog) component. All h2o-servers` descriptions can be obtained by issuing requests to this web service. Having bearings describing all h2o’s we then concurrently ask for all models created on all h2o instances.
Populate phase needs all fresh H2o models to be transformed to JARs. TAP introduces another service called [h2o-scoring-engine-publisher](https://github.com/trustedanalytics/h2o-scoring-engine-publisher) to perform these tasks. H2o-model-provider reaches publisher, downloads and places binary artifacts in model-catalog. Along with that, metadata related to models are sent to model-catalog. To avoid bandwidth consumption all uploads are done intelligently. By that it is meant that we do not re-send any data that already was stored inside model-catalog.
Memorize phase is responsible for saving informations about what was already sent to model-catalog. Data gathered in this phase are then used to determine all newly created models only during populate part.

To better grasp all interactions in described scenario, please refer to scheme below.

![alt text](/scheme.png "h2o-model-provider scenario")

---

## Required services
* **Catalog** - holds information about offerings
* **Model-catalog** - store of models and its artifacts
* **H2o-scoring-engine-publisher** - service capable of building JARs out of h2o models
* **Redis** - database that holds informations about already pushed/populated models

---

## REST API
Application does not expose any HTTP endpoints. All operations are executed automatically without user intervenions every predefined number of seconds.

---

## How to build

It's a Spring Boot application build by maven. All that's needed is a single command to compile, run tests and build a jar:

```
$ mvn verify
```

---

## How to run locally
Set all required environment variables:
* CATALOG_HOST - domain name of catalog service
* CATALOG_PORT - port number of catalog service
* CATALOG_USER & CATALOG_PASS - basic auth credentials to catalog service
* MODEL_CATALOG_HOST - domain name of model-catalog service
* MODEL_CATALOG_PORT - port number of model-catalog service
* SE_PUBLISHER_HOST - domain name of h2o-scoring-engine-publisher service
* SE_PUBLISHER_PORT - port h2o-scoring-engine publisher is exposed at
* REDIS_HOST - domain name of redis database instance
* REDIS_PORT - port of redis database instance
* TOKEN_URI - URI providing tokens to be used in communication with model-catalog
* CLIENT_ID - id of client registered in UAA to be used when asking for token
* CLIENT_SECRET - secret of client registered in UAA to be used when asking for token

Notice: Provide all hostnames without protocol (e.g. http://) prefixes.

When environment variables are in place start h2o-model-provider by issuing:
```
$ mvn spring-boot:run
```
