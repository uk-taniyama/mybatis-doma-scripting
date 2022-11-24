lint:
	sh gradlew spotlessApply

get-doma-template:
	git clone --depth 1 --branch 2.53.1 https://github.com/domaframework/doma
	cp -r doma/doma-template/src/main/ src/
	cp -r doma/doma-template/src/test/ src/
