vpath %.c ../source

CFLAGS += -g -DLINUX
LDFLAGS	+= -lcurses -lpthread -g

drivewire: drivewire.o

clean:
	rm drivewire drivewire.o

install:
	cp drivewire ~/bin
