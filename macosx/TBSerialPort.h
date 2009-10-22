/*--------------------------------------------------------------------------------------------------
//
//   File Name   :   TBSerialPort.h
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

/*!
	@header TBSerialPort.h
	@abstract Header file for Serial Port access
	@discussion Include TBSerialPort.h to access the class for serial port access.
	@copyright Tee-Boy
	@updated 2007-06-25
 */

#import <Foundation/Foundation.h>

#include <unistd.h>
#include <fcntl.h>
#include <sys/filio.h>
#include <sys/ioctl.h>
#include <termios.h>


/*!
	@interface TBSerialPort
	@abstract Serial Port access class
	@discussion This class manages provides access to all available serial port
	devices.  It has been designed to provide a clear and concise interface
	for applications that need serial communication services.
*/
@interface TBSerialPort : NSObject
{
	int				fd;					// file descriptor to path of serial device
	id				fOwner;				// owner of the port
    NSString		*fDeviceName;		// the OS-friendly name of the device
    NSString		*fServiceName;		// the view-friendly name of the device
    struct termios 	sOriginalTTYAttrs;	// original TTY attributes
    struct termios 	sTTYAttrs;			// maeleable TTY attributes
	NSLock			*serialLock;
	Boolean			allowedToRun;
	Boolean			logIncomingBytes;
	Boolean			logOutgoingBytes;
	id				delegate;
}

/*!
	@enum serialParity
	@abstract Constants for serial parity modes.
	@discussion These constants are used to set the parity mode of a serial
	port.
 */
typedef enum
{
    parityOdd,
    parityEven,
    parityNone
} serialParity;


// Port access methods

/*!
	@method initWithDeviceName:serviceName:
	@abstract Acquires a path to the specified serial port.
	@param deviceName A pointer to the string containing the port name.
	@param serviceName A pointer to the string containing the service name.
	@discussion This method opens a port and conditions it for immediate
	use.  The default settings are 9600 baud, 8 bit word size, no parity,
	and 1 stop bit.
	@result YES if the acquisition was successful; otherwise NO.
 */
- (id)initWithDeviceName:(NSString *)deviceName serviceName:(NSString *)serviceName;

/*!
	@method openPort:
	@abstract Acquires a path to the specified serial port.
	@param owner A pointer to the desired owner object of the port.
	@discussion This method opens a port and conditions it for immediate
	use.  The default settings are 9600 baud, 8 bit word size, no parity,
	and 1 stop bit.
	@result YES if the port acquisition was successful; otherwise NO.
 */
- (BOOL)openPort:(id)owner;

/*!
	@method closePort
	@abstract Releases a previously acquired port.
	@result YES if port was closed successfully; otherwise NO.
 */
- (BOOL)closePort;

/*!
 @method delegate
 @abstract Returns a pointer to the delegate.
 @result The pointer to the delegate that was previously set.
 */
- (id)delegate;

/*!
 @method setDelegate:
 @param data A pointer to an object to be the delegate.
 @abstract Sets the delegate for the class.
 */
- (void)setDelegate:(id)_value;

#if 0
/*!
	@method readData:length:
	@param data A pointer to an NSData object where data will be read.
	@abstract Obtains available data from the serial port.
	@result YES if the read was successful, NO if the read was not.
 */
- (BOOL)readData:(char *)data length:(int)length;
#endif

/*!
	@method readAvailableData
	@param data A pointer to an NSData object where data will be read.
	@abstract Obtains available data from the serial port.
	@result YES if the read was successful, NO if the read was not.
 */
- (NSData *)readAvailableData;

/*!
	@method writeData:
	@param data A pointer to an NSData object of the data to be written.
	@abstract Obtains available data from the serial port.
	@result YES if data was written; otherwise NO.
 */
- (BOOL)writeData:(NSData *)data;

/*!
	@method writeString:
	@param data A pointer to an NSString object of the data to be written.
	@abstract Obtains available data from the serial port.
	@result YES if data was written; otherwise NO.
 */
- (BOOL)writeString:(NSString *)data;


// Query methods

/*!
 @method inputLogging
 @abstract Returns the state of input logging
 @result TRUE if logging is on; FALSE logging is off
 */
- (Boolean)inputLogging;

/*!
 @method outputLogging
 @abstract Returns the state of output logging
 @result TRUE if logging is on; FALSE logging is off
 */
- (Boolean)outputLogging;

/*!
	@method bytesReady
	@abstract Returns the number of bytes available to read.
	@result The number of bytes that are ready for reading.
 */
- (int)bytesReady;


/*!
	@method isAcquired
	@abstract Returns the state of the port.
	@result YES if the port has been acquired; otherwise, NO.
 */
- (BOOL)isAcquired;


// Set methods

/*!
	@method owner
	@abstract Returns the owner of the port.
	@result owner ID, or nil if there is none.
 */
- (id)owner;

/*!
 @method setInputLogging:
 @abstract Turns on input logging and shows all bytes coming into the serial port
 @param value TRUE to turn on logging; FALSE to turn off logging
 */
- (void)setInputLogging:(Boolean)value;

/*!
 @method setOutputLogging:
 @abstract Turns on output logging and shows all bytes going out of serial port
 @param value TRUE to turn on logging; FALSE to turn off logging
 */
- (void)setOutputLogging:(Boolean)value;

/*!
	@method setBaudRate:
	@abstract Sets the baud rate of the serial port.
	@param baudRate The baud rate to set the serial port to.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setBaudRate:(int)baudRate;

/*!
	@method setWordSize
	@abstract Sets the word size of the serial port.
	@param wordSize The word size.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setWordSize:(int)wordSize;

/*!
	@method setParity:
	@abstract Sets the parity of the serial port.
	@param parity The desired parity.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setParity:(serialParity)parity;

/*!
	@method setStopBits:
	@abstract Sets the number of stop bits of the serial port.
	@param stopBits The number of stop bits to set.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setStopBits:(int)stopBits;

/*!
	@method setMinimumReadBytes
	@abstract Sets the baud rate of the serial port.
	@param number The minimum number of read bytes to set.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setMinimumReadBytes:(int)number;

/*!
	@method setReadTimeout:
	@param timeout The number of milliseconds to timeout if no data is present.
	@abstract Sets read timeout in milliseconds.
	@result YES if the port was successfully set; otherwise, NO.
 */
- (BOOL)setReadTimeout:(int)timeout;


// Get methods

/*!
	@method deviceName
	@abstract Returns the OS-friendly name of the serial port.
	@result The name of the serial port.
 */
- (NSString *)deviceName;

/*!
	@method serviceName
	@abstract Returns the user-friendly name of the serial port.
	@result The name of the serial port.
 */
- (NSString *)serviceName;

/*!
	@method baudRate
	@abstract Returns the baud rate of the serial port.
	@result The baud rate of the serial port.
 */
- (int)baudRate;

/*!
	@method wordSize
	@abstract Returns the word size of the serial port.
	@result The word size of the serial port.
 */
- (int)wordSize;

/*!
	@method parity
	@abstract Returns the parity of the serial port.
	@result The parity of the serial port.
 */
- (serialParity)parity;

/*!
	@method stopBits
	@abstract Returns the serial port's stop bits setting.
	@result The number of stop bits.
 */
- (int)stopBits;

/*!
	@method minimumReadBytes
	@abstract Returns the serial port's minimum read byte count.
	@result The minimum number of ready bytes is returned.
 */
- (int)minimumReadBytes;

/*!
	@method readTimeout
	@abstract Returns the read timeout of the serial port.
	@result The read timeout in milliseconds.
 */
- (int)readTimeout;

@end

@protocol SerialPort

- (void)availableData:(NSData *)data;

@end
