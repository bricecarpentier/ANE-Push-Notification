//
//  AirPushNotification.h
//  AirPushNotification
//
//  Created by Peter Nicolai on 6/3/13.
//  Copyright (c) 2013 Fresh Planet. All rights reserved.
//

#ifndef AirPushNotification_AirPushNotification_h
#define AirPushNotification_AirPushNotification_h


#define EXPORT __attribute__((visibility("default")))

#import <Foundation/Foundation.h>
#import <Adobe AIR/Adobe AIR.h>
#import <Adobe AIR/FlashRuntimeExtensions.h>

@interface AirPushNotification : NSObject <NSApplicationDelegate>

@end


FREObject setBadgeNb(FREContext ctx, void* funcData, uint32_t argc, FREObject argv[]);




void contextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctions, const FRENamedFunction** functions);
void contextFinalizer(FREContext ctx);




EXPORT
void initializer(void** extData, FREContextInitializer* ctxInitializer, FREContextFinalizer* ctxFinalizer);

EXPORT
void finalizer(void* extData);

#endif
