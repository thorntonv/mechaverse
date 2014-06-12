SHARED_LIBS = $(shell find target/ -type f -name 'libmechaverse-simulation.so')

.PHONY: all
.PHONY: clean
.PHONY: install
.PHONY: libinstall

all:
	mvn install
	
clean:
	mvn clean
	
libinstall:
	for f in $(SHARED_LIBS); do sudo libtool --mode=install cp $$f /usr/lib/; done

install: all libinstall
