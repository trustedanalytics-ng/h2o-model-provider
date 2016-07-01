# h2o-model-provider
Provider of models from TAP H2O instances.
It is intended to be used by `model-catalog`

## Required services

* **service-exposer** - holds information about service instances

## How to build

It's a Spring Boot application build by maven. All that's needed is a single command to compile, run tests and build a jar:

```
$ mvn verify
```

## How to run locally

```
$ mvn spring-boot:run
```
After starting a local instance, it's available at http://localhost:9995
