/*--------------------------------------------------------------------------------------------------
//
//   File Name   :   TBSerialManager.m
//
//   Description :   Serial port manager.
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
/*!
	@header TBSerialManager.h
	@copyright BP
	@updated 2005-05-29
	@meta http-equiv=”refresh” content=”0;http://www.apple.com”
 */


#import "TBSerialManager.h"
#import <IOKit/serial/IOSerialKeys.h>

#include <sys/param.h>
#include <sysexits.h>
#include <pthread.h>


@implementation TBSerialManager



- (id)init
{
    if ((self = [super init]))
    {
		NSMutableDictionary	*portsISee;
		NSEnumerator *oEnumerator, *kEnumerator;
		NSString *oValue, *kValue;

        
		// Allocate space for port dictionary
		
		portList = [[NSMutableDictionary alloc] init];
		
		
		// Get an dictionary of all ports and their device names.
		
        portsISee = [self availablePorts];
		
		oEnumerator = [portsISee objectEnumerator];
		kEnumerator = [portsISee keyEnumerator];
        

		// Sift through each port and create a TBSerialPort
		
		while ((kValue = [kEnumerator nextObject]))
		{
			TBSerialPort *port;
			
			oValue = [oEnumerator nextObject];

#ifdef DEBUG
			NSLog(@"Device name '%@' for port '%@'", oValue, kValue);
#endif
			port = [[TBSerialPort alloc] init:oValue :kValue];
			[portList setValue:port forKey:kValue];
		}
	}
        
        
    // Return self.
    
    return self;
}


- (void)dealloc
{
	[portList release];
	
	[super dealloc];
}



#pragma mark Query methods


- (BOOL)doesPortExist:(NSString *)name
{
	if ([portList objectForKey:name] != nil)
	{
		return YES;
	}
	
	
    return NO;
}



- (BOOL)isPortAvailable:(NSString *)name
{
	TBSerialPort *port = [portList objectForKey:name];
	
	if ([port owner] == nil)
	{
			return YES;
	}
	
	
	return NO;
}



#pragma mark Action methods

- (TBSerialPort *)reservePort:(NSString *)name forOwner:(id)object
{
	TBSerialPort *port = [portList objectForKey:name];
	
	if ([port owner] == nil)
	{
		if ([port openPort:object] == NO)
		{
			return nil;
		}
	}
	else
	{
		port = nil;
	}
		
	return port;
}



- (BOOL)releasePort:(NSString *)name
{
	TBSerialPort *port = [portList objectForKey:name];
	
	return [port closePort];
}



#if 0
- (id)initWithSerialPort:(NSString *)portName:(int)baudRate:(NSString *)serialProtocol
{
    int	status = -1;
    int i;
    
    
    if (self = [super init])
    {
        // Allocate and initialize our instance of SerialPort.
                
        port = [[TBSerialPort alloc] init];

        if (port == nil)
        {
            return nil;
        }
        
        
        // Harvest all serial devices on the system.
        
//        availableSerialDevices = [TBSerialManager harvestDeviceNames];
//        serialNameArray = [TBSerialManager harvestPortNames];
        
        
        // Determine if the passed name matches any of our port names.
        
        int count = [portsISee count];
        
        for (i = 0; i < count; i++)
        {
            NSString *namePtr = [portsISee objectAtIndex:i];
            
            if ([portName isEqualToString: namePtr] == YES)
            {
                break;
            }
        }

    
        // Verify that we found a match.
    
        if (i < count)
        {
            // We found a match, so get the device name.

            NSString *deviceNamePtr = [portsISee objectAtIndex:i];

            if (deviceNamePtr != nil)
            {
                status = [port acquirePort: deviceNamePtr];
                
                if (status != -1)
                {
                    // Set up serial port.

                    [port setBaudRate:baudRate];

                    if ([serialProtocol isEqualToString:@"8N1"] == YES)
                    {
                        [port setWordSize:8];
                        [port setParity:parityNone];
                        [port setStopBits:1];
                    }
                    else if ([serialProtocol isEqualToString:@"7E1"] == YES)
                    {
                        [port setWordSize:7];
                        [port setParity:parityEven];
                        [port setStopBits:1];
                    }

                    [port setMinimumReadBytes:0];
                    [port setReadTimeout:1];


                    // Spin off listener thread.
                    
                    allowedToRun = TRUE;
                    
                    serialLock = [[NSLock alloc] init];
                    
                    [NSThread detachNewThreadSelector:@selector(listener:) toTarget:self withObject:nil];
                }
            }
        }
    }
    
    
    // Return self.
    
    return self;
}



- (void)listener:(id)anObject
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
    const int readBufferSize = 300;
    u_char readBuffer[readBufferSize];
    NSData *serialData;
    NSDictionary *dictionary;

     
	NSLog(@"Entering Thread...\n");


    // Lock access to this thread.
    
    [serialLock lock];


    // Do processing so long as we are allowed to run.

    while (allowedToRun == TRUE)
    {
        int maxsize = 0;

        
        // Read up to 'maxsize' bytes from the port.
            
        maxsize = [port readData :readBuffer :readBufferSize];

        if (maxsize > 0)
        {
            // Package serial data into an NSData object

            serialData = [NSData dataWithBytesNoCopy:readBuffer length:maxsize freeWhenDone:NO];


            // Put that data into a new dictionary.
            
            dictionary = [NSDictionary dictionaryWithObject:serialData forKey:@"Data"];


            // Post the dictionary to the notification object.
            
            [nc postNotificationName:@"DataReady" object:self userInfo:dictionary];
        }
    }
        
    [port releasePort];
    
    [pool release];
    

    // Unlock the lock.
    
    [serialLock unlock];

	NSLog(@"Exiting Thread...\n");
}



- (void)dealloc
{
    allowedToRun = FALSE;
    
    // Wait for the lock.
    
    [serialLock lock];

    [super dealloc];

	
    return;
}



// Allows the caller to write data to the serial port.

- (int)sendToPort:(u_char *)userBuffer: (int)numBytes
{
    [port writeData :userBuffer :numBytes];
    
    return 0;
}
#endif


// The rest of this file contains methods and functions that should be internal to this class.

static kern_return_t MyFindSerialPorts(io_iterator_t *matchingServices, mach_port_t *masterPort);


+ (NSMutableArray *)harvestDeviceNames
{
    int		count = 0;
    mach_port_t masterPort = (mach_port_t)NULL;
    kern_return_t kernResult;
    NSMutableArray *devices;


    // Init the arrays.

    devices = [[NSMutableArray alloc] init];

    
    io_iterator_t serialPortIterator = 0;

    kernResult = MyFindSerialPorts(&serialPortIterator, &masterPort);

    if  (kernResult == kIOReturnSuccess)
    {
        io_object_t		rs232Service;


        while ((rs232Service = IOIteratorNext(serialPortIterator)))
        {
            CFTypeRef	bsdPathAsCFString;
    
            count++;
            
            bsdPathAsCFString = IORegistryEntryCreateCFProperty
            (
                rs232Service,
                CFSTR(kIOCalloutDeviceKey),
                kCFAllocatorDefault,
                0
            );

            [devices addObject: (NSString *)bsdPathAsCFString]; 
            CFRelease(bsdPathAsCFString);

            IOObjectRelease(rs232Service);
        }
    }

    
    IOObjectRelease(serialPortIterator);

    
    return devices;
}



- (NSMutableDictionary *)availablePorts
{
    int		count = 0;
    mach_port_t masterPort = (mach_port_t)NULL;
    kern_return_t kernResult;
    NSMutableDictionary *names;


    // Init the arrays.

    names = [[NSMutableDictionary alloc] init];

    
    io_iterator_t serialPortIterator = 0;

    kernResult = MyFindSerialPorts(&serialPortIterator, &masterPort);

    if  (kernResult == kIOReturnSuccess)
    {
        io_object_t		rs232Service;


        while ((rs232Service = IOIteratorNext(serialPortIterator)))
        {
            CFTypeRef	bsdPathAsCFString, serviceNameAsCFString;
    
            count++;
            
            serviceNameAsCFString = IORegistryEntryCreateCFProperty
            (
                rs232Service,
                CFSTR(kIOTTYBaseNameKey),
                kCFAllocatorDefault,
                0
            );
        
            bsdPathAsCFString = IORegistryEntryCreateCFProperty
			(
				 rs232Service,
				 CFSTR(kIOCalloutDeviceKey),
				 kCFAllocatorDefault,
				 0
			);
			
            [names setValue: (NSString *)bsdPathAsCFString
				forKey:(NSString *)serviceNameAsCFString]; 
        
            CFRelease(bsdPathAsCFString);
            CFRelease(serviceNameAsCFString);
			
            IOObjectRelease(rs232Service);
        }
    }

    
    IOObjectRelease(serialPortIterator);

    
    return names;
}



static kern_return_t MyFindSerialPorts(io_iterator_t *matchingServices, mach_port_t *masterPort)
{
    kern_return_t kernResult; 
    CFMutableDictionaryRef classesToMatch = NULL;


    *matchingServices = 0;

    kernResult = IOMasterPort(0, masterPort);

    if (kernResult != KERN_SUCCESS)
    {
        NSLog(@"IOMasterPort returned %d", kernResult);

        return kernResult;
    }
    
    
    // The provider class for serial devices is IOSerialBSDClient.

    classesToMatch = IOServiceMatching(kIOSerialBSDServiceValue);

    if (classesToMatch == NULL)
    {
        NSLog(@"IOServiceMatching returned a NULL dictionary.");

        return EX_UNAVAILABLE;
    }
    else
    { 
        CFDictionarySetValue
        (
            classesToMatch,
            CFSTR(kIOSerialBSDTypeKey),
            CFSTR(kIOSerialBSDAllTypes)
//            CFSTR(kIOSerialBSDRS232Type)
        );
    }
    
    
    kernResult = IOServiceGetMatchingServices
    (
        *masterPort,
        classesToMatch,
        matchingServices
    );

    if ( (kernResult != KERN_SUCCESS) || (*matchingServices == 0) )
    {
        if (kernResult == KERN_SUCCESS)
        {
            kernResult = EX_UNAVAILABLE; // Make sure error if no serial ports.
        }
    }
    
    
    return kernResult;
}

@end
