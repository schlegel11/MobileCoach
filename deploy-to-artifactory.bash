if [ -z "$buildNumber" ]; then
    buildNumber=`date +%s`
fi
mvn install
mvn deploy -Dusername=${ARTIFACTORY_USERNAME} -Dpassword=${ARTIFACTORY_PASSWORD} -Durl=${ARTIFACTORY_CONTEXT_URL}

