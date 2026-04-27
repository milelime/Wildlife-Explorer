# Wildlife Explorer — build without Maven/Gradle (needs JDK on PATH)
JAVA_SRC := src/main/java
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

run: build
	java -cp $(OUT) wildlifeexplorer.Main

clean:
	rm -rf build
