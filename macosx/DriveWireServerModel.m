//
//  DriveWireServerModel.m
//  DriveWire
//
//  Created by Boisy Pitre on 12/8/04.
//  Copyright 2008 BGP. All rights reserved.
//

#import "DriveWireServerModel.h"


@implementation DriveWireServerModel

#define MAX_TIME_BEFORE_RESET 0.5

static TBSerialManager *fSerialManager = nil;


- (void)setDelegate:(id)delegate;
{
   _delegate = delegate;
}

- (id)delegate;
{
   return _delegate;
}

- (id)init
{
	return [self initWithVersion:DW_DEFAULT_VERSION];
}


- (id)initWithVersion:(int)versionNumber
{
	if ((self = [super init]))
	{
		int32_t i;
		
#ifdef DEBUG
		NSLog(@"DriveWireServerModel initWithVersion:%d", versionNumber);
#endif
		
		// Allocate our array of drives	
		driveArray = [[NSMutableArray alloc] init];

#ifdef DEBUG
		NSLog(@"About to allocate drives");
#endif
		
		for (i = 0; i < DRIVE_COUNT; i++)
		{
			TBVirtualDriveController *drive;
		
			drive = [[TBVirtualDriveController alloc] init];
			[driveArray insertObject:drive atIndex:i];
		}

#ifdef DEBUG
		NSLog(@"Drives allocated");
#endif
		
		version = versionNumber;
		
		// set defaults
		[self setStatState:false];
		[self setLogState:false];
		[self setWirebugState:false];
		[self setMachineType:3];
		[self setMemAddress:0];

#ifdef DEBUG
		NSLog(@"Defaults set");
#endif
		
		fCurrentPort = nil;

		// Call the common init routine to do common initializaiton		
		[self initDesignated];

#ifdef DEBUG
		NSLog(@"Designated initialier called");
#endif		
	}	
	
	return self;
}

- (void)setupWatchdog;
{
   [self invalidateWatchdog];
   watchDog = [NSTimer scheduledTimerWithTimeInterval:MAX_TIME_BEFORE_RESET target:self selector:@selector(resetState:) userInfo:nil repeats:NO];
}

- (void)invalidateWatchdog;
{
   [watchDog invalidate];
   watchDog = nil;
}

- (void)initDesignated
{
	int32_t		i;
	NSArray		*keys = [NSArray arrayWithObjects:
		@"OpCode",
		@"DriveNumber",
		@"LSN",
		@"ReadCount",
		@"WriteCount",
		@"ReReadCount",
		@"ReWriteCount",
		@"GetStat",
		@"SetStat",
		@"Checksum",
		@"Error",
		nil];
	NSArray		*objects = [NSArray arrayWithObjects:
		@"NONE",
		@"0",
		@"0",
		@"0",
		@"0",
		@"0",
		@"0",
		@"NONE",
		@"NONE",
		@"0",
		@"0",
		nil];
	NSArray		*registerKeys = [NSArray arrayWithObjects:
		@"CC",
		@"DP",
		@"A",
		@"B",
		@"E",
		@"F",
		@"X",
		@"Y",
		@"U",
		@"S",
		@"PC",
		nil];
	NSArray		*registerObjects = [NSArray arrayWithObjects:
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		[NSNumber numberWithInt:0],
		nil];
				
	// Set drive numbers.
	for (i = 0; i < DRIVE_COUNT; i++)
	{
		[[driveArray objectAtIndex:i] setDriveID:i];
	}

	// Allocate stats manager.        
	statistics = [[NSMutableDictionary alloc] initWithObjects:objects forKeys:keys];

	// Allocate registers dictionary.        
	registers = [[NSMutableDictionary alloc] initWithObjects:registerObjects
													  forKeys:registerKeys];
	
	portDelegate = nil;

	if (version > 1)
	{
		validateWithCRC = NO;		/* We will do checksums on the data. */
	}
	else
	{
		validateWithCRC = YES;		/* We will do CRCs on the data. */	
	}

	if (fSerialManager == nil)
	{
		fSerialManager = [[TBSerialManager alloc] init];		
	}
		
    // Set up the devices
	fSerialPortNames = [[TBSerialManager availablePorts] retain];
   
   // Set up state variables
	currentState = @"OP_OPCODE";
   startOfData = inputBuffer;
   currentLocation = 0;
   
   // Setup printer buffer
   printBuffer = [[NSMutableData alloc] init];
   
	return;
}



- (void)dealloc
{
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
	
#ifdef DEBUG
	NSLog(@"DriveWireServerModel dealloc");
#endif
	
	// Remove ourself as observer from any notifications
	[nc removeObserver:self];

   // Release printer data
   [printBuffer release];
   
	// Release statistics & registers
	[statistics release];
   [registers release];
	
	[driveArray release];	
	[fSerialManager releasePort:fCurrentPort];
   [fSerialPortNames release];
   [super dealloc];
	
    return;
}



// This method communicates with the serial manager to reserve a specific
// communications port.  It returns YES if the port was successfully
// reserved, or NO if it wasn't.
- (Boolean)setCommPort:(NSString *)selectedPort;
{
	TBSerialPort *newPort;
	
	// If we're asked to set the same serial port we have set, return YES
	if ([selectedPort compare:fCurrentPort] == NSOrderedSame)
	{
		return YES;
	}
	
	// If we are passed "No Device", release the current port and return YES
	if ([selectedPort compare:@"No Device"] == NSOrderedSame)
	{
		[fSerialManager releasePort:fCurrentPort];
		fCurrentPort = nil;
		
		return YES;
	}
	
	// If the port passed is not available, return NO
	if ([fSerialManager isPortAvailable:selectedPort] == NO)
	{
		return NO;
	}
	
	// Attempt to reserve the port passed.
	newPort = [fSerialManager reservePort:selectedPort forOwner:self];

	if (newPort == nil)
	{
		return NO;
	}
	
	// At this point, we've reserved the requested port.
	// Release our fCurrentPort and point it to the passed port
	[fSerialManager releasePort:fCurrentPort];
	fCurrentPort = selectedPort;
	fPort = newPort;
   [self setMachineType:machineType];  // force the setting of the baud rate
#ifdef DEBUG
   [fPort setInputLogging:true];
   [fPort setOutputLogging:true];
#endif

   [fPort setDelegate:self];
	[self setPortDelegate:fPort];

	return YES;
}

- (id)portDelegate
{
	return portDelegate;
}

- (void)setPortDelegate:(id)handler
{
	portDelegate = handler;
#ifdef DEBUG
	NSLog(@"Now listening for data from device %@\n", [handler serviceName]);
#endif
}

- (NSMutableArray *)driveArray
{
	return driveArray;
}

- (void)setMachineType:(int)type
{
	machineType = type;
	
   switch (type)
   {
      case 1:
         [fPort setBaudRate:38400];
         break;
      case 2:
         [fPort setBaudRate:57600];
         break;
      case 3:
      default:
         [fPort setBaudRate:115200];
         break;
   }
}

- (int)machineType
{
	return machineType;
}

- (NSString *)serialPort
{
	return fCurrentPort;
}

- (void)setStatState:(Boolean)state
{
	statState = state;
}

- (Boolean)statState
{
	return statState;
}

- (void)setLogState:(Boolean)state
{
	logState = state;
}

- (void)setWirebugState:(Boolean)state
{
	wirebugState = state;
}

- (Boolean)logState
{
	return logState;
}

- (Boolean)wirebugState
{
	return wirebugState;
}

- (void)setMemAddress:(u_int16_t)newAddress
{
	memAddress = newAddress;
}

#pragma mark Calculation Routines

- (uint16_t)compute16BitChecksum :(const u_char *)data :(int)length
{
	uint16_t lastChecksum = 0x0000;
	uint8_t *ptr;
	
	ptr = (uint8_t *)data;
	while (length--)
	{
		lastChecksum += *(ptr++);
	}
	
	return lastChecksum;
}

- (uint8_t)compute8BitChecksum :(const u_char *)data :(int)length
{
	uint8_t lastChecksum = 0x00;
	uint8_t *ptr;
	
	ptr = (uint8_t *)data;
	while (length--)
	{
		lastChecksum += *(ptr++);
	}
	
	return lastChecksum;
}

- (uint16_t)computeCRC :(const u_char *)data :(int)length
{
	uint16_t i, crc = 0;
	uint16_t *ptr = (uint16_t *)data;
	
	while (--length >= 0)
	{
		crc = crc ^ *ptr++ << 8;
		
		for (i = 0; i < 8; i++)
		{
			if (crc & 0x8000)
			{
				crc = crc << 1 ^ 0x1021;
			}
			else
			{
				crc = crc << 1;
			}
		}
	}
	
	return (crc & 0xFFFF);
}

- (void)availableData:(NSData *)serialData;
{
    // Update globals with byte pointer and length.    
    dataBytes = [serialData bytes];
    dataLength = [serialData length];	
	
   // Call appropriate handler.
    while (dataLength > 0)
    {
       [self valueForKey:currentState];
    }
	
    return;
}

#pragma mark OpCode Processing Routines

- (void)OP_OPCODE
{
	u_char op = *(dataBytes++);

    // Decrement length by one byte.
    dataLength--;
    
   [self setupWatchdog];

   // Determine next action to take.
    switch (op)
    {
        case _OP_NOP:
          [self OP_NOP];
			break;
            
        case _OP_TIME:
          [self OP_TIME];
			break;
            
       case _OP_PRINTFLUSH:
          [self OP_PRINTFLUSH];
          break;
          
       case _OP_VPORT_READ:
          currentState = @"OP_VPORT_READ";
          break;
          
		case _OP_VPORT_WRITE:
			currentState = @"OP_VPORT_WRITE";
			break;
			
		case _OP_INIT:
          [self OP_INIT];
          break;
            
        case _OP_TERM:
          [self OP_TERM];
          break;
            
        case _OP_READ:
          currentState = @"OP_READ";
          break;
            
        case _OP_READEX:
          currentState = @"OP_READEX";
          break;
            
        case _OP_REREAD:
          currentState = @"OP_REREAD";
          break;
			
        case _OP_REREADEX:
          currentState = @"OP_REREADEX";
          break;
			
        case _OP_WRITE:
          currentState = @"OP_WRITE";
          break;
            
        case _OP_REWRITE:
          currentState = @"OP_REWRITE";
          break;
			
        case _OP_GETSTAT:
          currentState = @"OP_GETSTAT";
          break;
			
        case _OP_SETSTAT:
          currentState = @"OP_SETSTAT";
          break;
			
        case _OP_RESET1:
        case _OP_RESET2:
        case _OP_RESET3:
          [self OP_RESET];
          break;
			

// WireBug Section
		case _OP_WIREBUG_MODE:
          currentState = @"OP_WIREBUG_MODE";
          break;

        case _OP_RESYNC:
        default:
          // Resync in case of bad data transfer
          [self OP_RESYNC];
          break;
    }
}

- (void)OP_NOP;
{
   [self invalidateWatchdog];

	[statistics setObject:@"OP_NOP" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];
	
	currentState = @"OP_OPCODE";
}

- (void)OP_PRINTFLUSH;
{
   [self invalidateWatchdog];
   
	[statistics setObject:@"OP_PRINTFLUSH" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];
   [self flushPrinterBuffer];
   
	currentState = @"OP_OPCODE";
}

- (void)flushPrinterBuffer;
{
   NSDictionary *d = [NSDictionary dictionaryWithObject:[printBuffer copy] forKey:@"PrintData"];
   [printBuffer setLength:0];
   [_delegate updatePrinterView:d];
}

#define  MAX_SIZE_BEFORE_PRINTING   256

- (void)OP_PRINT;
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];
   
   unsigned char byte = dataBytes[0];
   [printBuffer appendBytes:&byte length:1];
	[statistics setObject:@"OP_PRINT" forKey:@"OpCode"];
   [statistics setObject:[NSString stringWithFormat:@"%d", byte] forKey:@"Byte"];
   [_delegate updateInfoView:statistics];

   
   if ([printBuffer length] > MAX_SIZE_BEFORE_PRINTING)
   {
      [self flushPrinterBuffer];
   }
	
   dataLength--;
   dataBytes++;
	currentState = @"OP_OPCODE";
}

- (void)OP_INIT
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];
   
	[statistics setObject:@"OP_INIT" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];

	currentState = @"OP_OPCODE";
}

- (void)OP_TERM
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];
      
	[statistics setObject:@"OP_TERM" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];
		
	currentState = @"OP_OPCODE";
}

- (void)OP_RESET
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];
   
	// Reset all statistics
	[statistics setObject:@"OP_RESET" forKey:@"OpCode"];
	[statistics setObject:@"0" forKey:@"LSN"];
	[statistics setObject:@"0" forKey:@"ReadCount"];
	[statistics setObject:@"0" forKey:@"WriteCount"];
	[statistics setObject:@"0" forKey:@"ReReadCount"];
	[statistics setObject:@"0" forKey:@"ReWriteCount"];
	[statistics setObject:@"NONE" forKey:@"GetStat"];
	[statistics setObject:@"NONE" forKey:@"SetStat"];
	[statistics setObject:@"0" forKey:@"Error"];
	[statistics setObject:@"00000" forKey:@"Checksum"];
   [_delegate updateInfoView:statistics];

	currentState = @"OP_OPCODE";
   currentLocation = 0;

   // Set WireBug state to FALSE
   [self setWirebugState:false];
}

- (void)OP_RESYNC
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];

	[statistics setObject:@"OP_RESYNC" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];
	
	currentState = @"OP_OPCODE";
}

- (void)OP_TIME
{
   // Invalidate watchdog timer
   [self invalidateWatchdog];
   
	time_t currentClock;
	struct tm *tpack;
	char os9tpack[7];

	time(&currentClock);
	tpack = localtime(&currentClock);
	os9tpack[0] = tpack->tm_year;
	os9tpack[1] = tpack->tm_mon + 1;
	os9tpack[2] = tpack->tm_mday;
	os9tpack[3] = tpack->tm_hour;
	os9tpack[4] = tpack->tm_min;
	os9tpack[5] = tpack->tm_sec;
	os9tpack[6] = tpack->tm_wday;
	
	// Send time packet to CoCo
	[portDelegate writeData:[NSData dataWithBytes:os9tpack length:7]];

	// Update log
	[statistics setObject:@"OP_TIME" forKey:@"OpCode"];
   [_delegate updateInfoView:statistics];
	
	currentState = @"OP_OPCODE";
}

- (void)OP_REREAD
{
	[self OP_READ];
}

- (void)OP_REREADEX
{
	[self OP_READEX];
}

- (void)resetState:(NSTimer*)theTimer
{
#ifdef DEBUG
   NSLog(@"Watchdog triggered... resetting state");
#endif
   currentLocation = 0;
   startOfData = inputBuffer;
   currentState = @"OP_OPCODE";
   watchDog = nil;
}

- (void)OP_VPORT_READ
{
	// We read 2 bytes (1 byte port number, 1 byte request count)
	// Check for special case where entire packet is ready. */
	if (currentLocation == 0 && dataLength == 2)
	{
		currentLocation = dataLength;
		dataLength = 0;
		startOfData = &dataBytes[0];
	}
	else
	{
		int32_t remaining;
		
		// Compute remaining free space in our local buffer.
		remaining = 2 - currentLocation;
		
		if (remaining > dataLength)
		{
			remaining = dataLength;
		}
		
		// Copy the maximum available amount to our local buffer
		// and adjust data pointer and length accordingly.		
		memcpy(inputBuffer + currentLocation, dataBytes, remaining);
		dataLength -= remaining;		
		dataBytes += remaining;		
		currentLocation += remaining;
	}    
    
	// Check if we have reached our terminal count.
	if (currentLocation == 2)
	{
		uint32_t portNumber, readCount;
		char b[256];
		
		// Invalidate watchdog timer
		[self invalidateWatchdog];
		
		// Reset the current location now that we've achieved it.
		currentLocation = 0;
		
		// Extract virtual port number and read count from data packet.
		portNumber = startOfData[0];
		readCount = startOfData[1];
		
		// Send response to the CoCo.
		int i;
		for (i = 0; i < 256; i++)
			b[i] = 'A';
		[portDelegate writeData:[NSData dataWithBytes:b length:readCount]];

		[statistics setObject:@"OP_VPORT_READ" forKey:@"OpCode"];
		[statistics setObject:[NSNumber numberWithInt:portNumber] forKey:@"VPort"];
		[statistics setObject:[NSNumber numberWithInt:readCount] forKey:@"ReadCount"];
		[_delegate updateInfoView:statistics];
		
		// Reset state
		currentState = @"OP_OPCODE";
		startOfData = inputBuffer;
	}
	
	return;
}

- (void)OP_VPORT_WRITE
{
	// We read 2 bytes (1 byte port number, 1 data byte)
	// Check for special case where entire packet is ready. */
	if (currentLocation == 0 && dataLength == 2)
	{
		currentLocation = dataLength;
		dataLength = 0;
		startOfData = &dataBytes[0];
	}
	else
	{
		int32_t remaining;
		
		// Compute remaining free space in our local buffer.
		remaining = 2 - currentLocation;
		
		if (remaining > dataLength)
		{
			remaining = dataLength;
		}
		
		// Copy the maximum available amount to our local buffer
		// and adjust data pointer and length accordingly.		
		memcpy(inputBuffer + currentLocation, dataBytes, remaining);
		dataLength -= remaining;		
		dataBytes += remaining;		
		currentLocation += remaining;
	}    
    
	// Check if we have reached our terminal count.
	if (currentLocation == 2)
	{
		uint32_t portNumber, dataByte;
		
		// Invalidate watchdog timer
		[self invalidateWatchdog];
		
		// Reset the current location now that we've achieved it.
		currentLocation = 0;
		
		// Extract virtual port number and read count from data packet.
		portNumber = startOfData[0];
		dataByte = startOfData[1];
		
		[statistics setObject:@"OP_VPORT_WRITE" forKey:@"OpCode"];
		[statistics setObject:[NSNumber numberWithInt:portNumber] forKey:@"VPort"];
		[statistics setObject:[NSNumber numberWithInt:dataByte] forKey:@"DataByte"];
		[_delegate updateInfoView:statistics];
		
		// Reset state
		currentState = @"OP_OPCODE";
		startOfData = inputBuffer;
	}
	
	return;
}

- (void)OP_READ
{
	// We read 4 bytes (1 byte drive number, 3 byte LSN)
	// Check for special case where entire packet is ready. */
	if (currentLocation == 0 && dataLength == 4)
	{
		currentLocation = dataLength;
		dataLength = 0;
		startOfData = &dataBytes[0];
	}
	else
	{
		int32_t remaining;
		
		// Compute remaining free space in our local buffer.
		remaining = 4 - currentLocation;
		
		if (remaining > dataLength)
		{
			remaining = dataLength;
		}
		
		// Copy the maximum available amount to our local buffer
		// and adjust data pointer and length accordingly.		
		memcpy(inputBuffer + currentLocation, dataBytes, remaining);
		dataLength -= remaining;		
		dataBytes += remaining;		
		currentLocation += remaining;
	}    
    
	// Check if we have reached our terminal count.
	if (currentLocation == 4)
	{
		uint32_t driveNumber;
		NSData *sectorBuffer = nil;
		unsigned int vLSN;
		uint16_t myChecksum;
		unsigned char b[2], response;
		
		// Invalidate watchdog timer
		[self invalidateWatchdog];
		
		// Reset the current location now that we've achieved it.
		currentLocation = 0;
		
		// Assume no error to CoCo for now.
		response = 0;
		
		// Extract drive number and LSN from data packet.
		driveNumber = startOfData[0];
		vLSN = (startOfData[1] << 16) | (startOfData[2] << 8) | startOfData[3];
		
		// Check drive number for veracity
		if (driveNumber >= DRIVE_COUNT)
		{
			response = 240;
		}
		else
		{
			// Read sector from disk image.
			sectorBuffer = [[driveArray objectAtIndex:driveNumber] readSectors:vLSN forCount:1];
			if (sectorBuffer == nil)
			{
				response = 246;		// E$NotRdy
			}
		}
		
		// Send the response code to the CoCo.
		[portDelegate writeData:[NSData dataWithBytes:&response length:1]];
		
		// If we have an OK response, we send the sector and Checksum.
		if (response == 0)
		{
			// Write sector to CoCo.
			if ([sectorBuffer bytes] != NULL)
			{
				// Send sector.
				[portDelegate writeData:sectorBuffer];
				
				// Compute Checksum from sector.
				if (validateWithCRC == NO)
				{
					myChecksum = [self compute16BitChecksum:[sectorBuffer bytes] :256];
				}
				else
				{
					myChecksum = [self computeCRC:[sectorBuffer bytes] :256];
				}
			}
			else
			{
				// If [sectorBuffer bytes] == NULL, then the DSK manager
				// read past the end of the file.  This is ok because
				// OS-9's view of the disk may be larger than the physical
				// file that holds the image.  We'll just send a fake
				// sector with zero bytes.
				
				u_char nullSector[256] =
				
				{
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
				};
                
				[portDelegate writeData:[NSData dataWithBytes:nullSector length:256]];
				
				// The Checksum will be zero.
				myChecksum = [self compute16BitChecksum:nullSector :256];
			}
            
			// Send statistical data via notification
			[statistics setObject:currentState forKey:@"OpCode"];
			[statistics setObject:[NSString stringWithFormat:@"%d", vLSN] forKey:@"LSN"];
			[statistics setObject:[NSString stringWithFormat:@"%d", driveNumber] forKey:@"DriveNumber"];
			
			// Kinda hackish -- we should get the "all" stats from the jukebox
			if ([currentState isEqual:@"OP_REREAD"] == YES)
			{
				int32_t sectorsReRead = [[statistics objectForKey:@"ReReadCount"] intValue] + 1;
				
				[statistics setObject:[NSString stringWithFormat:@"%d", sectorsReRead] forKey:@"ReReadCount"];
			}
			else
			{
				int32_t sectorsRead = [[statistics objectForKey:@"ReadCount"] intValue] + 1;
				
				[statistics setObject:[NSString stringWithFormat:@"%d", sectorsRead] forKey:@"ReadCount"];
			}
			
			[statistics setObject:[NSString stringWithFormat:@"%d", response] forKey:@"Error"];
			[statistics setObject:[NSString stringWithFormat:@"%d", myChecksum] forKey:@"Checksum"];
			[_delegate updateInfoView:statistics];
			
			// Send Checksum on to CoCo
			b[0] = myChecksum >> 8;
			b[1] = myChecksum & 0xFF;
			[portDelegate writeData:[NSData dataWithBytes:b length:2]];
		}
		
		// Reset state
		currentState = @"OP_OPCODE";
		startOfData = inputBuffer;
	}
	
	return;
}

- (void)OP_READEX
{
    // We read 4 bytes into this buffer (1 byte drive number, 3 byte LSN)
    // Check for special case where entire packet is ready. */
    if (currentLocation == 0 && dataLength == 4)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {
		int32_t remaining;

        // Compute remaining free space in our local buffer.
        remaining = 4 - currentLocation;
		
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.		
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);
        dataLength -= remaining;		
        dataBytes += remaining;		
        currentLocation += remaining;
    }    
    
    // Check if we have reached our terminal count.
    if (currentLocation == 4)
    {
        uint32_t driveNumber;
        NSData *sectorBuffer = nil;
        unsigned int vLSN;
		
       // Reset the current location now that we've achieved it.
        currentLocation = 0;
        
        // Assume no error to CoCo for now.
        readexResponse = 0;
		
        // Extract drive number and LSN from data packet.
        driveNumber = startOfData[0];
        vLSN = (startOfData[1] << 16) | (startOfData[2] << 8) | startOfData[3];
		
		// Check drive number for veracity
		if (driveNumber >= DRIVE_COUNT)
		{
			readexResponse = 240;
		}
		else
		{
			// Read sector from disk image.
			sectorBuffer = [[driveArray objectAtIndex:driveNumber] readSectors:vLSN forCount:1];
			if (sectorBuffer == nil)
			{
				readexResponse = 246;		// E$NotRdy
			}
		}

        {
            // Write sector to CoCo.
            if (readexResponse == 0 && [sectorBuffer bytes] != NULL)
            {
                // Send sector.
                [portDelegate writeData:sectorBuffer];
				
                // Compute Checksum from sector.
               if (validateWithCRC == NO)
               {
                  readexChecksum = [self compute16BitChecksum:[sectorBuffer bytes] :256];
               }
               else
               {
                  readexChecksum = [self computeCRC:[sectorBuffer bytes] :256];
               }
            }
            else
            {
#ifdef DEBUG
               NSLog(@"Writing zero bytes sector");
#endif
                // If [sectorBuffer bytes] == NULL, then the DSK manager
                // read past the end of the file.  This is ok because
                // OS-9's view of the disk may be larger than the physical
                // file that holds the image.  We'll just send a fake
                // sector with zero bytes.
                
                u_char nullSector[256] =
               {
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
               };
                   
                [portDelegate writeData:[NSData dataWithBytes:nullSector length:256]];
               
                // The Checksum will be zero.
               readexChecksum = [self compute16BitChecksum:nullSector :256];
            }
            
            // Send statistical data via notification
            [statistics setObject:currentState forKey:@"OpCode"];
            [statistics setObject:[NSString stringWithFormat:@"%d", vLSN] forKey:@"LSN"];
            [statistics setObject:[NSString stringWithFormat:@"%d", driveNumber] forKey:@"DriveNumber"];

            // Kinda hackish -- we should get the "all" stats from the jukebox
            if ([currentState isEqual:@"OP_REREADEX"] == YES)
            {
               int32_t sectorsReRead = [[statistics objectForKey:@"ReReadCount"] intValue] + 1;
               
               [statistics setObject:[NSString stringWithFormat:@"%d", sectorsReRead] forKey:@"ReReadCount"];
            }
            else
            {
               int32_t sectorsRead = [[statistics objectForKey:@"ReadCount"] intValue] + 1;
               
               [statistics setObject:[NSString stringWithFormat:@"%d", sectorsRead] forKey:@"ReadCount"];
            }

            [statistics setObject:[NSString stringWithFormat:@"%d", readexResponse] forKey:@"Error"];
            [statistics setObject:[NSString stringWithFormat:@"%d", readexChecksum] forKey:@"Checksum"];
           [_delegate updateInfoView:statistics];
        }

        currentState = @"OP_READEXP2";
        startOfData = inputBuffer;
        currentLocation = 0;
    }
	
    return;
}

- (void)OP_READEXP2
{
   // We read 2 bytes into this buffer (CoCo's checksum)
   // Here we're expecting the checksum from the CoCo
   // Check for special case where entire packet is ready. */
   if (currentLocation == 0 && dataLength == 2)
   {
      currentLocation = dataLength;
      dataLength = 0;
      startOfData = &dataBytes[0];
   }
   else
   {
      int32_t remaining;
      
      // Compute remaining free space in our local buffer.
      remaining = 2 - currentLocation;
      
      if (remaining > dataLength)
      {
         remaining = dataLength;
      }
      
      // Copy the maximum available amount to our local buffer
      // and adjust data pointer and length accordingly.		
      memcpy(inputBuffer + currentLocation, dataBytes, remaining);
      dataLength -= remaining;		
      dataBytes += remaining;		
      currentLocation += remaining;
   }    
   
   // Check if we have reached our terminal count.
   if (currentLocation == 2)
   {
      uint16_t cocoChecksum = 0;
      
      // Invalidate watchdog timer
      [self invalidateWatchdog];
      
      currentLocation = 0;
      
      cocoChecksum = startOfData[0] * 256 + startOfData[1];
      if (readexChecksum != cocoChecksum)
      {
         readexResponse = E_CRC;
      }
      
      // Send the response code to the CoCo.
      [portDelegate writeData:[NSData dataWithBytes:&readexResponse length:1]];
      
      currentState = @"OP_OPCODE";
      startOfData = inputBuffer;
      currentLocation = 0;
   }
   
   return;
}

- (void)OP_REWRITE
{
	[self OP_WRITE];
}

- (void)OP_WRITE
{
    // We read all 262 bytes into this buffer (1 byte drive number, 3 byte LSN, 256 byte sector, and 2 byte Checksum)
    // Check for special case where entire packet is ready. */
    if (currentLocation == 0 && dataLength == 262)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {		
		int32_t remaining;

        // Compute remaining free space in our local buffer.
        remaining = 262 - currentLocation;
		
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.		
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);		
        dataLength -= remaining;	
        dataBytes += remaining;
        currentLocation += remaining;
    }

   // Check if we have reached our terminal count.
    if (currentLocation == 262)
    {
       uint32_t vLSN;
       uint32_t driveNumber;
       uint16_t myChecksum = 0, cocoChecksum = 0;
       unsigned char response;
		
       // Invalidate watchdog timer
       [self invalidateWatchdog];
              
       // Reset the current location now that we've achieved it.
       currentLocation = 0;
        
       // Extract drive number and LSN from data packet.
       driveNumber = startOfData[0];
       vLSN = (startOfData[1] << 16) | (startOfData[2] << 8) | startOfData[3];
       cocoChecksum = startOfData[260] << 8 | startOfData[261];
		
       // Check drive number for veracity
       if (driveNumber >= DRIVE_COUNT)
       {
          response = 240;
       }
       else
       {
          // Compute Checksum from sector.
          if (validateWithCRC == NO)
          {
             myChecksum = [self compute16BitChecksum:&startOfData[4] :256];
          }
          else
          {
             myChecksum = [self computeCRC:&startOfData[4] :256];
          }
		
          // Compare Checksums and send appropriate flag.
          if (cocoChecksum == myChecksum)
          {
             // Sector transferred OK.
             response = 0;
			
             // Check to see if cartridge is inserted, then write sector to disk image.
             if ([[driveArray objectAtIndex:driveNumber] isEmpty] == YES)
             {
                response = 246;
             }
             else
             {
                NSData *sector = [[NSData alloc ] initWithBytesNoCopy:(void *)&startOfData[4] length:256 freeWhenDone:NO];
			
                [[driveArray objectAtIndex:driveNumber] writeSectors:vLSN forCount:1 sectors:sector];
             }
          }
          else
          {
             // Sector didn't transfer ok - send E$CRC.
             response = E_CRC;
          }
       }
		
       // Send statistical data via notification
       if (response == 0)
       {
          [statistics setObject:currentState forKey:@"OpCode"];
          [statistics setObject:[NSString stringWithFormat:@"%d", vLSN] forKey:@"LSN"];
          [statistics setObject:[NSString stringWithFormat:@"%d", driveNumber] forKey:@"DriveNumber"];
		
          // Kinda hackish -- we should get the "all" stats from the jukebox
          if ([currentState isEqualToString:@"OP_REWRITE"] == NO)
          {
             int32_t sectorsWritten = [[statistics objectForKey:@"WriteCount"] intValue] + 1;
			
             [statistics setObject:[NSString stringWithFormat:@"%d", sectorsWritten] forKey:@"WriteCount"];
          }
          else
          {
             int32_t sectorsReWritten = [[statistics objectForKey:@"ReWriteCount"] intValue] + 1;
			
             [statistics setObject:[NSString stringWithFormat:@"%d", sectorsReWritten] forKey:@"ReWriteCount"];
          }
		
          [statistics setObject:[NSString stringWithFormat:@"%d", response] forKey:@"Error"];
          [statistics setObject:[NSString stringWithFormat:@"%d", myChecksum] forKey:@"Checksum"];
          [_delegate updateInfoView:statistics];
       }
		
       // Send response to CoCo.
       [portDelegate writeData:[NSData dataWithBytes:&response length:1]];
	
       // Reset state.
       currentState = @"OP_OPCODE";
       startOfData = inputBuffer;
    }
    
    return;
}

- (void)OP_GETSTAT
{
	[self statCommon:@"GetStat"];
}

- (void)OP_SETSTAT
{
	[self statCommon:@"SetStat"];
}

- (void)statCommon:(NSString *)whichStat
{
	// We read 2 bytes into this buffer (1 byte drive number, 1 getstat code)
    int32_t expectedLength = 2;

   // Check for special case where entire packet is ready.
    if (currentLocation == 0 && dataLength == expectedLength)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {
		int32_t remaining;

        // Compute remaining free space in our local buffer.		
        remaining = expectedLength - currentLocation;
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);
        dataLength -= remaining;
        dataBytes += remaining;
        currentLocation += remaining;
    }
    
    // Check if we have reached our terminal count.
    if (currentLocation == expectedLength)
    {
		uint32_t driveNumber, statCode;
		
       // Invalidate watchdog timer
       [self invalidateWatchdog];
       
       // Reset the current location.
        currentLocation = 0;
        driveNumber = startOfData[0];
		statCode = startOfData[1];
		
		if (driveNumber < DRIVE_COUNT && [[driveArray objectAtIndex:driveNumber] isEmpty] == NO)
		{
			// Send statistical data via notification
			[statistics setObject:currentState forKey:@"OpCode"];
         [statistics setObject:[NSString stringWithFormat:@"%d", driveNumber] forKey:@"DriveNumber"];
			[statistics setObject:[self statCodeToString:statCode] forKey:whichStat];
         [_delegate updateInfoView:statistics];

			// Process specific stat codes (Version 3 and greater)
			if ([whichStat isEqualToString:@"SetStat"] == YES)
			{
				switch (statCode)
				{
					case 0x0C:	// SS.SQD
						// Eject the cartridge
						[[driveArray objectAtIndex:driveNumber] ejectCartridge:self];
						break;
				}
			}
      }
		
       // Reset state.
       currentState = @"OP_OPCODE";
       startOfData = inputBuffer;
    }
	
	return;
}

- (NSString *)statCodeToString:(int)code
{
    NSString *statString;
    
    switch (code)
    {
        case 0x00:
            statString = @"SS.Opt";
            break;
            
        case 0x02:
            statString = @"SS.Size";
            break;
            
        case 0x03:
            statString = @"SS.Reset";
            break;
            
        case 0x04:
            statString = @"SS.WTrk";
            break;
            
        case 0x05:
            statString = @"SS.Pos";
            break;
            
        case 0x06:
            statString = @"SS.EOF";
            break;
            
        case 0x0A:
            statString = @"SS.Frz";
            break;
            
        case 0x0B:
            statString = @"SS.SPT";
            break;
            
        case 0x0C:
            statString = @"SS.SQD";
            break;
            
        case 0x0D:
            statString = @"SS.DCmd";
            break;
            
        case 0x0E:
            statString = @"SS.DevNm";
            break;
            
        case 0x0F:
            statString = @"SS.FD";
            break;
            
        case 0x10:
            statString = @"SS.Ticks";
            break;
            
        case 0x11:
            statString = @"SS.Lock";
            break;
            
        case 0x12:
            statString = @"SS.VarSect";
            break;
			
        case 0x13:
            statString = @"SS.Eject";
            break;
			
        case 0x14:
            statString = @"SS.BlkRd";
            break;
            
        case 0x15:
            statString = @"SS.BlkWr";
            break;
            
        case 0x16:
            statString = @"SS.Reten";
            break;
            
        case 0x17:
            statString = @"SS.WFM";
            break;
            
        case 0x18:
            statString = @"SS.RFM";
            break;
            
        case 0x1B:
            statString = @"SS.Relea";
            break;
            
        case 0x1C:
            statString = @"SS.Attr";
            break;
            
        case 0x1E:
            statString = @"SS.RsBit";
            break;
            
        case 0x20:
            statString = @"SS.FDInf";
            break;
            
        case 0x26:
            statString = @"SS.DSize";
            break;
            
        default:
            statString = [[NSString alloc] initWithFormat:@"%d", code];
            break;
    }
	
    return(statString);
}


- (void)OP_WIREBUG_MODE
{
    // We read 23 bytes into this buffer
	
    // Check for special case where entire packet is ready. */
    if (currentLocation == 0 && dataLength == 23)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {
		int32_t remaining;

        // Compute remaining free space in our local buffer.
        remaining = 23 - currentLocation;
		
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.		
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);
        dataLength -= remaining;		
        dataBytes += remaining;		
        currentLocation += remaining;
    }    

    // Check if we have reached our terminal count.
    if (currentLocation == 23)
    {
       // Invalidate watchdog timer
       [self invalidateWatchdog];
       
       [statistics setObject:@"OP_WIREBUG" forKey:@"OpCode"];
       [_delegate updateInfoView:statistics];

       wirebugCoCoType = startOfData[0];
       wirebugCPUType = startOfData[1];

		// Reset the current location now that we've achieved it.
        currentLocation = 0;
        
        startOfData = inputBuffer;

		// Set WireBug state to TRUE
		[self setWirebugState:true];
		
		// Request registers
		[self readRegisters];
    }
}


- (void)OP_WIREBUG_READREGS_RESPONSE
{
    // Check for special case where entire packet is ready. */
    if (currentLocation == 0 && dataLength == 24)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {
		int32_t remaining;

        // Compute remaining free space in our local buffer.
        remaining = 24 - currentLocation;
		
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.		
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);
        dataLength -= remaining;		
        dataBytes += remaining;		
        currentLocation += remaining;
    }    

    // Check if we have reached our terminal count.
    if (currentLocation == 24)
    {
		uint8_t myChecksum = 0, targetChecksum = 0;
	
       // Invalidate the watchdog timer
       [self invalidateWatchdog];

       // Reset the current location now that we've achieved it.
        currentLocation = 0;
        
		// Extract Target's checksum.
		targetChecksum = startOfData[24 - 1];
	
		// Compute Checksum from sector.
		myChecksum = [self compute8BitChecksum:&startOfData[0] :23];
	
		// Compare Checksums and send appropriate flag.
		if (targetChecksum == myChecksum)
		{
			if (startOfData[0] == _OP_WIREBUG_READREGS)
			{
				_dp = startOfData[1];
				_cc = startOfData[2];
				_a  = startOfData[3];
				_b  = startOfData[4];
				_e  = startOfData[5];
				_f  = startOfData[6];
				_x  = startOfData[7] << 8 | startOfData[8];
				_y  = startOfData[9] << 8 | startOfData[10];
				_u  = startOfData[11] << 8 | startOfData[12];
				_md = startOfData[13];
				_v  = startOfData[14];
				_v  = startOfData[15];
				_s  = startOfData[16] << 8 | startOfData[17];
				_pc = startOfData[18] << 8 | startOfData[19];

				[registers setObject:[NSNumber numberWithChar:_cc] forKey:@"CC"];
				[registers setObject:[NSNumber numberWithChar:_dp] forKey:@"DP"];
				[registers setObject:[NSNumber numberWithChar:_a]  forKey:@"A"];
				[registers setObject:[NSNumber numberWithChar:_b]  forKey:@"B"];
				[registers setObject:[NSNumber numberWithChar:_e]  forKey:@"E"];
				[registers setObject:[NSNumber numberWithChar:_f]  forKey:@"F"];
				[registers setObject:[NSNumber numberWithInt:_x]  forKey:@"X"];
				[registers setObject:[NSNumber numberWithInt:_y]  forKey:@"Y"];
				[registers setObject:[NSNumber numberWithInt:_u]  forKey:@"U"];
				[registers setObject:[NSNumber numberWithChar:_md]  forKey:@"MD"];
				[registers setObject:[NSNumber numberWithInt:_v]  forKey:@"V"];
				[registers setObject:[NSNumber numberWithInt:_s]  forKey:@"S"];
				[registers setObject:[NSNumber numberWithInt:_pc] forKey:@"PC"];
				
				// Extract registers and post notification
				[[NSNotificationCenter defaultCenter] postNotificationName:@"wirebugRegisters"
																	object:self
																  userInfo:registers];
            
            // Now ask for memory
            [self readMemoryFrom:0x0000 to:0x0100];
			}
			else
			{
				// bad response
			}
		}
		else
		{
		}
		

		startOfData = inputBuffer;
//		[self goCoCo:nil];
    }
}


- (void)OP_WIREBUG_WRITEREGS_RESPONSE
{
}


- (void)OP_WIREBUG_READMEM_RESPONSE
{
   // Check for special case where entire packet is ready. */
   if (currentLocation == 0 && dataLength == 24)
   {
      currentLocation = dataLength;
      dataLength = 0;
      startOfData = &dataBytes[0];
   }
   else
   {
		int32_t remaining;
      
      // Compute remaining free space in our local buffer.
      remaining = 24 - currentLocation;
		
      if (remaining > dataLength)
      {
         remaining = dataLength;
      }
		
      // Copy the maximum available amount to our local buffer
      // and adjust data pointer and length accordingly.		
      memcpy(inputBuffer + currentLocation, dataBytes, remaining);
      dataLength -= remaining;		
      dataBytes += remaining;		
      currentLocation += remaining;
   }    
   
   // Check if we have reached our terminal count.
   if (currentLocation == 24)
   {
		startOfData = inputBuffer;
   }
}

- (void)OP_WIREBUG_WRITEMEM_RESPONSE
{
}


- (void)OP_WIREBUG_GO_RESPONSE
{
    // Check for special case where entire packet is ready. */
    if (currentLocation == 0 && dataLength == 24)
    {
        currentLocation = dataLength;
        dataLength = 0;
        startOfData = &dataBytes[0];
    }
    else
    {
		int32_t remaining;

        // Compute remaining free space in our local buffer.
        remaining = 24 - currentLocation;
		
        if (remaining > dataLength)
        {
            remaining = dataLength;
        }
		
        // Copy the maximum available amount to our local buffer
        // and adjust data pointer and length accordingly.		
        memcpy(inputBuffer + currentLocation, dataBytes, remaining);
        dataLength -= remaining;		
        dataBytes += remaining;		
        currentLocation += remaining;
    }    

    // Check if we have reached our terminal count.
    if (currentLocation == 24)
    {
       [self invalidateWatchdog];
		currentState = @"OP_OPCODE";
    }
}


- (void)readRegisters
{
	u_char requestBuffer[24];

   // Set state to capture remaining code
   currentState = @"OP_WIREBUG_READREGS_RESPONSE";

	requestBuffer[0] = _OP_WIREBUG_READREGS;
	requestBuffer[23] = [self compute8BitChecksum:&requestBuffer[0] :23];
	[portDelegate writeData:[NSData dataWithBytes:requestBuffer length:24]];
}


- (void)readMemoryFrom:(int)start to:(int)end;
{
	u_char requestBuffer[24];
   
   // Set state to capture remaining code
   currentState = @"OP_WIREBUG_READMEM_RESPONSE";
   
	requestBuffer[0] = _OP_WIREBUG_READMEM;
   requestBuffer[1] = (start >> 8) & 0xFF;
   requestBuffer[2] = (start >> 0) & 0xFF;
   requestBuffer[2] = (char)19;
	requestBuffer[23] = [self compute8BitChecksum:&requestBuffer[0] :23];
	[portDelegate writeData:[NSData dataWithBytes:requestBuffer length:24]];
}



- (void)goCoCo;
{
	u_char requestBuffer[24];

    // Set state to capture remaining code
    currentState = @"OP_WIREBUG_GO_RESPONSE";
   currentLocation = 0;

	requestBuffer[0] = _OP_WIREBUG_GO;
	requestBuffer[23] = [self compute8BitChecksum:&requestBuffer[0] :23];
	[portDelegate writeData:[NSData dataWithBytes:requestBuffer length:24]];
	[self setWirebugState:false];
   currentState = @"OP_OPCODE";
   currentLocation = 0;
}



- (void)sendRunTarget
{
	u_char msgOut[24];
	
	[statistics setObject:@"OP_WIREBUG_GO" forKey:@"OpCode"];
	msgOut[0] = _OP_WIREBUG_GO;
	[portDelegate writeData:[NSData dataWithBytes:msgOut length:24]];
	
    return;
}



#pragma mark NSCoding Protocol Methods

- (id)initWithCoder:(NSCoder *)coder;
{
	if ((self = [super init]))
	{
		NSString *savedPort;		
		
		driveArray = [[coder decodeObject] retain];
		[coder decodeValueOfObjCType:@encode(int) at:&version];
		savedPort = [[coder decodeObject] retain];
		[coder decodeValueOfObjCType:@encode(Boolean) at:&statState];
		[coder decodeValueOfObjCType:@encode(Boolean) at:&logState];
		[coder decodeValueOfObjCType:@encode(int) at:&machineType];

		[self initDesignated];		

		fCurrentPort = nil;
		
		[self setCommPort:savedPort];
		
		[self setMachineType:machineType];
	}
	
	return self;
}



- (void)encodeWithCoder:(NSCoder *)coder;
{
	[coder encodeObject:driveArray];
	[coder encodeValueOfObjCType:@encode(int) at:&version];
	[coder encodeObject:fCurrentPort];
	[coder encodeValueOfObjCType:@encode(Boolean) at:&statState];
	[coder encodeValueOfObjCType:@encode(Boolean) at:&logState];
	[coder encodeValueOfObjCType:@encode(int) at:&machineType];
}


@end
