# Wildlife Explorer — build without Maven/Gradle (needs JDK on PATH)
JAVA_SRC := src/main/java
RES := src/main/resources
OUT := build/classes

.PHONY: all build run clean help

help:
	@echo "Targets:"
	@echo "  make build   — compile all Java sources into $(OUT)"
	@echo "  make run     — build then start the Swing app"
	@echo "  make clean   — remove $(OUT)"

all: build

build:
	@mkdir -p $(OUT)
	@find $(JAVA_SRC) -name '*.java' > $(OUT)/sources.txt
	javac -d $(OUT) @$(OUT)/sources.txt
	@mkdir -p $(OUT)/wildlifeexplorer/bundled
	@cp -f $(RES)/wildlifeexplorer/bundled/trails.json $(RES)/wildlifeexplorer/bundled/wildlife.json $(OUT)/wildlifeexplorer/bundled/

run: build
	java -cp $(OUT) wildlifeexplorer.Main

clean:
	rm -rf build
