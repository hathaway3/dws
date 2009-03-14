/*--------------------------------------------------------------------------------------------------
//
//   File Name   :   TBVirtualDriveController.m
//
//   Description :   Virtual Drive Controller Implementation File
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

#import <TBVirtualDriveController.h>


@implementation TBVirtualDriveController

#pragma mark Init Methods

- (id)init
{
	if ((self = [super init]) != nil)
	{
		model = [[TBVirtualDriveModel alloc] init];

		if (model == nil)
		{
			// There was a problem allocating the model
			
			return nil;
		}
	}
	
	[self initDesignated];

	return self;
}



- (void)initDesignated
{
	if ([NSBundle loadNibNamed:@"TBVirtualDriveView" owner:self] == NO)
	{
		NSLog(@"We've got a load Nib problem\n");
	}
   
   // set ourself as the delegate of the model
   [model setDelegate:self];

	// Turn off LEDs
	[self ledOff];
	
	// Show drive as empty
	[self ejectCartridge:self];
}


- (void)dealloc
{
	[model release];
	[super dealloc];
}



- (TBVirtualDriveView *)view
{
	return virtualDriveView;
}


#pragma mark Drive ID Methods

- (void)setDriveID:(uint16_t)driveId;
{
	[driveNumber setStringValue:[NSString stringWithFormat:@"%d", driveId]];
	[model setDriveID:driveId];
}



- (uint16_t)driveID
{
	return [model driveID];
}



#pragma mark Cartridge Methods

- (NSString *)cartridgeLabel
{
    return [model cartridgeLabel];
}



- (NSString *)cartridgePath
{
    return [model cartridgePath];
}



- (void)selectAndInsertCartridge:(id)object
{
	NSOpenPanel *filePanel = [NSOpenPanel openPanel];
	
	
	// If the drive is not empty, do nothing
	if ([self isEmpty] == NO)
	{
		return;
	}
	
	
	[filePanel setAllowsMultipleSelection:NO];
	
	if ([filePanel runModalForDirectory:nil file:@"" types:[NSArray arrayWithObjects: @"dsk", @"img", @"os9", nil]] == NSOKButton)
	{
		NSArray *filenames = [filePanel filenames];
		
		NSString *cartridgeName = [filenames objectAtIndex:0];
		
		if (cartridgeName != nil)
		{           
			[self ejectCartridge:self];
			
			[self insertCartridge:cartridgeName];
		}
	}
}




- (BOOL)insertCartridge:(NSString *)cartridge
{
#ifdef DEBUG
	NSLog(@"Inserting Cartridge");
#endif
	
	if (cartridge != nil)
	{
		[model insertCartridge:cartridge];
		
		[diskLabel setStringValue:[cartridge lastPathComponent]];
		[diskLabel setEditable:NO];
		[driveDoor setHidden:NO];
		[diskLabel setHidden:NO];
	}

	return YES;
}



- (IBAction)ejectCartridge:(id)object
{
#ifdef DEBUG
	NSLog(@"Ejecting Cartridge");
#endif
	
	[model ejectCartridge];
	[driveDoor setHidden:YES];
	[diskLabel setHidden:YES];
}



- (BOOL)isEmpty
{
	return [model isEmpty];
}



#pragma mark Sector Access Methods

- (NSData *)readSectors:(uint32_t)lsn forCount:(uint32_t)count
{
#ifdef DEBUG
	NSLog(@"readSectors LSN[%d] Count[%d]", lsn, count);
#endif
	
	return [model readSectors:lsn forCount:count];
}



- (NSData *)writeSectors:(uint32_t)lsn forCount:(uint32_t)count sectors:(NSData *)sectors
{
#ifdef DEBUG
	NSLog(@"writeSectors LSN[%d] Count[%d]", lsn, count);
#endif
	
	return [model writeSectors:lsn forCount:count withData:sectors];
}



- (uint32_t)sectorsRead
{
	return [model sectorsRead];
}



- (uint32_t)sectorsWritten
{
	return [model sectorsWritten];
}



- (uint32_t)totalSectorsRead
{
	return [model totalSectorsRead];
}



- (uint32_t)totalSectorsWritten
{
	return [model totalSectorsWritten];
}



#pragma mark LED Methods
// Methods called when receiving LED commands from the model

- (void)ledRead
{
	[readLED setHidden:FALSE];
	[writeLED setHidden:TRUE];
}



- (void)ledWrite
{
	[writeLED setHidden:FALSE];
	[readLED setHidden:TRUE];
}



- (void)ledOff
{
	[readLED setHidden:TRUE];
	[writeLED setHidden:TRUE];
}


#pragma mark Coding Methods

- (id)initWithCoder:(NSCoder *)coder;
{
	if ((self = [super init]))
	{
		model = [[coder decodeObject] retain];

		[self initDesignated];

      // insert the cartridge
		[self insertCartridge:[coder decodeObject]];
	}
	
	return self;
}



- (void)encodeWithCoder:(NSCoder *)coder;
{
	[coder encodeObject:model];
   [coder encodeObject:[self cartridgePath]];
}



@end
