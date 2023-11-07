# Developer - How to Start Guide


## PowerAuth Data Adapter


### Standalone Run

- Use IntelliJ Idea run configuration at `../.run/DataAdapterApplication.run.xml`
- Open [http://localhost:9090/powerauth-data-adapter/actuator/health](http://localhost:9090/powerauth-data-adapter/actuator/health) and you should get `{"status":"UP"}`


### Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-data-adapter/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth --hub-mode=off status
```
