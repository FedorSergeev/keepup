# KeepUP CMS 2.0 library

![ci](https://github.com/FedorSergeev/keepup/actions/workflows/gradle.yml/badge.svg?branch=develop)
![codecov.io](https://codecov.io/gh/FedorSergeev/keepup/coverage.svg?branch=develop)
![codeql](https://github.com/FedorSergeev/keepup/actions/workflows/codeql-analysis.yml/badge.svg?branch=develop)

### Main benefits:

* reactive
* durable
* scalable
* provides entities extension without data source stand-in

## Modules

## 1. Core

Core modules contains basic set of entities and services for KeepUP based applications.

## 1.1 Profiles

Yet there are only two profiles as there is no possibility to use the source as the separate library.

Dev - profile for local application launch with local cache.
H2 - profile with already connected in-memory H2 database working in PostgreSQL mode

## 1.2 Security

Flags and profiles

Security rules

## 1.3 Managing custom user objects

Usually, in order to write a well-functioning and easily maintainable application, you need to control objects, their field types, possible operations, and other things. It is quite difficult to do this using the standard set of entities of the KeepUP framework, which is based on abstraction that implements the io.keepup.cms.core.persistence.Content interface. Therefore, we added an additional abstract service, from which you can inherit your class, which will manage operations on objects of the type you need:

```Java
public abstract class EntityOperationServiceBase<T> implements EntityService<T>`
```
        
T is the type of your object. For example, if you have an entity of type Customer, it is enough to create a class and mark it as a Spring managed bean, and you will have a service that provides CRUD operations on the desired entity.

```Java
@Service
public class CustomerOperationService extends EntityOperationServiceBase<Customer> {}
```

Any types can be used as object fields, but if they do not implement the Serializable interface, then when the object is saved, such fields will be wrapped in Proxy objects that implement this interface.

To map the primary identifier to the field of your object, use the @ContentId annotation, and to map a regular field, use the @ContentMapping annotation.

#### Example:

```Java
public class TestEntity implements Serializable {


    @ContentId
    private Long testId;

    @ContentMapping("some_value")
    private String someValue;
}
```

Please note that entities are being converted taking into account that if there is no such field or if the field has another type then the field in the entity will be null but the whole entity is not filtered. You can add additional filter to the reactive chain if you want to add some nullability checks or other predicates.