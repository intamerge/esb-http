# Prod build

PROJECT=esb-http

if [ ! -f ./setenv.sh ]; then
    echo "setenv.sh File not found! resorting to system Java"
else
    . ./setenv.sh
fi

echo ------------------------------------------------------------------------
echo Set the version from version.property
echo ------------------------------------------------------------------------

ant -Dgit_work_tree=/home/mwicks/Dev/git/intamerge-github/esb-http -Dproject_loc=/home/mwicks/Dev/git/intamerge-github/esb-http version

echo ------------------------------------------------------------------------
echo Set the license
echo ------------------------------------------------------------------------
ant -Dgit_work_tree=/home/mwicks/Dev/git/intamerge-github/esb-http -Dproject_loc=/home/mwicks/Dev/git/intamerge-github/esb-http headers

mvn license:format

echo ------------------------------------------------------------------------
echo Build
echo ------------------------------------------------------------------------

# following helps the esb-parent script
rm -fr target/${PROJECT}*

mvn -Dmaven.wagon.http.ssl.insecure=true clean install -Dmaven.test.skip


rc=$?
if [[ $rc -ne 0 ]] ; then
  echo 'exiting with mvn errors'; exit $rc
fi


