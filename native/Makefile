# Makefile builds libHapticFeedback.dylib for the Paint App
#
# Usage (from the native/ directory):
#   make          builds the dylib
#   make install  builds + copies the dylib next to Paint App.jar
#   make clean    removes build artefacts
#
# Requires:
#   - macOS with Xcode Command-Line Tools  (xcode-select --install)
#   - A JDK with $JAVA_HOME set  (e.g. export JAVA_HOME=$(/usr/libexec/java_home))

JAVA_HOME ?= $(shell /usr/libexec/java_home 2>/dev/null)

JNI_INCLUDES = -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/darwin"

CC      = clang
CFLAGS  = -dynamiclib -framework AppKit -ObjC $(JNI_INCLUDES)
SRC     = HapticFeedback.m
OUT     = libHapticFeedback.dylib

# Destination: the directory that contains Paint App.jar (one level up from native/)
JAR_DIR = ..

all: $(OUT)

$(OUT): $(SRC)
	$(CC) $(CFLAGS) -o $(OUT) $(SRC)
	@echo "Built $(OUT)"

# Copy next to the jar so -Djava.library.path=. finds it at runtime
install: $(OUT)
	cp $(OUT) $(JAR_DIR)/$(OUT)
	@echo "Installed $(OUT) -> $(JAR_DIR)/$(OUT)"

clean:
	rm -f $(OUT)
