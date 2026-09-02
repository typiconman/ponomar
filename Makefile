.PHONY: default build test clean

default: build

build:
	./mvnw -DskipTests package

test:
	./mvnw test

clean:
	./mvnw clean
