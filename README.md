KeepUP CMS 2.0 library

![ci](https://github.com/FedorSergeev/keepup/actions/workflows/gradle.yml/badge.svg?branch=develop)
![codecov.io](https://codecov.io/gh/FedorSergeev/keepup/coverage.svg?branch=develop)
![codeql](https://github.com/FedorSergeev/keepup/actions/workflows/codeql-analysis.yml/badge.svg?branch=develop)

Main benefits:

- reactive
- durable
- scalable
- provides entities extension without data source stand-in

Modules

1. Core

Core modules contains basic set of entities and services for KeepUP based applications.

1.1 Profiles

Yet there are only two profiles as there is no possibility to use the source as the separate library.

Dev - profile for local application launch with local cache.
H2 - profile with already connected in-memory H2 database working in PostgreSQL mode