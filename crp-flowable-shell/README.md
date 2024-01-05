# flowable-shell

Shell application to manipulate Flowable objects. 
## Build
```
mvnw install -DskipTests=true
```
## Execute
```
java -jar target/flowable-shell-1.0.0-SNAPSHOT.jar
```
## Use
```shell script
shell:>help
AVAILABLE COMMANDS

Deployment
       list-deployments, lsd: list deployments
       rmd, delete-deployments: Delete all deployments with given name, tenantId from runtime. WARNING - use only for testing purposes
       deploy: Deploy given application

Designer
       dx, designer-export: Export application model from modeler to file.

Model
       import: Import file to modeler.
       ls, list: List models.
       rm, delete-model: Delete model from modeler.
       export-bar: Export deployable model from modeler to file.
       export: Export model from modeler to file.

Raw Rest
       ex, execute: execute url.
       exl, execute-logged: execute url with logged in client.

Template Processor
       gt, generate-test: Generate test from flowable history.

Utils
       zip: Zip directory to file.
       configure: Configure flowable rest endpoint.
       unzip: Unzip file to directory.
```

### Export application model from modeler and deploy to app
```shell script
shell:> export-bar --name app --output-file-name target/test/app.bar
shell:> unzip target/test/app.bar target/test/app
shell:> zip target/test/app target/test/app-out.bar
shell:> deploy target/test/app-out.bar
```