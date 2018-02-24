# Basic Makefile just to make creating containers a bit easier outside of
# Jenkins itself (see also: Jenkinsfile)
IMAGE_NAME=jenkinsciinfra/plugin-site
ARTIFACT=plugin-site-api-1.0-SNAPSHOT.war
DATA_FILE=plugins.json.gzip


### Phony targets
#################
all: check container

container: plugindata target/$(ARTIFACT) deploy/plugin-site
	docker build -t $(IMAGE_NAME) deploy

plugindata: target/$(DATA_FILE)
	cp target/$(DATA_FILE) .

check: pom.xml
	@echo ">>> Make sure you run \`make run\` in another terminal first!"
	DATA_FILE_URL=http://127.0.0.1:8080/$(DATA_FILE) mvn -B clean verify

run: plugindata
	docker-compose up

clean:
	docker-compose down || true
	mvn -B clean
	rm -f $(DATA_FILE)
	rm -rf target deploy/plugin-site
	docker rmi $$(docker images -q -f "reference=$(IMAGE_NAME)") || true

#################


### Generate files
###################
target/plugins.json.gzip: pom.xml
	mvn -PgeneratePluginData

target/$(ARTIFACT): plugindata check
	mvn -B -Dmaven.test.skip=true package

deploy/plugin-site:
	(cd deploy && git clone https://github.com/jenkins-infra/plugin-site.git)
###################

.PHONY: all container plugindata check clean run
