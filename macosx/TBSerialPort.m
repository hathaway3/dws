/*--------------------------------------------------------------------------------------------------
//
//   File Name   :   TBSerialPort.m
//
//   Description :   Serial port.
//
//--------------------------------------------------------------------------------------------------
//
//  Copyright (c) 2007 Tee-Boy
//
//  This source code and specific concepts contained herein are Confidential
//  Information and Property of Tee-Boy.
//  Distribution is prohibited without written permission of Tee-Boy.
//
//--------------------------------------------------------------------------------------------------
//
//  Tee-Boy                                http://www.tee-boy.com/
//  441 Saint Paul Avenue
//  Opelousas, LA  70570                   info@tee-boy.com
//
//--------------------------------------------------------------------------------------------------
//  $Id$
//------------------------------------------------------------------------------------------------*/

#import "TBSerialPort.h"


@implementation TBSerialPort

#pragma mark Init Methods

- (id)init:(NSString *)deviceName :(NSString *)serviceName
{
    // Initialize our super.
    if ((self = [super init]))
    {
        fd = -1;		
		fOwner = nil;
		fDeviceName = [deviceName retain];
		fServiceName = [serviceName retain];
    }

    // Return ourself.    
    return(self);
}



- (void)dealloc
{
    allowedToRun = FALSE;
    
    // Wait for the lock.    
    [serialLock lock];
	
	// Release retained varaibles
	[fDeviceName release];
	[fServiceName release];
	
	// Call our super's dealloc
    [super dealloc];
	
    return;
}



- (id)delegate;
{
   return _delegate;
}

- (void)setDelegate:(id)delegate;
{
   _delegate = delegate;
}

#pragma mark Port Acquisition Methods


- (BOOL)openPort:(id)owner
{
    int status = -1;


	// If the port is already opened, return NO
	
	if (fd != -1)
	{
		return NO;
	}
	
	
    // Acquire the path to the device.  If error, return an error.
    
    status = open([fDeviceName cString], O_RDWR | O_NOCTTY | O_NDELAY);

    if (status != -1) 
    {
		fd = status;

		fOwner = owner;
		
        // Set blocking I/O, no signal on data ready...

        if ((status = fcntl(fd, F_SETFL, 0)) != -1) 
        {
            // Get the current options and save them for later reset.

            if ((status = tcgetattr(fd, &sOriginalTTYAttrs)) != -1) 
            {
                // Set raw input, one second timeout.
                // These options are documented in the man page for termios.
                
                sTTYAttrs = sOriginalTTYAttrs;
                cfmakeraw(&sTTYAttrs);
                sTTYAttrs.c_cflag |= CS8 | CLOCAL | CREAD;
                sTTYAttrs.c_lflag = IGNBRK | IGNPAR;
                sTTYAttrs.c_cc[VMIN] = 1;		// 1 chars to wait for
                sTTYAttrs.c_cc[VTIME] = 0;
				[self setBaudRate:9600];

        
                // Set the options.
                
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);

				[self setWordSize:8];
				[self setParity:parityNone];
				[self setStopBits:1];
				
				[self setMinimumReadBytes:0];
				[self setReadTimeout:1];
				
				
				// Spin off listener thread.
				
				allowedToRun = TRUE;
				
				serialLock = [[NSLock alloc] init];
				
				[NSThread detachNewThreadSelector:@selector(listener:) toTarget:self withObject:nil];
				
            }
        }
    }


	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (BOOL)closePort
{
	if (allowedToRun == true)
	{
		// Flag listener thread to quit
		allowedToRun = false;
		
		// Wait for thread to unlock
		while ([serialLock tryLock] == NO);

		if (fd != -1)
		{
			close(fd);
        
			fd = -1;

			fOwner = nil;
		}

		return YES;
	}
    
    
    return NO;
}


#pragma mark Data Acquisition Methods

// This method should run on its own thread.  It listens to any data coming in from the port,
// then packages that data and sends it to our delegate.
- (void)listener:(id)anObject
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    const int readBufferSize = 300;
    char readBuffer[readBufferSize];
    NSData *serialData;
	
#ifdef DEBUG
	NSLog(@"Entering Thread...\n");
#endif
	
    // 1. Lock access to this thread.
    
    [serialLock lock];
	

	// 2. Do processing so long as we are allowed to run.
	
    while (allowedToRun == TRUE)
    {
        int maxsize = 0;
		
        
        // 1. Read up to 'maxsize' bytes from the port.
		
		maxsize = read(fd, readBuffer, readBufferSize);

        if (maxsize > 0)
        {
            // 1. Package serial data into an NSData object
			
            serialData = [NSData dataWithBytesNoCopy:readBuffer length:maxsize freeWhenDone:NO];
			
            // 2. If logging is turned on, log all bytes
            if (logIncomingBytes == true)
            {
               NSLog(@"Incoming bytes: %@", [serialData description]);
            }
           
            // 2. Pass the data to the delegate
           [_delegate performSelectorOnMainThread:@selector(availableData:) withObject:serialData waitUntilDone:YES];
        }
    }
	
    [pool release];
    
	
    // 3. Unlock the lock.
    
#ifdef DEBUG
	NSLog(@"Exiting Thread...\n");
#endif
	
    [serialLock unlock];
}



#if 0
- (void)serialPortReadData:(NSDictionary *)dataDictionary
{
#if 0
   NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

	[nc postNotificationName:fServiceName object:self userInfo:dataDictionary];
#else
   [_delegate availableData:dataDictionary];
#endif
}
#endif


- (BOOL)readData :(char *)data :(int)length;
{
    int status = -1;
	
    
    if (fd != -1)
    {
		status = read(fd, data, length);
    }
    
    
	if (status == -1)
	{		
		return NO;
	}
	else
	{		
		return YES;
	}
}



- (NSData *)readAvailableData
{
    int status = -1, length = [self bytesReady];
	NSData *incoming;
	void *buffer = (void *)malloc(length);
	
    
    if (fd != -1)
    {
		status = read(fd, buffer, length);
    }
    
    
	if (status == -1)
	{
		free(buffer);
		
		return nil;
	}
	else
	{
		incoming = [NSData dataWithBytes:buffer length:status];
		
		return incoming;
	}
}



- (BOOL)writeData :(NSData *)data
{
    int status = -1;
    
    
    if (fd != -1)
    {
        status = write(fd, [data bytes], [data length]);
    }
    
   if (logOutgoingBytes == true)
   {
      NSLog(@"Outgoing bytes: %@", [data description]);
   }
    
	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (BOOL)writeString :(NSString *)data
{
    int status = -1;
    
    
    if (fd != -1)
    {
        status = write(fd, [data cString], [data length]);
    }
    
    
	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}


#pragma mark Data Query Methods

- (Boolean)inputLogging;
{
   return logIncomingBytes;
}

- (Boolean)outputLogging;
{
   return logOutgoingBytes;
}

- (int)bytesReady
{
    int	ready = -1;
    
    
    if (fd != -1)
    {
        ioctl(fd, FIONREAD, &ready);
    }
    
    
    return ready;
}



- (BOOL)isAcquired
{
    if (fd == -1)
    {
        return NO;
    }
    
    
    return YES;
}



- (NSString *)deviceName
{
    return fDeviceName;
}



- (NSString *)serviceName
{
    return fServiceName;
}



- (id)owner
{
    return fOwner;
}



#pragma mark Port Control Methods

- (BOOL)setBaudRate:(int)baudRate
{
    int status = -1;
    
    
    if (fd != -1)
    {
        if ((status = cfsetspeed(&sTTYAttrs, baudRate)) != -1)
        {
            status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
		}
    }
    

	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (int)baudRate
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        status = cfgetispeed(&sTTYAttrs);
    }
    

    return status;
}



- (BOOL)setWordSize:(int)wordSize;
{
    int		status = -1;

	
    if (fd != -1)
    {
        switch (wordSize)
        {
            case 8:
                sTTYAttrs.c_cflag &= ~CSIZE;
                sTTYAttrs.c_cflag |= CS8;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;

            case 7:
                sTTYAttrs.c_cflag &= ~CSIZE;
                sTTYAttrs.c_cflag |= CS7;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;
                
            case 6:
                sTTYAttrs.c_cflag &= ~CSIZE;
                sTTYAttrs.c_cflag |= CS6;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;

            case 5:
                sTTYAttrs.c_cflag &= ~CSIZE;
                sTTYAttrs.c_cflag |= CS5;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;
        }
    }
    
	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (int)wordSize;
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        switch (sTTYAttrs.c_cflag & CSIZE)
        {
            case CS8:
                status = 8;
                break;

            case CS7:
                status = 7;
                break;
                
            case CS6:
                status = 6;
                break;

            case CS5:
                status = 5;
                break;
        }
    }
    

    return status;
}

- (void)setInputLogging:(Boolean)value;
{
   logIncomingBytes = value;
}

- (void)setOutputLogging:(Boolean)value;
{
   logOutgoingBytes = value;
}


- (BOOL)setParity:(serialParity)parity
{
    int	status = -1;


    if (fd != -1)
    {
        switch (parity)
        {
            case parityNone:
                sTTYAttrs.c_cflag &= ~(PARENB | PARODD);
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;

            case parityOdd:
                sTTYAttrs.c_cflag |= (PARENB | PARODD);
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;

            case parityEven:
                sTTYAttrs.c_cflag |= PARENB;
                sTTYAttrs.c_cflag &= ~PARODD;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;
        }
    }

    
	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (serialParity)parity
{
    serialParity	parity = parityNone;
    
    
    if (fd != -1)
    {
        if ((sTTYAttrs.c_cflag & PARENB) != 0)
        {
            if ((sTTYAttrs.c_cflag & PARODD) != 0)
            {
                parity = parityOdd;
            }
            else
            {
                parity = parityEven;
            }
        }
    }
        

    return parity;
}



- (BOOL)setStopBits:(int)stopBits;
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        switch (stopBits)
        {
            case 1:
                sTTYAttrs.c_cflag &= ~CSTOPB;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;

            case 2:
                sTTYAttrs.c_cflag |= CSTOPB;
                status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
                break;
        }
    }
    

	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (int)stopBits;
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        status = 1;	// assume 1 stop bit.
        
        if ((sTTYAttrs.c_cflag & CSTOPB) != 0)
        {
            status = 2;
        }
    }
    

    return status;
}



// Sets the read timeout in milliseconds.
// Returns 0 (success) OR -1 (failure).

- (BOOL)setReadTimeout:(int)timeout
{
    int	status = -1;


    if (fd != -1)
    {
        sTTYAttrs.c_cc[VTIME] = timeout;

        status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
    }

	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (int)readTimeout
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        status = sTTYAttrs.c_cc[VTIME];
    }
    

    return status;
}



- (BOOL)setMinimumReadBytes:(int)number
{
    int	status = -1;


    if (fd != -1)
    {
        sTTYAttrs.c_cc[VMIN] = number;

        status = tcsetattr(fd, TCSANOW, &sTTYAttrs);
    }

	if (status == -1)
	{
		return NO;
	}
	else
	{
		return YES;
	}
}



- (int)minimumReadBytes
{
    int	status = -1;
    
    
    if (fd != -1)
    {
        status = sTTYAttrs.c_cc[VMIN];
    }
    

    return status;
}


@end
