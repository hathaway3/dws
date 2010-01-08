/*--------------------------------------------------------------------------------------------------
//
//   File Name   :   TBVirtualDriveModel.m
//
//   Description :   Virtual Drive Model Implementation File
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
// Jan-12-07  BGP
// Reworked model to now use a delegate to communicate to the controller instead of
// using NSNotifcationCenter.
//------------------------------------------------------------------------------------------------*/


#import <TBVirtualDriveModel.h>


@implementation TBVirtualDriveModel


static NSSound *insertSound, *ejectSound;
static int instanceCount = 0;

#pragma mark Init Methods

- (id)init
{
	if ((self = [super init]) != nil)
	{
		[self initDesignated];
	}
	
	return self;
}


- (void)initDesignated
{
	// Set the initial state of the drive

	led = LED_OFF;			// LED is off
	driveID = 0;			// Drive ID is 0
	cartridgePath = nil;	// There is no cartridge yet
	sectorReadCount = 0;	// The read count is 0
	sectorWriteCount = 0;	// The write count is 0
	totalSectorReadCount = 0;	// The read count is 0
	totalSectorWriteCount = 0;	// The write count is 0
	cartridgeHandle = nil;	// There is no cartridge inserted
	sectorSize = 256;		// The default sector size is 256 bytes
   
	if (instanceCount == 0)
	{
//      NSBundle *myFrameworkBundle = [NSBundle bundleWithIdentifier:@"com.tee-boy.TBVirtualDrive"];
      NSBundle *myFrameworkBundle = [NSBundle bundleForClass:[self class]];
#ifdef DEBUG
      NSLog(@"myFrameworksBundle = %@", [myFrameworkBundle description]);      
#endif

      NSString *insertSoundPath = [myFrameworkBundle pathForResource:@"insert" ofType:@"wav"];
      insertSound = [[NSSound alloc] initWithContentsOfFile:insertSoundPath byReference:YES];
#ifdef DEBUG
      NSLog(@"insertSoundPath = %@, insertSound = 0x%X", insertSoundPath, insertSound);
#endif
		[insertSound setDelegate:self];
      
      NSString *ejectSoundPath = [myFrameworkBundle pathForResource:@"eject" ofType:@"wav"];
      ejectSound = [[NSSound alloc] initWithContentsOfFile:ejectSoundPath byReference:YES];
#ifdef DEBUG
      NSLog(@"ejectSoundPath = %@, ejectSound = 0x%X", ejectSoundPath, ejectSound);
#endif
	}
	
	instanceCount++;
}


- (void)dealloc
{
	instanceCount--;
	
	if (instanceCount == 0)
	{
		[insertSound release];
		[ejectSound release];
	}
	
	[super dealloc];
}


- (void)setDelegate:(id)_delegate
{
	delegate = _delegate;
}



#pragma mark Drive ID Methods


- (void)setDriveID:(uint16_t)value;
{
	driveID = value;
	
	return;
}

- (uint16_t)driveID
{
	return driveID;
}


#pragma mark Cartridge Methods


- (NSString *)cartridgeLabel
{
    return [cartridgePath lastPathComponent];
}



- (NSString *)cartridgePath
{
    return cartridgePath;
}



- (BOOL)insertCartridge:(NSString *)cartridgeName
{
    if (cartridgeHandle != nil)
    {
        // User didn't close!
        
        return FALSE;
    }
    
    
    cartridgeHandle = [[NSFileHandle fileHandleForUpdatingAtPath:cartridgeName] retain];
	
    if (cartridgeHandle == nil)
    {
        return FALSE;
    }


	// Turn on Read LED
	
	[self turnOnReadLED:self];


	// Reset the LED timer if it was already set -- delegate will clear LED

	if (ledTimer != nil)
	{
		[ledTimer invalidate];
	}

	ledTimer = [NSTimer timerWithTimeInterval:1 target:self selector:@selector(shutOffLED:) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:ledTimer forMode:NSDefaultRunLoopMode];
	

	// Play the "insert" sound
	
	[insertSound play];		

	
	// Thread-safe exchange of cartridgePath
	
	id old = cartridgePath;

	cartridgePath = [cartridgeName retain];
	
	[old release];
	
	
    return TRUE;
}



- (void)ejectCartridge
{
    // Determine if this object's file handle is valid.
    
    if (cartridgeHandle != nil)
    {
		// There's a disk in the drive -- eject it
		
        [cartridgeHandle closeFile];
        
        [cartridgeHandle release];

		
		// Reset cartridge specific counts
		sectorReadCount = 0;
		sectorWriteCount = 0;


		// Play the "eject" sound
		
		[ejectSound play];		
		

		cartridgePath = nil;
		
		cartridgeHandle = nil;
	}
	
    
    // Return
	
    return;
}



- (BOOL)isEmpty
{
    // Determine if the drive is empty
    
    if (cartridgeHandle != nil)
    {
		return FALSE;
    }
	
    return TRUE;
}



#pragma mark Sector Access Methods

- (NSData *)readSectors:(uint32_t)lsn forCount:(uint32_t)count
{
    NSData *sectors;
    unsigned long long offset = lsn * sectorSize;
	
	
    // Determine there is a disk in the drive
    
    if (cartridgeHandle == nil)
    {
        return nil;
    }
	
	// Turn on Read LED
	[self turnOnReadLED:self];
	
	
	// Reset the LED timer if it was already set
	if (ledTimer != nil)
	{
		[ledTimer invalidate];
	}
	ledTimer = [NSTimer timerWithTimeInterval:.1 target:self selector:@selector(shutOffLED:) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:ledTimer forMode:NSDefaultRunLoopMode];
	
    // Seek to offset based on LSN passed.
    [cartridgeHandle seekToFileOffset:offset];
	
	
    // Read sector at current position.
    sectors = [cartridgeHandle readDataOfLength:sectorSize * count];
	
	sectorReadCount += count;
	totalSectorReadCount += count;
	
	
    // Return sectors passed.
    
    return sectors;
}



- (NSData *)writeSectors:(uint32_t)lsn forCount:(uint32_t)count withData:(NSData *)sectors
{
    unsigned long long offset = lsn * sectorSize;
	
	
    // Determine there is a disk in the drive
    
    if (cartridgeHandle == nil)
    {
        return nil;
    }
	
	
	// Turn on write LED
	
	[self turnOnWriteLED:self];
	
    
	// Reset the LED timer if it was already set
	
	if (ledTimer != nil)
	{
		[ledTimer invalidate];
	}
	ledTimer = [NSTimer timerWithTimeInterval:.1 target:self selector:@selector(shutOffLED:) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:ledTimer forMode:NSDefaultRunLoopMode];
	
	
	// Seek to offset based on LSN passed
	
    [cartridgeHandle seekToFileOffset:offset];
	
	
    // Write sector at current position.
	
    [cartridgeHandle writeData:sectors];
	
	sectorWriteCount += count;
	totalSectorWriteCount += count;
	
    
    // Return sectors passed.
    
    return sectors;
}




- (uint32_t)sectorsRead
{
	return sectorReadCount;
}



- (uint32_t)sectorsWritten
{
	return sectorWriteCount;
}



- (uint32_t)totalSectorsRead
{
	return totalSectorReadCount;
}



- (uint32_t)totalSectorsWritten
{
	return totalSectorWriteCount;
}



#pragma mark LED Control Methods

- (void)shutOffLED:(id)sender
{
	ledTimer = nil;
	
	led = LED_OFF;

    if ([delegate respondsToSelector:@selector(ledOff)])
	{
		[delegate ledOff];
	}
}



- (void)turnOnReadLED:(id)sender
{
	// Turn on Read LED if not already on
	
	if (led != LED_READ)
	{
		led = LED_READ;
		if ([delegate respondsToSelector:@selector(ledRead)])
		{
			[delegate ledRead];
		}
	}
	
	
	// Reset the LED timer if it was already set
	
	if (ledTimer != nil)
	{
		[ledTimer invalidate];
	}
	
	ledTimer = [NSTimer timerWithTimeInterval:.1 target:self selector:@selector(shutOffLED:) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:ledTimer forMode:NSDefaultRunLoopMode];

	
	return;
}



- (void)turnOnWriteLED:(id)sender
{
	// Turn on Write LED if not already on

	if (led != LED_WRITE)
	{
		led = LED_WRITE;
		if ([delegate respondsToSelector:@selector(ledWrite)])
		{
			[delegate ledWrite];
		}
	}


	// Reset the LED timer if it was already set

	if (ledTimer != nil)
	{
		[ledTimer invalidate];
	}

	ledTimer = [NSTimer timerWithTimeInterval:.1 target:self selector:@selector(shutOffLED:) userInfo:nil repeats:NO];
	[[NSRunLoop currentRunLoop] addTimer:ledTimer forMode:NSDefaultRunLoopMode];


	return;
}



- (void)sound:(NSSound *)sound didFinishPlaying:(BOOL)aBool
{
	if (aBool == YES)
	{
		[self shutOffLED:self];
	}
}



- (id)initWithCoder:(NSCoder *)coder;
{
	if ((self = [super init]))
	{
		[self initDesignated];
	}

	return self;
}



- (void)encodeWithCoder:(NSCoder *)coder;
{
}



@end