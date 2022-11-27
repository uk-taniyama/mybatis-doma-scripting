.PHONY: help ## (default)
help:
	@grep " #""#" Makefile | sed -e 's/.PHONY: *//' | sed -e 's/ *## */\t/'

.PHONY: test ##
test:
	sh gradlew test

.PHONY: TEST ## テストを再実行する。
TEST:
	sh gradlew --rerun-tasks test

.PHONY: clean ##
clean:
	rm -rf build

.PHONY: build ##
build:
	sh gradlew jar

.PHONY: rebuild ##
rebuild: clean build

.PHONY: lint ##
lint:
	sh gradlew spotlessApply

get-doma-template:
	git clone --depth 1 --branch 2.53.1 https://github.com/domaframework/doma
	cp -r doma/doma-template/src/main/ src/
	cp -r doma/doma-template/src/test/ src/
