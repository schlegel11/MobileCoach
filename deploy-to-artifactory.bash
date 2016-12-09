if [ -z "$buildNumber" ]; then
    buildNumber=`date +%s`
fi
mvn install -DskipTests=true
mvn test
mvn deploy -Dusername=${ARTIFACTORY_USERNAME} -Dpassword=${ARTIFACTORY_PASSWORD} -Durl=${ARTIFACTORY_CONTEXT_URL}

